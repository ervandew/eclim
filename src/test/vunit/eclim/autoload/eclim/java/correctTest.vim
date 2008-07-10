" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for correct.vim
"
" License:
"
" Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
function! SetUp ()
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
endfunction " }}}

" TestCorrect() {{{
function! TestCorrect ()
  edit! src/org/eclim/test/correct/TestCorrectVUnit.java
  call PeekRedir()

  write
  call PeekRedir()
  let errors = getloclist(0)
  call VUAssertEquals(1, len(errors), 'No errors to correct.')

  call cursor(errors[0].lnum, errors[0].col)
  JavaCorrect
  call PeekRedir()

  call VUAssertTrue(bufname('%') =~ 'TestCorrectVUnit.java_correct$',
    \ 'Correct window not opened.')
  call VUAssertEquals('ArrayList cannot be resolved to a type', getline(1),
    \ 'Wrong error message.')

  call VUAssertTrue(search("Import 'ArrayList' (java.util)"),
    \ 'Required correction not found.')

  call cursor(11, 1)
  exec "normal \<cr>"
  call PeekRedir()

  call VUAssertTrue(search('^import java\.'),
    \ 'Correction not applied.')

  bdelete!
endfunction " }}}

" vim:ft=vim:fdm=marker
