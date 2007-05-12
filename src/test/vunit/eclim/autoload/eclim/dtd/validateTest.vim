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
  edit! dtd/test.dtd
  write
  call PeekRedir()
  for line in readfile(expand('%'))
    echo '|' . line
  endfor

  let errors = getloclist(0)
  call VUAssertEquals(1, len(errors))

  call VUAssertEquals('dtd/test.dtd', bufname(errors[0].bufnr))
  call VUAssertEquals(3, errors[0].lnum)
  call VUAssertEquals(45, errors[0].col)
  call VUAssertEquals('e', errors[0].type)

  bdelete!
endfunction " }}}

" vim:ft=vim:fdm=marker
