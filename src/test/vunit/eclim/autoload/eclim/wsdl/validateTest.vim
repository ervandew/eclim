" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for validate.vim
"
" License:
"
" Copyright (C) 2005 - 2010  Eric Van Dewoestine
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
function! SetUp()
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_web'
endfunction " }}}

" TestValidate() {{{
function! TestValidate()
  edit! wsdl/GoogleSearch.wsdl
  write
  call PeekRedir()

  let errors = getloclist(0)
  call VUAssertEquals(3, len(errors))

  let name = substitute(bufname(errors[0].bufnr), '\', '/', 'g')
  call VUAssertEquals(name, 'wsdl/GoogleSearch.wsdl', 'error 0 name')
  call VUAssertEquals(13, errors[0].lnum, 'error 0 line')
  call VUAssertEquals(44, errors[0].col, 'error 0 col')
  call VUAssertEquals('w', errors[0].type, 'error 0 type')

  let name = substitute(bufname(errors[1].bufnr), '\', '/', 'g')
  call VUAssertEquals(name, 'wsdl/GoogleSearch.wsdl', 'error 1 name')

  " weird line/col results on windows for some reason
  if has('win32') || has('win64')
    call VUAssertEquals(2, errors[1].lnum, 'error 1 line')
    call VUAssertEquals(8, errors[1].col, 'error 1 col')
    call VUAssertEquals('e', errors[1].type, 'error 1 type')
  else
    call VUAssertEquals(14, errors[1].lnum, 'error 1 line')
    call VUAssertEquals(14, errors[1].col, 'error 1 col')
    call VUAssertEquals('e', errors[1].type, 'error 1 type')
  endif

  let name = substitute(bufname(errors[2].bufnr), '\', '/', 'g')
  call VUAssertEquals(name, 'wsdl/GoogleSearch.wsdl', 'error 2 name')

  " weird line/col results on windows for some reason
  if has('win32') || has('win64')
    call VUAssertEquals(29, errors[2].lnum, 'error 2 line')
    call VUAssertEquals(39, errors[2].col, 'error 2 col')
    call VUAssertEquals('e', errors[2].type, 'error 2 type')
  else
    call VUAssertEquals(48, errors[2].lnum, 'error 2 line')
    call VUAssertEquals(49, errors[2].col, 'error 2 col')
    call VUAssertEquals('e', errors[2].type, 'error 2 type')
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
