" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for util.vim
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

" TestFileExists() {{{
function! TestFileExists()
  edit! src/org/eclim/test/src/TestPrototypeVUnit.java
  call PeekRedir()

  call VUAssertTrue(eclim#java#util#FileExists('org/eclim/test/bean/TestBeanVUnit.java'),
    \ 'TestBeanVUnit.java not found.')
  call VUAssertFalse(eclim#java#util#FileExists('org/eclim/test/TestBlah.java'),
    \ 'TestBlah.java found?')
endfunction " }}}

" TestGetClassname() {{{
function! TestGetClassname()
  edit! src/org/eclim/test/src/TestPrototypeVUnit.java
  call PeekRedir()

  call VUAssertEquals('TestPrototypeVUnit', eclim#java#util#GetClassname())
  call VUAssertEquals('TestBeanVUnit',
    \ eclim#java#util#GetClassname(
    \   fnamemodify('src/org/eclim/test/bean/TestBeanVUnit.java', ':p')))
endfunction " }}}

" TestGetClassDeclarationPosition() {{{
function! TestGetClassDeclarationPosition()
  edit! src/org/eclim/test/src/TestPrototypeVUnit.java
  call PeekRedir()

  call VUAssertEquals(3, eclim#java#util#GetClassDeclarationPosition(0))
endfunction " }}}

" TestGetFullyQualifiedClassname() {{{
function! TestGetFullyQualifiedClassname()
  edit! src/org/eclim/test/src/TestPrototypeVUnit.java
  call PeekRedir()

  call VUAssertEquals('org.eclim.test.src.TestPrototypeVUnit',
    \ eclim#java#util#GetFullyQualifiedClassname())
  call VUAssertEquals('org.eclim.test.bean.TestBeanVUnit',
    \ eclim#java#util#GetFullyQualifiedClassname(
    \   fnamemodify('src/org/eclim/test/bean/TestBeanVUnit.java', ':p')))
endfunction " }}}

" TestGetPackage() {{{
function! TestGetPackage()
  edit! src/org/eclim/test/src/TestPrototypeVUnit.java
  call PeekRedir()

  call VUAssertEquals('org.eclim.test.src', eclim#java#util#GetPackage())
  call VUAssertEquals('org.eclim.test.bean',
    \ eclim#java#util#GetPackage(fnamemodify('src/org/eclim/test/bean/TestBeanVUnit.java', ':p')))
endfunction " }}}

" TestGetPackageFromImport() {{{
function! TestGetPackageFromImport()
  edit! src/org/eclim/test/impl/TestImplVUnit.java
  call PeekRedir()

  call VUAssertEquals('java.util', eclim#java#util#GetPackageFromImport('HashMap'))
  call VUAssertEquals('', eclim#java#util#GetPackageFromImport('Blah'))
endfunction " }}}

" TestGetSelectedFields() {{{
function! TestGetSelectedFields()
  edit! src/org/eclim/test/bean/TestBeanVUnit.java
  call PeekRedir()

  let fields = eclim#java#util#GetSelectedFields(8, 10)
  call VUAssertEquals(['description', 'date', 'valid'], fields)
endfunction " }}}

" TestIsImported() {{{
function! TestIsImported()
  edit! src/org/eclim/test/impl/TestImplVUnit.java
  call PeekRedir()

  call VUAssertTrue(eclim#java#util#IsImported('java.util.HashMap'),
    \ 'ArrayList not imported.')
  call VUAssertTrue(eclim#java#util#IsImported('org.eclim.test.impl.TestImpl'),
    \ 'TestImpl not imported.')
  call VUAssertFalse(eclim#java#util#IsImported('java.io.File'),
    \ 'File imported.')
endfunction " }}}

" TestJavac() {{{
function! TestJavac()
  edit! src-javac/org/eclim/test/Test.java
  call PeekRedir()

  call delete(
    \ g:TestEclimWorkspace .
    \ 'eclim_unit_test_java/bin/org/eclim/test/Test.class')

  call VUAssertFalse(filereadable(
    \ g:TestEclimWorkspace .
    \ 'eclim_unit_test_java/bin/org/eclim/test/Test.class'))

  Javac

  call VUAssertTrue(filereadable(
    \ g:TestEclimWorkspace .
    \ 'eclim_unit_test_java/bin/org/eclim/test/Test.class'))
endfunction " }}}

" TestValidate() {{{
function! TestValidate()
  edit! src/org/eclim/test/src/TestSrcVUnit.java
  call PeekRedir()

  write
  call PeekRedir()

  let results = getloclist(0)
  echo 'results = ' . string(results)

  call VUAssertEquals(len(results), 3, 'Wrong number of results.')
  call VUAssertEquals(10, results[0].lnum, 'Wrong line num.')
  call VUAssertEquals(5, results[0].col, 'Wrong col num.')
  call VUAssertEquals(
    \ "List is a raw type. " .
    \ "References to generic type List<E> should be parameterized",
    \ results[0].text, 'Wrong result.')
  call VUAssertEquals(10, results[1].lnum, 'Wrong line num.')
  call VUAssertEquals(21, results[1].col, 'Wrong col num.')
  call VUAssertEquals(
    \ "ArrayList is a raw type. " .
    \ "References to generic type ArrayList<E> should be parameterized",
    \ results[1].text, 'Wrong result.')
  call VUAssertEquals(11, results[2].lnum, 'Wrong line num.')
  call VUAssertEquals(10, results[2].col, 'Wrong col num.')
  call VUAssertEquals(
    \ "The method a() is undefined for the type List",
    \ results[2].text, 'Wrong result.')

  " test sorting results by severity
  let g:EclimValidateSortResults = 'severity'
  try
    write
    call PeekRedir()

    let results = getloclist(0)
    echo 'results = ' . string(results)

    call VUAssertEquals(len(results), 3, 'Wrong number of results.')
    call VUAssertEquals(11, results[0].lnum, 'Wrong line num.')
    call VUAssertEquals(10, results[0].col, 'Wrong col num.')
    call VUAssertEquals(
      \ "The method a() is undefined for the type List",
      \ results[0].text, 'Wrong result.')
    call VUAssertEquals(10, results[1].lnum, 'Wrong line num.')
    call VUAssertEquals(5, results[1].col, 'Wrong col num.')
    call VUAssertEquals(
      \ "List is a raw type. " .
      \ "References to generic type List<E> should be parameterized",
      \ results[1].text, 'Wrong result.')
    call VUAssertEquals(10, results[2].lnum, 'Wrong line num.')
    call VUAssertEquals(21, results[2].col, 'Wrong col num.')
    call VUAssertEquals(
      \ "ArrayList is a raw type. " .
      \ "References to generic type ArrayList<E> should be parameterized",
      \ results[2].text, 'Wrong result.')
  finally
    let g:EclimValidateSortResults = 'occurrence'
  endtry

  " test linked file
  edit! ../eclim_unit_test_java_linked/src/org/eclim/test/TestLinked.java
  write
  call PeekRedir()

  let results = getloclist(0)
  echo 'results = ' . string(results)

  call VUAssertEquals(len(results), 2, 'Wrong number of results for linked resource.')
  call VUAssertEquals(10, results[0].lnum, 'Wrong line num for linked resource.')
  call VUAssertEquals(9, results[0].col, 'Wrong col num for linked resource.')
  call VUAssertEquals(
    \ 'Syntax error on token ".", invalid VariableDeclarator',
    \ results[0].text, 'Wrong result for linked resource.')
endfunction " }}}

" vim:ft=vim:fdm=marker
