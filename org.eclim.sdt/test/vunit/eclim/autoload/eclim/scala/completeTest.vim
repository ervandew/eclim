" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for complete.vim
"
" License:
"
" Copyright (C) 2011 - 2012  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_scala'
  set completeopt-=preview
endfunction " }}}

" TestCodeComplete() {{{
function! TestCodeComplete()
  edit! src/eclim/test/complete/TestComplete.scala
  call vunit#PeekRedir()

  call cursor(9, 11)
  let start = eclim#scala#complete#CodeComplete(1, '')
  call vunit#AssertEquals(9, start, 'Wrong starting column.')

  call cursor(9, 11)
  let results = eclim#scala#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  call vunit#AssertTrue(len(results) > 3, 'Not enough results.')
  call vunit#AssertEquals(results[0].word, 'scalaMethod1',
    \ 'Results does not contain scalaMethod1')
  call vunit#AssertEquals(results[1].word, 'scalaMethod2(',
    \ 'Results does not contain scalaMethod2')
  call vunit#AssertEquals(results[2].word, 'scalaMethod3(',
    \ 'Results does not contain scalaMethod3')

  call cursor(14, 11)
  let start = eclim#scala#complete#CodeComplete(1, '')
  call vunit#AssertEquals(9, start, 'Wrong starting column.')

  call cursor(14, 11)
  let results = eclim#scala#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  call vunit#AssertEquals(len(results), 3, 'Not enough results.')
  call vunit#AssertEquals(results[0].word, 'javaMethod1',
    \ 'Results does not contain javaMethod1')
  call vunit#AssertEquals(results[1].word, 'javaMethod2(',
    \ 'Results does not contain javaMethod2')
  call vunit#AssertEquals(results[2].word, 'javaMethod3(',
    \ 'Results does not contain javaMethod3')
endfunction " }}}

" vim:ft=vim:fdm=marker
