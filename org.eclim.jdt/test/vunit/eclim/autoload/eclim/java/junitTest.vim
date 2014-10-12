" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for junit.vim
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

function! TestJUnit() " {{{
  edit! src/org/eclim/test/junit/run/FooTest.java
  call vunit#PeekRedir()

  " some previous test causes class files to be removed, so we need to build the
  " project to ensure the class files we need are present.
  ProjectBuild

  call cursor(1, 1)
  JUnit!
  call vunit#AssertEquals(winnr('$'), 2, 'Run full test: windows')
  winc j

  call vunit#PeekRedir()
  echo "##############"
  let lnum = 1
  while lnum <= line('$')
    echo getline(lnum)
    let lnum += 1
  endwhile
  echo "##############"

  call vunit#AssertEquals(bufname('%'), '[JUnit Output]', 'Run full test: name')
  call vunit#AssertEquals(getline(2), 'Testsuite: org.eclim.test.junit.run.FooTest',
    \ 'Run full test: Testsuite')
  call vunit#AssertTrue(getline(4) =~
    \ 'Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: [0-9.]\+ sec',
    \ 'Run full test: Tests run')

  let lines = sort(getline(6, 8))
  call vunit#AssertTrue(lines[0] =~ 'Testcase: bar took [0-9.]\+ sec',
    \ 'Run full test: bar')
  call vunit#AssertTrue(lines[1] =~ 'Testcase: foo took [0-9.]\+ sec',
    \ 'Run full test: foo')
  call vunit#AssertTrue(lines[2] =~ 'Testcase: fooString took [0-9.]\+ sec',
    \ 'Run full test: fooString')
  bdelete

  call cursor(12, 5)
  silent JUnit!
  call vunit#AssertEquals(winnr('$'), 2, 'Run test: windows')
  winc j
  call vunit#AssertEquals(bufname('%'), '[JUnit Output]', 'Run test: name')
  call vunit#AssertEquals(line('$'), 6, 'Run test: lines')
  call vunit#AssertEquals(getline(2), 'Testsuite: org.eclim.test.junit.run.FooTest',
    \ 'Run test: Testsuite')
  call vunit#AssertTrue(getline(4) =~
    \ 'Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: [0-9.]\+ sec',
    \ 'Run test: Tests run')
  call vunit#AssertTrue(getline(6) =~ 'Testcase: foo took [0-9.]\+ sec',
    \ 'Run test: foo')
  bdelete

  edit! src/org/eclim/test/junit/run/Foo.java
  call cursor(12, 5)
  silent JUnit!
  call vunit#AssertEquals(winnr('$'), 2, 'Run class test: windows')
  winc j
  call vunit#AssertEquals(bufname('%'), '[JUnit Output]', 'Run class test: name')
  call vunit#AssertEquals(line('$'), 6, 'Run class test: lines')
  call vunit#AssertEquals(getline(2), 'Testsuite: org.eclim.test.junit.run.FooTest',
    \ 'Run class test: Testsuite')
  call vunit#AssertTrue(getline(4) =~
    \ 'Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: [0-9.]\+ sec',
    \ 'Run class test: Tests run')
  call vunit#AssertTrue(getline(6) =~ 'Testcase: fooString took [0-9.]\+ sec',
    \ 'Run class test: fooString')
  bdelete

  call cursor(12, 5)
  silent JUnit! **/run/*Test
  call vunit#AssertEquals(winnr('$'), 2, 'Run pattern: windows')
  winc j
  call vunit#AssertEquals(bufname('%'), '[JUnit Output]', 'Run pattern: name')
  call vunit#AssertEquals(line('$'), 15, 'Run pattern: lines')
  call vunit#AssertEquals(getline(2), 'Testsuite: org.eclim.test.junit.run.BarTest',
    \ 'Run pattern: Testsuite 1')
  call vunit#AssertTrue(getline(4) =~
    \ 'Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: [0-9.]\+ sec',
    \ 'Run pattern: Tests run 1')
  call vunit#AssertEquals(getline(9), 'Testsuite: org.eclim.test.junit.run.FooTest',
    \ 'Run pattern: Testsuite 2')
  call vunit#AssertTrue(getline(11) =~
    \ 'Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: [0-9.]\+ sec',
    \ 'Run pattern: Tests run 2')
  bdelete
endfunction " }}}

function! TestJUnitFindTest() " {{{
  edit! src/org/eclim/test/junit/run/Foo.java
  call vunit#PeekRedir()

  call cursor(1, 1)
  JUnitFindTest
  call vunit#AssertEquals(bufname('%'), 'src/org/eclim/test/junit/run/FooTest.java',
    \ 'Find test from class.')
  call vunit#AssertEquals(line('.'), 7, 'Find class from test line.')
  call vunit#AssertEquals(col('.'), 1, 'Find class from test column.')

  JUnitFindTest
  call vunit#AssertEquals(bufname('%'), 'src/org/eclim/test/junit/run/Foo.java',
    \ 'Find class from test.')
  call vunit#AssertEquals(line('.'), 3, 'Find class from test line.')
  call vunit#AssertEquals(col('.'), 1, 'Find class from test column.')

  call cursor(12, 5)
  JUnitFindTest
  call vunit#AssertEquals(bufname('%'), 'src/org/eclim/test/junit/run/FooTest.java',
    \ 'Find test method from class.')
  call vunit#AssertEquals(line('.'), 16, 'Find test method from class line.')
  call vunit#AssertEquals(col('.'), 3, 'Find test method from class column.')

  JUnitFindTest
  call vunit#AssertEquals(bufname('%'), 'src/org/eclim/test/junit/run/Foo.java',
    \ 'Find class method from test.')
  call vunit#AssertEquals(line('.'), 10, 'Find class method from test line.')
  call vunit#AssertEquals(col('.'), 3, 'Find class method from test column.')
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

function! TestCommandCompleteTest() " {{{
  edit! src/org/eclim/test/junit/SomeClassVUnitTest.java
  call vunit#PeekRedir()

  let results = eclim#java#junit#CommandCompleteTest(
    \ 'org.eclim.test.junit.run',
    \ 'JUnit org.eclim.test.junit.run', 30)
  call vunit#AssertEquals(results, [
      \ 'org.eclim.test.junit.run.BarTest',
      \ 'org.eclim.test.junit.run.FooTest',
    \ ])

  let results = eclim#java#junit#CommandCompleteTest('F', 'JUnit F', 7)
  call vunit#AssertEquals(results, [
      \ 'org.eclim.test.junit.run.FooTest',
    \ ])
endfunction " }}}

" vim:ft=vim:fdm=marker
