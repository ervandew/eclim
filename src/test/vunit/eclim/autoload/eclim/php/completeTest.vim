" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for complete.vim
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

" SetUp() {{{
function! SetUp ()
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_php'
endfunction " }}}

" TestComplete() {{{
function! TestComplete()
  edit! php/complete/test.php
  call PeekRedir()

  call cursor(10, 13)
  let start = eclim#php#complete#CodeComplete(1, '')
  call VUAssertEquals(12, start, 'Wrong starting column.')

  let results = eclim#php#complete#CodeComplete(0, '')
  call PeekRedir()
  echo 'results = ' . string(results)
  call VUAssertTrue(len(results) == 3, 'Wrong number of results.')
  call VUAssertEquals('methodA1(', results[0].word, 'Wrong result.')
  call VUAssertEquals('methodA2()', results[1].word, 'Wrong result.')
  call VUAssertEquals('variable1', results[2].word, 'Wrong result.')
endfunction " }}}

" vim:ft=vim:fdm=marker
