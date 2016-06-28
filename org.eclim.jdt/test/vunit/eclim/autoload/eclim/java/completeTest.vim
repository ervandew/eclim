" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for complete.vim
"
" License:
"
" Copyright (C) 2005 - 2016  Eric Van Dewoestine
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
  set completeopt-=preview
  let g:EclimJavaCompleteLayout = 'standard'
  call eclim#WaitOnRunningJobs(3000)
endfunction " }}}

function! TestCodeCompleteMissingImport() " {{{
  edit! src/org/eclim/test/complete/TestCompletionVUnit.java
  call vunit#PeekRedir()

  call vunit#AssertFalse(search('^import java\.util\.List;', 'n'),
    \ 'List already imported.')

  call cursor(10, 11)
  let start = eclim#java#complete#CodeComplete(1, '')
  call vunit#AssertEquals(9, start, 'Wrong starting column.')
  let g:EclimTestPromptQueue = [1] " choose java.util.List
  let result = eclim#java#complete#CodeComplete(0, '')
  call vunit#AssertTrue(result, -1, 'Wrong completion result.')
  call vunit#AssertTrue(search('^import java\.util\.List;', 'n'),
    \ 'List not imported.')
  call vunit#AssertEquals(line('.'), 11, 'Wrong line number after import.')
  call vunit#AssertEquals(col('.'), 11, 'Wrong col number after import.')
endfunction " }}}

function! TestCodeComplete() " {{{
  edit! src/org/eclim/test/complete/TestCompletionVUnit.java
  call vunit#PeekRedir()

  call cursor(17, 10)
  let start = eclim#java#complete#CodeComplete(1, '')
  call vunit#AssertEquals(9, start, 'Wrong starting column.')

  call cursor(11, 10)
  let start = eclim#java#complete#CodeComplete(1, '')
  call vunit#AssertEquals(9, start, 'Wrong starting column.')

  call cursor(17, 10)
  let results = eclim#java#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  call vunit#AssertTrue(len(results) > 1, 'Not enough results.')
  call vunit#AssertTrue(eclim#util#ListContains(results, ".*'add('.*"),
    \ 'Results does not contain add()')
  call vunit#AssertTrue(eclim#util#ListContains(results, ".*'contains('.*"),
    \ 'Results does not contain contains()')
  call vunit#AssertTrue(eclim#util#ListContains(results, ".*'isEmpty()'.*"),
    \ 'Results does not contain isEmpty()')
  call vunit#AssertTrue(eclim#util#ListContains(results, ".*'remove('.*"),
    \ 'Results does not contain remove()')

  call cursor(11, 10)
  let start = eclim#java#complete#CodeComplete(1, '')
  let results = eclim#java#complete#CodeComplete(0, 'a')
  call vunit#PeekRedir()
  echom 'Results: ' . string(results)
  call vunit#AssertTrue(len(results) > 1, 'Not enough results.')
  call vunit#AssertTrue(eclim#util#ListContains(results, ".*'add('.*"),
    \ 'Narrowed results does not contain add()')
  call vunit#AssertTrue(eclim#util#ListContains(results, ".*'addAll('.*"),
    \ 'Narrowed results does not contain addAll()')

  for result in results
    call vunit#AssertTrue(string(result) =~ "'word': 'a",
      \ 'Narrowed results contains result not starting with "a"')
  endfor
endfunction " }}}

function! TestCodeCompleteUnicode() " {{{
  edit! src/org/eclim/test/complete/TestUnicode.java
  call vunit#PeekRedir()

  call cursor(6, 18)
  let start = eclim#java#complete#CodeComplete(1, '')
  call vunit#AssertEquals(15, start, 'Wrong starting column.')

  call cursor(6, 18)
  let results = eclim#java#complete#CodeComplete(0, '')
  echom 'Results: ' . string(results)
  call vunit#PeekRedir()
  call vunit#AssertTrue(len(results) > 10, 'Not enough results.')
  call vunit#AssertTrue(len(results) < 30, 'Too many results.')
  call vunit#AssertTrue(eclim#util#ListContains(results, ".*'println'.*"),
    \ 'Results does not contain println()')
  call vunit#AssertFalse(eclim#util#ListContains(results, ".*'append'.*"),
    \ 'Results contains append()')

  " actually tests the unicode support of GetOffset
  call cursor(16, 34)
  let start = eclim#java#complete#CodeComplete(1, '')
  call vunit#AssertEquals(31, start, 'Wrong starting column.')

  call cursor(16, 34)
  let results = eclim#java#complete#CodeComplete(0, '')
  echom 'Results: ' . string(results)
  call vunit#PeekRedir()
  call vunit#AssertTrue(len(results) > 10, 'Not enough results.')
  call vunit#AssertTrue(len(results) < 30, 'Too many results.')
  call vunit#AssertTrue(eclim#util#ListContains(results, ".*'println'.*"),
    \ 'Results does not contain println()')
  call vunit#AssertFalse(eclim#util#ListContains(results, ".*'append'.*"),
    \ 'Results contains append()')
endfunction " }}}

function! TestCodeCompleteLinkedResource() " {{{
  edit! ../eclim_unit_test_java_linked/src/org/eclim/test/TestLinked.java
  call vunit#PeekRedir()

  call cursor(10, 10)
  let start = eclim#java#complete#CodeComplete(1, '')
  call vunit#PeekRedir()
  call vunit#AssertEquals(9, start, 'Wrong starting column.')

  call cursor(10, 10)
  let results = eclim#java#complete#CodeComplete(0, '')
  call vunit#PeekRedir()
  call vunit#AssertTrue(len(results) > 10, 'Not enough results (full complete).')
  call vunit#AssertTrue(len(results) < 60, 'Too many results (full complete).')
  call vunit#AssertTrue(eclim#util#ListContains(results, ".*'add('.*"),
    \ 'Results does not contain add()')
  call vunit#AssertTrue(eclim#util#ListContains(results, ".*'addAll('.*"),
    \ 'Results does not contain addAll()')
  call vunit#AssertTrue(eclim#util#ListContains(results, ".*'clear()'.*"),
    \ 'Results does not contain clear()')

  call cursor(16, 10)
  let start = eclim#java#complete#CodeComplete(1, '')
  let results = eclim#java#complete#CodeComplete(0, 'a')
  call vunit#PeekRedir()
  call vunit#AssertTrue(len(results) > 2, 'Not enough results (complete "a").')
  call vunit#AssertTrue(len(results) < 10, 'Too many results (complete "a").')
  call vunit#AssertTrue(eclim#util#ListContains(results, ".*'add('.*"),
    \ 'Results does not contain add()')
  call vunit#AssertTrue(eclim#util#ListContains(results, ".*'addAll('.*"),
    \ 'Results does not contain addAll()')
  call vunit#AssertFalse(eclim#util#ListContains(results, ".*'clear()'.*"),
    \ 'Results contain clear()')
endfunction " }}}

" vim:ft=vim:fdm=marker
