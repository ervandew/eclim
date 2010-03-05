" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for hierarchy.vim
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

" TestJavaHierarchy() {{{
function! TestJavaHierarchy()
  edit! src/org/eclim/test/hierarchy/TestHierarchyVUnit.java
  call PeekRedir()

  call cursor(1, 1)
  JavaHierarchy

  call VUAssertTrue(bufname('%') =~ '^\[Hierarchy\]$')
  call VUAssertEquals(getline(1), 'public class TestHierarchyVUnit')
  call VUAssertEquals(getline(2), '  public class Component')
  call VUAssertEquals(getline(3), '    public interface ImageObserver')
  call VUAssertEquals(getline(4), '    public interface MenuContainer')
  call VUAssertEquals(getline(5), '    public interface Serializable')
  call VUAssertEquals(getline(6), '  public interface Comparable')
  call VUAssertEquals(getline(7), '  public interface PropertyChangeListener')
  call VUAssertEquals(getline(8), '    public interface EventListener')

  let winnr = winnr()

  echom 'test opening of Comparable'
  call cursor(6, 1)
  exec "normal \<cr>"
  let name = substitute(bufname('%'), '\', '/', 'g')
  call VUAssertEquals(name, g:EclimTempDir . '/java/lang/Comparable.java')
  call VUAssertEquals(winnr('$'), 3)
  bdelete
  exec winnr . 'winc w'
  call PeekRedir()

  echom 'test opening of PropertyChangeListener'
  call cursor(7, 1)
  normal E
  let name = substitute(bufname('%'), '\', '/', 'g')
  call VUAssertEquals(name, g:EclimTempDir . '/java/beans/PropertyChangeListener.java')
  call VUAssertEquals(winnr('$'), 2)
  exec winnr . 'winc w'
  call PeekRedir()

  echom 'test opening of EventListener'
  call cursor(8, 1)
  normal E
  let name = substitute(bufname('%'), '\', '/', 'g')
  call VUAssertEquals(name, g:EclimTempDir . '/java/util/EventListener.java')
  call VUAssertEquals(winnr('$'), 2)
  exec winnr . 'winc w'
  bdelete
  call PeekRedir()
endfunction " }}}

" vim:ft=vim:fdm=marker
