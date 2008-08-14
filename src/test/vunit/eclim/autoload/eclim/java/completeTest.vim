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
endfunction " }}}

" TestCodeCompleteUnicode() {{{
function! TestCodeCompleteUnicode ()
  edit! src/org/eclim/test/complete/TestUnicode.java
  call PeekRedir()

  call cursor(6, 17)
  let start = eclim#java#complete#CodeComplete(1, '')
  call VUAssertEquals(15, start, 'Wrong starting column.')

  call cursor(6, 17)
  let results = eclim#java#complete#CodeComplete(0, '')
  call PeekRedir()
  call VUAssertTrue(len(results) > 10, 'Not enough results.')
  call VUAssertTrue(len(results) < 30, 'Too many results.')
  call VUAssertTrue(eclim#util#ListContains(results, ".*'println('.*"),
    \ 'Results does not contain println()')
  call VUAssertFalse(eclim#util#ListContains(results, ".*'append('.*"),
    \ 'Results contains print()')

  " actually tests the unicode support of GetCharacterOffset
  call cursor(16, 33)
  let start = eclim#java#complete#CodeComplete(1, '')
  call VUAssertEquals(31, start, 'Wrong starting column.')

  call cursor(16, 33)
  let results = eclim#java#complete#CodeComplete(0, '')
  call PeekRedir()
  call VUAssertTrue(len(results) > 10, 'Not enough results.')
  call VUAssertTrue(len(results) < 30, 'Too many results.')
  call VUAssertTrue(eclim#util#ListContains(results, ".*'println('.*"),
    \ 'Results does not contain println()')
  call VUAssertFalse(eclim#util#ListContains(results, ".*'append('.*"),
    \ 'Results contains print()')
endfunction " }}}

" vim:ft=vim:fdm=marker
