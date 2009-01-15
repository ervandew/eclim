" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for format.vim
"
" License:
"
" Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test'
endfunction " }}}

" TestFormat() {{{
function! TestFormat()
  edit! xml/format.xml
  call PeekRedir()

  call VUAssertEquals(2, line('$'))
  call VUAssertTrue(getline('$') =~ '^<blah attr1.*</blah>$')

  XmlFormat

  call VUAssertEquals(5, line('$'))
  call VUAssertTrue(
     \ getline(2) =~ '^<blah attr1.*attr5="five" attr6="six" attr7="seven">$')
  call VUAssertEquals('  <one>one</one>', getline(3))
  call VUAssertEquals('  <two/>', getline(4))
  call VUAssertEquals('</blah>', getline(5))
endfunction " }}}

" TestFormatFail() {{{
function! TestFormatFail()
  edit! xml/format_fail.xml
  call PeekRedir()

  XmlFormat

  call VUAssertEquals(1, len(getloclist(0)))
endfunction " }}}

" vim:ft=vim:fdm=marker
