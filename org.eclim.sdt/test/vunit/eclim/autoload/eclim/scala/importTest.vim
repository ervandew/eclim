" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2014  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_scala'
endfunction " }}}

function! TestImport() " {{{
  edit! src/eclim/test/include/TestImportVUnit.scala
  call vunit#PeekRedir()

  call cursor(4, 14)
  let g:EclimTestPromptQueue = [1] " choose java.util.List
  ScalaImport
  call vunit#AssertTrue(search('^import java\.util\.List', 'n'),
    \ 'List not imported.')

  call search('TestScala')
  ScalaImport
  call vunit#AssertTrue(search('^import eclim\.test\.TestScala', 'n'),
    \ 'TestScala not imported.')
endfunction " }}}

" vim:ft=vim:fdm=marker
