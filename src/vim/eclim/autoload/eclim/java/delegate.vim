" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/delegate.html
"
" License:
"
" Copyright (c) 2005 - 2008
"
" Licensed under the Apache License, Version 2.0 (the "License");
" you may not use this file except in compliance with the License.
" You may obtain a copy of the License at
"
"      http://www.apache.org/licenses/LICENSE-2.0
"
" Unless required by applicable law or agreed to in writing, software
" distributed under the License is distributed on an "AS IS" BASIS,
" WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
" See the License for the specific language governing permissions and
" limitations under the License.
"
" }}}

" Script Variables {{{
let s:command_delegate =
  \ '-command java_delegate -p "<project>" -f "<file>" -o <offset>'
let s:command_insert =
  \ '-command java_delegate -p "<project>" -f "<file>" -t "<type>" ' .
  \ '-s <superType> <methods>'
" }}}

" Delegate() {{{
" Opens a window that allows the user to choose delegate methods to implement.
function! eclim#java#delegate#Delegate ()
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

  call eclim#java#delegate#DelegateWindow(command)
endfunction " }}}

" DelegateWindow(command) {{{
function! eclim#java#delegate#DelegateWindow (command)
  let name = eclim#java#util#GetFilename() . "_delegate"
  if eclim#util#TempWindowCommand(a:command, name)
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
function! s:AddDelegate (visual)
  call eclim#java#impl#ImplAdd
    \ (s:command_insert, function("eclim#java#delegate#DelegateWindow"), a:visual)
endfunction " }}}

" vim:ft=vim:fdm=marker
