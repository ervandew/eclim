" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for search.vim
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

" SetUp() {{{
function! SetUp()
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
  call VUAssertEquals(7, col('.'), 'Wrong col.')

  quit

  " find method
  call cursor(5, 9)
  call eclim#php#search#SearchContext()
  call VUAssertEquals(
    \ g:TestEclimWorkspace . 'eclim_unit_test_php/php/models.php', expand('%:p'), 'Wrong file.')
  call VUAssertEquals(13, line('.'), 'Wrong line.')
  call VUAssertEquals(19, col('.'), 'Wrong col.')

  quit

  " find variable
  call cursor(6, 17)
  call eclim#php#search#SearchContext()
  call VUAssertEquals(
    \ g:TestEclimWorkspace . 'eclim_unit_test_php/php/models.php', expand('%:p'), 'Wrong file.')
  call VUAssertEquals(8, line('.'), 'Wrong line.')
  call VUAssertEquals(7, col('.'), 'Wrong col.')

  quit

  " find function
  call cursor(7, 1)
  call eclim#php#search#SearchContext()
  call VUAssertEquals(
    \ g:TestEclimWorkspace . 'eclim_unit_test_php/php/models.php', expand('%:p'), 'Wrong file.')
  call VUAssertEquals(3, line('.'), 'Wrong line.')
  call VUAssertEquals(10, col('.'), 'Wrong col.')

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
  :PhpSearch -p CONSTANT1 -t field
  call VUAssertEquals(
    \ g:TestEclimWorkspace . 'eclim_unit_test_php/php/models.php', expand('%:p'), 'Wrong file.')
  call VUAssertEquals(29, line('.'), 'Wrong line.')
  call VUAssertEquals(1, col('.'), 'Wrong col.')

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
