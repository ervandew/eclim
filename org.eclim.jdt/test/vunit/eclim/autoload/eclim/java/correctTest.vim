" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for correct.vim
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
endfunction " }}}

function! TestCorrect() " {{{
  edit! src/org/eclim/test/correct/TestCorrectVUnit.java
  call vunit#PeekRedir()

  call histadd('cmd', 'write') | write
  call vunit#PeekRedir()
  let errors = getloclist(0)
  call vunit#AssertEquals(1, len(errors), 'No errors to correct.')

  call cursor(errors[0].lnum, errors[0].col)
  JavaCorrect
  call vunit#PeekRedir()

  call vunit#AssertTrue(bufname('%') =~ 'TestCorrectVUnit.java_correct$',
    \ 'Correct window not opened.')
  call vunit#AssertEquals('ArrayList cannot be resolved to a type', getline(1),
    \ 'Wrong error message.')

  let line = search("Import 'ArrayList' (java.util)")
  call vunit#AssertTrue(line, 'Required correction not found.')

  exec "normal \<cr>"
  call vunit#PeekRedir()

  call vunit#AssertTrue(search('^import java\.'), 'Correction not applied.')
endfunction " }}}

" vim:ft=vim:fdm=marker
