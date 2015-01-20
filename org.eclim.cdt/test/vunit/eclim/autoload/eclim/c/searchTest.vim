" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2005 - 2015  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_c'
endfunction " }}}

function! TestFindInclude() " {{{
  edit! src/test_search_vunit.c
  call vunit#PeekRedir()

  call cursor(1, 14)
  :CSearchContext
  call vunit#PeekRedir()

  let name = bufname('%')
  if has('win32') || has('win64')
    call vunit#AssertTrue(name =~ '\\include\\stdio\.h', 'Wrong result file: ' . name)
  else
    call vunit#AssertEquals(name, '/usr/include/stdio.h', 'Wrong result file.')
  endif
  bdelete

  call cursor(3, 14)
  :CSearchContext
  call vunit#PeekRedir()

  let name = substitute(bufname('%'), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test.h', 'Wrong result file.')
endfunction " }}}

function! TestSearchElement() " {{{
  edit! src/test_search_vunit.c
  call vunit#PeekRedir()

  " EXIT_SUCCESS
  call cursor(13, 13)
  :CSearchContext
  call vunit#PeekRedir()

  let name = bufname('%')
  if has('win32') || has('win64')
    call vunit#AssertTrue(name =~ '\\include\\stdlib\.h', 'Wrong result file: ' . name)
  else
    call vunit#AssertEquals(name, '/usr/include/stdlib.h', 'Wrong result file.')
  endif
  call vunit#AssertTrue(getline('.') =~ '#define\s\+EXIT_SUCCESS', 'Wrong line: ' . getline('.'))
  bdelete

  " puts
  call cursor(12, 3)
  :CSearchContext
  call vunit#PeekRedir()

  let name = bufname('%')
  if has('win32') || has('win64')
    call vunit#AssertTrue(name =~ '\\include\\stdio\.h', 'Wrong result file: ' . name)
  else
    call vunit#AssertEquals(name, '/usr/include/stdio.h', 'Wrong result file.')
  endif
  call vunit#AssertTrue(getline('.') =~ 'extern\s\+int\s\+puts\>', 'Wrong line: ' . getline('.'))
  bdelete

  " testFunction (definition)
  call cursor(11, 7)
  :CSearchContext
  call vunit#PeekRedir()

  let name = substitute(bufname('%'), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test.c', '(def) Wrong result file.')
  call vunit#AssertEquals(line('.'), 1, '(def) Wrong line number.')
  call vunit#AssertEquals(col('.'), 6, '(def) Wrong line number.')
  bdelete

  " testFunction (declaration)
  call cursor(11, 7)
  :CSearch -x declarations
  call vunit#PeekRedir()

  let name = substitute(bufname('%'), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test.h', '(dec) Wrong result file.')
  call vunit#AssertEquals(line('.'), 4, '(dec) Wrong line number.')
  call vunit#AssertEquals(col('.'), 6, '(dec) Wrong line number.')
  bdelete

  " testFunction (references)
  call cursor(11, 7)
  :CSearch -x references
  call vunit#PeekRedir()

  let results = getqflist()
  echo string(results)
  call vunit#AssertEquals(2, len(results), '(ref) Wrong number of results.')
  let name = substitute(bufname(results[0].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test_search_vunit.c', '(ref0) Wrong result file.')
  call vunit#AssertEquals(results[0].lnum, 11, '(ref0) Wrong line number.')
  call vunit#AssertEquals(results[0].col, 3, '(ref0) Wrong line number.')

  let name = substitute(bufname(results[1].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test_search.c', '(ref1) Wrong result file.')
  call vunit#AssertEquals(results[1].lnum, 11, '(ref1) Wrong line number.')
  call vunit#AssertEquals(results[1].col, 3, '(ref1) Wrong line number.')
  bdelete

  " testFunction (all)
  call cursor(11, 7)
  :CSearch -x all
  call vunit#PeekRedir()

  let results = getqflist()
  echo string(results)
  call vunit#AssertEquals(4, len(results), '(all) Wrong number of results.')
  let name = substitute(bufname(results[0].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test.h', '(all0) Wrong result file.')
  call vunit#AssertEquals(results[0].lnum, 4, '(all0) Wrong line number.')
  call vunit#AssertEquals(results[0].col, 6, '(all0) Wrong line number.')

  let name = substitute(bufname(results[1].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test.c', '(all1) Wrong result file.')
  call vunit#AssertEquals(results[1].lnum, 1, '(all1) Wrong line number.')
  call vunit#AssertEquals(results[1].col, 6, '(all1) Wrong line number.')

  let name = substitute(bufname(results[2].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test_search_vunit.c', '(all2) Wrong result file.')
  call vunit#AssertEquals(results[2].lnum, 11, '(all2) Wrong line number.')
  call vunit#AssertEquals(results[2].col, 3, '(all2) Wrong line number.')

  let name = substitute(bufname(results[3].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test_search.c', '(all3) Wrong result file.')
  call vunit#AssertEquals(results[3].lnum, 11, '(all3) Wrong line number.')
  call vunit#AssertEquals(results[3].col, 3, '(all3) Wrong line number.')
  bdelete
endfunction " }}}

function! TestSearchFunction() " {{{
  edit! src/test_search_vunit.c
  call vunit#PeekRedir()

  :CSearch -p test_search_vunit_function -t function
  call vunit#PeekRedir()

  let name = substitute(bufname('%'), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test_search_vunit.c', 'Wrong result file.')
  call vunit#AssertEquals(line('.'), 16, 'Wrong line number.')
  call vunit#AssertEquals(col('.'), 5, 'Wrong line number.')
endfunction " }}}

function! TestSearchStruct() " {{{
  edit! src/test_search_vunit.c
  call vunit#PeekRedir()

  :CSearch -p test_search_vunit_struct -t class_struct
  call vunit#PeekRedir()

  let name = substitute(bufname('%'), '\', '/', 'g')
  call vunit#AssertEquals(name, 'src/test_search_vunit.c', 'Wrong result file.')
  call vunit#AssertEquals(line('.'), 5, 'Wrong line number.')
  call vunit#AssertEquals(col('.'), 8, 'Wrong line number.')
endfunction " }}}

" vim:ft=vim:fdm=marker
