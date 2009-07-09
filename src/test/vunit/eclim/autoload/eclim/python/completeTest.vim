" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for complete.vim
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_python'
endfunction " }}}

" TestComplete() {{{
function! TestComplete()
  edit! test/complete/test_complete.py
  call PeekRedir()

  call cursor(3, 8)
  let start = eclim#python#complete#CodeComplete(1, '')
  call VUAssertEquals(7, start, 'Wrong starting column.')

  let results = eclim#python#complete#CodeComplete(0, '')
  call PeekRedir()
  call VUAssertEquals(len(results), 10, 'Wrong number of results.')
  call VUAssertEquals('Test1', results[0].word, 'Wrong result.')
  call VUAssertEquals('Test2', results[1].word, 'Wrong result.')
  call VUAssertEquals('Test3', results[2].word, 'Wrong result.')

  call cursor(5, 9)
  let start = eclim#python#complete#CodeComplete(1, '')
  call VUAssertEquals(7, start, 'Wrong starting column.')

  let results = eclim#python#complete#CodeComplete(0, '')
  call PeekRedir()
  call VUAssertEquals(len(results), 3, 'Wrong number of results.')
  call VUAssertEquals('test1', results[0].word, 'Wrong result.')
  call VUAssertEquals('test2', results[1].word, 'Wrong result.')
  call VUAssertEquals('test3', results[2].word, 'Wrong result.')
endfunction " }}}

" TestCompleteUnicode() {{{
function! TestCompleteUnicode()
  edit! test/complete/test_complete_unicode.py
  call PeekRedir()

  call cursor(6, 8)
  let start = eclim#python#complete#CodeComplete(1, '')
  call VUAssertEquals(7, start, 'Wrong starting column.')

  let results = eclim#python#complete#CodeComplete(0, '')
  call PeekRedir()
  call VUAssertEquals(len(results), 10, 'Wrong number of results.')
  call VUAssertEquals('Test1', results[0].word, 'Wrong result.')
  call VUAssertEquals('Test2', results[1].word, 'Wrong result.')
  call VUAssertEquals('Test3', results[2].word, 'Wrong result.')

  call cursor(8, 9)
  let start = eclim#python#complete#CodeComplete(1, '')
  call VUAssertEquals(7, start, 'Wrong starting column.')

  let results = eclim#python#complete#CodeComplete(0, '')
  call PeekRedir()
  call VUAssertEquals(len(results), 3, 'Wrong number of results.')
  call VUAssertEquals('test1', results[0].word, 'Wrong result.')
  call VUAssertEquals('test2', results[1].word, 'Wrong result.')
  call VUAssertEquals('test3', results[2].word, 'Wrong result.')
endfunction " }}}

" vim:ft=vim:fdm=marker
