" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for search.vim
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

" SetUp() {{{
function! SetUp()
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_php'
endfunction " }}}

" TestSearchContext() {{{
function! TestSearchContext()
  edit! php/search/find.php
  call vunit#PeekRedir()

  " find class
  call cursor(4, 15)
  PhpSearchContext
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(name, 'php/models.php', 'Wrong file.')
  call vunit#AssertEquals(6, line('.'), 'Wrong line.')
  call vunit#AssertEquals(7, col('.'), 'Wrong col.')

  quit

  " find method
  call cursor(5, 9)
  PhpSearchContext
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(name, 'php/models.php', 'Wrong file.')
  call vunit#AssertEquals(13, line('.'), 'Wrong line.')
  call vunit#AssertEquals(19, col('.'), 'Wrong col.')

  quit

  " find variable
  call cursor(6, 17)
  PhpSearchContext
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(name, 'php/models.php', 'Wrong file.')
  call vunit#AssertEquals(8, line('.'), 'Wrong line.')
  call vunit#AssertEquals(7, col('.'), 'Wrong col.')

  quit

  " find function
  call cursor(7, 1)
  PhpSearchContext
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(name, 'php/models.php', 'Wrong file.')
  call vunit#AssertEquals(3, line('.'), 'Wrong line.')
  call vunit#AssertEquals(10, col('.'), 'Wrong col.')

  quit

  " find constant
  call cursor(8, 6)
  PhpSearchContext
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(name, 'php/models.php', 'Wrong file.')
  call vunit#AssertEquals(60, line('.'), 'Wrong line.')
  call vunit#AssertEquals(1, col('.'), 'Wrong col.')

  quit
endfunction " }}}

" TestSearchExact() {{{
function! TestSearchExact()
  edit! php/search/find.php
  call vunit#PeekRedir()

  " find class
  :PhpSearch -p TestA -t class
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(name, 'php/models.php', 'Wrong file.')
  call vunit#AssertEquals(6, line('.'), 'Wrong line.')
  call vunit#AssertEquals(7, col('.'), 'Wrong col.')

  quit

  " find method
  :PhpSearch -p methodA2 -t function
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(name, 'php/models.php', 'Wrong file.')
  call vunit#AssertEquals(13, line('.'), 'Wrong line.')
  call vunit#AssertEquals(19, col('.'), 'Wrong col.')

  quit

  " find function
  :PhpSearch -p functionA -t function
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(name, 'php/models.php', 'Wrong file.')
  call vunit#AssertEquals(3, line('.'), 'Wrong line.')
  call vunit#AssertEquals(10, col('.'), 'Wrong col.')

  quit

  " find constant
  :PhpSearch -p CONSTANT1 -t field
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(name, 'php/models.php', 'Wrong file.')
  call vunit#AssertEquals(60, line('.'), 'Wrong line.')
  call vunit#AssertEquals(1, col('.'), 'Wrong col.')

  quit
endfunction " }}}

" TestSearchPattern() {{{
function! TestSearchPattern()
  edit! php/search/find.php
  call vunit#PeekRedir()

  " find class
  :PhpSearch -p method* -t function
  lclose
  call vunit#PeekRedir()

  let results = getloclist(0)
  echo string(results)
  call vunit#AssertEquals(4, len(results), 'Wrong number of results.')
endfunction " }}}

" vim:ft=vim:fdm=marker
