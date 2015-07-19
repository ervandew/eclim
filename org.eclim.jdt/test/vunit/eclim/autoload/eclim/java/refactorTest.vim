" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2005 - 2015  Eric Van Dewoestine
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

function! TestMove() " {{{
  edit! src/org/eclim/test/refactoring/move/p1/TestMove.java
  call vunit#PeekRedir()

  let g:EclimRefactorPromptDefault = 1 " execute
  JavaMove org.eclim.test.refactoring.move.p2

  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(name,
    \ 'src/org/eclim/test/refactoring/move/p2/TestMove.java',
    \ 'Move result file incorrect')
  call vunit#AssertEquals(getline(1),
    \ 'package org.eclim.test.refactoring.move.p2;', 'Move package incorrect')

  RefactorUndo
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(name,
    \ 'src/org/eclim/test/refactoring/move/p1/TestMove.java',
    \ 'Undo result file incorrect')
  call vunit#AssertEquals(getline(1),
    \ 'package org.eclim.test.refactoring.move.p1;', 'Undo package incorrect')
endfunction " }}}

function! TestRenameField() " {{{
  let g:EclimRefactorPromptDefault = 2 " preview
  edit! src/org/eclim/test/refactoring/rename/vn1/TestN1VUnit.java
  call vunit#PeekRedir()

  call cursor(5, 30)
  call vunit#AssertEquals(getline('.'),
    \ '  public static final String FOO = "value";', 'FOO field incorrect.')

  JavaRename BAR
  call vunit#PeekRedir()
  call vunit#AssertEquals(expand('%'), '[Refactor Preview]', 'wrong preview title')
  call vunit#AssertEquals(line('$'), 4, 'wrong number of preview lines')
  call vunit#AssertEquals(line(1), '|diff|: ' . getcwd() . '/src/org/eclim/test/refactoring/rename/vn1/TestN1VUnit.java', 'wrong preview line 1')
  call vunit#AssertEquals(line(2), '|diff|: ' . getcwd() . '/src/org/eclim/test/refactoring/rename/vn1/vn2/TestN2VUnit.java', 'wrong preview line 2')
  call vunit#AssertEquals(line(3), '', 'wrong preview line 3')
  call vunit#AssertEquals(line(4), '|Execute Refactoring|', 'wrong preview line 4')

  call cursor(1, 1)
  exec "normal \<cr>"

  call vunit#AssertEquals(expand('%'), 'TestN1VUnit.current.java', 'wrong diff current 1')
  call vunit#AssertTrue(&diff, 'current diff not enabled 1')
  call vunit#AssertEquals(getline(5),
    \ '  public static final String FOO = "value";', 'current FOO field incorrect 1')
  winc l
  call vunit#AssertEquals(expand('%'), 'TestN1VUnit.new.java', 'wrong diff new 1')
  call vunit#AssertTrue(&diff, 'new diff not enabled 1')
  call vunit#AssertEquals(getline(5),
    \ '  public static final String BAR = "value";', 'new FOO field incorrect 1')

  let numwins = winnr('$')
  winc j
  call vunit#AssertEquals(expand('%'), '[Refactor Preview]', 'wrong preview title 2')
  call cursor(2, 1)
  exec "normal \<cr>"
  call vunit#AssertEquals(winnr('$'), numwins, 'wrong number of windows')

  call vunit#AssertEquals(expand('%'), 'TestN2VUnit.current.java', 'wrong diff current 2')
  call vunit#AssertTrue(&diff, 'current diff not enabled 2')
  call vunit#AssertEquals(getline(3),
    \ 'import static org.eclim.test.refactoring.rename.vn1.TestN1VUnit.FOO;',
    \ 'current import FOO field incorrect.')
  call vunit#AssertEquals(getline(11),
    \ '    System.out.println(FOO);', 'current FOO reference incorrect 1')
  winc l
  call vunit#AssertEquals(expand('%'), 'TestN2VUnit.new.java', 'wrong diff new 2')
  call vunit#AssertTrue(&diff, 'new diff not enabled 2')
  call vunit#AssertEquals(getline(3),
    \ 'import static org.eclim.test.refactoring.rename.vn1.TestN1VUnit.BAR;',
    \ 'new import FOO field incorrect.')
  call vunit#AssertEquals(getline(11),
    \ '    System.out.println(BAR);', 'new FOO reference incorrect 1')

  winc j
  call cursor(4, 1)
  exec "normal \<cr>"
  call vunit#AssertEquals(winnr('$'), numwins - 3, 'wrong number of windows')

  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(name,
    \ 'src/org/eclim/test/refactoring/rename/vn1/TestN1VUnit.java',
    \ 'Wrong result file')
  call vunit#AssertEquals(getline(5),
    \ '  public static final String BAR = "value";', 'field rename incorrect')
  let lines = readfile(
    \ 'src/org/eclim/test/refactoring/rename/vn1/vn2/TestN2VUnit.java')
  call vunit#AssertEquals(lines[2],
    \ 'import static org.eclim.test.refactoring.rename.vn1.TestN1VUnit.BAR;',
    \ 'static field import rename incorrect')
  call vunit#AssertEquals(lines[10],
    \ '    System.out.println(BAR);',
    \ 'static field reference rename incorrect')
