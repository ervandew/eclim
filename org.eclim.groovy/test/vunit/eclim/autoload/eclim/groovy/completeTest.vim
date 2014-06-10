" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2014  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_groovy'
endfunction " }}}

function! TestComplete() " {{{
  edit! src/org/eclim/test/complete/TestCompletion.groovy
  call vunit#PeekRedir()

  call cursor(7, 11)
  let start = eclim#groovy#complete#CodeComplete(1, '')
  call vunit#AssertEquals(9, start, 'Wrong starting column.')

  let results = eclim#groovy#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  for r in results
    echom string(r)
  endfor
  call vunit#AssertTrue(len(results) >= 10, 'Wrong number of results.')
  call vunit#AssertEquals('add(', results[0].word, 'Wrong result at 0.')
  call vunit#AssertEquals('addAll(', results[1].word, 'Wrong result at 1.')
endfunction " }}}

" vim:ft=vim:fdm=marker
