" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for delegate.vim
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

" TestCorrect() {{{
function! TestCorrect ()
  edit! src/org/eclim/test/delegate/TestDelegateVUnit.java
  call PeekRedir()

  call cursor(8, 3)
  JavaDelegate
  call VUAssertTrue(bufname('%') =~ 'TestDelegateVUnit.java_delegate$',
    \ 'Delegate window not opened.')
  call VUAssertEquals('org.eclim.test.delegate.TestDelegateVUnit', getline(1),
    \ 'Wrong type in delegate window.')

  call VUAssertTrue(search('^\s*public abstract int size ()'),
    \ 'Super method size() not found')

  exec "normal Vjj\<cr>"

  call VUAssertTrue(search('^\s*//public abstract int size ()'),
    \ 'Super method size() not commented out after add.')
  bdelete

  call VUAssertTrue(search('public int size ()$'), 'size() not added.')
  call VUAssertTrue(search('return list\.size();$'), 'size() not delegating.')
  call VUAssertTrue(search('public boolean isEmpty ()$'), 'isEmpty() not added.')
  call VUAssertTrue(search('return list\.isEmpty();$'), 'isEmpty() not delegating.')
  call VUAssertTrue(search('public boolean contains (Object o)$'),
    \ 'contains(Object) not added.')
  call VUAssertTrue(search('return list\.contains(o);$'),
    \ 'contains(Object) not delegating.')
endfunction " }}}

" vim:ft=vim:fdm=marker
