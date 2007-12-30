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

" TestGetDtd() {{{
function! TestGetDtd ()
  edit! xml/test_nodtd.xml
  call PeekRedir()
  call VUAssertEquals('', eclim#xml#util#GetDtd())

  edit! xml/spring_test.xml
  call PeekRedir()
  call VUAssertEquals(
    \ "http://www.springframework.org/dtd/spring-beans.dtd",
    \ eclim#xml#util#GetDtd())

  bdelete!
endfunction " }}}

" TestGetXsd() {{{
function! TestGetXsd ()
  edit! xml/spring_test.xml
  call PeekRedir()
  call VUAssertEquals('', eclim#xml#util#GetXsd())

  edit! pom.xml
  call PeekRedir()
  call VUAssertEquals(
    \ 'http://maven.apache.org/maven-v4_0_0.xsd', eclim#xml#util#GetXsd())

  bdelete!
endfunction " }}}

" TestGetElementName() {{{
function! TestGetElementName ()
  edit! pom.xml
  call PeekRedir()

  call cursor(6, 1)
  call VUAssertEquals('project', eclim#xml#util#GetElementName())

  call cursor(6, 9)
  call VUAssertEquals('project', eclim#xml#util#GetElementName())

  call cursor(9, 22)
  call VUAssertEquals('modelVersion', eclim#xml#util#GetElementName())

  call cursor(27, 3)
  call VUAssertEquals('dependencies', eclim#xml#util#GetElementName())

  call cursor(27, 17)
  call VUAssertEquals('dependencies', eclim#xml#util#GetElementName())

  call cursor(23, 19)
  call VUAssertEquals('', eclim#xml#util#GetElementName())

  bdelete!
endfunction " }}}

" TestGetParentElementName() {{{
function! TestGetParentElementName ()
  edit! pom.xml
  call PeekRedir()

  call cursor(6, 1)
  call VUAssertEquals('', eclim#xml#util#GetParentElementName())

  call cursor(7, 8)
  call VUAssertEquals('', eclim#xml#util#GetParentElementName())

  call cursor(9, 10)
  call VUAssertEquals('project', eclim#xml#util#GetParentElementName())

  call cursor(9, 27)
  call VUAssertEquals('project', eclim#xml#util#GetParentElementName())

  call cursor(17, 27)
  call VUAssertEquals('dependency', eclim#xml#util#GetParentElementName())

  call cursor(28, 1)
  call VUAssertEquals('', eclim#xml#util#GetParentElementName())

  call cursor(32, 53)
  call VUAssertEquals('resource', eclim#xml#util#GetParentElementName())

  call cursor(32, 54)
  call VUAssertEquals('resources', eclim#xml#util#GetParentElementName())

  bdelete!
endfunction " }}}

" vim:ft=vim:fdm=marker
