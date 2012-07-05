" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for doc.vim
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
endfunction " }}}

" TestComment() {{{
function! TestComment()
  edit! src/org/eclim/test/doc/TestCommentVUnit.java
  call vunit#PeekRedir()

  call cursor(11, 3)
  JavaDocComment

  call vunit#AssertEquals('  /**', getline(11), 'Wrong doc line 11.')
  call vunit#AssertEquals('   * {@inheritDoc}', getline(12), 'Wrong doc line 12.')
  call vunit#AssertEquals('   * @see Object#equals(Object)', getline(13),
    \ 'Wrong doc line 13.')
  call vunit#AssertEquals('   */', getline(14), 'Wrong doc line 14.')

  call cursor(5, 3)
  JavaDocComment

  call vunit#AssertEquals('  /**', getline(5), 'Wrong doc line 5.')
  call vunit#AssertEquals('   * ', getline(6), 'Wrong doc line 6.')
  call vunit#AssertEquals('   * ', getline(7), 'Wrong doc line 7.')
  call vunit#AssertEquals('   * @param _id', getline(8), 'Wrong doc line 8.')
  call vunit#AssertEquals('   * @param _name', getline(9), 'Wrong doc line 9.')
  call vunit#AssertEquals('   * @return', getline(10), 'Wrong doc line 10.')
  call vunit#AssertEquals('   * ', getline(11), 'Wrong doc line 11.')
  call vunit#AssertEquals('   * @throws IOException', getline(12), 'Wrong doc line 12.')
  call vunit#AssertEquals('   */', getline(13), 'Wrong doc line 13.')
endfunction " }}}

" TestSearch() {{{
function! TestSearch()
  edit! src/org/eclim/test/doc/TestDocSearchVUnit.java
  call vunit#PeekRedir()

  call cursor(5, 11)
  let g:EclimJavaDocSearchSingleResult = 'lopen'
  JavaDocSearch -x declarations
  call vunit#PeekRedir()

  call vunit#AssertEquals('javadoc_search_results', bufname('%'),
    \ 'Search results window not opened.')
  call vunit#AssertEquals(1, line('$'), 'Wrong number of results.')
  call vunit#AssertEquals('http://docs.oracle.com/javase/1.5.0/docs/api/java/awt/List.html',
    \ line('1'), 'Wrong result.')
endfunction " }}}

" TestJavadoc() {{{
function! TestJavadoc()
  edit! src/org/eclim/test/doc/javadoc/TestJavadocVUnit.java
  call vunit#PeekRedir()

  call vunit#AssertFalse(filereadable(
    \ g:TestEclimWorkspace .
    \ 'eclim_unit_test_java/doc/org/eclim/test/doc/javadoc/TestJavadocVUnit.html'))

  Javadoc src/org/eclim/test/doc/javadoc/TestJavadocVUnit.java

  call vunit#AssertTrue(filereadable(
    \ g:TestEclimWorkspace .
    \ 'eclim_unit_test_java/doc/org/eclim/test/doc/javadoc/TestJavadocVUnit.html'))
endfunction " }}}

" vim:ft=vim:fdm=marker
