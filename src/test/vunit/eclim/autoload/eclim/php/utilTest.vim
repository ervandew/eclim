" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for util.vim
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_php'
endfunction " }}}

" TestValidate() {{{
function! TestValidate()
  edit! php/src/test.php
  call PeekRedir()

  call eclim#php#util#UpdateSrcFile(1)
  call PeekRedir()

  let results = getloclist(0)
  echo 'results = ' . string(results)

  call VUAssertTrue(len(results) == 2, 'Wrong number of results.')

  call VUAssertEquals(5, results[0].lnum, 'Wrong line num.')
  call VUAssertEquals(5, results[0].col, 'Wrong col num.')
  call VUAssertEquals("Syntax Error: expecting: ',' or ';'", results[0].text, 'Wrong result.')

  call VUAssertEquals(7, results[1].lnum, 'Wrong line num.')
  call VUAssertEquals(3, results[1].col, 'Wrong col num.')
  call VUAssertEquals('discarding unexpected </div>', results[1].text, 'Wrong result.')
endfunction " }}}

" vim:ft=vim:fdm=marker
