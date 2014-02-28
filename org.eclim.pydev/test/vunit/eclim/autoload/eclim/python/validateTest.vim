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

function! TestValidate() " {{{
  edit! test/validate/test_validate.py
  write

  let loc = getloclist(0)
  call vunit#AssertEquals(len(loc), 2)
  call vunit#AssertEquals(loc[0].lnum, 1)
  call vunit#AssertEquals(loc[0].type, 'w')
  call vunit#AssertEquals(loc[0].text, "Unused import: common")
  call vunit#AssertEquals(loc[1].lnum, 3)
  call vunit#AssertEquals(loc[1].type, 'e')
  call vunit#AssertEquals(loc[1].text, "Undefined variable: foobar")
endfunction " }}}

" vim:ft=vim:fdm=marker
