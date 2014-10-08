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
" Regex used to search for any object ID
" Use a non greedy search to match the closing parenthesis using \{-}
let s:id_search_by_regex = '(id=.\{-})'

" Pattern used to search for specific object ID
let s:id_search_by_value = '(id=<value>)'

" TODO This needs to be maintained per thread. We can use debug session id +
" thread id as key
let s:debug_step_prev_file = ''
let s:debug_step_prev_line = ''

let s:breakpoint_sign = 'eclim_breakpoint'
let s:breakpoint_sign_current = 'eclim_breakpoint_cur'

let s:variable_buf_name = 'Debug Variables'
let s:thread_buf_name = 'Debug Threads'
let s:breakpoint_buf_name = 'Breakpoints'

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

let s:command_breakpoint_add =
  \ '-command java_debug_breakpoint_add ' .
  \ '-p "<project>" -f "<file>" -l "<line>"'

let s:command_breakpoint_get_all = '-command java_debug_breakpoint_get'
let s:command_breakpoint_get =
  \ '-command java_debug_breakpoint_get -f "<file>"'

let s:command_breakpoint_remove_all = '-command java_debug_breakpoint_remove'
let s:command_breakpoint_remove_file =
  \ '-command java_debug_breakpoint_remove -f "<file>"'
let s:command_breakpoint_remove =
  \ '-command java_debug_breakpoint_remove -f "<file>" -l "<line>"'

let s:command_step = '-command java_debug_step -a "<action>"'
let s:command_step_thread = '-command java_debug_step -a "<action>" -t "<thread_id>"'

let s:command_status = '-command java_debug_status'

let s:command_variable_expand = '-command java_debug_variable_expand -v "<value_id>"'
let s:command_variable_detail = '-command java_debug_variable_detail -v "<value_id>"'
" }}}

function! s:DefineStatusWinSettings() " {{{
  " Defines settings that are applicable in any of the debug status windows.
  if !exists(":JavaDebug")
    command -nargs=0 -buffer JavaDebugStop :call eclim#java#debug#DebugStop()
    command -nargs=0 -buffer JavaDebugStatus :call eclim#java#debug#Status()
    command -nargs=+ -buffer JavaDebugStep :call eclim#java#debug#Step(<f-args>)

    command -nargs=0 -buffer JavaDebugThreadSuspendAll
      \ :call eclim#java#debug#DebugThreadSuspendAll()
    command -nargs=0 -buffer JavaDebugThreadResume
      \ :call eclim#java#debug#DebugThreadResume()
    command -nargs=0 -buffer JavaDebugThreadResumeAll
      \ :call eclim#java#debug#DebugThreadResumeAll()
  endif
endfunction " }}}

function! s:DefineThreadWinSettings() " {{{
  " Defines settings that are applicable only in thread window.
  nnoremap <silent> <buffer> s :call eclim#java#debug#DebugThreadSuspend()<CR>
  nnoremap <silent> <buffer> S :call eclim#java#debug#DebugThreadSuspendAll()<CR>
  nnoremap <silent> <buffer> r :call eclim#java#debug#DebugThreadResume()<CR>
  nnoremap <silent> <buffer> R :call eclim#java#debug#DebugThreadResumeAll()<CR>

  nnoremap <silent> <buffer> ? :call eclim#help#BufferHelp(
    \ [
      \ 's - suspend the thread under the cursor',
      \ 'S - suspend all threads',
      \ 'r - resume the thread under the cursor',
      \ 'R - resume all threads',
    \ ],
    \ 'vertical', 40)<CR>
endfunction " }}}

function! s:DefineVariableWinSettings() " {{{
  " Defines settings that are applicable only in variable window.
  nnoremap <silent> <buffer> <CR> :call <SID>VariableExpand()<CR>
  nnoremap <silent> <buffer> p :call <SID>VariableDetail()<CR>

  nnoremap <silent> <buffer> ? :call eclim#help#BufferHelp(
    \ [
      \ '<CR> - expand the variable under the cursor',
      \ 'p - preview toString value',
    \ ],
    \ 'vertical', 40)<CR>
endfunction " }}}

function! s:DefineBreakpointWinSettings() " {{{
  " Defines settings that are applicable only in breakpoint window.
  call eclim#display#signs#Define(
    \ s:breakpoint_sign, '•', '')

  nnoremap <silent> <buffer> t :call <SID>BreakpointToggle()<CR>
  nnoremap <silent> <buffer> d :call <SID>BreakpointRemove()<CR>

  nnoremap <silent> <buffer> ? :call eclim#help#BufferHelp(
    \ [
      \ 't - toggle breakpoint under cursor',
      \ 'd - delete breakpoint under cursor',
      \ ],
    \ 'vertical', 40)<CR>
