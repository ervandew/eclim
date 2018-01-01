" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for java/new.vim
"
" License:
"
" Copyright (C) 2014 - 2017  Eric Van Dewoestine
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

function! TestJavaNewExistingPackage() " {{{
  edit! src/org/eclim/test/TestMain.java
  call vunit#PeekRedir()
  call s:Delete('src/org/eclim/test/TestNew.java')

  call vunit#AssertEquals(winnr('$'), 1, 'too many initial windows.')

  let g:EclimTestPromptQueue = [0]
  JavaNew class org.eclim.test.TestNew

  call vunit#AssertEquals(winnr('$'), 2, 'new window not opened.')

  call vunit#AssertEquals(
    \ bufname('%'), 'src/org/eclim/test/TestNew.java',
    \ 'wrong new class filename.')
endfunction " }}}

function! TestJavaNewChildPackage() " {{{
  edit! src/org/eclim/test/TestMain.java
  call vunit#PeekRedir()
  call s:Delete('src/org/eclim/test/testnew')

  call vunit#AssertEquals(winnr('$'), 1, 'too many initial windows.')
  call vunit#AssertFalse(
    \ isdirectory('src/org/eclim/test/testnew'),
    \ 'package dir already exists.')

  let g:EclimTestPromptQueue = [0]
  JavaNew class org.eclim.test.testnew.TestNew

  call vunit#AssertEquals(winnr('$'), 2, 'new window not opened.')
  call vunit#AssertTrue(
    \ isdirectory('src/org/eclim/test/testnew'),
    \ 'package dir not created.')

  call vunit#AssertEquals(
    \ bufname('%'), 'src/org/eclim/test/testnew/TestNew.java',
    \ 'wrong new class filename.')
endfunction " }}}

function! TestJavaNewParentPackage() " {{{
  edit! src/org/eclim/test/TestMain.java
  call s:Delete('src/testnew/test')
  call vunit#PeekRedir()

  call vunit#AssertEquals(winnr('$'), 1, 'too many initial windows.')
  call vunit#AssertFalse(
    \ isdirectory('src/testnew/test'),
    \ 'package dir already exists.')

  let g:EclimTestPromptQueue = [0]
  JavaNew class testnew.test.TestNew

  call vunit#AssertEquals(winnr('$'), 2, 'new window not opened.')
  call vunit#AssertTrue(
    \ isdirectory('src/testnew/test'),
    \ 'package dir not created.')

  call vunit#AssertEquals(
    \ bufname('%'), 'src/testnew/test/TestNew.java',
    \ 'wrong new class filename.')
endfunction " }}}

function! s:Delete(file) " {{{
  if isdirectory(a:file)
    call system('rm -r ' . a:file)
  else
    call delete(a:file)
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