endfunction " }}}

function! TestRenameMethod() " {{{
  let g:EclimRefactorPromptDefault = 2 " preview
  edit! src/org/eclim/test/refactoring/rename/vn1/vn2/TestN2VUnit.java
  call vunit#PeekRedir()

  call cursor(13, 10)
  call vunit#AssertEquals(getline('.'),
    \ '    test.testMethod();', 'testMethod reference incorrect.')

  JavaRename testRename
  call vunit#PeekRedir()
  call vunit#AssertEquals(expand('%'), '[Refactor Preview]', 'wrong preview title')
  call vunit#AssertEquals(line('$'), 4, 'wrong number of preview lines')
  call vunit#AssertEquals(line(1), '|diff|: ' . getcwd() . '/src/org/eclim/test/refactoring/rename/vn1/TestN1VUnit.java', 'wrong preview line 1')
  call vunit#AssertEquals(line(2), '|diff|: ' . getcwd() . '/src/org/eclim/test/refactoring/rename/vn1/vn2/TestN2VUnit.java', 'wrong preview line 2')
  call vunit#AssertEquals(line(3), '', 'wrong preview line 3')
  call vunit#AssertEquals(line(4), '|Execute Refactoring|', 'wrong preview line 4')

  call cursor(1, 1)
  exec "normal \<cr>"

  call vunit#AssertEquals(expand('%'), 'TestN1VUnit.current.java', 'wrong diff current 1')
  call vunit#AssertTrue(&diff, 'current diff not enabled 1')
  call vunit#AssertEquals(getline(7),
    \ '  public void testMethod()', 'current testMethod incorrect 1')
  winc l
  call vunit#AssertEquals(expand('%'), 'TestN1VUnit.new.java', 'wrong diff new 1')
  call vunit#AssertTrue(&diff, 'new diff not enabled 1')
  call vunit#AssertEquals(getline(7),
    \ '  public void testRename()', 'new testMethod incorrect 1')

  let numwins = winnr('$')
  winc j
  call vunit#AssertEquals(expand('%'), '[Refactor Preview]', 'wrong preview title 2')
  call cursor(2, 1)
  exec "normal \<cr>"
  call vunit#AssertEquals(winnr('$'), numwins, 'wrong number of windows')

  call vunit#AssertEquals(expand('%'), 'TestN2VUnit.current.java', 'wrong diff current 2')
  call vunit#AssertTrue(&diff, 'current diff not enabled 2')
  call vunit#AssertEquals(getline(13),
    \ '    test.testMethod();', 'current testMethod reference incorrect 1')
  winc l
  call vunit#AssertEquals(expand('%'), 'TestN2VUnit.new.java', 'wrong diff new 2')
  call vunit#AssertTrue(&diff, 'new diff not enabled 2')
  call vunit#AssertEquals(getline(13),
    \ '    test.testRename();', 'new testMethod reference incorrect 1')

  winc j
  call cursor(4, 1)
  exec "normal \<cr>"
  call vunit#AssertEquals(winnr('$'), numwins - 3, 'wrong number of windows')

  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(name,
    \ 'src/org/eclim/test/refactoring/rename/vn1/vn2/TestN2VUnit.java',
    \ 'Wrong result file')
  call vunit#AssertEquals(getline(13),
    \ '    test.testRename();', 'method reference rename incorrect')
  let lines = readfile(
    \ 'src/org/eclim/test/refactoring/rename/vn1/TestN1VUnit.java')
  call vunit#AssertEquals(lines[6],
    \ '  public void testRename()', 'method rename incorrect')
