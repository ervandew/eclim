" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for search.vim
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

" TestFindDefinition() {{{
function! TestFindDefinition()
  edit! php/search/find.php
  call PeekRedir()

  " find class
  call cursor(4, 15)
  call eclim#php#search#SearchContext()
  call VUAssertEquals(
    \ g:TestEclimWorkspace . 'eclim_unit_test_php/php/models.php', expand('%:p'), 'Wrong file.')
  call VUAssertEquals(6, line('.'), 'Wrong line.')
  call VUAssertEquals(1, col('.'), 'Wrong col.')

  quit

  " find method
  call cursor(5, 9)
  call eclim#php#search#SearchContext()
  call VUAssertEquals(
    \ g:TestEclimWorkspace . 'eclim_unit_test_php/php/models.php', expand('%:p'), 'Wrong file.')
  call VUAssertEquals(13, line('.'), 'Wrong line.')
  call VUAssertEquals(3, col('.'), 'Wrong col.')

  quit

  " find variable
  call cursor(6, 17)
  call eclim#php#search#SearchContext()
  call VUAssertEquals(
    \ g:TestEclimWorkspace . 'eclim_unit_test_php/php/models.php', expand('%:p'), 'Wrong file.')
  call VUAssertEquals(8, line('.'), 'Wrong line.')
  call VUAssertEquals(3, col('.'), 'Wrong col.')

  quit

  " find function
  call cursor(7, 1)
  call eclim#php#search#SearchContext()
  call VUAssertEquals(
    \ g:TestEclimWorkspace . 'eclim_unit_test_php/php/models.php', expand('%:p'), 'Wrong file.')
  call VUAssertEquals(3, line('.'), 'Wrong line.')
  call VUAssertEquals(1, col('.'), 'Wrong col.')

  quit

  " find constant
  call cursor(8, 6)
  call eclim#php#search#SearchContext()
  call VUAssertEquals(
    \ g:TestEclimWorkspace . 'eclim_unit_test_php/php/models.php', expand('%:p'), 'Wrong file.')
  call VUAssertEquals(29, line('.'), 'Wrong line.')
  call VUAssertEquals(1, col('.'), 'Wrong col.')

  quit
endfunction " }}}

" TestSearchExact() {{{
function! TestSearchExact()
  edit! php/search/find.php
  call PeekRedir()

  " find class
  :PhpSearch -p TestA -t class
  call VUAssertEquals(
    \ g:TestEclimWorkspace . 'eclim_unit_test_php/php/models.php', expand('%:p'), 'Wrong file.')
  call VUAssertEquals(6, line('.'), 'Wrong line.')
  call VUAssertEquals(7, col('.'), 'Wrong col.')

  quit

  " find method
  :PhpSearch -p methodA2 -t function
  call VUAssertEquals(
    \ g:TestEclimWorkspace . 'eclim_unit_test_php/php/models.php', expand('%:p'), 'Wrong file.')
  call VUAssertEquals(13, line('.'), 'Wrong line.')
  call VUAssertEquals(19, col('.'), 'Wrong col.')

  quit

  " find function
  :PhpSearch -p functionA -t function
  call VUAssertEquals(
    \ g:TestEclimWorkspace . 'eclim_unit_test_php/php/models.php', expand('%:p'), 'Wrong file.')
  call VUAssertEquals(3, line('.'), 'Wrong line.')
  call VUAssertEquals(10, col('.'), 'Wrong col.')

  quit

  " find constant
  :PhpSearch -p CONSTANT1 -t constant
  call VUAssertEquals(
    \ g:TestEclimWorkspace . 'eclim_unit_test_php/php/models.php', expand('%:p'), 'Wrong file.')
  call VUAssertEquals(29, line('.'), 'Wrong line.')
  call VUAssertEquals(10, col('.'), 'Wrong col.')

  quit
endfunction " }}}

" TestSearchPattern() {{{
function! TestSearchPattern()
  edit! php/search/find.php
  call PeekRedir()

  " find class
  :PhpSearch -p method* -t function
  lclose
  call PeekRedir()

  let results = getloclist(0)
  echo string(results)
  call VUAssertEquals(4, len(results), 'Wrong number of results.')
endfunction " }}}

" vim:ft=vim:fdm=marker
