" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for hierarchy.vim
"
" License:
"
" Copyright (C) 2005 - 2013  Eric Van Dewoestine
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

function! TestJavaHierarchy() " {{{
  edit! src/org/eclim/test/hierarchy/TestHierarchyVUnit.java

  call cursor(1, 1)
  JavaHierarchy
  call vunit#PeekRedir()

  call vunit#AssertTrue(bufname('%') =~ '^\[Hierarchy\]$')
  call vunit#AssertEquals(getline(1), 'public class TestHierarchyVUnit')
  call vunit#AssertEquals(getline(2), '  public class Component')
  call vunit#AssertEquals(getline(3), '    public interface ImageObserver')
  call vunit#AssertEquals(getline(4), '    public interface MenuContainer')
  call vunit#AssertEquals(getline(5), '    public interface Serializable')
  call vunit#AssertEquals(getline(6), '  public interface Comparable')
  call vunit#AssertEquals(getline(7), '  public interface PropertyChangeListener')
  call vunit#AssertEquals(getline(8), '    public interface EventListener')

  let winnr = winnr()

  echom 'test opening of Comparable'
  call cursor(6, 1)
  exec "normal \<cr>"
  let name = fnamemodify(bufname('%'), ':t')
  call vunit#AssertEquals(name, 'Comparable.java')
  call vunit#AssertEquals(winnr('$'), 3)
  bdelete
  exec winnr . 'winc w'
  call vunit#PeekRedir()

  echom 'test opening of PropertyChangeListener'
  call cursor(7, 1)
  normal E
  let name = fnamemodify(bufname('%'), ':t')
  call vunit#AssertEquals(name, 'PropertyChangeListener.java')
  call vunit#AssertEquals(winnr('$'), 2)
  exec winnr . 'winc w'
  call vunit#PeekRedir()

  echom 'test opening of EventListener'
  call cursor(8, 1)
  normal E
  let name = fnamemodify(bufname('%'), ':t')
  call vunit#AssertEquals(name, 'EventListener.java')
  call vunit#AssertEquals(winnr('$'), 2)
  exec winnr . 'winc w'
  bdelete
  call vunit#PeekRedir()
endfunction " }}}

function! TestJavaCallHierarchyCallers() " {{{
  edit! src/org/eclim/test/hierarchy/TestCallHierarchyExternal.java
  call vunit#PeekRedir()

  call cursor(21, 29)
  :JavaCallHierarchy
  call vunit#PeekRedir()

  call vunit#AssertEquals('[Call Hierarchy]', expand('%'), 'Wrong window')
  call vunit#AssertEquals(6, line('$'), 'Wrong number of lines')

  call vunit#AssertEquals(
    \ 'getEurythmics() : SweetDreams - org.eclim.test.hierarchy.TestCallHierarchyExternal',
    \ getline(1), 'Wrong content on line 1')
  call vunit#AssertEquals(
    \ '  barWithStuff(Object) : Object - org.eclim.test.hierarchy.TestCallHierarchy.SubClass',
    \ getline(2), 'Wrong content on line 2')
  call vunit#AssertEquals(
    \ '    foo() : void - org.eclim.test.hierarchy.TestCallHierarchy',
    \ getline(3), 'Wrong content on line 3')
  call vunit#AssertEquals(
    \ '  foo() : void - org.eclim.test.hierarchy.TestCallHierarchy',
    \ getline(4), 'Wrong content on line 4')

  let chwin = winnr()

  call cursor(1, 1)
  exec "normal \<cr>"
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(
    \ 'src/org/eclim/test/hierarchy/TestCallHierarchyExternal.java', name,
    \ 'Wrong window (TestCallHierarchyExternal.java)')
  call vunit#AssertEquals(21, line('.'), 'Wrong line (TestCallHierarchyExternal.java)')
  call vunit#AssertEquals(3, col('.'), 'Wrong column (TestCallHierarchyExternal.java)')
  exec chwin . 'winc w'

  call cursor(2, 1)
  exec "normal \<cr>"
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(
    \ 'src/org/eclim/test/hierarchy/TestCallHierarchy.java', name,
    \ 'Wrong window (TestCallHierarchy.java)')
  call vunit#AssertEquals(35, line('.'), 'Wrong line (TestCallHierarchy.java)')
  call vunit#AssertEquals(33, col('.'), 'Wrong column (TestCallHierarchy.java)')
endfunction " }}}

function! TestJavaCallHierarchyCallees() " {{{
  edit! src/org/eclim/test/hierarchy/TestCallHierarchy.java
  call vunit#PeekRedir()

  call cursor(34, 12)
  :JavaCallHierarchy!
  call vunit#PeekRedir()

  call vunit#AssertEquals('[Call Hierarchy]', expand('%'), 'Wrong window')
  call vunit#AssertEquals(5, line('$'), 'Wrong number of lines')

  call vunit#AssertEquals(
    \ 'barWithStuff(Object) : Object - org.eclim.test.hierarchy.TestCallHierarchy.SubClass',
    \ getline(1), 'Wrong line 1')
  call vunit#AssertEquals(
    \ '  getEurythmics() : SweetDreams - org.eclim.test.hierarchy.TestCallHierarchyExternal',
    \ getline(2), 'Wrong line 2')
  call vunit#AssertEquals(
    \ '    SweetDreams - org.eclim.test.hierarchy.TestCallHierarchyExternal',
    \ getline(3), 'Wrong line 3')

  let chwin = winnr()

  call cursor(2, 1)
  exec "normal \<cr>"
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(
    \ 'src/org/eclim/test/hierarchy/TestCallHierarchy.java', name,
    \ 'Wrong window (TestCallHierarchy.java)')
  call vunit#AssertEquals(35, line('.'), 'Wrong line (TestCallHierarchy.java)')
  call vunit#AssertEquals(7, col('.'), 'Wrong column (TestCallHierarchy.java)')
  exec chwin . 'winc w'

  call cursor(3, 1)
  exec "normal \<cr>"
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(
    \ 'src/org/eclim/test/hierarchy/TestCallHierarchyExternal.java', name,
    \ 'Wrong window (TestCallHierarchyExternal.java)')
  call vunit#AssertEquals(22, line('.'), 'Wrong line (mod1.c')
  call vunit#AssertEquals(12, col('.'), 'Wrong column (mod1.c')
endfunction " }}}

" vim:ft=vim:fdm=marker