endfunction " }}}

function! eclim#java#debug#DebugStart(...) " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  if v:servername == ''
    call eclim#util#EchoError(
      \ "Error: To debug, VIM must be running in server mode.\n" .
      \ "Example: vim --servername <name>")
    return
  endif

  if a:0 != 2
    call eclim#util#EchoError(
      \ "Please specify the host and port of the java process to connect to.\n" .
      \ "Example: JavaDebugStart locahost 1044")
    return
  endif

  let host = a:1
  let port = a:2

  if port !~ '^\d\+'
    call eclim#util#EchoError("Error: Please specify a valid port number.")
    return
  endif

  let project = eclim#project#util#GetCurrentProjectName()
  let command = s:command_start
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<host>', host, '')
  let command = substitute(command, '<port>', port, '')
  let command = substitute(command, '<vim_servername>', v:servername, '')
  let result = eclim#Execute(command)

  call eclim#util#Echo(result)
endfunction " }}}

function! eclim#java#debug#DebugStop() " {{{
  let command = s:command_stop
  let result = eclim#Execute(command)

  " Auto close the debug status window
  call eclim#util#DeleteBuffer(s:variable_buf_name)
  call eclim#util#DeleteBuffer(s:thread_buf_name)

  " Remove the sign from previous location
  if (s:debug_step_prev_line != '' && s:debug_step_prev_file != '')
    call eclim#display#signs#UnplaceFromBuffer(s:debug_step_prev_line,
      \ bufnr(s:debug_step_prev_file))
  endif

  let s:debug_step_prev_line = ''
  let s:debug_step_prev_file = ''

  call eclim#util#Echo(result)
endfunction " }}}

function! eclim#java#debug#DebugThreadSuspend() " {{{
  " Check if we are in the right window
  if (bufname("%") != s:thread_buf_name)
    call eclim#util#EchoError("Thread suspend command not applicable in this window.")
    return
  endif

  " Suspends thread under cursor.
  let thread_id = s:GetIdUnderCursor()
  if thread_id == ""
    call eclim#util#Echo("No valid thread found under cursor")
    return
  endif

  let command = s:command_thread_suspend
  let command = substitute(command, '<thread_id>', thread_id, '')

  call eclim#util#Echo(eclim#Execute(command))
endfunction " }}}

function! eclim#java#debug#DebugThreadSuspendAll() " {{{
  " Suspends all threads.
  let command = s:command_session_suspend
  call eclim#util#Echo(eclim#Execute(command))
endfunction " }}}

function! eclim#java#debug#DebugThreadResume() " {{{
  " Resume a single thread.
  " Even if thread_id is empty, invoke resume. If there is atleast one
  " suspended thread, then the server could resume that. If not, it will
  " re-run a message.
  " Check if we are in the right window
  if (bufname("%") == s:thread_buf_name)
    let thread_id = s:GetIdUnderCursor()
  else
    let thread_id = ""
  endif

  let command = s:command_thread_resume
  let command = substitute(command, '<thread_id>', thread_id, '')

  let result = eclim#Execute(command)

  " Remove the sign from previous location. This is needed here even though it
  " is done in GoToFile function. There may be a time gap until the next
  " breakpoint is hit or the program terminates. We don't want to highlight
  " the current line until then.
  if (s:debug_step_prev_line != '' && s:debug_step_prev_file != '')
    call eclim#display#signs#UnplaceFromBuffer(s:debug_step_prev_line,
      \ bufnr(s:debug_step_prev_file))
  endif

  call eclim#util#Echo(result)
endfunction " }}}

function! eclim#java#debug#DebugThreadResumeAll() " {{{
  " Resumes all threads.
  let command = s:command_session_resume
  let result = eclim#Execute(command)

  " Remove the sign from previous location. This is needed here even though it
  " is done in GoToFile function. There may be a time gap until the next
  " breakpoint is hit or the program terminates. We don't want to highlight
  " the current line until then.
  if (s:debug_step_prev_line != '' && s:debug_step_prev_file != '')
    call eclim#display#signs#UnplaceFromBuffer(s:debug_step_prev_line,
      \ bufnr(s:debug_step_prev_file))
  endif

  call eclim#util#Echo(result)
endfunction " }}}

function! eclim#java#debug#BreakpointAdd() " {{{
  " Adds breakpoint for current cursor position.
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#lang#SilentUpdate()
  let line = line('.')

  let command = s:command_breakpoint_add
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<line>', line, '')

  call eclim#util#Echo(eclim#Execute(command))
