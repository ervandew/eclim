" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for util.vim
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_ruby'
endfunction " }}}

" TestValidate() {{{
function! TestValidate()
  edit! src/src/testValidate.rb
  call PeekRedir()

  Validate
  call PeekRedir()

  let results = getloclist(0)
  echo 'results = ' . string(results)

  call VUAssertEquals(len(results), 1, 'Wrong number of results.')

  call VUAssertEquals(2, results[0].lnum, 'Wrong line num.')
  call VUAssertEquals(11, results[0].col, 'Wrong col num.')
  call VUAssertEquals(
    \ "syntax error, unexpected tRPAREN",
    \ results[0].text, 'Wrong result.')
endfunction " }}}

" vim:ft=vim:fdm=marker
