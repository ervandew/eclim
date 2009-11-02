" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/tools.html
"
" License:
"
" Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
  let s:open_console = 'Open jconsole'
  let s:view_info = 'View Info'
  let s:view_stacks = 'View Stacks'
  let s:view_map = 'View Memory Map'
  let s:args_main = 'Arguments To Main Method'
  let s:args_vm = 'Arguments To JVM'

  let s:supported_command = '\(' .
      \ s:open_console . '\|' .
      \ s:view_info . '\|' .
      \ s:view_stacks . '\|' .
      \ s:view_map .
    \ '\)'

  hi link JpsArguments Normal
  hi link JpsViewAdditional Normal
  hi JpsViewAdditional gui=underline,bold term=underline,bold cterm=underline,bold
" }}}

" Jps() {{{
" Function to execute jps and push the results into a new window where the
" user can perform additional commands.
function eclim#java#tools#Jps()
  call eclim#util#Echo('Executing...')

  let content = []
  let processes = eclim#java#tools#GetJavaProcesses()
  if len(processes) == 1 && string(processes[0]) == '0'
    return
  endif

  for process in processes
    if len(content) > 0
      call add(content, "")
    endif

    call add(content, process.id . ' - ' . process.name)

    if executable('jconsole')
      call add(content, "\t" . s:open_console)
    endif

    if executable('jinfo')
      call add(content, "\t" . s:view_info)
    endif

    if executable('jstack')
      call add(content, "\t" . s:view_stacks)
    endif

    if executable('jmap')
      call add(content, "\t" . s:view_map)
    endif

    call add(content, "")

    call add(content, "\t" . s:args_main . " {")
    let args_main = has_key(process, 'args_main') ?
      \ map(split(process.args_main), '"\t\t" . v:val') : []
    let content = content + args_main
    call add(content, "\t}")

    if has_key(process, 'args_vm')
      call add(content, "")
      call add(content, "\t" . s:args_vm . " {")
      let args_vm = map(split(process.args_vm), '"\t\t" . v:val')
      let content = content + args_vm
      call add(content, "\t}")
    endif
  endfor

  if len(content) == 0
    call add(content, 'No Running Java Processes Found')
  endif

  call eclim#util#TempWindow('Java_Processes', content)

  setlocal ft=jps_list
  setlocal foldmethod=syntax
  setlocal foldlevel=0
  setlocal foldtext=getline(v:foldstart)

  exec 'syntax match JpsViewAdditional /' . s:supported_command . '$/'
  exec 'syntax region JpsArguments start=/' . s:args_main . ' {$/ end=/^\s*}$/ fold'
  exec 'syntax region JpsArguments start=/' . s:args_vm . ' {$/ end=/^\s*}$/ fold'

  nnoremap <silent> <buffer> <cr> :call <SID>ViewAdditionalInfo()<cr>

  call eclim#util#Echo(' ')
endfunction " }}}

" GetJavaProcesses() {{{
" Gets a list of maps containing an entry for each running java process
" excluding the jps process and any processes with no classname or jar
" file (eg vms started by opera).
" id - The process id.
" name - The classname or jar file the process was started from.
" args_main - Any arguments passed to the main method.
" args_vm - Any arguments passed to the vm.
function eclim#java#tools#GetJavaProcesses()
  let java_processes = []
  let result = eclim#util#System('jps -vV')
  if v:shell_error
    call eclim#util#EchoError('Unable to execute jps - ' . result)
    return [0]
  endif
  let vm_args = split(result, '\n')
  for process in split(eclim#util#System('jps -lm'), '\n')
    if process =~ 'sun.tools.jps.Jps' "|| process =~ '^[0-9]\+\s*$'
      continue
    endif

    let java_process_info = {}
    let java_process_info['id'] = substitute(process, '\(.\{-}\) .*', '\1', '')
    let java_process_info['name'] =
      \ substitute(process, '.\{-} \(.\{-}\) .*', '\1', '')
    if process =~ '.\{-} .\{-} \(.*\)'
      let java_process_info['args_main'] =
        \ substitute(process, '.\{-} .\{-} \(.*\)', '\1', '')
    endif

    let index = 0
    for args in vm_args
      if args =~ '^' . java_process_info.id . '\>'
        if args =~ '.\{-} .\{-} \(.*\)'
          let java_process_info['args_vm'] =
            \ substitute(args, '.\{-} .\{-} \(.*\)', '\1', '')
        endif
        call remove(vm_args, index)
      endif
      let index += 1
    endfor

    call add(java_processes, java_process_info)
  endfor
  return java_processes
endfunction " }}}

" ViewAdditionalInfo() {{{
" Invoked by mapping on jps window.
function s:ViewAdditionalInfo()
  let line = getline('.')
  if line =~ '^\s*' . s:supported_command . '$'
    " get the process id.
    let lnum = search('^[0-9]\+ - ', 'bn')
    let id = substitute(getline(lnum), '^\([0-9]\+\) - .*', '\1', '')

    if line =~ '^\s*' . s:open_console . '$'
      call s:OpenConsole(id)
    elseif line =~ '^\s*' . s:view_info . '$'
      call s:ViewInfo(id)
    elseif line =~ '^\s*' . s:view_stacks . '$'
      call s:ViewStacks(id)
    elseif line =~ '^\s*' . s:view_map . '$'
      call s:ViewMap(id)
    endif
  endif
endfunction " }}}

" OpenConsole(id) {{{
" Open jconsole for the process with the given id.
function s:OpenConsole(id)
  call eclim#util#Echo('Executing...')

  if has('win32') || has('win64')
    call eclim#util#Exec('silent! !start jconsole ' . a:id)
  else
    call eclim#util#Exec('silent! !jconsole ' . a:id . ' &')
  endif
  exec "normal! \<c-l>"

  call eclim#util#Echo(' ')
endfunction " }}}

" ViewInfo(id) {{{
" Open a window with extended info for the process with the given id.
function s:ViewInfo(id)
  if executable('jinfo')
    call eclim#util#Echo('Executing...')

    let content = split(eclim#util#System('jinfo ' . a:id), '\n')
    if v:shell_error
      call eclim#util#EchoError('Unable to execute jinfo.')
      return
    endif

    call eclim#util#TempWindow('Java_Process_Info_' . a:id, content)
    setlocal ft=jproperties

    call eclim#util#Echo(' ')
  endif
endfunction " }}}

" ViewStacks(id) {{{
" Open a window containing thread stacks for the process with the given id.
function s:ViewStacks(id)
  if executable('jstack')
    call eclim#util#Echo('Executing...')
    let content = split(eclim#util#System('jstack ' . a:id), '\n')

    if v:shell_error
      call eclim#util#EchoError('Unable to execute jstack.')
      return
    endif

    call map(content, 'substitute(v:val, "^   \\(\\S\\)", "  \\1", "")')
    call map(content, 'substitute(v:val, "^\t", "      ", "")')

    call eclim#util#TempWindow('Java_Process_Stacks_' . a:id, content)
    setlocal ft=java

    call eclim#util#Echo(' ')
  endif
endfunction " }}}

" ViewMap(id) {{{
" Open a window containing memory map for the process with the supplied id.
function s:ViewMap(id)
  if executable('jmap')
    call eclim#util#Echo('Executing...')
    let content = split(eclim#util#System('jmap ' . a:id), '\n')

    if v:shell_error
      call eclim#util#EchoError('Unable to execute jmap.')
      return
    endif

    call eclim#util#TempWindow('Java_Process_Map_' . a:id, content)

    call eclim#util#Echo(' ')
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
