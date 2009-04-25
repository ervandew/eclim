" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for util.vim
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

" TestGetOffset() {{{
function! TestGetOffset()
  new
  call setline(1, ['one two', 'three four'])

  call cursor(1, 1)
  call VUAssertEquals(0, eclim#util#GetOffset())

  call cursor(2, 7)
  call VUAssertEquals(14, eclim#util#GetOffset())
endfunction " }}}

" TestGetCurrentElementColumn() {{{
function! TestGetCurrentElementColumn()
  new
  call setline(1, 'one two')

  call cursor(1, 1)
  call VUAssertEquals(1, eclim#util#GetCurrentElementColumn())

  call cursor(1, 3)
  call VUAssertEquals(1, eclim#util#GetCurrentElementColumn())

  call cursor(1, 4)
  call VUAssertEquals(5, eclim#util#GetCurrentElementColumn())
endfunction " }}}

" TestGetCurrentElementPosition() {{{
function! TestGetCurrentElementPosition()
  new
  call setline(1, ['one two', 'three four'])

  call cursor(1, 1)
  call VUAssertEquals('0;3', eclim#util#GetCurrentElementPosition())

  call cursor(2, 9)
  call VUAssertEquals('14;4', eclim#util#GetCurrentElementPosition())
endfunction " }}}

" TestGetCurrentElementOffset() {{{
function! TestGetCurrentElementOffset()
  new
  call setline(1, ['one two', 'three four'])

  call cursor(1, 2)
  call VUAssertEquals('0', eclim#util#GetCurrentElementOffset())

  call cursor(2, 9)
  call VUAssertEquals('14', eclim#util#GetCurrentElementOffset())
endfunction " }}}

" TestGrabUri() {{{
function! TestGrabUri()
  new
  call setline(1, [
      \ '"http://www.google.com?q=blah"',
      \ "'http://www.google.com?q=blah'",
      \ "<http://www.google.com?q=blah>",
      \ "(http://www.google.com?q=blah)",
      \ "{http://www.google.com?q=blah}",
      \ "[http://www.google.com?q=blah]",
      \ " http://www.google.com?q=blah ",
      \ "http://www.google.com?q=blah",
      \ "<url>http://www.google.com?q=blah<url>",
    \])

  call cursor(1, 3)
  call VUAssertEquals('http://www.google.com?q=blah', eclim#util#GrabUri())

  call cursor(2, 20)
  call VUAssertEquals('http://www.google.com?q=blah', eclim#util#GrabUri())

  call cursor(3, 28)
  call VUAssertEquals('http://www.google.com?q=blah', eclim#util#GrabUri())

  call cursor(4, 6)
  call VUAssertEquals('http://www.google.com?q=blah', eclim#util#GrabUri())

  call cursor(5, 2)
  call VUAssertEquals('http://www.google.com?q=blah', eclim#util#GrabUri())

  call cursor(6, 23)
  call VUAssertEquals('http://www.google.com?q=blah', eclim#util#GrabUri())

  call cursor(7, 5)
  call VUAssertEquals('http://www.google.com?q=blah', eclim#util#GrabUri())

  call cursor(8, 8)
  call VUAssertEquals('http://www.google.com?q=blah', eclim#util#GrabUri())

  call cursor(9, 11)
  call VUAssertEquals('http://www.google.com?q=blah', eclim#util#GrabUri())
endfunction " }}}

" TestListContains() {{{
function! TestListContains()
  let list = ['one', 'two', 'three']

  call VUAssertTrue(eclim#util#ListContains(list, 'two'))
  call VUAssertTrue(eclim#util#ListContains(list, 't.*'))
  call VUAssertFalse(eclim#util#ListContains(list, 'four'))
  call VUAssertFalse(eclim#util#ListContains(list, '.*t'))
endfunction " }}}

" TestParseArgs() {{{
function! TestParseArgs()
  call VUAssertEquals(['one', 'two', 'three'],
    \ eclim#util#ParseArgs('one two three'))
  call VUAssertEquals(['one two', 'three'],
    \ eclim#util#ParseArgs('"one two" three'))
  call VUAssertEquals(['one two', 'three'],
    \ eclim#util#ParseArgs('one\ two three'))
  call VUAssertEquals(['"one two"', 'three'],
    \ eclim#util#ParseArgs("'\"one two\"' three"))
endfunction " }}}

" TestParseCmdLine() {{{
function! TestParseCmdLine()
  call VUAssertEquals(['one', 'two', 'three'],
    \ eclim#util#ParseCmdLine('one two three'))
  call VUAssertEquals(['one\ two', 'three'],
    \ eclim#util#ParseCmdLine('one\ two three'))
endfunction " }}}

" TestSimplify() {{{
function! TestSimplify()
  let file = 'file:///blah\duh\ foo\bar'
  call VUAssertEquals('file:///blah/duh\ foo/bar', eclim#util#Simplify(file))

  exec 'cd ' . g:TestEclimWorkspace

  let file = g:TestEclimWorkspace . 'eclim_unit_test/test_root_file.txt'
  call VUAssertEquals('eclim_unit_test/test_root_file.txt', eclim#util#Simplify(file))
endfunction " }}}

" vim:ft=vim:fdm=marker
