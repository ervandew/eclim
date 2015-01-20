" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2005 - 2015  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_ruby'
endfunction " }}}

function! TestCompleteModule() " {{{
  edit! src/complete/testComplete.rb
  call vunit#PeekRedir()

  " module statics
  call cursor(11, 13)
  let start = eclim#ruby#complete#CodeComplete(1, '')
  call vunit#AssertEquals(12, start, 'Wrong starting column.')

  let results = eclim#ruby#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  echo 'results = ' . string(results)
  call vunit#AssertEquals(len(results), 2, 'Wrong number of results.')
  call vunit#AssertEquals('ID', results[0].word, 'Wrong result.')
  call vunit#AssertEquals('moduleMethodA', results[1].word, 'Wrong result.')

  " module method
  call cursor(10, 19)
  let start = eclim#ruby#complete#CodeComplete(1, '')
  call vunit#AssertEquals(11, start, 'Wrong starting column.')

  let results = eclim#ruby#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  echo 'results = ' . string(results)
  call vunit#AssertEquals(len(results), 1, 'Wrong number of results.')
  call vunit#AssertEquals('moduleMethodA', results[0].word, 'Wrong result.')
endfunction " }}}

function! TestCompleteBuiltin() " {{{
  edit! src/complete/testComplete.rb
  call vunit#PeekRedir()

  " number method
  call cursor(8, 5)
  let start = eclim#ruby#complete#CodeComplete(1, '')
  call vunit#AssertEquals(2, start, 'Wrong starting column.')

  let results = eclim#ruby#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  echo 'results = ' . string(results)
  " DLTK returns times() and, for some reason:
  " timestamp_file(name, target_prefix) - MakeMakefile
  " This occurs both in eclim and in the eclipse gui. Probably another dltk
  " bug.
  "call vunit#AssertEquals(len(results), 1, 'Wrong number of results.')
  call vunit#AssertTrue(len(results) >= 1, 'Wrong number of results.')
  call vunit#AssertEquals('times', results[0].word, 'Wrong result.')

  " list method
  call cursor(7, 6)
  let start = eclim#ruby#complete#CodeComplete(1, '')
  call vunit#AssertEquals(3, start, 'Wrong starting column.')

  let results = eclim#ruby#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  echo 'results = ' . string(results)
  call vunit#AssertTrue(len(results) >= 2, 'Wrong number of results.')
  call vunit#AssertEquals('each', results[0].word, 'Wrong result.')
  call vunit#AssertEquals('each_index', results[1].word, 'Wrong result.')
  "call vunit#AssertEquals('each_with_index', results[2].word, 'Wrong result.')
endfunction " }}}

function! TestCompleteUser() " {{{
  edit! src/complete/testComplete.rb
  call vunit#PeekRedir()

  " user class method
  call cursor(5, 10)
  let start = eclim#ruby#complete#CodeComplete(1, '')
  call vunit#AssertEquals(5, start, 'Wrong starting column.')

  let results = eclim#ruby#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  echo 'results = ' . string(results)
  call vunit#AssertEquals(len(results), 2, 'Wrong number of results.')
  call vunit#AssertEquals('testA', results[0].word, 'Wrong result.')
  call vunit#AssertEquals('testB', results[1].word, 'Wrong result.')

  " user class all
  call cursor(4, 6)
  let start = eclim#ruby#complete#CodeComplete(1, '')
  call vunit#AssertEquals(5, start, 'Wrong starting column.')

  let results = eclim#ruby#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  echo 'results = ' . string(results)
  call vunit#AssertTrue(len(results) > 2, 'Wrong number of results.')
endfunction " }}}

" vim:ft=vim:fdm=marker
