" Author:  Eric Van Dewoestine
"
" Description: {{{
"
" License:
"
" Copyright (C) 2011 - 2014  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_scala'
endfunction " }}}

function! TestValidate() " {{{
  edit! src/eclim/test/src/TestSrc.scala
  call vunit#PeekRedir()

  write
  Validate
  call vunit#PeekRedir()

  let results = getloclist(0)
  echo 'results = ' . string(results)

  call vunit#AssertEquals(len(results), 2, 'Wrong number of results.')
  call vunit#AssertEquals(9, results[0].lnum, 'Wrong line num.')
  call vunit#AssertEquals(5, results[0].col, 'Wrong col num.')
  call vunit#AssertEquals(
    \ "value foo is not a member of eclim.test.TestScala",
    \ results[0].text, 'Wrong result.')
  call vunit#AssertEquals(11, results[1].lnum, 'Wrong line num.')
  call vunit#AssertEquals(23, results[1].col, 'Wrong col num.')
  call vunit#AssertEquals(
    \ "not found: type ArrayList",
    \ results[1].text, 'Wrong result.')
endfunction " }}}

" vim:ft=vim:fdm=marker
