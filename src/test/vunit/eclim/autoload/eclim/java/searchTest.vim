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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
endfunction " }}}

" TestSearch() {{{
function! TestSearch()
  edit! src/org/eclim/test/search/TestSearchVUnit.java
  call PeekRedir()

  call cursor(8, 11)
  JavaSearch

  call VUAssertEquals('/tmp/java/util/List.java', bufname('%'),
    \ 'Wrong or no file found for List.')
  call VUAssertTrue(getline('.') =~ 'public interface List',
    \ 'Not on class declaration.')
  bdelete!

  call cursor(12, 5)
  JavaSearch
  call VUAssertTrue(getline('.') =~ 'private List list',
    \ 'Not on variable declaration.')
endfunction " }}}

" vim:ft=vim:fdm=marker
