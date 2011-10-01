" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for project/problems.vim
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
  exec 'cd ' . g:TestEclimWorkspace
endfunction " }}}

" TestProjectProblems() {{{
function! TestProjectProblems()
  edit! eclim_unit_test/src/org/eclim/test/Test.java

  ProjectProblems
  winc p

  call vunit#PeekRedir()
  echom 'before: ' . string(getqflist())
  call vunit#AssertEquals(len(getqflist()), 2)

  5,5delete _
  " force the write to look like a user issued command
  call histadd('cmd', 'write')
  write

  call vunit#PeekRedir()
  echom 'after: ' . string(getqflist())
  call vunit#AssertEquals(len(getqflist()), 0)
endfunction " }}}

" vim:ft=vim:fdm=marker
