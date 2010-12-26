" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for project/problems.vim
"
" License:
"
" Copyright (C) 2005 - 2010  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace
endfunction " }}}

" TestProjectProblems() {{{
function! TestProjectProblems()
  edit! eclim_unit_test_java/src/org/eclim/test/problems/TestProblemsVUnit.java

  ProjectProblems
  winc p

  let length = len(getqflist())
  5,5delete _
  write

  call vunit#AssertEquals(len(getqflist()), length - 2)
endfunction " }}}

" vim:ft=vim:fdm=marker