endfunction " }}}

function! TestRenameType() " {{{
  let g:EclimRefactorPromptDefault = 2 " preview
  edit! src/org/eclim/test/refactoring/rename/vn1/TestN1VUnit.java
  call vunit#PeekRedir()

  call cursor(3, 18)
  call vunit#AssertEquals(getline('.'),
    \ 'public class TestN1VUnit', 'class declaration incorrect.')

  JavaRename TestR1VUnit
  call vunit#PeekRedir()
  call vunit#AssertEquals(expand('%'), '[Refactor Preview]', 'wrong preview title')
  call vunit#AssertEquals(line('$'), 4, 'wrong number of preview lines')
  call vunit#AssertEquals(line(1), '|diff|: ' . getcwd() . '/src/org/eclim/test/refactoring/rename/vn1/vn2/TestN2VUnit.java', 'wrong preview line 1')
  call vunit#AssertEquals(line(2), " other: Rename compilation unit 'TestN1VUnit.java' to 'TestR1VUnit.java'", 'wrong preview line 2')
  call vunit#AssertEquals(line(3), '', 'wrong preview line 3')
  call vunit#AssertEquals(line(4), '|Execute Refactoring|', 'wrong preview line 4')

  call cursor(1, 1)
  exec "normal \<cr>"

  call vunit#AssertEquals(expand('%'), 'TestN2VUnit.current.java', 'wrong diff current 1')
  call vunit#AssertTrue(&diff, 'current diff not enabled 1')
  call vunit#AssertEquals(getline(3),
    \ 'import static org.eclim.test.refactoring.rename.vn1.TestN1VUnit.BAR;',
    \ 'current type static import incorrect 1')
  call vunit#AssertEquals(getline(5),
    \ 'import org.eclim.test.refactoring.rename.vn1.TestN1VUnit;',
    \ 'current type import incorrect 1')
  call vunit#AssertEquals(getline(12),
    \ '    TestN1VUnit test = new TestN1VUnit();',
    \ 'current type reference incorrect 1')
  winc l
  call vunit#AssertEquals(expand('%'), 'TestN2VUnit.new.java', 'wrong diff new 1')
  call vunit#AssertTrue(&diff, 'new diff not enabled')
  call vunit#AssertEquals(getline(3),
    \ 'import static org.eclim.test.refactoring.rename.vn1.TestR1VUnit.BAR;',
    \ 'new type static import incorrect')
  call vunit#AssertEquals(getline(5),
    \ 'import org.eclim.test.refactoring.rename.vn1.TestR1VUnit;',
    \ 'new type import incorrect')
  call vunit#AssertEquals(getline(12),
    \ '    TestR1VUnit test = new TestR1VUnit();',
    \ 'new type reference incorrect')

  let numwins = winnr('$')
  winc j
  call vunit#AssertEquals(expand('%'), '[Refactor Preview]', 'wrong preview title 2')
  call cursor(4, 1)
  exec "normal \<cr>"
  call vunit#AssertEquals(winnr('$'), numwins - 3, 'wrong number of windows')

  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(name,
    \ 'src/org/eclim/test/refactoring/rename/vn1/TestR1VUnit.java',
    \ 'Wrong result file')
  call vunit#AssertEquals(getline(3),
    \ 'public class TestR1VUnit', 'class declaration rename incorrect')
  let lines = readfile(
    \ 'src/org/eclim/test/refactoring/rename/vn1/vn2/TestN2VUnit.java')
  call vunit#AssertEquals(lines[2],
    \ 'import static org.eclim.test.refactoring.rename.vn1.TestR1VUnit.BAR;',
    \ 'type static import rename incorrect')
  call vunit#AssertEquals(lines[4],
    \ 'import org.eclim.test.refactoring.rename.vn1.TestR1VUnit;',
    \ 'type import rename incorrect')
  call vunit#AssertEquals(lines[11],
    \ '    TestR1VUnit test = new TestR1VUnit();',
    \ 'type reference rename incorrect')
endfunction " }}}

