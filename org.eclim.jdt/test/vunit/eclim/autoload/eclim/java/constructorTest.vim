" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for constructor.vim
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
endfunction " }}}

" TestConstructor() {{{
function! TestConstructor()
  edit! src/org/eclim/test/constructor/TestConstructorVUnit.java
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

" vim:ft=vim:fdm=marker
