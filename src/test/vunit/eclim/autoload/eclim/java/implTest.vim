" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for impl.vim
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

" TestJavaImpl() {{{
function! TestJavaImpl()
  edit! src/org/eclim/test/impl/TestImplVUnit.java
  call PeekRedir()

  JavaImpl

  call VUAssertTrue(bufname('%') =~ 'src/org/eclim/test/impl/TestImplVUnit\.java_impl$')

  call cursor(line('$'), 1)
  let line = search('public abstract String put(Integer key, String value)', 'bc')

  call VUAssertTrue(line > 0, 'put method not found.')
  call VUAssertEquals(getline(line),
    \ '  public abstract String put(Integer key, String value)')

  silent! exec "normal \<cr>"

  call VUAssertEquals(getline(line),
    \ '  //public abstract String put(Integer key, String value)')
  quit
  call cursor(1, 1)
  call VUAssertTrue(search('public String put(Integer key, String value)', 'c'),
    \ 'Method not inserted.')
endfunction " }}}

" TestJavaImplSub() {{{
function! TestJavaImplSub()
  edit! src/org/eclim/test/impl/TestSubImplVUnit.java
  call PeekRedir()

  JavaImpl

  call VUAssertTrue(bufname('%') =~
    \ 'src/org/eclim/test/impl/TestSubImplVUnit\.java_impl$')

  let compareLine = search('public abstract int compare(String o1, String o2)')

  call VUAssertTrue(compareLine > 0, 'compare method not found.')
  call VUAssertEquals(getline(compareLine),
    \ '  public abstract int compare(String o1, String o2)')

  silent! exec "normal \<cr>"

  call VUAssertEquals(getline(compareLine),
    \ '  //public abstract int compare(String o1, String o2)')

  let putLine = search('public abstract String put(Integer key, String value)')

  call VUAssertTrue(putLine > 0, 'put method not found.')
  call VUAssertEquals(getline(putLine),
    \ '  public abstract String put(Integer key, String value)')

  silent! exec "normal \<cr>"

  call VUAssertEquals(getline(putLine),
    \ '  //public abstract String put(Integer key, String value)')

  bdelete

  call cursor(1, 1)
  call VUAssertTrue(search('public int compare(String o1, String o2)'),
    \ 'put method not inserted.')
  call VUAssertTrue(search('public String put(Integer key, String value)'),
    \ 'compare method not inserted.')
endfunction " }}}

" vim:ft=vim:fdm=marker
