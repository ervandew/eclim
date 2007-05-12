" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for complete.vim
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
