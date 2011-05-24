" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for validate.vim
"
" License:
"
" Copyright (C) 2005 - 2011  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_python'
endfunction " }}}

" TestCompiler() {{{
function! TestCompiler()
  edit! test/validate/test_validate_compiler.py
  write

  let loc = getloclist(0)
  call vunit#AssertEquals(len(loc), 1)
  call vunit#AssertEquals(loc[0].lnum, 3)
  call vunit#AssertEquals(loc[0].type, 'e')
  call vunit#AssertEquals(loc[0].text, 'unexpected indent')
endfunction " }}}

" TestPyflakes() {{{
function! TestPyflakes()
  edit! test/validate/test_validate_pyflakes.py
  write

  let loc = getloclist(0)
  call vunit#AssertEquals(len(loc), 2)
  call vunit#AssertEquals(loc[0].lnum, 1)
  call vunit#AssertEquals(loc[0].type, 'w')
  call vunit#AssertEquals(loc[0].text, " 'common' imported but unused")
  call vunit#AssertEquals(loc[1].lnum, 3)
  call vunit#AssertEquals(loc[1].type, 'e')
  call vunit#AssertEquals(loc[1].text, " undefined name 'foobar'")
endfunction " }}}

" TestPylint() {{{
function! TestPylint()
  edit! test/validate/test_validate_pylint.py
  PyLint

  function CompareQf(i1, i2)
    return a:i1.lnum == a:i2.lnum ? 0 : a:i1.lnum > a:i2.lnum ? 1 : -1
  endfunction

  let qf = sort(getqflist(), "CompareQf")
  call vunit#AssertEquals(len(qf), 2)
  call vunit#AssertEquals(qf[0].lnum, 3)
  call vunit#AssertEquals(qf[0].type, 'w')
  call vunit#AssertEquals(qf[0].text, ' Unused import test2')
  call vunit#AssertEquals(qf[1].lnum, 5)
  call vunit#AssertEquals(qf[1].type, 'w')
  call vunit#AssertEquals(qf[1].text, ' Statement seems to have no effect')
endfunction " }}}

" TestRope() {{{
function! TestRope()
  edit! test/validate/test_validate_rope.py
  Validate

  let loc = getloclist(0)
  call vunit#AssertEquals(len(loc), 1)
  call vunit#AssertEquals(loc[0].lnum, 3)
  call vunit#AssertEquals(loc[0].type, 'e')
  call vunit#AssertEquals(loc[0].text, 'Unresolved attribute foobar')
endfunction " }}}

" vim:ft=vim:fdm=marker
