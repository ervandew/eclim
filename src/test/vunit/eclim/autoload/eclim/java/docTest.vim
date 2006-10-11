" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for doc.vim
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
endfunction " }}}

" TestComment() {{{
function! TestComment ()
  edit! src/org/eclim/test/doc/TestCommentVUnit.java
  call PeekRedir()

  call cursor(11, 3)
  JavaDocComment

  call VUAssertEquals('  /**', getline(11), 'Wrong doc line 11.')
  call VUAssertEquals('   * {@inheritDoc}', getline(12), 'Wrong doc line 12.')
  call VUAssertEquals('   * @see Object#equals(Object)', getline(13),
    \ 'Wrong doc line 13.')
  call VUAssertEquals('   */', getline(14), 'Wrong doc line 14.')

  call cursor(5, 3)
  JavaDocComment

  call VUAssertEquals('  /**', getline(5), 'Wrong doc line 5.')
  call VUAssertEquals('   *', getline(6), 'Wrong doc line 6.')
  call VUAssertEquals('   *', getline(7), 'Wrong doc line 7.')
  call VUAssertEquals('   * @param _id', getline(8), 'Wrong doc line 8.')
  call VUAssertEquals('   * @param _name', getline(9), 'Wrong doc line 9.')
  call VUAssertEquals('   * @return', getline(10), 'Wrong doc line 10.')
  call VUAssertEquals('   *', getline(11), 'Wrong doc line 11.')
  call VUAssertEquals('   * @throws IOException', getline(12), 'Wrong doc line 12.')
  call VUAssertEquals('   */', getline(13), 'Wrong doc line 13.')

  bdelete!
endfunction " }}}

" TestSearch() {{{
function! TestSearch ()
  edit! src/org/eclim/test/doc/TestDocSearchVUnit.java
  call PeekRedir()

  call cursor(5, 11)
  let g:EclimJavaDocSearchSingleResult = 'lopen'
  JavaDocSearch -x declarations
  call PeekRedir()

  call VUAssertEquals('javadoc_search_results', bufname('%'),
    \ 'Search results window not opened.')
  call VUAssertEquals(1, line('$'), 'Wrong number of results.')
  call VUAssertEquals('http://java.sun.com/j2se/1.5.0/docs/api/java/awt/List.html',
    \ line('1'), 'Wrong result.')

  bdelete!
endfunction " }}}

" vim:ft=vim:fdm=marker
