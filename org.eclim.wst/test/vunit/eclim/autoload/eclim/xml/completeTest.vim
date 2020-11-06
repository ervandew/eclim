" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for complete.vim
"
" License:
"
" Copyright (C) 2005 - 2020  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_web'
endfunction " }}}

" TestCompleteXsd() {{{
function! TestCompleteXsd()
  edit! xsd/test.xsd
  call vunit#PeekRedir()

  call cursor(11, 11)
  let start = eclim#xml#complete#CodeComplete(1, '')
  call vunit#AssertEquals(8, start, 'Wrong starting column.')

  let results = eclim#xml#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  echo string(results)
  call vunit#AssertEquals(len(results), 1, 'Wrong number of results.')
  call vunit#AssertTrue(eclim#util#ListContains(results, ".*'unique'.*"),
    \ 'Results does not contain xs:unique')
endfunction " }}}

" vim:ft=vim:fdm=marker
