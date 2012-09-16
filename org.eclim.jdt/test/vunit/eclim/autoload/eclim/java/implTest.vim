" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for impl.vim
"
" License:
"
" Copyright (C) 2005 - 2012  Eric Van Dewoestine
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

function! TestJavaConstructor() " {{{
  edit! src/org/eclim/test/impl/TestConstructorVUnit.java
  call vunit#PeekRedir()

  call vunit#AssertFalse(search('public TestConstructorVUnit()'),
    \ 'Empty constructor already exists.')
  JavaConstructor
  call vunit#AssertTrue(search('public TestConstructorVUnit()'),
    \ 'Empty constructor not added.')

  call vunit#AssertFalse(search('public TestConstructorVUnit(int id, String name)'),
    \ 'Two arg constructor already exists.')
  5,6JavaConstructor
  call vunit#AssertTrue(search('public TestConstructorVUnit(int id, String name)'),
    \ 'Two arg constructor not added.')
endfunction " }}}

function! TestJavaImpl() " {{{
  edit! src/org/eclim/test/impl/TestImplVUnit.java
  call vunit#PeekRedir()

  JavaImpl

  let name = substitute(bufname('%'), '\', '/', 'g')
  call vunit#AssertTrue(name =~ 'src/org/eclim/test/impl/TestImplVUnit\.java_impl$')

  call cursor(line('$'), 1)
  let line = search('public String put(Integer,String)', 'bc')
  call vunit#AssertTrue(line > 0, 'put method not found.')
  call vunit#AssertTrue(getline(line) =~ '  public String put(Integer,String)')

  silent! exec "normal \<cr>"

  call vunit#AssertEquals(search('public String put(Integer,String)', 'w'), 0,
    \ 'put still in results')
  quit
  call cursor(1, 1)
  call vunit#AssertTrue(search('public String put(Integer \w\+, String \w\+)', 'c'),
    \ 'Method not inserted.')
endfunction " }}}

function! TestJavaImplSub() " {{{
  edit! src/org/eclim/test/impl/TestSubImplVUnit.java
  call vunit#PeekRedir()

  JavaImpl

  let name = substitute(bufname('%'), '\', '/', 'g')
  call vunit#AssertTrue(name =~ 'src/org/eclim/test/impl/TestSubImplVUnit\.java_impl$')

  let compareLine = search('public abstract int compare(String,String)')

  call vunit#AssertTrue(compareLine > 0, 'compare method not found.')
  call vunit#AssertTrue(getline(compareLine) =~
    \ '  public abstract int compare(String,String)')

  silent! exec "normal \<cr>"

  call vunit#AssertEquals(search('public abstract int compare(String,String)', 'w'), 0,
    \ 'put still in results')

  let putLine = search('public String put(Integer,String)')

  call vunit#AssertTrue(putLine > 0, 'put method not found.')
  call vunit#AssertTrue(getline(putLine) =~ '  public String put(Integer,String)')

  silent! exec "normal \<cr>"

  call vunit#AssertEquals(search('public String put(Integer,String)', 'w'), 0,
    \ 'put still in results')

  bdelete

  call cursor(1, 1)
  call vunit#AssertTrue(search('public int compare(String \w\+, String \w\+)'),
    \ 'put method not inserted.')
  call vunit#AssertTrue(search('public String put(Integer \w\+, String \w\+)'),
    \ 'compare method not inserted.')
endfunction " }}}

function! TestJavaDelegate() " {{{
  edit! src/org/eclim/test/impl/TestDelegateVUnit.java
  call vunit#PeekRedir()

  call cursor(8, 3)
  JavaDelegate
  call vunit#AssertTrue(bufname('%') =~ 'TestDelegateVUnit.java_delegate$',
    \ 'Delegate window not opened.')
  call vunit#AssertEquals('org.eclim.test.impl.TestDelegateVUnit.list', getline(1),
    \ 'Wrong type in delegate window.')

  call vunit#AssertTrue(search('^\s*public abstract Iterator<Double> iterator()'),
    \ 'Super method iterator() not found')
  exec "normal \<cr>"
  call vunit#AssertTrue(search('^\s*public abstract boolean add(Double)'),
    \ 'Super method add() not found')
  exec "normal \<cr>"

  call vunit#AssertEquals(search('^\s*public abstract Iterator<Double> iterator()', 'w'),
    \ 0, 'Super method iterator() still in results')
  call vunit#AssertEquals(search('^\s*public abstract boolean add(Double)', 'w'),
    \ 0, 'Super method add() still in restuls')
  bdelete

  call vunit#AssertTrue(search('public Iterator<Double> iterator()'),
    \ 'iterator() not added.')
  call vunit#AssertTrue(search('return list\.iterator();$'), 'iterator() not delegating.')
  call vunit#AssertTrue(search('public boolean add(Double \w\+)'), 'add() not added.')
  call vunit#AssertTrue(search('return list\.add(\w\+);$'), 'add() not delegating.')
endfunction " }}}

" vim:ft=vim:fdm=marker
