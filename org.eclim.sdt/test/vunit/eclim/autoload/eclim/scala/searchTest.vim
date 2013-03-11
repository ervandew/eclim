" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for search.vim
"
" License:
"
" Copyright (C) 2012  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_scala'
endfunction " }}}

function! TestSearchScala() " {{{
  edit! src/eclim/test/search/TestSearch.scala
  call vunit#PeekRedir()

  call cursor(8, 23)
  ScalaSearch
  call vunit#PeekRedir()
  call vunit#AssertEquals(
    \ bufname('%'), 'src/eclim/test/TestScala.scala', 'Wrong class file name')
  call vunit#AssertEquals(line('.'), 5, 'Wrong class line')
  call vunit#AssertEquals(col('.'), 7, 'Wrong class col')
  bdelete!

  call cursor(9, 21)
  ScalaSearch
  call vunit#PeekRedir()
  call vunit#AssertEquals(
    \ bufname('%'), 'src/eclim/test/TestScala.scala', 'Wrong method file name')
  call vunit#AssertEquals(line('.'), 7, 'Wrong method line')
  call vunit#AssertEquals(col('.'), 7, 'Wrong method col')
  bdelete!
endfunction " }}}

function! TestSearchJava() " {{{
  edit! src/eclim/test/search/TestSearch.scala
  call vunit#PeekRedir()

  call cursor(13, 20)
  ScalaSearch
  call vunit#PeekRedir()
  call vunit#AssertEquals(
    \ bufname('%'), 'src/eclim/test/TestJava.java', 'Wrong class file name')
  call vunit#AssertEquals(line('.'), 5, 'Wrong class line')
  call vunit#AssertEquals(col('.'), 1, 'Wrong class col')
  bdelete!

  call cursor(14, 12)
  ScalaSearch
  call vunit#PeekRedir()
  call vunit#AssertEquals(
    \ bufname('%'), 'src/eclim/test/TestJava.java', 'Wrong method file name')
  call vunit#AssertEquals(line('.'), 7, 'Wrong method line')
  call vunit#AssertEquals(col('.'), 3, 'Wrong method col')
  bdelete!
endfunction " }}}

" vim:ft=vim:fdm=marker
