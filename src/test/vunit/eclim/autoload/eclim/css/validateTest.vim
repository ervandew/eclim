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
  edit! css/validate.css
  write
  call vunit#PeekRedir()
  for line in readfile(expand('%'))
    echo '|' . line
  endfor

  let errors = getloclist(0)
  call vunit#AssertEquals(2, len(errors))

  let name = substitute(bufname(errors[0].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'css/validate.css')
  call vunit#AssertEquals(2, errors[0].lnum)
  call vunit#AssertEquals(1, errors[0].col)
  call vunit#AssertEquals('e', errors[0].type)
  call vunit#AssertEquals('bald is not a font-weight value', errors[0].text)

  let name = substitute(bufname(errors[1].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'css/validate.css')
  call vunit#AssertEquals(6, errors[1].lnum)
  call vunit#AssertEquals(1, errors[1].col)
  call vunit#AssertEquals('e', errors[1].type)
  call vunit#AssertEquals("Property fnt-size doesn't exist", errors[1].text)
endfunction " }}}

" vim:ft=vim:fdm=marker
