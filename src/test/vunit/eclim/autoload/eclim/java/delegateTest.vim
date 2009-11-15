" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for delegate.vim
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

" TestJavaDelegate() {{{
function! TestJavaDelegate()
  edit! src/org/eclim/test/delegate/TestDelegateVUnit.java
  call PeekRedir()

  call cursor(8, 3)
  JavaDelegate
  call VUAssertTrue(bufname('%') =~ 'TestDelegateVUnit.java_delegate$',
    \ 'Delegate window not opened.')
  call VUAssertEquals('org.eclim.test.delegate.TestDelegateVUnit', getline(1),
    \ 'Wrong type in delegate window.')

  call VUAssertTrue(search('^\s*public abstract Iterator<Double> iterator()'),
    \ 'Super method iterator() not found')
  call VUAssertTrue(search('^\s*public abstract boolean add(Double \w)'),
    \ 'Super method add() not found')

  exec "normal Vkkk\<cr>"

  call VUAssertTrue(search('^\s*//public abstract Iterator<Double> iterator()'),
    \ 'Super method add() not commented out after add.')
  call VUAssertTrue(search('^\s*//public abstract boolean add(Double \w)'),
    \ 'Super method add() not commented out after add.')
  bdelete

  call VUAssertTrue(search('public Iterator<Double> iterator()$'),
    \ 'iterator() not added.')
  call VUAssertTrue(search('return list\.iterator();$'), 'iterator() not delegating.')
  call VUAssertTrue(search('public boolean add(Double \w)$'), 'add() not added.')
  call VUAssertTrue(search('return list\.add(\w);$'), 'add() not delegating.')
endfunction " }}}

" vim:ft=vim:fdm=marker
