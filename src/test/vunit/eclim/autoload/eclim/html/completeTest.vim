" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for complete.vim
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
endfunction " }}}

" TestCompleteAttribute() {{{
function! TestCompleteAttribute ()
  edit! html/test.html
  call PeekRedir()

  call cursor(7, 9)
  let start = eclim#html#complete#CodeComplete(1, '')
  call VUAssertEquals(7, start, 'Wrong starting column.')

  let results = eclim#html#complete#CodeComplete(0, '')
  call PeekRedir()
  echo 'results = ' . string(results)
  call VUAssertTrue(len(results) == 2, 'Wrong number of results.')
  call VUAssertTrue(eclim#util#ListContains(results, ".*href.*"),
    \ 'Results does not contain href')
endfunction " }}}

" TestCompleteElement() {{{
function! TestCompleteElement ()
  edit! html/test.html
  call PeekRedir()

  call cursor(5, 7)
  let start = eclim#html#complete#CodeComplete(1, '')
  call VUAssertEquals(5, start, 'Wrong starting column.')

  let results = eclim#html#complete#CodeComplete(0, '')
  call PeekRedir()
  echo 'results = ' . string(results)
  call VUAssertTrue(len(results) == 7, 'Wrong number of results.')
  call VUAssertTrue(eclim#util#ListContains(results, ".*h1.*"),
    \ 'Results does not contain h1')
  call VUAssertTrue(eclim#util#ListContains(results, ".*h2.*"),
    \ 'Results does not contain h2')
endfunction " }}}

" TestCompleteCss() {{{
function! TestCompleteCss ()
  edit! html/test.html
  call PeekRedir()

  call cursor(4, 20)
  let start = eclim#html#complete#CodeComplete(1, '')
  call VUAssertEquals(15, start, 'Wrong starting column.')

  let results = eclim#html#complete#CodeComplete(0, '')
  call PeekRedir()
  echo 'results = ' . string(results)
  call VUAssertTrue(len(results) == 8, 'Wrong number of results.')
  call VUAssertTrue(eclim#util#ListContains(results, ".*font.*"),
    \ 'Results does not contain font')
  call VUAssertTrue(eclim#util#ListContains(results, ".*font-family.*"),
    \ 'Results does not contain font-family')
  call VUAssertTrue(eclim#util#ListContains(results, ".*font-size.*"),
    \ 'Results does not contain font-size')
  call VUAssertTrue(eclim#util#ListContains(results, ".*font-weight.*"),
    \ 'Results does not contain font-weight')
endfunction " }}}

" vim:ft=vim:fdm=marker
