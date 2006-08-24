" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/junit.html
"
" License:
"
" Copyright (c) 2005 - 2006
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
let s:command_impl =
  \ '-filter vim -command java_junit_impl -p "<project>" -f "<file>" <base>'
let s:command_insert =
  \ '-filter vim -command java_junit_impl -p "<project>" -f "<file>" <base> ' .
  \ '-t "<type>" -s <superType> <methods>'
" }}}

" JUnitImpl() {{{
" Opens a window that allows the user to choose methods to implement tests
" for.
function! eclim#java#junit#JUnitImpl ()
  if !eclim#project#IsCurrentFileInProject()
    return
  endif

  call eclim#java#util#SilentUpdate()

  let project = eclim#project#GetCurrentProjectName()

  let command = s:command_impl
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', eclim#java#util#GetFilename(), '')
  let base = substitute(expand('%:t'), 'Test', '', '')
  let base = substitute(eclim#java#util#GetPackage(), '\.', '/', 'g') . "/" . base
  if eclim#java#util#FileExists(base)
    let base = fnamemodify(base, ':r')
    let base = substitute(base, '/', '.', 'g')
    let command = substitute(command, '<base>', '-b ' . base, '')
  else
    let base = ""
    let command = substitute(command, '<base>', '', '')
  endif

  call eclim#java#junit#JUnitImplWindow(command)
  let b:base = base
endfunction " }}}

" JUnitImplWindow(command) {{{
function! eclim#java#junit#JUnitImplWindow (command)
  call eclim#util#TempWindowCommand
    \ (a:command, eclim#java#util#GetFilename() . "_impl")

  setlocal ft=java
  call eclim#java#impl#ImplWindowFolding()

  nnoremap <silent> <buffer> <cr> :call <SID>AddTestImpl(0)<cr>
  vnoremap <silent> <buffer> <cr> :<C-U>call <SID>AddTestImpl(1)<cr>
endfunction " }}}

" AddTestImpl(visual) {{{
function! s:AddTestImpl (visual)
  let command = s:command_insert
  if b:base != ""
    let command = substitute(command, '<base>', '-b ' . b:base, '')
  else
    let command = substitute(command, '<base>', '', '')
  endif

  call eclim#java#impl#ImplAdd
    \ (command, function("eclim#java#junit#JUnitImplWindow"), a:visual)
endfunction " }}}

" vim:ft=vim:fdm=marker
