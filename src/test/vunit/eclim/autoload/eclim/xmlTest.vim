" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for xml.vim
"
" License:
"
" Copyright (c) 2005 - 2006
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

" TestValidate() {{{
function! TestValidate ()
  edit! xml/test_nodtd.xml
  write
  call PeekRedir()

  let errors = getloclist(0)
  call VUAssertEquals(1, len(errors))

  call VUAssertEquals('xml/test_nodtd.xml', bufname(errors[0].bufnr))
  call VUAssertEquals(7, errors[0].lnum)
  call VUAssertEquals(5, errors[0].col)
  call VUAssertEquals('e', errors[0].type)

  edit! xml/test_dtd.xml
  write
  call PeekRedir()

  let errors = getloclist(0)
  call VUAssertEquals(2, len(errors))

  call VUAssertEquals('xml/test_dtd.xml', bufname(errors[0].bufnr))
  call VUAssertEquals(12, errors[0].lnum)
  call VUAssertEquals(11, errors[0].col)
  call VUAssertEquals('e', errors[0].type)

  call VUAssertEquals('xml/test_dtd.xml', bufname(errors[1].bufnr))
  call VUAssertEquals(13, errors[1].lnum)
  call VUAssertEquals(9, errors[1].col)
  call VUAssertEquals('e', errors[1].type)

  bdelete!
endfunction " }}}

" The following 2 tests appear to crash vim.
"TestDtdDefinition() {{{
"function! TestDtdDefinition ()
"  edit! xml/spring_test.xml
"  call PeekRedir()
"
"  call cursor(28, 10)
"  call eclim#xml#definition#DtdDefinition('')
"  call PeekRedir()
"
"  call VUAssertEquals(
"    \ "http://www.springframework.org/dtd/spring-beans.dtd", expand('%'))
"  call VUAssertEquals('<!ELEMENT description (#PCDATA)>', getline('.'))
"
"  bdelete!
"  bdelete!
"endfunction " }}}

"TestXsdDefinition() {{{
"function! TestXsdDefinition ()
"  edit! pom.xml
"  call PeekRedir()
"
"  call cursor(11, 8)
"  call eclim#xml#definition#XsdDefinition('')
"  call PeekRedir()
"
"  call VUAssertEquals('http://maven.apache.org/maven-v4_0_0.xsd', expand('%'))
"  call VUAssertEquals(
"    \ '      <xs:element name="artifactId" minOccurs="0" type="xs:string">',
"    \ getline('.'))
"
"  bdelete!
"  bdelete!
"endfunction " }}}

" vim:ft=vim:fdm=marker
