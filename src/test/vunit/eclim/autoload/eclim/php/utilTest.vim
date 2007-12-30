" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for util.vim
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_php'
endfunction " }}}

" TestValidate() {{{
function! TestValidate()
  edit! php/src/test.php
  call PeekRedir()

  call eclim#php#util#UpdateSrcFile(1)
  call PeekRedir()

  let results = getloclist(0)
  echo 'results = ' . string(results)

  call VUAssertTrue(len(results) == 2, 'Wrong number of results.')

  call VUAssertEquals(5, results[0].lnum, 'Wrong line num.')
  call VUAssertEquals(5, results[0].col, 'Wrong col num.')
  call VUAssertEquals("Syntax Error: expecting: ',' or ';'", results[0].text, 'Wrong result.')

  call VUAssertEquals(7, results[1].lnum, 'Wrong line num.')
  call VUAssertEquals(3, results[1].col, 'Wrong col num.')
  call VUAssertEquals('discarding unexpected </div>', results[1].text, 'Wrong result.')
endfunction " }}}

" vim:ft=vim:fdm=marker
