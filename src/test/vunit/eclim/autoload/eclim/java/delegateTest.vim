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

" TestCorrect() {{{
function! TestCorrect()
  edit! src/org/eclim/test/delegate/TestDelegateVUnit.java
  call PeekRedir()

  call cursor(8, 3)
  JavaDelegate
  call VUAssertTrue(bufname('%') =~ 'TestDelegateVUnit.java_delegate$',
    \ 'Delegate window not opened.')
  call VUAssertEquals('org.eclim.test.delegate.TestDelegateVUnit', getline(1),
    \ 'Wrong type in delegate window.')

  call VUAssertTrue(search('^\s*public abstract int size()'),
    \ 'Super method size() not found')

  exec "normal Vjj\<cr>"

  call VUAssertTrue(search('^\s*//public abstract int size()'),
    \ 'Super method size() not commented out after add.')
  bdelete

  call VUAssertTrue(search('public int size()$'), 'size() not added.')
  call VUAssertTrue(search('return list\.size();$'), 'size() not delegating.')
  call VUAssertTrue(search('public boolean isEmpty()$'), 'isEmpty() not added.')
  call VUAssertTrue(search('return list\.isEmpty();$'), 'isEmpty() not delegating.')
  call VUAssertTrue(search('public boolean contains(Object o)$'),
    \ 'contains(Object) not added.')
  call VUAssertTrue(search('return list\.contains(o);$'),
    \ 'contains(Object) not delegating.')
endfunction " }}}

" vim:ft=vim:fdm=marker
