" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for hierarchy.vim
"
" License:
"
" Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
endfunction " }}}

" TestCallHierarchy() {{{
function! TestCallHierarchy()
  edit! src/callhierarchy/mod2.c
  call PeekRedir()

  call cursor(6, 3)
  :CCallHierarchy
  call PeekRedir()

  call VUAssertEquals('[Call Hierarchy]', expand('%'), 'Wrong window')
  call VUAssertEquals(8, line('$'), 'Wrong number of lines')

  call VUAssertEquals('fun2(int)', getline(1), 'Wrong content on line 1')
  call VUAssertEquals('   fun1(int)', getline(2), 'Wrong content on line 2')
  call VUAssertEquals('     main()', getline(3), 'Wrong content on line 3')
  call VUAssertEquals('     fun3(int)', getline(4), 'Wrong content on line 4')
  call VUAssertEquals('   fun3(int)', getline(5), 'Wrong content on line 5')
  call VUAssertEquals('   fun3(int)', getline(6), 'Wrong content on line 6')

  let chwin = winnr()

  call cursor(1, 1)
  exec "normal \<cr>"
  let name = substitute(expand('%'), '\', '/', 'g')
  call VUAssertEquals('src/callhierarchy/mod2.c', name, 'Wrong window (mod2.c)')
  call VUAssertEquals(1, line('.'), 'Wrong line (mod2.c')
  call VUAssertEquals(5, col('.'), 'Wrong column (mod2.c')
  exec chwin . 'winc w'

  call cursor(3, 1)
  exec "normal \<cr>"
  let name = substitute(expand('%'), '\', '/', 'g')
  call VUAssertEquals('src/callhierarchy/main.c', name, 'Wrong window (main.c)')
  call VUAssertEquals(6, line('.'), 'Wrong line (main.c')
  call VUAssertEquals(28, col('.'), 'Wrong column (main.c')
endfunction " }}}

" vim:ft=vim:fdm=marker
