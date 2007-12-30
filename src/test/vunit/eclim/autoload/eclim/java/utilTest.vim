" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for util.vim
"
" License:
"
" Copyright (c) 2005 - 2008
"
" Licensed under the Apache License, Version 2.0 (the "License");
" you may not use this file except in compliance with the License.
" You may obtain a copy of the License at
"
"      http://www.apache.org/licenses/LICENSE-2.0
"
" Unless required by applicable law or agreed to in writing, software
" distributed under the License is distributed on an "AS IS" BASIS,
" WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
" See the License for the specific language governing permissions and
" limitations under the License.
"
" }}}

" SetUp() {{{
function! SetUp ()
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
endfunction " }}}

" TestFileExists() {{{
function! TestFileExists ()
  edit! src/org/eclim/test/src/TestPrototypeVUnit.java
  call PeekRedir()

  call VUAssertTrue(eclim#java#util#FileExists('org/eclim/test/bean/TestBeanVUnit.java'),
    \ 'TestBeanVUnit.java not found.')
  call VUAssertFalse(eclim#java#util#FileExists('org/eclim/test/TestBlah.java'),
    \ 'TestBlah.java found?')
endfunction " }}}

" TestGetClassname() {{{
function! TestGetClassname ()
  edit! src/org/eclim/test/src/TestPrototypeVUnit.java
  call PeekRedir()

  call VUAssertEquals('TestPrototypeVUnit', eclim#java#util#GetClassname())
  call VUAssertEquals('TestBeanVUnit',
    \ eclim#java#util#GetClassname(
    \   fnamemodify('src/org/eclim/test/bean/TestBeanVUnit.java', ':p')))
endfunction " }}}

" TestGetClassDeclarationPosition() {{{
function! TestGetClassDeclarationPosition ()
  edit! src/org/eclim/test/src/TestPrototypeVUnit.java
  call PeekRedir()

  call VUAssertEquals(3, eclim#java#util#GetClassDeclarationPosition(0))
endfunction " }}}

" TestGetFullyQualifiedClassname() {{{
function! TestGetFullyQualifiedClassname ()
  edit! src/org/eclim/test/src/TestPrototypeVUnit.java
  call PeekRedir()

  call VUAssertEquals('org.eclim.test.src.TestPrototypeVUnit',
    \ eclim#java#util#GetFullyQualifiedClassname())
  call VUAssertEquals('org.eclim.test.bean.TestBeanVUnit',
    \ eclim#java#util#GetFullyQualifiedClassname(
    \   fnamemodify('src/org/eclim/test/bean/TestBeanVUnit.java', ':p')))
endfunction " }}}

" TestGetPackage() {{{
function! TestGetPackage ()
  edit! src/org/eclim/test/src/TestPrototypeVUnit.java
  call PeekRedir()

  call VUAssertEquals('org.eclim.test.src', eclim#java#util#GetPackage())
  call VUAssertEquals('org.eclim.test.bean',
    \ eclim#java#util#GetPackage(fnamemodify('src/org/eclim/test/bean/TestBeanVUnit.java', ':p')))
endfunction " }}}

" TestGetPackageFromImport() {{{
function! TestGetPackageFromImport ()
  edit! src/org/eclim/test/impl/TestImplVUnit.java
  call PeekRedir()

  call VUAssertEquals('java.util', eclim#java#util#GetPackageFromImport('ArrayList'))
  call VUAssertEquals('', eclim#java#util#GetPackageFromImport('Blah'))
endfunction " }}}

" TestGetSelectedFields() {{{
function! TestGetSelectedFields ()
  edit! src/org/eclim/test/bean/TestBeanVUnit.java
  call PeekRedir()

  let fields = eclim#java#util#GetSelectedFields(8, 10)
  call VUAssertEquals(['description', 'date', 'valid'], fields)
endfunction " }}}

" TestIsImported() {{{
function! TestIsImported ()
  edit! src/org/eclim/test/impl/TestImplVUnit.java
  call PeekRedir()

  call VUAssertTrue(eclim#java#util#IsImported('java.util.ArrayList'),
    \ 'ArrayList not imported.')
  call VUAssertTrue(eclim#java#util#IsImported('org.eclim.test.impl.TestImpl'),
    \ 'TestImpl not imported.')
  call VUAssertFalse(eclim#java#util#IsImported('java.io.File'),
    \ 'File imported.')
endfunction " }}}

" vim:ft=vim:fdm=marker
