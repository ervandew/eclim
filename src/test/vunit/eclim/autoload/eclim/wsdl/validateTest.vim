" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for validate.vim
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
  edit! wsdl/GoogleSearch.wsdl
  write
  call PeekRedir()

  let errors = getloclist(0)
  call VUAssertEquals(3, len(errors))

  call VUAssertEquals('wsdl/GoogleSearch.wsdl', bufname(errors[0].bufnr))
  call VUAssertEquals(13, errors[0].lnum)
  call VUAssertEquals(44, errors[0].col)
  call VUAssertEquals('w', errors[0].type)

  call VUAssertEquals('wsdl/GoogleSearch.wsdl', bufname(errors[1].bufnr))
  call VUAssertEquals(14, errors[1].lnum)
  call VUAssertEquals(14, errors[1].col)
  call VUAssertEquals('e', errors[1].type)

  call VUAssertEquals('wsdl/GoogleSearch.wsdl', bufname(errors[2].bufnr))
  call VUAssertEquals(48, errors[2].lnum)
  call VUAssertEquals(49, errors[2].col)
  call VUAssertEquals('e', errors[2].type)

  bdelete!
endfunction " }}}

" vim:ft=vim:fdm=marker
