" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for util.vim
"
" License:
"
" Copyright (c) 2005 - 2006
"
" Licensed under the Apache License, Version 2.0 (the "License");
" you may not use this file except in compliance with the License.
" You may obtain a copy of the License at
"
"      http://www.apache.org/licenses/LICENSE-2.0
"
" Unless required by applicable law or agreed to in writing, software
" distributed under the License is distributed on an "AS IS" BASIS,
" WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
" See the License for the specific language governing permissions and
" limitations under the License.
"
" }}}

" TestGetCharacterOffset() {{{
function! TestGetCharacterOffset ()
  new
  call setline(1, ['one two', 'three four'])

  call cursor(1, 1)
  call VUAssertEquals(0, eclim#util#GetCharacterOffset())

  call cursor(2, 7)
  call VUAssertEquals(14, eclim#util#GetCharacterOffset())

  bdelete!
endfunction " }}}

" TestGetCurrentElementColumn() {{{
function! TestGetCurrentElementColumn ()
  new
  call setline(1, 'one two')

  call cursor(1, 1)
  call VUAssertEquals(1, eclim#util#GetCurrentElementColumn())

  call cursor(1, 3)
  call VUAssertEquals(1, eclim#util#GetCurrentElementColumn())

  call cursor(1, 4)
  call VUAssertEquals(5, eclim#util#GetCurrentElementColumn())

  bdelete!
endfunction " }}}

" TestGetCurrentElementPosition() {{{
function! TestGetCurrentElementPosition ()
  new
  call setline(1, ['one two', 'three four'])

  call cursor(1, 1)
  call VUAssertEquals('0;3', eclim#util#GetCurrentElementPosition())

  call cursor(2, 9)
  call VUAssertEquals('14;4', eclim#util#GetCurrentElementPosition())

  bdelete!
endfunction " }}}

" TestGetCurrentElementOffset() {{{
function! TestGetCurrentElementOffset ()
  new
  call setline(1, ['one two', 'three four'])

  call cursor(1, 2)
  call VUAssertEquals('0', eclim#util#GetCurrentElementOffset())

  call cursor(2, 9)
  call VUAssertEquals('14', eclim#util#GetCurrentElementOffset())

  bdelete!
endfunction " }}}

" TestGrabUri() {{{
function! TestGrabUri ()
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

  bdelete!
endfunction " }}}

" TestListContains() {{{
function! TestListContains ()
  let list = ['one', 'two', 'three']

  call VUAssertTrue(eclim#util#ListContains(list, 'two'))
  call VUAssertTrue(eclim#util#ListContains(list, 't.*'))
  call VUAssertFalse(eclim#util#ListContains(list, 'four'))
  call VUAssertFalse(eclim#util#ListContains(list, '.*t'))
endfunction " }}}

" TestParseArgs() {{{
function! TestParseArgs ()
  call VUAssertEquals(['one', 'two', 'three'],
    \ eclim#util#ParseArgs('one two three'))
  call VUAssertEquals(['one\ two', 'three'],
    \ eclim#util#ParseArgs('one\ two three'))
endfunction " }}}

" TestSimplify() {{{
function! TestSimplify ()
  let file = 'file:///blah\duh\ foo\bar'
  call VUAssertEquals('file:///blah/duh\ foo/bar', eclim#util#Simplify(file))

  exec 'cd ' . g:TestEclimWorkspace

  let file = g:TestEclimWorkspace . 'eclim_unit_test_java/build.xml'
  call VUAssertEquals('eclim_unit_test_java/build.xml', eclim#util#Simplify(file))

  "cd eclim_unit_test_java
  "let file = '../eclim_unit_test_java/build.xml'
  "call VUAssertEquals('build.xml', eclim#util#Simplify(file))
endfunction " }}}

" vim:ft=vim:fdm=marker
