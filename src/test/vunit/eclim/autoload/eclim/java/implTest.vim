" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for impl.vim
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
endfunction " }}}

" TestJavaImpl() {{{
function! TestJavaImpl()
  edit! src/org/eclim/test/impl/TestImplVUnit.java
  call PeekRedir()

  JavaImpl

  call VUAssertTrue(bufname('%') =~ 'src/org/eclim/test/impl/TestImplVUnit\.java_impl$')

  call cursor(line('$'), 1)
  let line = search('public boolean equals(Object obj)', 'bc')

  call VUAssertTrue(line > 0, 'Equals method not found.')
  call VUAssertEquals(getline(line), '  public boolean equals(Object obj)')

  silent! exec "normal \<cr>"

  call VUAssertEquals(getline(line), '  //public boolean equals(Object obj)')
  quit
  call cursor(1, 1)
  call VUAssertTrue(search('public boolean equals(Object obj)', 'c'),
    \ 'Method no inserted.')
endfunction " }}}

" vim:ft=vim:fdm=marker
