" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for hierarchy.vim
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_c'
  set expandtab
  set shiftwidth=2 tabstop=2
endfunction " }}}

" TestCallHierarchy() {{{
function! TestCallHierarchy()
  edit! src/callhierarchy/mod2.c
  call vunit#PeekRedir()

  call cursor(6, 3)
  :CCallHierarchy
  call vunit#PeekRedir()

  call vunit#AssertEquals('[Call Hierarchy]', expand('%'), 'Wrong window')
  call vunit#AssertEquals(8, line('$'), 'Wrong number of lines')

  call vunit#AssertEquals('fun2(int)', getline(1), 'Wrong content on line 1')
  call vunit#AssertEquals('  fun1(int)', getline(2), 'Wrong content on line 2')
  call vunit#AssertEquals('    main()', getline(3), 'Wrong content on line 3')
  call vunit#AssertEquals('    fun3(int)', getline(4), 'Wrong content on line 4')
  call vunit#AssertEquals('  fun3(int)', getline(5), 'Wrong content on line 5')
  call vunit#AssertEquals('  fun3(int)', getline(6), 'Wrong content on line 6')

  let chwin = winnr()

  call cursor(1, 1)
  exec "normal \<cr>"
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals('src/callhierarchy/mod2.c', name, 'Wrong window (mod2.c)')
  call vunit#AssertEquals(1, line('.'), 'Wrong line (mod2.c')
  call vunit#AssertEquals(5, col('.'), 'Wrong column (mod2.c')
  exec chwin . 'winc w'

  call cursor(3, 1)
  exec "normal \<cr>"
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals('src/callhierarchy/main.c', name, 'Wrong window (main.c)')
  call vunit#AssertEquals(6, line('.'), 'Wrong line (main.c')
  call vunit#AssertEquals(28, col('.'), 'Wrong column (main.c')

  " test that the source isn't cached.
  edit! src/callhierarchy/mod2.c
  call vunit#PeekRedir()

  call cursor(6, 3)
  normal yyp
  call cursor(7, 3)
  :CCallHierarchy
  call vunit#PeekRedir()

  call vunit#AssertEquals('[Call Hierarchy]', expand('%'), 'No results on update call')
  call vunit#AssertEquals(9, line('$'), 'Wrong number of lines on update call')
endfunction " }}}

" vim:ft=vim:fdm=marker
