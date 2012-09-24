" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for junit.vim
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

function! SetUp() " {{{
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
endfunction " }}}

function! TestJUnitImpl() " {{{
  edit! src/org/eclim/test/junit/SomeClassVUnitTest.java
  call vunit#PeekRedir()

  call cursor(3, 1)
  JUnitImpl
  call vunit#AssertTrue(bufname('%') =~ 'SomeClassVUnitTest.java_impl$',
    \ 'JUnit impl window not opened.')
  call vunit#AssertEquals('org.eclim.test.junit.SomeClassVUnitTest', getline(1),
    \ 'Wrong type in junit impl window.')

  call vunit#AssertTrue(search('^\s*public void aMethod()'),
    \ 'Super method aMethod() not found')
  call vunit#AssertTrue(search('^\s*public void aMethod(String)'),
    \ 'Super method aMethod(String) not found')
  exec "normal \<cr>"
  call vunit#AssertEquals(search('^\s*public void aMethod()'), 0,
    \ 'Super method aMethod() still present after add.')
  call vunit#AssertEquals(search('^\s*public void aMethod(String)'), 0,
    \ 'Super method aMethod(String) still resent after add.')

  call vunit#AssertTrue(search('^\s*public boolean equals(Object)'),
    \ 'Super method equals() not found')
  exec "normal \<cr>"
  call vunit#AssertEquals(search('^\s*public abstract boolean equals(Object)'), 0,
    \ 'Super method equals() still present after add.')
  bdelete

  call vunit#AssertTrue(search('@Test\_s\+public void aMethod()'),
    \ 'testAMethod() not added.')
  call vunit#AssertTrue(search('@Test\_s\+public void equals()'),
    \ 'testEquals() not added.')
endfunction " }}}

" vim:ft=vim:fdm=marker
