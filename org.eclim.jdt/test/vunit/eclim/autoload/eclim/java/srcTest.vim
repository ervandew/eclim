" Author:  Eric Van Dewoestine
"
" License: " {{{
"
" Copyright (C) 2005 - 2015  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
  set expandtab
  set shiftwidth=2 tabstop=2
endfunction " }}}

function! TestValidate() " {{{
  edit! src/org/eclim/test/src/TestSrcVUnit.java
  call vunit#PeekRedir()

  call histadd('cmd', 'write') | write
  call vunit#PeekRedir()

  let results = getloclist(0)
  echo 'results = ' . string(results)

  call vunit#AssertEquals(len(results), 3, 'Wrong number of results.')
  call vunit#AssertEquals(10, results[0].lnum, 'Wrong line num.')
  call vunit#AssertEquals(5, results[0].col, 'Wrong col num.')
  call vunit#AssertEquals(
    \ "List is a raw type. " .
    \ "References to generic type List<E> should be parameterized",
    \ results[0].text, 'Wrong result.')
  call vunit#AssertEquals(10, results[1].lnum, 'Wrong line num.')
  call vunit#AssertEquals(21, results[1].col, 'Wrong col num.')
  call vunit#AssertEquals(
    \ "ArrayList is a raw type. " .
    \ "References to generic type ArrayList<E> should be parameterized",
    \ results[1].text, 'Wrong result.')
  call vunit#AssertEquals(11, results[2].lnum, 'Wrong line num.')
  call vunit#AssertEquals(10, results[2].col, 'Wrong col num.')
  call vunit#AssertEquals(
    \ "The method a() is undefined for the type List",
    \ results[2].text, 'Wrong result.')

  " test sorting results by severity
  let g:EclimValidateSortResults = 'severity'
  try
    write
    call vunit#PeekRedir()

    let results = getloclist(0)
    echo 'results = ' . string(results)

    call vunit#AssertEquals(len(results), 3, 'Wrong number of results.')
    call vunit#AssertEquals(11, results[0].lnum, 'Wrong line num.')
    call vunit#AssertEquals(10, results[0].col, 'Wrong col num.')
    call vunit#AssertEquals(
      \ "The method a() is undefined for the type List",
      \ results[0].text, 'Wrong result.')
    call vunit#AssertEquals(10, results[1].lnum, 'Wrong line num.')
    call vunit#AssertEquals(5, results[1].col, 'Wrong col num.')
    call vunit#AssertEquals(
      \ "List is a raw type. " .
      \ "References to generic type List<E> should be parameterized",
      \ results[1].text, 'Wrong result.')
    call vunit#AssertEquals(10, results[2].lnum, 'Wrong line num.')
    call vunit#AssertEquals(21, results[2].col, 'Wrong col num.')
    call vunit#AssertEquals(
      \ "ArrayList is a raw type. " .
      \ "References to generic type ArrayList<E> should be parameterized",
      \ results[2].text, 'Wrong result.')
  finally
    let g:EclimValidateSortResults = 'occurrence'
  endtry

  " test linked file
  edit! ../eclim_unit_test_java_linked/src/org/eclim/test/TestLinked.java
  write
  call vunit#PeekRedir()

  let results = getloclist(0)
  echo 'results = ' . string(results)

  call vunit#AssertEquals(len(results), 4, 'Wrong number of results for linked resource.')
  call vunit#AssertEquals(10, results[0].lnum, 'Wrong line num for linked resource error 1.')
  call vunit#AssertEquals(9, results[0].col, 'Wrong col num for linked resource error 1.')
  call vunit#AssertEquals(
    \ 'Syntax error on token ".", invalid VariableDeclarator',
    \ results[0].text, 'Wrong result for linked resource error 1.')
  call vunit#AssertEquals(15, results[1].lnum, 'Wrong line num for linked resource error 2.')
  call vunit#AssertEquals(5, results[1].col, 'Wrong col num for linked resource error 2.')
  call vunit#AssertEquals(
    \ 'ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized',
    \ results[1].text, 'Wrong result for linked resource error 2.')
  call vunit#AssertEquals(15, results[2].lnum, 'Wrong line num for linked resource error 3.')
  call vunit#AssertEquals(26, results[2].col, 'Wrong col num for linked resource error 3.')
  call vunit#AssertEquals(
    \ 'ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized',
    \ results[1].text, 'Wrong result for linked resource error 3.')
  call vunit#AssertEquals(16, results[3].lnum, 'Wrong line num for linked resource error 4.')
  call vunit#AssertEquals(10, results[3].col, 'Wrong col num for linked resource error 4.')
  call vunit#AssertEquals(
    \ 'The method a() is undefined for the type ArrayList',
    \ results[3].text, 'Wrong result for linked resource error 4.')
