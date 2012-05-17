" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for validate.vim
"
" License:
"
" Copyright (C) 2005 - 2012  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_web'
endfunction " }}}

function! TestValidate() " {{{
  edit! html/test.html
  call histadd('cmd', 'write') | write
  call vunit#PeekRedir()
  for line in readfile(expand('%'))
    echo '|' . line
  endfor

  let errors = getloclist(0)
  call vunit#AssertEquals(3, len(errors))

  let name = substitute(bufname(errors[0].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'html/test.html')
  call vunit#AssertEquals(5, errors[0].lnum)
  call vunit#AssertEquals(5, errors[0].col)
  call vunit#AssertEquals('e', errors[0].type)
  call vunit#AssertEquals('<h> is not recognized!', errors[0].text)

  let name = substitute(bufname(errors[1].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'html/test.html')
  call vunit#AssertEquals(5, errors[1].lnum)
  call vunit#AssertEquals(5, errors[1].col)
  call vunit#AssertEquals('w', errors[1].type)
  call vunit#AssertEquals('discarding unexpected <h>', errors[1].text)

  let name = substitute(bufname(errors[2].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'html/test.html')
  call vunit#AssertEquals(8, errors[2].lnum)
  call vunit#AssertEquals(5, errors[2].col)
  call vunit#AssertEquals('w', errors[2].type)
  call vunit#AssertEquals('discarding unexpected </div>', errors[2].text)
endfunction " }}}

" vim:ft=vim:fdm=marker
