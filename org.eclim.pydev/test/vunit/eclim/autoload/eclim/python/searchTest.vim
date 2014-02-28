" Author:  Eric Van Dewoestine
"
" License: {{{
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_python'
endfunction " }}}

function! TestFindDefinition() " {{{
  edit! test/search/test_search.py
  call vunit#PeekRedir()

  call cursor(1, 18)
  PythonSearch
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertTrue(name =~ 'test/common/__init__.py')
  call vunit#AssertEquals(getline(1), 'from test.common.functions import *')
  bdelete

  call cursor(3, 13)
  PythonSearch
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertTrue(name =~ 'test/common/objects.py')
  call vunit#AssertEquals(getline('.'), 'class Test1(object):')
  bdelete

  call cursor(5, 8)
  PythonSearch
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertTrue(name =~ 'test/common/functions.py')
  call vunit#AssertEquals(getline('.'), "def test3(foo, bar='baz', *args, **kwargs):")
  bdelete
endfunction " }}}

" vim:ft=vim:fdm=marker
