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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
endfunction " }}}

" TestSearch() {{{
function! TestSearch ()
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

  bdelete!
endfunction " }}}

" vim:ft=vim:fdm=marker