function! TestRenamePackage() " {{{
  let g:EclimRefactorPromptDefault = 2 " preview
  edit! src/org/eclim/test/refactoring/rename/vn1/vn2/TestN2VUnit.java
  call vunit#PeekRedir()

  cd src/org/eclim/test/refactoring/rename/vn1/vn2
  let cwd = substitute(getcwd(), '\', '/', 'g')
  call vunit#AssertEquals(cwd, g:TestEclimWorkspace . 'eclim_unit_test_java/src/org/eclim/test/refactoring/rename/vn1/vn2', 'cwd incorrect')

  call cursor(1, 44)
  call vunit#AssertEquals(getline('.'),
    \ 'package org.eclim.test.refactoring.rename.vn1.vn2;',
    \ 'package declaration incorrect.')

  JavaRename org.eclim.test.refactoring.rename.vr1
  call vunit#PeekRedir()
  call vunit#AssertEquals(expand('%'), '[Refactor Preview]', 'wrong preview title')
  call vunit#AssertEquals(line('$'), 4, 'wrong number of preview lines')
  let cwd = substitute(getcwd(), '\', '/', 'g')
  call vunit#AssertEquals(line(1), '|diff|: ' . cwd . '/src/org/eclim/test/refactoring/rename/vn1/vn2/TestN2VUnit.java', 'wrong preview line 1')
  call vunit#AssertEquals(line(2), " other: Rename package 'org.eclim.test.refactoring.rename.vn1' and subpackages to 'org.eclim.test.refactoring.rename.vr1'", 'wrong preview line 1')
  call vunit#AssertEquals(line(3), '', 'wrong preview line 3')
  call vunit#AssertEquals(line(4), '|Execute Refactoring|', 'wrong preview line 4')

  call cursor(1, 1)
  exec "normal \<cr>"

  call vunit#AssertEquals(expand('%'), 'TestN2VUnit.current.java', 'wrong diff current 1')
  call vunit#AssertTrue(&diff, 'current diff not enabled 1')
  call vunit#AssertEquals(getline(3),
    \ 'import static org.eclim.test.refactoring.rename.vn1.TestR1VUnit.BAR;',
    \ 'current vn1 static import incorrect 1')
  call vunit#AssertEquals(getline(5),
    \ 'import org.eclim.test.refactoring.rename.vn1.TestR1VUnit;',
    \ 'current vn1 import incorrect 1')
  winc l
  call vunit#AssertEquals(expand('%'), 'TestN2VUnit.new.java', 'wrong diff new 1')
  call vunit#AssertTrue(&diff, 'new diff not enabled 1')
  call vunit#AssertEquals(getline(3),
    \ 'import static org.eclim.test.refactoring.rename.vr1.TestR1VUnit.BAR;',
    \ 'new vn1 static import incorrect 1')
  call vunit#AssertEquals(getline(5),
    \ 'import org.eclim.test.refactoring.rename.vr1.TestR1VUnit;',
    \ 'new vn1 import incorrect 1')

  let numwins = winnr('$')
  winc j
  call cursor(4, 1)
  exec "normal \<cr>"
  call vunit#AssertEquals(winnr('$'), numwins - 3, 'wrong number of windows')

  let cwd = substitute(getcwd(), '\', '/', 'g')
  call vunit#AssertEquals(cwd, g:TestEclimWorkspace . 'eclim_unit_test_java/src/org/eclim/test/refactoring/rename/vr1/vn2', 'cwd rename incorrect')
  call vunit#AssertEquals(expand('%'), 'TestN2VUnit.java', 'Wrong result file')
  call vunit#AssertEquals(getline(1),
    \ 'package org.eclim.test.refactoring.rename.vr1.vn2;',
    \ 'package declaration rename incorrect')
  call vunit#AssertEquals(getline(3),
    \ 'import static org.eclim.test.refactoring.rename.vr1.TestR1VUnit.BAR;',
    \ 'vn1 static import rename incorrect')
  call vunit#AssertEquals(getline(5),
    \ 'import org.eclim.test.refactoring.rename.vr1.TestR1VUnit;',
    \ 'vn1 import rename incorrect')
  let lines = readfile('../TestR1VUnit.java')
  call vunit#AssertEquals(lines[0],
    \ 'package org.eclim.test.refactoring.rename.vr1;',
    \ 'package declaration rename incorrect (vr1)')
endfunction " }}}

" vim:ft=vim:fdm=marker
