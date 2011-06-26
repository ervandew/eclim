" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for complete.vim
"
" License:
"
" Copyright (C) 2005 - 2011  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_php'
endfunction " }}}

" TestCompletePhp() {{{
function! TestCompletePhp()
  edit! php/complete/test.php
  call vunit#PeekRedir()

  call cursor(16, 13)
  let start = eclim#php#complete#CodeComplete(1, '')
  call vunit#AssertEquals(12, start, 'Wrong starting column.')

  let results = eclim#php#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  echo 'results = ' . string(results)
  call vunit#AssertEquals(len(results), 3, 'Wrong number of results.')
  call vunit#AssertEquals('methodA1(', results[0].word, 'Wrong result.')
  call vunit#AssertEquals('methodA2()', results[1].word, 'Wrong result.')
  call vunit#AssertEquals('variable1', results[2].word, 'Wrong result.')
endfunction " }}}

" TestCompleteCss() {{{
function! TestCompleteCss()
  edit! php/complete/test.php
  call vunit#PeekRedir()

  call cursor(5, 13)
  let start = eclim#html#complete#CodeComplete(1, '')
  call vunit#AssertEquals(8, start, 'Wrong starting column.')

  let results = eclim#html#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  echo 'results = ' . string(results)
  call vunit#AssertEquals(len(results), 8, 'Wrong number of results.')
  call vunit#AssertTrue(eclim#util#ListContains(results, ".*font.*"),
    \ 'Results does not contain font')
  call vunit#AssertTrue(eclim#util#ListContains(results, ".*font-family.*"),
    \ 'Results does not contain font-family')
  call vunit#AssertTrue(eclim#util#ListContains(results, ".*font-size.*"),
    \ 'Results does not contain font-size')
  call vunit#AssertTrue(eclim#util#ListContains(results, ".*font-weight.*"),
    \ 'Results does not contain font-weight')
endfunction " }}}

" html completion doesn't work on headless eclimd on windows due to hang in
" native method call.
if has('win32') || has('win64')
  finish
endif

" TestCompleteHtml() {{{
function! TestCompleteHtml()
  edit! php/complete/test.php
  call vunit#PeekRedir()

  call cursor(22, 7)
  let start = eclim#php#complete#CodeComplete(1, '')
  call vunit#AssertEquals(5, start, 'Wrong starting column.')

  let results = eclim#php#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  echo 'results = ' . string(results)
  call vunit#AssertEquals(len(results), 9, 'Wrong number of results.')
  call vunit#AssertEquals('h1', results[0].word, 'Wrong result.')
  call vunit#AssertEquals('h2', results[1].word, 'Wrong result.')
  call vunit#AssertEquals('h3', results[2].word, 'Wrong result.')
  call vunit#AssertEquals('h4', results[3].word, 'Wrong result.')
  call vunit#AssertEquals('h5', results[4].word, 'Wrong result.')
  call vunit#AssertEquals('h6', results[5].word, 'Wrong result.')
  call vunit#AssertEquals('header', results[6].word, 'Wrong result.')
  call vunit#AssertEquals('hgroup', results[7].word, 'Wrong result.')
  call vunit#AssertEquals('hr', results[8].word, 'Wrong result.')
endfunction " }}}

" vim:ft=vim:fdm=marker
