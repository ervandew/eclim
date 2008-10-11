" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for find.vim
"
" License:
"
" Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
function! SetUp ()
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_python'
endfunction " }}}

" TestFindDefinition() {{{
function! TestFindDefinition()
  edit! test/find/test_find.py
  call PeekRedir()

  call cursor(1, 18)
  PythonFindDefinition
  call VUAssertTrue(expand('%') =~ 'test/common/__init__.py')
  call VUAssertEquals(getline(1), 'from test.common.functions import *')
  bdelete

  call cursor(3, 13)
  PythonFindDefinition
  call VUAssertTrue(expand('%') =~ 'test/common/objects.py')
  call VUAssertEquals(getline('.'), 'class Test1 (object):')
  bdelete

  call cursor(5, 8)
  PythonFindDefinition
  call VUAssertTrue(expand('%') =~ 'test/common/functions.py')
  call VUAssertEquals(getline('.'), 'def test3 ():')
  bdelete
endfunction " }}}

" vim:ft=vim:fdm=marker
