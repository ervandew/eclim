" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for impl.vim
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

" TestJavaImpl() {{{
function! TestJavaImpl ()
  edit! src/org/eclim/test/impl/TestImplVUnit.java
  call PeekRedir()

  JavaImpl

  call VUAssertTrue(bufname('%') =~ 'src/org/eclim/test/impl/TestImplVUnit\.java_impl$')

  call cursor(line('$'), 1)
  let line = search('public boolean equals (Object obj)', 'bc')

  call VUAssertTrue(line > 0, 'Equals method not found.')
  call VUAssertEquals(getline(line), '  public boolean equals (Object obj)')

  silent! exec "normal \<cr>"

  call VUAssertEquals(getline(line), '  //public boolean equals (Object obj)')
  quit
  call cursor(1, 1)
  call VUAssertTrue(search('public boolean equals (Object obj)', 'c'),
    \ 'Method no inserted.')

  bdelete!
endfunction " }}}

" vim:ft=vim:fdm=marker
