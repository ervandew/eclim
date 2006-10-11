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

" TestCodeComplete() {{{
function! TestCodeComplete ()
  edit! src/org/eclim/test/complete/TestCompletionVUnit.java
  call PeekRedir()

  call cursor(17, 10)
  let start = eclim#java#complete#CodeComplete(1, '')
  call VUAssertEquals(9, start, 'Wrong starting column.')

  call cursor(11, 10)
  let start = eclim#java#complete#CodeComplete(1, '')
  call VUAssertEquals(9, start, 'Wrong starting column.')

  call cursor(17, 10)
  let results = eclim#java#complete#CodeComplete(0, '')
  call PeekRedir()
  call VUAssertTrue(len(results) > 1, 'Not enough results.')
  call VUAssertTrue(eclim#util#ListContains(results, ".*'add('.*"),
    \ 'Results does not contain add()')
  call VUAssertTrue(eclim#util#ListContains(results, ".*'contains('.*"),
    \ 'Results does not contain contains()')
  call VUAssertTrue(eclim#util#ListContains(results, ".*'isEmpty()'.*"),
    \ 'Results does not contain isEmpty()')
  call VUAssertTrue(eclim#util#ListContains(results, ".*'remove('.*"),
    \ 'Results does not contain remove()')

  call cursor(11, 10)
  " vim deletes the text back to the base after the first call to the
  " completefunc, simulate that here.
  normal x
  let results = eclim#java#complete#CodeComplete(0, 'a')
  call PeekRedir()
  call VUAssertTrue(len(results) > 1, 'Not enough results.')
  call VUAssertTrue(eclim#util#ListContains(results, ".*'add'.*"),
    \ 'Narrowed results does not contain add()')
  call VUAssertTrue(eclim#util#ListContains(results, ".*'addAll'.*"),
    \ 'Narrowed results does not contain addAll()')

  for result in results
    call VUAssertTrue(string(result) =~ "'word': 'a",
      \ 'Narrowed results contains result not starting with "a"')
  endfor

  bdelete!
endfunction " }}}

" vim:ft=vim:fdm=marker
