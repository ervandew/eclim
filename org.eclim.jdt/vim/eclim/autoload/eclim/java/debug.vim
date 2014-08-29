" Author:  Kannan Rajah
"
" License: {{{
"
" Copyright (C) 2014  Eric Van Dewoestine
"
" This program is free software: you can redistribute it and/or modify
" it under the terms of the GNU General Public License as published by
" the Free Software Foundation, either version 3 of the License, or
" (at your option) any later version.
"
" This program is distributed in the hope that it will be useful,
" but WITHOUT ANY WARRANTY; without even the implied warranty of
" MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
" GNU General Public License for more details.
"
" You should have received a copy of the GNU General Public License
" along with this program.  If not, see <http://www.gnu.org/licenses/>.
"
" }}}

" Script Variables {{{
" TODO This needs to be maintained per thread. We can use debug session id +
" thread id as key
let s:debug_step_prev_file = ''
let s:debug_step_prev_line = ''

let s:debug_step_sign_name = 'debug_step'
let s:breakpoint_sign_name = 'breakpoint'

let s:variables_win_name = 'Debug Variables'
let s:threads_win_name = 'Debug Threads'

let s:command_start =
  \ '-command java_debug_start -p "<project>" ' .
  \ '-h "<host>" -n "<port>" -v "<vim_servername>"'

let s:command_stop = '-command java_debug_stop'

let s:command_session_suspend = '-command java_debug_thread_suspend'
let s:command_thread_suspend = '-command java_debug_thread_suspend ' .
  \ '-t "<thread_id>"'

let s:command_session_resume = '-command java_debug_thread_resume'
let s:command_thread_resume = '-command java_debug_thread_resume ' .
  \ '-t "<thread_id>"'

let s:command_breakpoint_toggle =
  \ '-command java_debug_breakpoint_toggle ' .
  \ '-p "<project>" -f "<file>" -l "<line>"'

let s:command_breakpoint = '-command java_debug_breakpoint -a "<action>"'

let s:command_step = '-command java_debug_step -a "<action>"'

let s:command_status = '-command java_debug_status'
" }}}

function! eclim#java#debug#DefineStatusWinCommands() " {{{
  if !exists(":JavaDebugStop")
    command -nargs=0 -buffer JavaDebugStop :call eclim#java#debug#DebugStop()
  endif

  if !exists(":JavaDebugThreadSuspend")
    command -nargs=? -buffer JavaDebugThreadSuspend 
      \ :call eclim#java#debug#DebugThreadSuspend(<f-args>)
  endif

  if !exists(":JavaDebugThreadResume")
    command -nargs=? -buffer JavaDebugThreadResume 
      \ :call eclim#java#debug#DebugThreadResume(<f-args>)
  endif

  if !exists(":JavaDebugBreakpoint")
    command -nargs=1 -buffer JavaDebugBreakpoint 
      \ :call eclim#java#debug#Breakpoint('<args>')
  endif

  if !exists(":JavaDebugStep")
    command -nargs=1 -buffer JavaDebugStep :call eclim#java#debug#Step('<args>')
  endif

  if !exists(":JavaDebugStatus")
    command -nargs=0 -buffer JavaDebugStatus 
      \ :call eclim#java#debug#Status()
  endif

  if !exists(":JavaDebugGoToFile")
    command -nargs=+ JavaDebugGoToFile :call eclim#java#debug#GoToFile(<f-args>)
  endif

endfunction " }}}

function! eclim#java#debug#DebugStart(host, port) " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  if (v:servername == '')
    call eclim#util#EchoError("Error: To debug, start VIM in server mode.
          \ Usage: vim --servername <name>")
    return
  endif

  if (a:host == '')
    call eclim#util#EchoError("Error: Please specify a host.")
    return
  endif

  if (a:port == '')
    call eclim#util#EchoError("Error: Please specify a port.")
    return
  endif

  call eclim#display#signs#DefineLineHL(s:debug_step_sign_name, 'DebugBreak')

  let project = eclim#project#util#GetCurrentProjectName()
  let command = s:command_start
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<host>', a:host, '')
  let command = substitute(command, '<port>', a:port, '')
  let command = substitute(command, '<vim_servername>', v:servername, '')
  let result = eclim#Execute(command)

  call eclim#util#Echo(result)
endfunction " }}}

function! eclim#java#debug#DebugStop() " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let file = eclim#lang#SilentUpdate()

  let command = s:command_stop
  let result = eclim#Execute(command)

  " Auto close the debug status window
  call eclim#util#DeleteBuffer(s:variables_win_name)
  call eclim#util#DeleteBuffer(s:threads_win_name)

  " Remove the sign from previous location
  if (s:debug_step_prev_line != '' && s:debug_step_prev_file != '')
    call eclim#display#signs#UnplaceFromBuffer(s:debug_step_prev_line,
      \ bufnr(s:debug_step_prev_file))
  endif

  let s:debug_step_prev_line = ''
  let s:debug_step_prev_file = ''

  call eclim#util#Echo(result)
endfunction " }}}

function! eclim#java#debug#DebugThreadSuspend(...) " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let file = eclim#lang#SilentUpdate()

  if a:0 == 0
    let thread_id = eclim#java#debug#GetThreadIdUnderCursor()
  else
    let thread_id = a:1
  endif

  if thread_id != ""
    let command = s:command_thread_suspend
    let command = substitute(command, '<thread_id>', thread_id, '')
  else
    let command = s:command_session_suspend
  endif

  let result = eclim#Execute(command)

  call eclim#util#Echo(result)
