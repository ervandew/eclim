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

" TestCompleteXsd() {{{
function! TestCompleteXsd()
  edit! xsd/test.xsd
  call PeekRedir()

  call cursor(11, 11)
  let start = eclim#xml#complete#CodeComplete(1, '')
  call VUAssertEquals(8, start, 'Wrong starting column.')

  let results = eclim#xml#complete#CodeComplete(0, '')
  call PeekRedir()
  echo string(results)
  call VUAssertTrue(len(results) == 1, 'Wrong number of results.')
  call VUAssertTrue(eclim#util#ListContains(results, ".*'unique'.*"),
    \ 'Results does not contain xs:unique')
endfunction " }}}

" vim:ft=vim:fdm=marker
