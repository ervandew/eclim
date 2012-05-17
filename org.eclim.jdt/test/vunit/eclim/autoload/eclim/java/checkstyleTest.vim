" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for bean.vim
"
" License:
"
" Copyright (C) 2005 - 2012  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
endfunction " }}}

function! TestManual() " {{{
  let g:EclimJavaCheckstyleOnSave = 0
  edit! src/org/eclim/test/checkstyle/TestCheckstyleVUnit.java
  call vunit#PeekRedir()

  Checkstyle
  call vunit#PeekRedir()

  let results = getloclist(0)
  call vunit#AssertEquals(len(results), 3)
  for result in results
    call vunit#AssertEquals(bufname(result.bufnr), expand('%'), 'File does not match.')
    call vunit#AssertTrue(result.text =~ '^\[checkstyle\]', 'Missing namespace.')
  endfor

  call setloclist(0, [], 'r')
  let results = getloclist(0)
  call vunit#AssertEquals(len(results), 0)
endfunction " }}}

function! TestAuto() " {{{
  let g:EclimLogLevel = 10
  let g:EclimJavaCheckstyleOnSave = 1
  edit! src/org/eclim/test/checkstyle/TestCheckstyleVUnit.java
  call vunit#PeekRedir()

  call histadd('cmd', 'write') | write
  call vunit#PeekRedir()
  let g:EclimLogLevel = 4

  let results = getloclist(0)
  call vunit#AssertEquals(len(results), 3)
  for result in results
    call vunit#AssertEquals(bufname(result.bufnr), expand('%'))
    call vunit#AssertTrue(result.text =~ '^\[checkstyle\]', 'Missing namespace.')
  endfor

  call setloclist(0, [], 'r')
  let results = getloclist(0)
  call vunit#AssertEquals(len(results), 0)
endfunction " }}}

" vim:ft=vim:fdm=marker
