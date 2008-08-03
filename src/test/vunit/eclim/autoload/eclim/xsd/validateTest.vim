" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for validate.vim
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_web'
endfunction " }}}

" TestValidate() {{{
function! TestValidate ()
  edit! xsd/test.xsd
  write
  call PeekRedir()
  for line in readfile(expand('%'))
    echo '|' . line
  endfor

  let errors = getloclist(0)
  call VUAssertEquals(2, len(errors))

  call VUAssertEquals('xsd/test.xsd', bufname(errors[0].bufnr))
  call VUAssertEquals(3, errors[0].lnum)
  call VUAssertEquals(43, errors[0].col)
  call VUAssertEquals('e', errors[0].type)

  call VUAssertEquals('xsd/test.xsd', bufname(errors[1].bufnr))
  call VUAssertEquals(11, errors[1].lnum)
  call VUAssertEquals(12, errors[1].col)
  call VUAssertEquals('e', errors[1].type)
endfunction " }}}

" vim:ft=vim:fdm=marker
