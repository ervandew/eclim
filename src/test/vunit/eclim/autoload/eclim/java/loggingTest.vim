" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for logging.vim
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

" TestLoggerDefine() {{{
function! TestLoggerDefine ()
  edit! src/org/eclim/test/logging/TestLoggingVUnit.java
  call PeekRedir()

  call cursor(5, 3)
  normal ologger.
  call PeekRedir()

  echom '|' . getline(1)
  echom '|' . getline(2)
  echom '|' . getline(3)
  call VUAssertTrue(search('^import .*Log;$'),
    \ 'Logger import not found.')
  call VUAssertTrue(search('^\s*private static final Log logger.*'),
    \ 'Logger declaration not found.')

  bdelete!
endfunction " }}}

" vim:ft=vim:fdm=marker