endfunction " }}}

function! eclim#java#debug#BreakpointGet() " {{{
  " Displays breakpoints present in file loaded in current window.
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let file = expand('%:p')

  let command = s:command_breakpoint_get
  let command = substitute(command, '<file>', file, '')

  call s:DisplayBreakpoints(eclim#Execute(command))
endfunction " }}}

function! eclim#java#debug#BreakpointGetAll() " {{{
  " Displays all breakpoints in the workspace.
  let command = s:command_breakpoint_get_all
  call s:DisplayBreakpoints(eclim#Execute(command))
endfunction " }}}

function! s:BreakpointRemove() " {{{
  " Removes breakpoint defined under the cursor.
  let loc_list_entry = getline(line('.'))

  " location list entry is of the form: filename|line_num col col_num|project
  let tokens = split(loc_list_entry, "|")
  let file = tokens[0]
  let line = s:Trim(split(tokens[1], " ")[0])

  let command = s:command_breakpoint_remove
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<line>', line, '')

  call eclim#util#Echo(eclim#Execute(command))
  call eclim#java#debug#BreakpointGetAll()
endfunction " }}}

function! eclim#java#debug#BreakpointRemoveFile() " {{{
  " Removes all breakpoints defined in file loaded in current
  " window.
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let file = expand('%:p')

  let command = s:command_breakpoint_remove_file
  let command = substitute(command, '<file>', file, '')

  call eclim#util#Echo(eclim#Execute(command))
endfunction " }}}

function! eclim#java#debug#BreakpointRemoveAll() " {{{
  " Removes all breakpoints from workspace.
  let command = s:command_breakpoint_remove_all
  call eclim#util#Echo(eclim#Execute(command))
endfunction " }}}

function! s:DisplayBreakpoints(results) " {{{
  if (type(a:results) != g:LIST_TYPE)
    return
  endif

  if empty(a:results)
    echo "No breakpoints"
    return
  endif

  call eclim#util#SetLocationList(eclim#util#ParseLocationEntries(a:results))
  exec 'lopen ' . g:EclimLocationListHeight

  call s:DefineBreakpointWinSettings()

  " Place sign for breakpoints that are enabled
  let line_num = 0
  for result in a:results
    let line_num = line_num + 1

    if !has_key(result, "metaInfo")
      continue
    endif

    let enabled = result.metaInfo
    if enabled == "e"
      call eclim#display#signs#Place(s:breakpoint_sign, line_num)
    endif
  endfor
endfunction " }}}

function! s:BreakpointToggle() " {{{
  "Enables or disables the breakpoint under cursor.

  let loc_list_entry = getline(line('.'))

  " location list entry is of the form: filename|line_num col col_num|project
  let tokens = split(loc_list_entry, "|")
  let file = tokens[0]
  let project = s:Trim(tokens[2])
  let line = s:Trim(split(tokens[1], " ")[0])

  let command = s:command_breakpoint_toggle
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<line>', line, '')

  let result = eclim#Execute(command)

  if (result == "1")
    call eclim#display#signs#Place(s:breakpoint_sign, line('.'))
  elseif (result == "0")
    call eclim#display#signs#Unplace(line('.'))
  else
    call eclim#util#EchoError("Breakpoint does not exist")
  endif

endfunction " }}}

function! eclim#java#debug#Step(action) " {{{
  if (bufname("%") == s:thread_buf_name)
    let thread_id = s:GetIdUnderCursor()
  else
    let thread_id = ""
  endif

  if thread_id != ""
    let command = s:command_step_thread
    let command = substitute(command, '<thread_id>', thread_id, '')
  else
    " Step through the currently stepping thread, if one exists
    let command = s:command_step
  endif

  let command = substitute(command, '<action>', a:action, '')
  let result = eclim#Execute(command)

  if type(result) == g:STRING_TYPE
    call eclim#util#Echo(result)
  endif
endfunction " }}}

function! eclim#java#debug#Status() " {{{
  let command = s:command_status
  let results = eclim#Execute(command)

  if type(results) != g:DICT_TYPE
    return
  endif

  if has_key(results, 'state')
    let state = [results.state]
  else
    let state = []
  endif

  if has_key(results, 'threads')
    let threads = results.threads
  else
    let threads = []
  endif

  if has_key(results, 'variables')
    let vars = results.variables
  else
    let vars = []
  endif

  call s:CreateStatusWindow(state + threads, vars)
endfunction " }}}

function! s:CreateStatusWindow(threads, vars) " {{{
  " Creates the debug status windows if they do not already exist.
  " The newly created windows are initialized with given content.

  " Store current position and restore in the end so that creation of new
  " window does not end up moving the cursor
  let cur_bufnr = bufnr('%')
  let cur_line = line('.')
  let cur_col = col('.')

  let thread_win_opts = {'orientation': 'horizontal'}
  call eclim#util#TempWindow(s:thread_buf_name, a:threads, thread_win_opts)

  setlocal foldmethod=expr
  setlocal foldexpr=eclim#display#fold#GetTreeFold(v:lnum)
  setlocal foldtext=eclim#display#fold#TreeFoldText()
  " Display the stacktrace of suspended threads
  setlocal foldlevel=5
  " Avoid the ugly - symbol on folded lines
  setlocal fillchars="fold:\ "
  setlocal nonumber
  call s:DefineThreadWinSettings()
  call s:DefineStatusWinSettings()

  let var_win_opts = {
    \ 'orientation': g:EclimJavaDebugStatusWinOrientation,
    \ 'width': g:EclimJavaDebugStatusWinWidth,
    \ 'height': g:EclimJavaDebugStatusWinHeight
  \ }
  call eclim#util#TempWindow(s:variable_buf_name, a:vars, var_win_opts) 
  setlocal foldmethod=expr
  setlocal foldexpr=eclim#display#fold#GetTreeFold(v:lnum)
  setlocal foldtext=eclim#display#fold#TreeFoldText()
  " Avoid the ugly - symbol on folded lines
  setlocal fillchars="fold:\ "
  setlocal nonumber
  call s:DefineVariableWinSettings()
  call s:DefineStatusWinSettings()

  " Restore position
  call eclim#util#GoToBufferWindow(cur_bufnr)
  call cursor(cur_line, cur_col)
  redraw!
endfunction " }}}

function! s:VariableExpand() " {{{
  " Expands the variable value under cursor and adds the child variables under
  " it in the tree.

  " Check if we are in the right window
  if (bufname("%") != s:variable_buf_name)
    call eclim#util#EchoError("Variable expand command not applicable in this window.")
    return
  endif

  " Return if the current line does not contain any fold
  if (matchstr(getline(line('.')), "▸\\|▾") == "")
    return
  endif

  " Make the buffer writable
  setlocal modifiable
  setlocal noreadonly

  let id = s:GetIdUnderCursor()
  if (id != "")
    let command = s:command_variable_expand
    let command = substitute(command, '<value_id>', id, '')

    let results = eclim#Execute(command)
    if (type(results) == g:LIST_TYPE && len(results) > 0)
      call append(line('.'), results)

      " Remove the placeholder line used to get folding to work.
      " But first unfold if its folded.
      foldopen

      let cur_line = line('.')
      let cur_col = col('.')
      
      let empty_line = line('.') + len(results) + 1
      exec 'silent ' . empty_line . ',' . empty_line . 'd'

      " Restore cursor position
      call cursor(cur_line, cur_col)
    else
      exec "normal! za"
    endif
  else
    exec "normal! za"
  endif

  " Restore settings
  setlocal nomodified
  setlocal nomodifiable
  setlocal readonly

endfunction " }}}

function! s:VariableDetail() " {{{
  " Displays the detail of the variable value under cursor in status line.

  " Check if we are in the right window
  if (bufname("%") != s:variable_buf_name)
    call eclim#util#EchoError("Variable detail command not applicable in this window.")
    return
  endif

  let id = s:GetIdUnderCursor()
  if (id == "")
    return
  endif

  let command = s:command_variable_detail
  let command = substitute(command, '<value_id>', id, '')

  let result = eclim#Execute(command)
  call eclim#util#Echo(result)
endfunction " }}}

function! eclim#java#debug#GoToFile(file, line) " {{{
  " Remove the sign from previous location
  if (s:debug_step_prev_line != '' && s:debug_step_prev_file != '')
    call eclim#display#signs#UnplaceFromBuffer(s:debug_step_prev_line,
      \ bufnr(s:debug_step_prev_file))
  endif

  " Jump out of status window so that the buffer is opened in the code window
  " If you are in variable window, first go to thread window
  if (bufname("%") == s:thread_buf_name)
    call eclim#util#GoToBufferWindow(b:filename)
  endif

  " If you are in thread window, go back to code window
  if (bufname("%") == s:variable_buf_name)
    call eclim#util#GoToBufferWindow(b:filename)
    call eclim#util#GoToBufferWindow(b:filename)
  endif

  let s:debug_step_prev_file = a:file
  let s:debug_step_prev_line = a:line
  call eclim#util#GoToBufferWindowOrOpen(a:file, "edit")
  call cursor(a:line, '^')

  " gross, but seems to be the easiest way to remove the sign from all buffers
  silent! call eclim#display#signs#Undefine(s:breakpoint_sign_current)

  call eclim#display#signs#Define(
    \ s:breakpoint_sign_current, '•',
    \ g:EclimHighlightSuccess,
    \ g:EclimJavaDebugLineHighlight)
  call eclim#display#signs#PlaceInBuffer(
    \ s:breakpoint_sign_current, bufnr(a:file), a:line)
