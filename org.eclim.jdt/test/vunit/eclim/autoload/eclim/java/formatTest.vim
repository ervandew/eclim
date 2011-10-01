" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for format.vim
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
  set expandtab
  set shiftwidth=2 tabstop=2
endfunction " }}}

" TestJavaFormatOneLine() {{{
function! TestJavaFormatOneLine()
  edit! src/org/eclim/test/format/TestFormatVUnit.java
  call vunit#PeekRedir()

  call vunit#AssertEquals(getline(24),
    \ 'System.out.println("test formatting");',
    \ 'Intial line incorrect.')

  call cursor(24, 1)
  JavaFormat
  call vunit#PeekRedir()

  call vunit#AssertEquals(getline(24),
    \ '    System.out.println("test formatting");',
    \ 'Result line incorrect.')
endfunction " }}}

" TestJavaFormatRange() {{{
function! TestJavaFormatRange()
  edit! src/org/eclim/test/format/TestFormatVUnit.java
  call vunit#PeekRedir()

  call vunit#AssertEquals(getline(25),
    \ 'if(true){',
    \ 'Intial line 1 incorrect.')

  call vunit#AssertEquals(getline(26),
    \ 'System.out.println("test format if");',
    \ 'Intial line 2 incorrect.')

  call vunit#AssertEquals(getline(27),
    \ '}',
    \ 'Intial line 3 incorrect.')

  25,27JavaFormat
  call vunit#PeekRedir()

  call vunit#AssertEquals(getline(25),
    \ '    if (true) {',
    \ 'Result line 1 incorrect.')

  call vunit#AssertEquals(getline(26),
    \ '      System.out.println("test format if");',
    \ 'Result line 2 incorrect.')

  call vunit#AssertEquals(getline(27),
    \ '    }',
    \ 'Result line 3 incorrect.')
endfunction " }}}

" TestJavaFormatWholeFile() {{{
function! TestJavaFormatWholeFile()
  edit! src/org/eclim/test/format/TestFormatVUnit.java
  call vunit#PeekRedir()

  call vunit#AssertEquals(getline(20),
    \ 'public',
    \ 'Intial line 1 incorrect.')

  call vunit#AssertEquals(getline(21),
    \ 'void main(String[] args)',
    \ 'Intial line 2 incorrect.')

  call vunit#AssertEquals(getline(22),
    \ 'throws Exception',
    \ 'Intial line 3 incorrect.')

  %JavaFormat
  call vunit#PeekRedir()

  call vunit#AssertEquals(getline(20),
    \ '  public void main(String[] args) throws Exception {',
    \ 'Result line 1 incorrect.')
endfunction " }}}

" vim:ft=vim:fdm=marker
