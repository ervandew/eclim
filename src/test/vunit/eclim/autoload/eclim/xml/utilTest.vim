" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for util.vim
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

" TestGetDtd() {{{
function! TestGetDtd ()
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test'
  edit! xml/test_nodtd.xml
  call PeekRedir()
  call VUAssertEquals('', eclim#xml#util#GetDtd())

  edit! xml/spring_test.xml
  call PeekRedir()
  call VUAssertEquals(
    \ "http://www.springframework.org/dtd/spring-beans.dtd",
    \ eclim#xml#util#GetDtd())
endfunction " }}}

" TestGetXsd() {{{
function! TestGetXsd ()
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test'
  edit! xml/spring_test.xml
  call PeekRedir()
  call VUAssertEquals('', eclim#xml#util#GetXsd())

  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
  edit! pom.xml
  call PeekRedir()
  call VUAssertEquals(
    \ 'http://maven.apache.org/maven-v4_0_0.xsd', eclim#xml#util#GetXsd())
endfunction " }}}

" TestGetElementName() {{{
function! TestGetElementName ()
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
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
endfunction " }}}

" TestGetParentElementName() {{{
function! TestGetParentElementName ()
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
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
endfunction " }}}

" vim:ft=vim:fdm=marker