endfunction " }}}

function! s:GetIdUnderCursor() " {{{
  " Returns the object ID under cursor. Object ID is present in the form:
  " (id=X) where X is the ID.
  "
  " An empty string is returned if there is no valid ID.

  " Look for the substring (id=X) where X is the object ID
  let id_substr = matchstr(getline("."), s:id_search_by_regex)
  if (id_substr == "")
    return ""
  else
    let id = split(id_substr, '=')[1]
    " remove the trailing ) character
    return substitute(id, ")","","g")
  endif
endfunction " }}}

function! eclim#java#debug#ThreadViewUpdate(thread_id, kind, value) " {{{
  " Updates the thread window with new thread information.
  " Update kind can be one of the following:
  " a: Add a new thread
  " m: Modify an existing thread
  " r: Remove an existing thread
  
  " Overwrite the message containing the long list of arguments to this function
  call eclim#util#Echo("Refreshing ...")

  " Store current position and restore in the end
  let cur_bufnr = bufnr('%')
  let cur_line = line('.')
  let cur_col = col('.')

  let found = eclim#util#GoToBufferWindow(s:thread_buf_name)
  if found == 0
    return
  endif

  setlocal modifiable
  setlocal noreadonly

  let lines = split(a:value, "<eol>")
  if a:kind == 'a'
    " Add to the end
    call append(line('$'), lines)
  else
    " Go to line having the thread id
    call cursor(1, 1)
    let pattern = substitute(s:id_search_by_value, '<value>', a:thread_id, '')
    " Don't wrap search.
    let match_line = search(pattern, 'W')
    if match_line != 0
      " Get the next thread entry. Don't wrap search.
      let next_match_line = search(s:id_search_by_regex, 'W')
      if next_match_line == 0
        let last_line_del = line('$')
      else
        let last_line_del = next_match_line - 1
      endif

      " Delete the desired lines
      let undolevels = &undolevels
      set undolevels=-1
      silent exec match_line . ',' . last_line_del . 'delete _'
      let &undolevels = undolevels

      " In case of modify, add the new values
      if a:kind == 'm'
        call append(match_line - 1, lines)
      endif
    endif
  endif

  setlocal nomodifiable
  setlocal readonly

  " Restore position
  call eclim#util#GoToBufferWindow(cur_bufnr)
  call cursor(cur_line, cur_col)
  call eclim#util#Echo(" ")
