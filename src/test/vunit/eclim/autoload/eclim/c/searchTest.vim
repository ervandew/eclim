" Author:  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_c'
endfunction " }}}

" TestFindInclude() {{{
function! TestFindInclude()
  edit! src/test_search_vunit.c
  call PeekRedir()

  call cursor(1, 14)
  :CSearchContext
  call PeekRedir()

  let results = getloclist(0)
  echo string(results)
  call VUAssertEquals(1, len(results), 'Wrong number of results.')
  call VUAssertEquals(
    \ bufname(results[0].bufnr), '/usr/include/stdio.h', 'Wrong result file.')
endfunction " }}}

" TestSearchElement() {{{
function! TestSearchElement()
  edit! src/test_search_vunit.c
  call PeekRedir()

  call cursor(11, 13)
  :CSearchContext
  call PeekRedir()

  let results = getloclist(0)
  echo string(results)
  call VUAssertEquals(1, len(results), 'Wrong number of results.')
  call VUAssertEquals(
    \ bufname(results[0].bufnr), '/usr/include/stdlib.h', 'Wrong result file.')
  call VUAssertEquals(results[0].lnum, 135, 'Wrong line number.')
  call VUAssertEquals(results[0].col, 9, 'Wrong line number.')
endfunction " }}}

" TestSearchFunction() {{{
function! TestSearchFunction()
  edit! src/test_search_vunit.c
  call PeekRedir()

  :CSearch -p test_search_vunit_function -t function
  call PeekRedir()

  let results = getloclist(0)
  echo string(results)
  call VUAssertEquals(1, len(results), 'Wrong number of results.')
  call VUAssertEquals(
    \ bufname(results[0].bufnr), 'src/test_search_vunit.c', 'Wrong result file.')
  call VUAssertEquals(results[0].lnum, 14, 'Wrong line number.')
  call VUAssertEquals(results[0].col, 5, 'Wrong line number.')
endfunction " }}}

" TestSearchStruct() {{{
function! TestSearchStruct()
  edit! src/test_search_vunit.c
  call PeekRedir()

  :CSearch -p test_search_vunit_struct -t class_struct
  call PeekRedir()

  let results = getloclist(0)
  echo string(results)
  call VUAssertEquals(1, len(results), 'Wrong number of results.')
  call VUAssertEquals(
    \ bufname(results[0].bufnr), 'src/test_search_vunit.c', 'Wrong result file.')
  call VUAssertEquals(results[0].lnum, 4, 'Wrong line number.')
  call VUAssertEquals(results[0].col, 8, 'Wrong line number.')
endfunction " }}}

" vim:ft=vim:fdm=marker
