" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for search.vim
"
" License:
"
" Copyright (C) 2005 - 2014  Eric Van Dewoestine
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

function! TearDown() " {{{
  silent! unlet g:EclimTestPromptQueue
endfunction " }}}

function! TestSearch() " {{{
  edit! src/org/eclim/test/search/TestSearchVUnit.java

  call cursor(8, 11)
  let g:EclimTestPromptQueue = [1]
  JavaSearch
  call vunit#PeekRedir()
  let name = fnamemodify(bufname('%'), ':t')
  echom 'line: ' . line('.') . ' ' . getline('.')
  call vunit#AssertEquals(name, 'List.java', 'Wrong or no file found for List.')
  call vunit#AssertTrue(getline('.') =~ 'public interface List',
    \ 'Not on List class declaration.')

  call vunit#AssertTrue(search('extends Collection'), 'Could not find "extends Collection"')
  normal w
  let g:EclimTestPromptQueue = [1]
  JavaSearch
  call vunit#PeekRedir()
  let name = fnamemodify(bufname('%'), ':t')
  echom 'line: ' . line('.') . ' ' . getline('.')
  call vunit#AssertEquals(name, 'Collection.java', 'Wrong or no file found for Collection.')
  call vunit#AssertTrue(getline('.') =~ 'public interface Collection',
    \ 'Not on Collection class declaration.')

  bdelete!
  bdelete!

  call cursor(12, 5)
  let g:EclimTestPromptQueue = [1]
  JavaSearch
  call vunit#PeekRedir()
  echom 'line: ' . line('.') . ' ' . getline('.')
  call vunit#AssertTrue(getline('.') =~ 'private List list',
    \ 'Not on variable declaration.')
endfunction " }}}

" vim:ft=vim:fdm=marker
