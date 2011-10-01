" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for complete.vim
"
" License:
"
" Copyright (C) 2005 - 2010  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_python'
endfunction " }}}

" TestComplete() {{{
function! TestComplete()
  edit! test/complete/test_complete.py
  call vunit#PeekRedir()

  call cursor(3, 8)
  let start = eclim#python#complete#CodeComplete(1, '')
  call vunit#AssertEquals(7, start, 'Wrong starting column.')

  let results = eclim#python#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  call vunit#AssertEquals(len(results), 10, 'Wrong number of results.')
  call vunit#AssertEquals('Test1', results[0].word, 'Wrong result.')
  call vunit#AssertEquals('t', results[0].kind, 'Wrong result.')
  call vunit#AssertEquals('Test2', results[1].word, 'Wrong result.')
  call vunit#AssertEquals('Test3', results[2].word, 'Wrong result.')

  call cursor(5, 9)
  let start = eclim#python#complete#CodeComplete(1, '')
  call vunit#AssertEquals(7, start, 'Wrong starting column.')

  let results = eclim#python#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  call vunit#AssertEquals(len(results), 3, 'Wrong number of results.')
  call vunit#AssertEquals('test1()', results[0].word, 'Wrong result.')
  call vunit#AssertEquals('f', results[0].kind, 'Wrong result.')
  call vunit#AssertEquals('test2(', results[1].word, 'Wrong result.')
  call vunit#AssertEquals('test3(', results[2].word, 'Wrong result.')
endfunction " }}}

" TestCompleteUnicode() {{{
function! TestCompleteUnicode()
  edit! test/complete/test_complete_unicode.py
  call vunit#PeekRedir()

  call cursor(6, 8)
  let start = eclim#python#complete#CodeComplete(1, '')
  call vunit#AssertEquals(7, start, 'Wrong starting column.')

  let results = eclim#python#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  call vunit#AssertEquals(len(results), 10, 'Wrong number of results.')
  call vunit#AssertEquals('Test1', results[0].word, 'Wrong result.')
  call vunit#AssertEquals('Test2', results[1].word, 'Wrong result.')
  call vunit#AssertEquals('Test3', results[2].word, 'Wrong result.')

  call cursor(8, 9)
  let start = eclim#python#complete#CodeComplete(1, '')
  call vunit#AssertEquals(7, start, 'Wrong starting column.')

  let results = eclim#python#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  call vunit#AssertEquals(len(results), 3, 'Wrong number of results.')
  call vunit#AssertEquals('test1()', results[0].word, 'Wrong result.')
  call vunit#AssertEquals('test2(', results[1].word, 'Wrong result.')
  call vunit#AssertEquals('test3(', results[2].word, 'Wrong result.')
endfunction " }}}

" vim:ft=vim:fdm=marker
