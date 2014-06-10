" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2014  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_groovy'
endfunction " }}}

function! TestValidate() " {{{
  edit! src/org/eclim/test/src/TestSrc.groovy
  write

  let loc = getloclist(0)
  call vunit#AssertEquals(len(loc), 2)
  call vunit#AssertEquals(loc[0].lnum, 8)
  call vunit#AssertEquals(loc[0].type, 'e')
  call vunit#AssertEquals(loc[0].text,
    \ "Groovy:unexpected token: } @ line 8, column 3.")
  call vunit#AssertEquals(loc[1].lnum, 10)
  call vunit#AssertEquals(loc[1].type, 'e')
  call vunit#AssertEquals(loc[1].text,
    \ "Groovy:unexpected token: err @ line 10, column 3.")
endfunction " }}}

" vim:ft=vim:fdm=marker
