" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for correct.vim
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

" SetUp() {{{
function! SetUp ()
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
endfunction " }}}

" TestCorrect() {{{
function! TestCorrect ()
  edit! src/org/eclim/test/correct/TestCorrectVUnit.java
  call PeekRedir()

  write
  call PeekRedir()
  let errors = getloclist(0)
  call VUAssertEquals(1, len(errors), 'No errors to correct.')

  call cursor(errors[0].lnum, errors[0].col)
  JavaCorrect
  call PeekRedir()

  call VUAssertTrue(bufname('%') =~ 'TestCorrectVUnit.java_correct$',
    \ 'Correct window not opened.')
  call VUAssertEquals('ArrayList cannot be resolved to a type', getline(1),
    \ 'Wrong error message.')

  call VUAssertTrue(search("Import 'ArrayList' (java.util)"),
    \ 'Required correction not found.')

  call cursor(16, 1)
  exec "normal \<cr>"
  call PeekRedir()

  let lines = readfile(expand('%'))
  call PeekRedir()
  for line in lines
    echom '|' . line
  endfor

  call VUAssertTrue(search('^import java\.'),
    \ 'Correction not applied.')

  bdelete!
endfunction " }}}

" vim:ft=vim:fdm=marker
