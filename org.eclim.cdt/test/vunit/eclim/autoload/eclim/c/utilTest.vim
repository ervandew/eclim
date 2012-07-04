" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for util.vim
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

function! SetUp() " {{{
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_c'
endfunction " }}}

function! TestUpdateCSrcFile() " {{{
  edit! src/test_src.c
  call vunit#PeekRedir()

  call histadd('cmd', 'write') | write
  call vunit#PeekRedir()

  let results = getloclist(0)
  echo string(results)
  call vunit#AssertEquals(2, len(results), 'Wrong number of results.')
  call vunit#AssertEquals(results[0].lnum, 1, 'Wrong lnum.')
  call vunit#AssertEquals(results[0].col, 1, 'Wrong lnum.')
  call vunit#AssertEquals(
    \ results[0].text, 'Unresolved inclusion: <stdi.h>', 'Wrong error msg.')

  call vunit#AssertEquals(results[1].lnum, 5, 'Wrong lnum.')
  call vunit#AssertEquals(results[1].col, 3, 'Wrong lnum.')
  call vunit#AssertEquals(results[1].text, 'Syntax error', 'Wrong error msg.')
endfunction " }}}

" vim:ft=vim:fdm=marker