endfunction " }}}

function! eclim#java#debug#SessionTerminated() " {{{
  " Cleans up the debug status window.

  " Store current position and restore in the end
  let cur_bufnr = bufnr('%')
  let cur_line = line('.')
  let cur_col = col('.')

  let found = eclim#util#GoToBufferWindow(s:thread_buf_name)
  if found == 0
    return
  endif

  setlocal modifiable
  setlocal noreadonly

  " Go to line having the thread id
  call cursor(1, 1)
  s/Connect\(ed\|ing\)/Disconnected/

  " Delete any threads that have not been cleared
  if line('$') > 1
    silent 2,$delete _
  endif

  setlocal nomodifiable
  setlocal readonly

  " Restore position
  call eclim#util#GoToBufferWindow(cur_bufnr)
  call cursor(cur_line, cur_col)

  call eclim#util#TempWindowClear(s:variable_buf_name)
endfunction " }}}

function! eclim#java#debug#VariableViewUpdate(value) " {{{
  " Updates the variable window with new set of values.

  call eclim#util#Echo("Refreshing ...")
  call eclim#util#TempWindowClear(s:variable_buf_name, split(a:value, "<eol>"))
  call eclim#util#Echo(" ")
endfunction " }}}
      
function! s:Trim(value) " {{{
  " Removes any leading and trailing white spaces.

  return substitute(a:value, "^\\s\\+\\|\\s\\+$","","g")
endfunction " }}}

" vim:ft=vim:fdm=marker
