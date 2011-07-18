" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/delegate.html
"
" License:
"
" Copyright (C) 2005 - 2011  Eric Van Dewoestine
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
  \ '-command java_delegate -p "<project>" -f "<file>" -o <offset> -e <encoding>' .
  \ ' -t "<type>" -s "<superType>" <methods>'
" }}}

" Delegate() {{{
" Opens a window that allows the user to choose delegate methods to implement.
function! eclim#java#delegate#Delegate()
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  call eclim#java#util#SilentUpdate()

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()
  let offset = eclim#util#GetCurrentElementOffset()
  let encoding = eclim#util#GetEncoding()

  let command = s:command_delegate
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<offset>', offset, '')
  let command = substitute(command, '<encoding>', encoding, '')

  call eclim#java#delegate#DelegateWindow(command, offset, encoding)
endfunction " }}}

" DelegateWindow(command, [offset, encoding]) {{{
function! eclim#java#delegate#DelegateWindow(command, ...)
  let name = eclim#project#util#GetProjectRelativeFilePath() . "_delegate"
  let project = eclim#project#util#GetCurrentProjectName()
  let workspace = eclim#project#util#GetProjectWorkspace(project)
  let port = eclim#client#nailgun#GetNgPort(workspace)

  if eclim#util#TempWindowCommand(a:command, name, port)
    setlocal ft=java
    let offset = len(a:000) >= 1 ? a:000[0] : getbufvar('%', 'offset')
    let encoding = len(a:000) >= 2 ? a:000[1] : getbufvar('%', 'encoding')
    if offset == '' || encoding == ''
      throw 'Invalid state: offset=' . offset . ' encoding=' . encoding
    endif
    let b:offset = offset
    let b:encoding = encoding
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
  let command = s:command_insert
  let command = substitute(command, '<offset>', b:offset, '')
  let command = substitute(command, '<encoding>', b:encoding, '')
  call eclim#java#impl#ImplAdd
    \ (command, function("eclim#java#delegate#DelegateWindow"), a:visual)
endfunction " }}}

" vim:ft=vim:fdm=marker