endfunction " }}}

function! TestCheckstyleManual() " {{{
  call eclim#project#util#SetProjectSetting(
    \ "org.eclim.java.checkstyle.onvalidate", "false")

  edit! src/org/eclim/test/src/TestCheckstyleVUnit.java
  call vunit#PeekRedir()

  Checkstyle
  call vunit#PeekRedir()

  let results = getloclist(0)
  call vunit#AssertEquals(len(results), 3)
  for result in results
    call vunit#AssertEquals(bufname(result.bufnr), expand('%'), 'File does not match.')
    call vunit#AssertTrue(result.text =~ '^\[checkstyle\]', 'Missing namespace.')
  endfor

  call setloclist(0, [], 'r')
  let results = getloclist(0)
  call vunit#AssertEquals(len(results), 0)
endfunction " }}}

function! TestCheckstyleAuto() " {{{
  call eclim#project#util#SetProjectSetting(
    \ "org.eclim.java.checkstyle.onvalidate", "true")
  try
    edit! src/org/eclim/test/src/TestCheckstyleVUnit.java
    call vunit#PeekRedir()

    call histadd('cmd', 'write') | write
    call vunit#PeekRedir()

    let results = getloclist(0)
    call vunit#AssertEquals(len(results), 3)
    for result in results
      call vunit#AssertEquals(bufname(result.bufnr), expand('%'))
      call vunit#AssertTrue(result.text =~ '^\[checkstyle\]', 'Missing namespace.')
    endfor

    call setloclist(0, [], 'r')
    let results = getloclist(0)
    call vunit#AssertEquals(len(results), 0)
  finally
    call eclim#project#util#SetProjectSetting(
      \ "org.eclim.java.checkstyle.onvalidate", "false")
  endtry
endfunction " }}}

function! TestJavaFormatOneLine() " {{{
  edit! src/org/eclim/test/src/TestFormatVUnit.java
  call vunit#PeekRedir()

  call vunit#AssertEquals(getline(8),
    \ 'System.out.println("test formatting");',
    \ 'Intial line incorrect.')

  call cursor(8, 1)
  JavaFormat
  call vunit#PeekRedir()

  call vunit#AssertEquals(getline(8),
    \ '    System.out.println("test formatting");',
    \ 'Result line incorrect.')
endfunction " }}}

function! TestJavaFormatRange() " {{{
  edit! src/org/eclim/test/src/TestFormatVUnit.java
  call vunit#PeekRedir()

  call vunit#AssertEquals(getline(9),
    \ 'if(true){',
    \ 'Intial line 1 incorrect.')

  call vunit#AssertEquals(getline(10),
    \ 'System.out.println("test format if");',
    \ 'Intial line 2 incorrect.')

  call vunit#AssertEquals(getline(11),
    \ '}',
    \ 'Intial line 3 incorrect.')

  9,11JavaFormat
  call vunit#PeekRedir()

  call vunit#AssertEquals(getline(9),
    \ '    if (true) {',
    \ 'Result line 1 incorrect.')

  call vunit#AssertEquals(getline(10),
    \ '      System.out.println("test format if");',
    \ 'Result line 2 incorrect.')

  call vunit#AssertEquals(getline(11),
    \ '    }',
    \ 'Result line 3 incorrect.')
endfunction " }}}

function! TestJavaFormatWholeFile() " {{{
  edit! src/org/eclim/test/src/TestFormatVUnit.java
  call vunit#PeekRedir()

  call vunit#AssertEquals(getline(4),
    \ 'public',
    \ 'Intial line 1 incorrect.')

  call vunit#AssertEquals(getline(5),
    \ 'void main(String[] args)',
    \ 'Intial line 2 incorrect.')

  call vunit#AssertEquals(getline(6),
    \ 'throws Exception',
    \ 'Intial line 3 incorrect.')

  %JavaFormat
  call vunit#PeekRedir()

  call vunit#AssertEquals(getline(4),
    \ '  public void main(String[] args) throws Exception {',
    \ 'Result line 1 incorrect.')
endfunction " }}}

" vim:ft=vim:fdm=marker
