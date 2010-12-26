" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for xml.vim
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test'
endfunction " }}}

" TestValidate() {{{
function! TestValidate()
  edit! xml/test_nodtd.xml
  write
  call vunit#PeekRedir()

  let errors = getloclist(0)
  call vunit#AssertEquals(1, len(errors))

  let name = substitute(bufname(errors[0].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'xml/test_nodtd.xml')
  call vunit#AssertEquals(7, errors[0].lnum)
  call vunit#AssertEquals(5, errors[0].col)
  call vunit#AssertEquals('e', errors[0].type)

  edit! xml/test_dtd.xml
  write
  call vunit#PeekRedir()

  let errors = getloclist(0)
  call vunit#AssertEquals(2, len(errors))

  let name = substitute(bufname(errors[0].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'xml/test_dtd.xml')
  call vunit#AssertEquals(12, errors[0].lnum)
  call vunit#AssertEquals(11, errors[0].col)
  call vunit#AssertEquals('e', errors[0].type)

  let name = substitute(bufname(errors[1].bufnr), '\', '/', 'g')
  call vunit#AssertEquals(name, 'xml/test_dtd.xml')
  call vunit#AssertEquals(13, errors[1].lnum)
  call vunit#AssertEquals(9, errors[1].col)
  call vunit#AssertEquals('e', errors[1].type)
endfunction " }}}

" The following 2 tests appear to crash vim.
"TestDtdDefinition() {{{
"function! TestDtdDefinition()
"  edit! xml/spring_test.xml
"  call vunit#PeekRedir()
"
"  call cursor(28, 10)
"  call eclim#xml#definition#DtdDefinition('')
"  call vunit#PeekRedir()
"
"  call vunit#AssertEquals(
"    \ "http://www.springframework.org/dtd/spring-beans.dtd", expand('%'))
"  call vunit#AssertEquals('<!ELEMENT description (#PCDATA)>', getline('.'))
"endfunction " }}}

"TestXsdDefinition() {{{
"function! TestXsdDefinition()
"  edit! pom.xml
"  call vunit#PeekRedir()
"
"  call cursor(11, 8)
"  call eclim#xml#definition#XsdDefinition('')
"  call vunit#PeekRedir()
"
"  call vunit#AssertEquals('http://maven.apache.org/maven-v4_0_0.xsd', expand('%'))
"  call vunit#AssertEquals(
"    \ '      <xs:element name="artifactId" minOccurs="0" type="xs:string">',
"    \ getline('.'))
"endfunction " }}}

" vim:ft=vim:fdm=marker
