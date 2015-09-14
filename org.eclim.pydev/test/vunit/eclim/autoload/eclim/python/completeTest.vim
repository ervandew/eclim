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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_python'
endfunction " }}}

function! TestComplete() " {{{
  edit! test/complete/test_complete.py
  call s:TestComplete(3)
endfunction " }}}

function! TestCompleteUnicode() " {{{
  edit! test/complete/test_complete_unicode.py
  call s:TestComplete(6)
endfunction " }}}

function! s:TestComplete(line) " {{{
  call vunit#PeekRedir()

  " complete before the 't'
  call cursor(a:line, 8)
  let start = eclim#python#complete#CodeComplete(1, '')
  call vunit#AssertEquals(7, start, 'Wrong starting column.')

  let results = eclim#python#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  call vunit#AssertEquals(len(results), 12, 'Wrong number of results.')
  call vunit#AssertEquals('__dict__', results[0].word, 'Wrong result at 0.')
  call vunit#AssertEquals('__file__', results[1].word, 'Wrong result at 1.')
  call vunit#AssertEquals('__name__', results[2].word, 'Wrong result at 2.')
  call vunit#AssertEquals('__path__', results[3].word, 'Wrong result at 3.')
  call vunit#AssertEquals('functions', results[4].word, 'Wrong result at 4.')
  call vunit#AssertEquals('objects', results[5].word, 'Wrong result at 5.')
  call vunit#AssertEquals('Test1', results[6].word, 'Wrong result at 6.')
  call vunit#AssertEquals('test1()', results[7].word, 'Wrong result at 7.')
  call vunit#AssertEquals('Test2', results[8].word, 'Wrong result at 8.')
  call vunit#AssertEquals('test2(', results[9].word, 'Wrong result at 9.')
  call vunit#AssertEquals('Test3', results[10].word, 'Wrong result at 10.')
  call vunit#AssertEquals('test3(', results[11].word, 'Wrong result at 11.')

  " complete after the 't'
  call cursor(a:line, 9)
  let start = eclim#python#complete#CodeComplete(1, '')
  call vunit#AssertEquals(7, start, 'Wrong starting column.')

  let results = eclim#python#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  call vunit#AssertEquals(len(results), 3, 'Wrong number of results.')
  call vunit#AssertEquals('test1()', results[0].word, 'Wrong result at 0.')
  call vunit#AssertEquals('test2(', results[1].word, 'Wrong result at 2.')
  call vunit#AssertEquals('test3(', results[2].word, 'Wrong result at 3.')
endfunction " }}}

" vim:ft=vim:fdm=marker
