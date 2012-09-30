" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for util.vim
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
endfunction " }}}

function! TestFileExists() " {{{
  edit! src/org/eclim/test/src/TestPrototypeVUnit.java
  call vunit#PeekRedir()

  call vunit#AssertTrue(eclim#java#util#FileExists('org/eclim/test/impl/TestBeanVUnit.java'),
    \ 'TestBeanVUnit.java not found.')
  call vunit#AssertFalse(eclim#java#util#FileExists('org/eclim/test/TestBlah.java'),
    \ 'TestBlah.java found?')
endfunction " }}}

function! TestGetClassname() " {{{
  edit! src/org/eclim/test/src/TestPrototypeVUnit.java
  call vunit#PeekRedir()

  call vunit#AssertEquals('TestPrototypeVUnit', eclim#java#util#GetClassname())
  call vunit#AssertEquals('TestBeanVUnit',
    \ eclim#java#util#GetClassname(
    \   fnamemodify('src/org/eclim/test/impl/TestBeanVUnit.java', ':p')))
endfunction " }}}

function! TestGetClassDeclarationPosition() " {{{
  edit! src/org/eclim/test/src/TestPrototypeVUnit.java
  call vunit#PeekRedir()

  call vunit#AssertEquals(3, eclim#java#util#GetClassDeclarationPosition(0))
endfunction " }}}

function! TestGetFullyQualifiedClassname() " {{{
  edit! src/org/eclim/test/src/TestPrototypeVUnit.java
  call vunit#PeekRedir()

  call vunit#AssertEquals('org.eclim.test.src.TestPrototypeVUnit',
    \ eclim#java#util#GetFullyQualifiedClassname())
  call vunit#AssertEquals('org.eclim.test.impl.TestBeanVUnit',
    \ eclim#java#util#GetFullyQualifiedClassname(
    \   fnamemodify('src/org/eclim/test/impl/TestBeanVUnit.java', ':p')))
endfunction " }}}

function! TestGetPackage() " {{{
  edit! src/org/eclim/test/src/TestPrototypeVUnit.java
  call vunit#PeekRedir()

  call vunit#AssertEquals('org.eclim.test.src', eclim#java#util#GetPackage())
  call vunit#AssertEquals('org.eclim.test.impl',
    \ eclim#java#util#GetPackage(fnamemodify('src/org/eclim/test/impl/TestBeanVUnit.java', ':p')))
endfunction " }}}

function! TestGetPackageFromImport() " {{{
  edit! src/org/eclim/test/impl/TestImplVUnit.java
  call vunit#PeekRedir()

  call vunit#AssertEquals('java.util', eclim#java#util#GetPackageFromImport('HashMap'))
  call vunit#AssertEquals('', eclim#java#util#GetPackageFromImport('Blah'))
endfunction " }}}

function! TestGetSelectedFields() " {{{
  edit! src/org/eclim/test/impl/TestBeanVUnit.java
  call vunit#PeekRedir()

  let fields = eclim#java#util#GetSelectedFields(8, 10)
  call vunit#AssertEquals(['description', 'date', 'valid'], fields)
endfunction " }}}

function! TestIsImported() " {{{
  edit! src/org/eclim/test/impl/TestImplVUnit.java
  call vunit#PeekRedir()

  call vunit#AssertTrue(eclim#java#util#IsImported('java.util.HashMap'),
    \ 'ArrayList not imported.')
  call vunit#AssertTrue(eclim#java#util#IsImported('org.eclim.test.impl.TestImpl'),
    \ 'TestImpl not imported.')
  call vunit#AssertFalse(eclim#java#util#IsImported('java.io.File'),
    \ 'File imported.')
endfunction " }}}

" vim:ft=vim:fdm=marker
