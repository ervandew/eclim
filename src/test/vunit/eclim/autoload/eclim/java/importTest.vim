" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for import.vim
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

" TestImport() {{{
function! TestImport ()
  edit! src/org/eclim/test/include/TestImportVUnit.java
  call PeekRedir()

  call cursor(5, 11)
  JavaImport
  call VUAssertFalse(search('^import .*TestUnusedImportVUnit;'),
    \ 'TestUnusedImportVUnit imported.')

  call cursor(6, 11)
  JavaImport
  call VUAssertFalse(search('^import .*String;'), 'String imported.')

  call cursor(7, 11)
  JavaImport
  call VUAssertTrue(search('^import java\.util\.ArrayList;'),
    \ 'ArrayList not imported.')

  bdelete!
endfunction " }}}

" TestUnusedImport() {{{
function! TestUnusedImport ()
  edit! src/org/eclim/test/include/TestUnusedImportVUnit.java
  call PeekRedir()

  call VUAssertTrue(search('^import java\.util\.ArrayList;$'),
    \ 'ArrayList import not found.')
  call VUAssertTrue(search('^import java\.util\.List;$'),
    \ 'List import not found.')

  JavaImportClean

  call VUAssertFalse(search('^import java\.util\.ArrayList;$'),
    \ 'ArrayList import still found.')
  call VUAssertFalse(search('^import java\.util\.List;$'),
    \ 'List import still found.')

  bdelete!
endfunction " }}}

" vim:ft=vim:fdm=marker
