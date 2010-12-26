" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for search.vim
"
" License:
"
" Copyright (C) 2005 - 2010  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_c'
endfunction " }}}

" TestFindInclude() {{{
function! TestFindInclude()
  edit! src/test_search_vunit.c
  call vunit#PeekRedir()

  call cursor(1, 14)
  :CSearchContext
  call vunit#PeekRedir()

  let results = getloclist(0)
  echo string(results)
  call vunit#AssertEquals(1, len(results), 'Wrong number of results.')
  let name = bufname(results[0].bufnr)
  if has('win32') || has('win64')
    call vunit#AssertTrue(name =~ '\\include\\stdio\.h', 'Wrong result file: ' . name)
  else
    call vunit#AssertEquals(name, '/usr/include/stdio.h', 'Wrong result file.')
  endif
  bdelete

  call cursor(3, 14)
  :CSearchContext
  call vunit#PeekRedir()

  let results = getloclist(0)
  echo string(results)
  call vunit#AssertEquals(1, len(results), 'Wrong number of results.')
  let name = substitute(bufname(results[0].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test.h', 'Wrong result file.')
endfunction " }}}

" TestSearchElement() {{{
function! TestSearchElement()
  edit! src/test_search_vunit.c
  call vunit#PeekRedir()

  " EXIT_SUCCESS
  call cursor(13, 13)
  :CSearchContext
  call vunit#PeekRedir()

  let results = getloclist(0)
  echo string(results)
  call vunit#AssertEquals(1, len(results), 'Wrong number of results.')
  let name = bufname(results[0].bufnr)
  if has('win32') || has('win64')
    call vunit#AssertTrue(name =~ '\\include\\stdlib\.h', 'Wrong result file: ' . name)
  else
    call vunit#AssertEquals(name, '/usr/include/stdlib.h', 'Wrong result file.')
  endif
  call vunit#AssertTrue(getline('.') =~ '#define\s\+EXIT_SUCCESS', 'Wrong line: ' . getline('.'))
  bdelete

  " testFunction (definition)
  call cursor(11, 7)
  :CSearchContext
  call vunit#PeekRedir()

  let results = getloclist(0)
  echo string(results)
  call vunit#AssertEquals(1, len(results), 'Wrong number of results.')
  let name = substitute(bufname(results[0].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test.c', 'Wrong result file.')
  call vunit#AssertEquals(results[0].lnum, 1, 'Wrong line number.')
  call vunit#AssertEquals(results[0].col, 6, 'Wrong line number.')
  bdelete

  " testFunction (declaration)
  call cursor(11, 7)
  :CSearch -x declarations
  call vunit#PeekRedir()

  let results = getloclist(0)
  echo string(results)
  call vunit#AssertEquals(1, len(results), 'Wrong number of results.')
  let name = substitute(bufname(results[0].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test.h', 'Wrong result file.')
  call vunit#AssertEquals(results[0].lnum, 4, 'Wrong line number.')
  call vunit#AssertEquals(results[0].col, 6, 'Wrong line number.')
  bdelete

  " testFunction (references)
  call cursor(11, 7)
  :CSearch -x references
  call vunit#PeekRedir()

  let results = getloclist(0)
  echo string(results)
  call vunit#AssertEquals(2, len(results), 'Wrong number of results.')
  let name = substitute(bufname(results[0].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test_search_vunit.c', 'Wrong result file.')
  call vunit#AssertEquals(results[0].lnum, 11, 'Wrong line number.')
  call vunit#AssertEquals(results[0].col, 3, 'Wrong line number.')

  let name = substitute(bufname(results[1].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test_search.c', 'Wrong result file.')
  call vunit#AssertEquals(results[1].lnum, 11, 'Wrong line number.')
  call vunit#AssertEquals(results[1].col, 3, 'Wrong line number.')
  bdelete

  " testFunction (all)
  call cursor(11, 7)
  :CSearch -x all
  call vunit#PeekRedir()

  let results = getloclist(0)
  echo string(results)
  call vunit#AssertEquals(4, len(results), 'Wrong number of results.')
  let name = substitute(bufname(results[0].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test.h', 'Wrong result file.')
  call vunit#AssertEquals(results[0].lnum, 4, 'Wrong line number.')
  call vunit#AssertEquals(results[0].col, 6, 'Wrong line number.')

  let name = substitute(bufname(results[1].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test.c', 'Wrong result file.')
  call vunit#AssertEquals(results[1].lnum, 1, 'Wrong line number.')
  call vunit#AssertEquals(results[1].col, 6, 'Wrong line number.')

  let name = substitute(bufname(results[2].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test_search_vunit.c', 'Wrong result file.')
  call vunit#AssertEquals(results[2].lnum, 11, 'Wrong line number.')
  call vunit#AssertEquals(results[2].col, 3, 'Wrong line number.')

  let name = substitute(bufname(results[3].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test_search.c', 'Wrong result file.')
  call vunit#AssertEquals(results[3].lnum, 11, 'Wrong line number.')
  call vunit#AssertEquals(results[3].col, 3, 'Wrong line number.')
  bdelete
endfunction " }}}

" TestSearchFunction() {{{
function! TestSearchFunction()
  edit! src/test_search_vunit.c
  call vunit#PeekRedir()

  :CSearch -p test_search_vunit_function -t function
  call vunit#PeekRedir()

  let results = getloclist(0)
  echo string(results)
  call vunit#AssertEquals(1, len(results), 'Wrong number of results.')
  let name = substitute(bufname(results[0].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test_search_vunit.c', 'Wrong result file.')
  call vunit#AssertEquals(results[0].lnum, 16, 'Wrong line number.')
  call vunit#AssertEquals(results[0].col, 5, 'Wrong line number.')
endfunction " }}}

" TestSearchStruct() {{{
function! TestSearchStruct()
  edit! src/test_search_vunit.c
  call vunit#PeekRedir()

  :CSearch -p test_search_vunit_struct -t class_struct
  call vunit#PeekRedir()

  let results = getloclist(0)
  echo string(results)
  call vunit#AssertEquals(1, len(results), 'Wrong number of results.')
  let name = substitute(bufname(results[0].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test_search_vunit.c', 'Wrong result file.')
  call vunit#AssertEquals(results[0].lnum, 5, 'Wrong line number.')
  call vunit#AssertEquals(results[0].col, 8, 'Wrong line number.')
endfunction " }}}

" vim:ft=vim:fdm=marker
