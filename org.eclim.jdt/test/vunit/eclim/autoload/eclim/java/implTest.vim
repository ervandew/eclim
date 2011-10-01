" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for impl.vim
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
  set expandtab
  set shiftwidth=2 tabstop=2
endfunction " }}}

" TestJavaImpl() {{{
function! TestJavaImpl()
  edit! src/org/eclim/test/impl/TestImplVUnit.java
  call vunit#PeekRedir()

  JavaImpl

  let name = substitute(bufname('%'), '\', '/', 'g')
  call vunit#AssertTrue(name =~ 'src/org/eclim/test/impl/TestImplVUnit\.java_impl$')

  call cursor(line('$'), 1)
  let line = search('public abstract String put(Integer \w\+, String \w\+)', 'bc')
  call vunit#AssertTrue(line > 0, 'put method not found.')
  call vunit#AssertTrue(getline(line) =~ 
    \ '  public abstract String put(Integer \w\+, String \w\+)')

  silent! exec "normal \<cr>"

  call vunit#AssertTrue(getline(line) =~
    \ '  //public abstract String put(Integer \w\+, String \w\+)')
  quit
  call cursor(1, 1)
  call vunit#AssertTrue(search('public String put(Integer \w\+, String \w\+)', 'c'),
    \ 'Method not inserted.')
endfunction " }}}

" TestJavaImplSub() {{{
function! TestJavaImplSub()
  edit! src/org/eclim/test/impl/TestSubImplVUnit.java
  call vunit#PeekRedir()

  JavaImpl

  let name = substitute(bufname('%'), '\', '/', 'g')
  call vunit#AssertTrue(name =~ 'src/org/eclim/test/impl/TestSubImplVUnit\.java_impl$')

  let compareLine = search('public abstract int compare(String \w\+, String \w\+)')

  call vunit#AssertTrue(compareLine > 0, 'compare method not found.')
  call vunit#AssertTrue(getline(compareLine) =~
    \ '  public abstract int compare(String \w\+, String \w\+)')

  silent! exec "normal \<cr>"

  call vunit#AssertTrue(getline(compareLine) =~
    \ '  //public abstract int compare(String \w\+, String \w\+)')

  let putLine = search('public abstract String put(Integer \w\+, String \w\+)')

  call vunit#AssertTrue(putLine > 0, 'put method not found.')
  call vunit#AssertTrue(getline(putLine) =~
    \ '  public abstract String put(Integer \w\+, String \w\+)')

  silent! exec "normal \<cr>"

  call vunit#AssertTrue(getline(putLine) =~
    \ '  //public abstract String put(Integer \w\+, String \w\+)')

  bdelete

  call cursor(1, 1)
  call vunit#AssertTrue(search('public int compare(String \w\+, String \w\+)'),
    \ 'put method not inserted.')
  call vunit#AssertTrue(search('public String put(Integer \w\+, String \w\+)'),
    \ 'compare method not inserted.')
endfunction " }}}

" vim:ft=vim:fdm=marker
