" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for import.vim
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
