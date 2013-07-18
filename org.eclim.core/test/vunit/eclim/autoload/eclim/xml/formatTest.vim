" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for format.vim
"
" License:
"
" Copyright (C) 2005 - 2013  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test'
  set shiftwidth=2
endfunction " }}}

function! TestFormat() " {{{
  edit! xml/format.xml
  call vunit#PeekRedir()

  call vunit#AssertEquals(2, line('$'))
  call vunit#AssertTrue(getline('$') =~ '^<blah attr1.*</blah>$')

  XmlFormat

  call vunit#AssertEquals(5, line('$'))
  call vunit#AssertTrue(
     \ getline(2) =~ '^<blah attr1.*attr5="five" attr6="six" attr7="seven">$')
  call vunit#AssertEquals('  <one>one</one>', getline(3))
  call vunit#AssertEquals('  <two/>', getline(4))
  call vunit#AssertEquals('</blah>', getline(5))
endfunction " }}}

" vim:ft=vim:fdm=marker
