" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for common.vim
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

" TestOpenRelative() {{{
function! TestOpenRelative ()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test_java/build.xml

  call eclim#common#OpenRelative('edit', 'pom.xml')
  call VUAssertTrue(bufwinnr('eclim_unit_test_java/pom.xml') > -1,
    \ 'Did not open pom.xml.')
  bdelete
endfunction " }}}

" TestOpenFiles() {{{
function! TestOpenFiles ()
  exec 'cd ' . g:TestEclimWorkspace
  call eclim#common#OpenFiles('split',
    \ 'eclim_unit_test_java/build.xml eclim_unit_test_java/pom.xml')
  call VUAssertTrue(bufwinnr('eclim_unit_test_java/build.xml') > -1,
    \ 'Did not open build.xml.')
  call VUAssertTrue(bufwinnr('eclim_unit_test_java/pom.xml') > -1,
    \ 'Did not open pom.xml.')
  bdelete
  bdelete
endfunction " }}}

" TestSwapWords() {{{
function! TestSwapWords ()
  new
  call setline(1, 'one, two')
  call cursor(1, 1)
  call eclim#common#SwapWords()
  call VUAssertEquals('two, one', getline(1), "Words not swaped correctly.")
  bdelete!
endfunction " }}}

" TestCommandCompleteRelative() {{{
function! TestCommandCompleteRelative ()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test_java/build.xml
  let results = eclim#common#CommandCompleteRelative('p', 'SplitRelative p', 15)
  call VUAssertEquals(1, len(results), "Wrong number of results.")
  call VUAssertEquals('pom.xml', results[0], "Wrong result.")
  bdelete
endfunction " }}}

" vim:ft=vim:fdm=marker
