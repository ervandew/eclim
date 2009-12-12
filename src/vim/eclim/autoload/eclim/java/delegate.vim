" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/delegate.html
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
let s:command_delegate =
  \ '-command java_delegate -p "<project>" -f "<file>" -o <offset> -e <encoding>'
let s:command_insert =
  \ '-command java_delegate -p "<project>" -f "<file>" -t "<type>" ' .
  \ '-s "<superType>" <methods>'
" }}}

" Delegate() {{{
" Opens a window that allows the user to choose delegate methods to implement.
function! eclim#java#delegate#Delegate()
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  call eclim#java#util#SilentUpdate()

  let project = eclim#project#util#GetCurrentProjectName()
  let filename = eclim#java#util#GetFilename()
  let offset = eclim#util#GetCurrentElementOffset()

  let command = s:command_delegate
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', filename, '')
  let command = substitute(command, '<offset>', offset, '')
  let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')

  call eclim#java#delegate#DelegateWindow(command)
endfunction " }}}

" DelegateWindow(command) {{{
function! eclim#java#delegate#DelegateWindow(command)
  let name = eclim#java#util#GetFilename() . "_delegate"
  let project = eclim#project#util#GetCurrentProjectName()
  let workspace = eclim#project#util#GetProjectWorkspace(project)
  let port = eclim#client#nailgun#GetNgPort(workspace)

  if eclim#util#TempWindowCommand(a:command, name, port)
    setlocal ft=java
    call eclim#java#impl#ImplWindowFolding()

    if line('$') == 1
      let error = getline(1)
      close
      call eclim#util#EchoError(error)
    endif
    nnoremap <silent> <buffer> <cr> :call <SID>AddDelegate(0)<cr>
    vnoremap <silent> <buffer> <cr> :<C-U>call <SID>AddDelegate(1)<cr>
  endif
endfunction " }}}

" AddDelegate(visual) {{{
function! s:AddDelegate(visual)
  call eclim#java#impl#ImplAdd
    \ (s:command_insert, function("eclim#java#delegate#DelegateWindow"), a:visual)
endfunction " }}}

" vim:ft=vim:fdm=marker