endfunction " }}}

function! eclim#java#debug#DebugThreadResume(...) " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let file = eclim#lang#SilentUpdate()

  if a:0 == 0
    let thread_id = eclim#java#debug#GetThreadIdUnderCursor()
  else
    let thread_id = a:1
  endif

  if thread_id != ""
    let command = s:command_thread_resume
    let command = substitute(command, '<thread_id>', thread_id, '')
  else
    let command = s:command_session_resume
  endif

  let result = eclim#Execute(command)

  " Remove the sign from previous location. This is needed here even though it
  " is done in GoToFile function. There may be a time gap until the next
  " breakpoint is hit or the program terminates. We don't want to highligh
  " the current line until then.
  if (s:debug_step_prev_line != '' && s:debug_step_prev_file != '')
    call eclim#display#signs#UnplaceFromBuffer(s:debug_step_prev_line,
      \ bufnr(s:debug_step_prev_file))
  endif

  call eclim#util#Echo(result)
endfunction " }}}

function! eclim#java#debug#Breakpoint(action) " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let command = s:command_breakpoint
  let command = substitute(command, '<action>', a:action, '')

  call eclim#java#debug#DisplayPositions(eclim#Execute(command))
endfunction " }}}

function! eclim#java#debug#DisplayPositions(results) " {{{
  if (type(a:results) != g:LIST_TYPE)
    return
  endif

  if empty(a:results)
    echo "No breakpoints"
    return
  endif

  call eclim#util#SetLocationList(eclim#util#ParseLocationEntries(a:results))
  let locs = getloclist(0)
  exec 'lopen ' . g:EclimLocationListHeight
endfunction " }}}

function! eclim#java#debug#BreakpointToggle() " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#lang#SilentUpdate()
  let line = line('.')

  let command = s:command_breakpoint_toggle
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<line>', line, '')

  let result = eclim#Execute(command)

  call eclim#display#signs#Define(s:breakpoint_sign_name, '*', '')

  if (result == "1")
    call eclim#display#signs#Place(s:breakpoint_sign_name, line)
    call eclim#util#Echo("Breakpoint added")
  else
    call eclim#display#signs#Unplace(line)
    call eclim#util#Echo("Breakpoint removed")
  endif

endfunction " }}}

function! eclim#java#debug#Step(action) " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let command = s:command_step
  let command = substitute(command, '<action>', a:action, '')
  call eclim#Execute(command)
endfunction " }}}

function! eclim#java#debug#Status() " {{{
  let command = s:command_status
  let results = eclim#Execute(command)
  if type(results) == g:DICT_TYPE
    call eclim#java#debug#DisplayStatus(results)
  endif
endfunction " }}}

function! eclim#java#debug#DisplayStatus(results) " {{{
  let status = a:results.status
  let threads = a:results.threads
  let vars = a:results.variables

  " Store current position and restore in the end so that creation of new
  " window does not end up moving the cursor
  let cur_bufnr = bufnr('%')
  let cur_line = line('.')
  let cur_col = col('.')

  call eclim#util#TempWindow(
    \ s:threads_win_name, [status] + threads,
    \ {'orientation': 'horizontal', 'singleWinOnly' : 0})
  setlocal foldmethod=expr
  setlocal foldexpr=eclim#display#fold#GetNeatFold(v:lnum)
  setlocal foldtext=eclim#display#fold#NeatFoldText()
  setlocal foldlevel=1
  setlocal nonu
  call eclim#java#debug#DefineStatusWinCommands()

  call eclim#util#TempWindow(
    \ s:variables_win_name, vars,
    \ {'orientation': 'vertical', 'singleWinOnly' : 0})

  setlocal foldmethod=expr
  setlocal foldexpr=eclim#display#fold#GetNeatFold(v:lnum)
  setlocal foldtext=eclim#display#fold#NeatFoldText()
  setlocal nonu
  call eclim#java#debug#DefineStatusWinCommands()

  " Restore position
  call eclim#util#GoToBufferWindow(cur_bufnr)
  call cursor(cur_line, 1)
endfunction " }}}

function! eclim#java#debug#GoToFile(file, line) " {{{
  " Remove the sign from previous location
  if (s:debug_step_prev_line != '' && s:debug_step_prev_file != '')
    call eclim#display#signs#UnplaceFromBuffer(s:debug_step_prev_line,
      \ bufnr(s:debug_step_prev_file))
  endif

  let s:debug_step_prev_file = a:file
  let s:debug_step_prev_line = a:line
  call eclim#util#GoToTabAwareBufferWindowOrOpen(a:file, "tabnew")
  call cursor(a:line, '^')

  " TODO sign id is line number. Can conflict with other signs while
  " unplacing
  call eclim#display#signs#PlaceInBuffer(s:debug_step_sign_name,
    \ bufnr(a:file), a:line)
endfunction " }}}

function! eclim#java#debug#GetThreadIdUnderCursor() " {{{
  " Check if we are in the right window
  if (bufname("%") != s:threads_win_name)
    return
  endif

  let line = line(".")
  " Ignore the first line as it is the status
  if (line == 1)
    return ""
  endif

  let line_arr = split(getline("."), '(')
  if (len(line_arr) > 1)
    let thread_info_arr = split(line_arr[0], ':')   
    if (len(thread_info_arr) == 2)
      " trim any white space before returning the thread id
      return substitute(thread_info_arr[1], "^\\s\\+\\|\\s\\+$","","g")
    endif
  endif

  " Did not find a valid thread id
  return ""
endfunction " }}}

" vim:ft=vim:fdm=marker
