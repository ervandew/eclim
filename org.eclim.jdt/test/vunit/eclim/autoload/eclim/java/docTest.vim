" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2005 - 2015  Eric Van Dewoestine
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

function! SetUp() " {{{
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
endfunction " }}}

function! TestComment() " {{{
  edit! src/org/eclim/test/doc/TestCommentVUnit.java
  set shiftwidth=2
  set tabstop=2
  set expandtab
  call vunit#PeekRedir()

  call cursor(11, 3)
  JavaDocComment

  call vunit#AssertEquals('  /**', getline(11), 'Wrong doc line 11.')
  call vunit#AssertEquals('   * {@inheritDoc}', getline(12), 'Wrong doc line 12.')
  call vunit#AssertEquals('   *', getline(13), 'Wrong doc line 13.')
  call vunit#AssertEquals('   * @see Object#equals(Object)', getline(14),
    \ 'Wrong doc line 14.')
  call vunit#AssertEquals('   */', getline(15), 'Wrong doc line 15.')

  call cursor(5, 3)
  JavaDocComment

  call vunit#AssertEquals('  /**', getline(5), 'Wrong doc line 5.')
  call vunit#AssertEquals('   *', getline(6), 'Wrong doc line 6.')
  call vunit#AssertEquals('   *', getline(7), 'Wrong doc line 7.')
  call vunit#AssertEquals('   * @param _id', getline(8), 'Wrong doc line 8.')
  call vunit#AssertEquals('   * @param _name', getline(9), 'Wrong doc line 9.')
  call vunit#AssertEquals('   * @return', getline(10), 'Wrong doc line 10.')
  call vunit#AssertEquals('   *', getline(11), 'Wrong doc line 11.')
  call vunit#AssertEquals('   * @throws IOException', getline(12), 'Wrong doc line 12.')
  call vunit#AssertEquals('   */', getline(13), 'Wrong doc line 13.')
endfunction " }}}

function! TestPreview() " {{{
  edit! src/org/eclim/test/doc/TestPreview.java
  call vunit#PeekRedir()
  set shiftwidth=2
  set tabstop=2
  set expandtab
  set splitbelow

  " class reference
  call cursor(17, 9)
  JavaDocPreview
  call vunit#AssertEquals(winnr('$'), 2, 'Class ref: Wrong win nums.')
  winc j
  call vunit#AssertEquals(&previewwindow, 1, 'Class ref: Not preview window.')
  call vunit#AssertEquals('|org[0]|.|eclim[1]|.|test[2]|.|doc[3]|.TestPreview', getline(1))
  call vunit#AssertEquals('', getline(2))
  call vunit#AssertEquals('A test class for javadoc previews.', getline(3))
  call vunit#AssertEquals('Author:', getline(4))
  call vunit#AssertEquals('   Eric Van Dewoestine', getline(5))
  winc k

  " constructor reference
  call cursor(17, 35)
  JavaDocPreview
  call vunit#AssertEquals(winnr('$'), 2, 'Constructor ref: Wrong win nums.')
  winc j
  call vunit#AssertEquals(&previewwindow, 1, 'Constructor ref: Not preview window.')
  call vunit#AssertEquals(
    \ '|org[0]|.|eclim[1]|.|test[2]|.|doc[3]|.|TestPreview[4]|.TestPreview(|String[5]|[] args)',
    \ getline(1))
  call vunit#AssertEquals('', getline(2))
  call vunit#AssertEquals('Constructs a new instance from the supplied arguments.', getline(3))
  call vunit#AssertEquals('Parameters:', getline(4))
  call vunit#AssertEquals('  args The arguments.', getline(5))
  winc k

  " method reference
  call cursor(18, 7)
  JavaDocPreview
  call vunit#AssertEquals(winnr('$'), 2, 'Method ref: Wrong win nums.')
  winc j
  call vunit#AssertEquals(&previewwindow, 1, 'Method ref: Not preview window.')
  call vunit#AssertEquals(
    \ '|String[0]| |org[1]|.|eclim[2]|.|test[3]|.|doc[4]|.|TestPreview[5]|.test()',
    \ getline(1))
  call vunit#AssertEquals('', getline(2))
  call vunit#AssertEquals('A test method.', getline(3))
  call vunit#AssertEquals('Returns:', getline(4))
  call vunit#AssertEquals('   a test |String[6]|', getline(5))

  " follow link
  call cursor(5, 13)
  exec "normal \<cr>"
  call vunit#AssertEquals(winnr('$'), 2, 'Link: Wrong win nums.')
  call vunit#AssertEquals(&previewwindow, 1, 'Link: Not preview window.')
  call vunit#AssertEquals('|java[0]|.|lang[1]|.String', getline(1))
  call vunit#AssertTrue(getline(3) =~ '^The String class')

  " back
  exec "normal \<c-o>"
  call vunit#AssertEquals(
    \ '|String[0]| |org[1]|.|eclim[2]|.|test[3]|.|doc[4]|.|TestPreview[5]|.test()',
    \ getline(1), 'back to method')

  " forward
  " not working from vunit for some reason
  "exec "normal \<c-i>"
  "call vunit#AssertEquals('java.lang.String', getline(1), 'forward to String')
endfunction " }}}

function! TestSearch() " {{{
  edit! src/org/eclim/test/doc/TestDocSearchVUnit.java
  call vunit#PeekRedir()

  JavaDocSearch -x declarations -p List
  call vunit#PeekRedir()

  call vunit#AssertEquals('javadoc_search_results', bufname('%'),
    \ 'Search results window not opened.')
  call vunit#AssertEquals(2, line('$'), 'Wrong number of results.')
  call vunit#AssertEquals('http://docs.oracle.com/javase/7/docs/api/java/awt/List.html',
    \ line('1'), 'Wrong result 1.')
  call vunit#AssertEquals('http://docs.oracle.com/javase/7/docs/api/java/util/List.html',
    \ line('2'), 'Wrong result 2.')
endfunction " }}}

function! TestJavadoc() " {{{
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
