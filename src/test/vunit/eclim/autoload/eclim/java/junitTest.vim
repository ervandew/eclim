" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for junit.vim
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

" TestJUnitImpl4() {{{
function! TestJUnitImpl4()
  call s:InitFile('4')

  call cursor(3, 1)
  JUnitImpl
  call vunit#AssertTrue(bufname('%') =~ 'SomeClassTestVUnit.java_impl$',
    \ 'JUnit impl window not opened.')
  call vunit#AssertEquals('org.eclim.test.junit.SomeClassTestVUnit', getline(1),
    \ 'Wrong type in junit imple window.')

  call vunit#AssertTrue(search('^\s*public void aMethod()'),
    \ 'Super method aMethod() not found')
  call vunit#AssertTrue(search('^\s*public void aMethod(String name)'),
    \ 'Super method aMethod(String) not found')
  call vunit#AssertTrue(search('^\s*public abstract int compare(String \w\+, String \w\+)'),
    \ 'Super method compare() not found')

  call cursor(6, 1)
  exec "normal \<cr>"

  call cursor(12, 1)
  exec "normal Vj\<cr>"

  call vunit#AssertTrue(search('^\s*//public void aMethod()'),
    \ 'Super method aMethod() not commented out after add.')
  call vunit#AssertTrue(search('^\s*//public void aMethod(String name)'),
    \ 'Super method aMethod(String) not commented out after add.')
  call vunit#AssertTrue(search('^\s*//public abstract int compare(String \w\+, String \w\+)'),
    \ 'Super method compare() not commented out after add.')
  call vunit#AssertTrue(search('^\s*//public abstract boolean equals(Object \w\+)'),
    \ 'Super method equals() not commented out after add.')
  bdelete

  call vunit#AssertTrue(search('* @see org.eclim.test.junit.SomeClassVUnit#aMethod()$'),
    \ 'see testAMethod() not added.')
  call vunit#AssertTrue(search('* @see org.eclim.test.junit.SomeClassVUnit#aMethod(String)$'),
    \ 'see testAMethod(String) not added.')
  call vunit#AssertTrue(search('@Test\_s\+public void aMethod()$'),
    \ 'testAMethod() not added.')
  call vunit#AssertTrue(search('* @see java.util.Comparator#compare(String,String)$'),
    \ 'see compare() not added.')
  call vunit#AssertTrue(search('@Test\_s\+public void compare()$'),
    \ 'testCompare() not added.')
  call vunit#AssertTrue(search('@Test\_s\+public void equals()$'),
    \ 'testEquals() not added.')
endfunction " }}}

" TestJUnitImpl3() {{{
function! TestJUnitImpl3()
  call s:InitFile('3')

  call cursor(3, 1)
  JUnitImpl
  call vunit#AssertTrue(bufname('%') =~ 'SomeClassTestVUnit.java_impl$',
    \ 'JUnit impl window not opened.')
  call vunit#AssertEquals('org.eclim.test.junit.SomeClassTestVUnit', getline(1),
    \ 'Wrong type in junit imple window.')

  call vunit#AssertTrue(search('^\s*public void aMethod()'),
    \ 'Super method aMethod() not found')
  call vunit#AssertTrue(search('^\s*public void aMethod(String name)'),
    \ 'Super method aMethod(String) not found')
  call vunit#AssertTrue(search('^\s*public abstract int compare(String \w\+, String \w\+)'),
    \ 'Super method compare() not found')

  call cursor(6, 1)
  exec "normal \<cr>"

  call cursor(12, 1)
  exec "normal Vj\<cr>"

  call vunit#AssertTrue(search('^\s*//public void aMethod()'),
    \ 'Super method aMethod() not commented out after add.')
  call vunit#AssertTrue(search('^\s*//public void aMethod(String name)'),
    \ 'Super method aMethod(String) not commented out after add.')
  call vunit#AssertTrue(search('^\s*//public abstract int compare(String \w\+, String \w\+)'),
    \ 'Super method compare() not commented out after add.')
  call vunit#AssertTrue(search('^\s*//public abstract boolean equals(Object \w\+)'),
    \ 'Super method equals() not commented out after add.')
  bdelete

  call vunit#AssertTrue(search('* @see org.eclim.test.junit.SomeClassVUnit#aMethod()$'),
    \ 'see testAMethod() not added.')
  call vunit#AssertTrue(search('* @see org.eclim.test.junit.SomeClassVUnit#aMethod(String)$'),
    \ 'see testAMethod(String) not added.')
  call vunit#AssertTrue(search('public void testAMethod()$'),
    \ 'testAMethod() not added.')
  call vunit#AssertTrue(search('* @see java.util.Comparator#compare(String,String)$'),
    \ 'see compare() not added.')
  call vunit#AssertTrue(search('public void testCompare()$'),
    \ 'testCompare() not added.')
  call vunit#AssertTrue(search('public void testEquals()$'),
    \ 'testEquals() not added.')
endfunction " }}}

" s:InitFile(version) {{{
function s:InitFile(version)
  edit! src/org/eclim/test/junit/SomeClassTestVUnit.java
  call vunit#PeekRedir()

  1,$delete _
  call append(1, [
      \ 'package org.eclim.test.junit;',
      \ '',
      \ 'public class SomeClassTestVUnit',
      \ '{',
      \ '}',
    \ ])
  1,1delete _
  " i think eclipse may ignore subisiquent saves if they occur too fast, so
  " add some human time.
  sleep 1
  write
  call vunit#PeekRedir()

  ProjectSettings

  call search('junit\.version=', 'w')
  let setting = getline('.')
  let setting = substitute(setting, '\(.*junit\.version=\).*', '\1' . a:version, '')
  call setline(line('.'), setting)
  write
  bdelete
endfunction " }}}

" vim:ft=vim:fdm=marker
