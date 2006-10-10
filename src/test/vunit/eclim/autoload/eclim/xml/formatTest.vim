" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for format.vim
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

" TestFormat() {{{
function! TestFormat()
  edit! xml/format.xml
  call PeekRedir()

  call VUAssertEquals(2, line('$'))
  call VUAssertTrue(getline('$') =~ '^<blah attr1.*</blah>$')

  XmlFormat

  call VUAssertEquals(6, line('$'))
  call VUAssertTrue(getline(2) =~ '^<blah attr1.*attr5="five"$')
  call VUAssertEquals('  attr6="six" attr7="seven">', getline(3))
  call VUAssertEquals('  <one>one</one>', getline(4))
  call VUAssertEquals('  <two/>', getline(5))
  call VUAssertEquals('</blah>', getline(6))

  bdelete!
endfunction " }}}

" TestFormatFail() {{{
function! TestFormatFail()
  edit! xml/format_fail.xml
  call PeekRedir()

  XmlFormat

  call VUAssertEquals(1, len(getloclist(0)))

  bdelete!
endfunction " }}}

" vim:ft=vim:fdm=marker
