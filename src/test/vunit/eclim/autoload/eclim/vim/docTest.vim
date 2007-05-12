" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for doc.vim
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

" TestFindDoc() {{{
function! TestFindDoc ()
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
  edit! vim/test.vim
  call PeekRedir()

  call cursor(3, 4)
" This test case may be crashing vim !!
  "call eclim#vim#doc#FindDoc('')
  "call VUAssertTrue(&ft == 'help', 'not help file: v:version')
  "call VUAssertTrue(getline('.') =~ '\*v:version\*', 'v:version')
  "bdelete

  "call cursor(4, 4)
  "call eclim#vim#doc#FindDoc('')
  "call VUAssertTrue(&ft == 'help', 'not help file: :command')
  "call VUAssertTrue(getline('.') =~ '\*:command\*', ':command')
  "bdelete

  "call cursor(4, 11)
  "call eclim#vim#doc#FindDoc('')
  "call VUAssertTrue(&ft == 'help', 'not help file: -nargs')
  "call VUAssertTrue(getline('.') =~ '\*E175\*', '-nargs')
  "bdelete

  "call cursor(4, 20)
  "call eclim#vim#doc#FindDoc('')
  "call VUAssertTrue(&ft == 'help', 'not help file: -complete')
  "call VUAssertTrue(getline('.') =~ '\*:command-completion\*', '-complete')
  "bdelete

  "call cursor(4, 30)
  "call eclim#vim#doc#FindDoc('')
  "call VUAssertTrue(&ft == 'help', 'not help file: customlist')
  "call VUAssertTrue(getline('.') =~ '\*:command-completion-customlist\*', 'customlist')
  "bdelete

  "call cursor(4, 65)
  "call eclim#vim#doc#FindDoc('')
  "call VUAssertTrue(&ft == 'help', 'not help file: :call')
  "call VUAssertTrue(getline('.') =~ '\*:call\*', ':call')
  "bdelete

  "call cursor(4, 56)
  "call eclim#vim#doc#FindDoc('')
  "call VUAssertEquals('vim/test.vim', expand('%'))

  "call cursor(6, 24)
  "call eclim#vim#doc#FindDoc('')
  "call VUAssertTrue(&ft == 'help', 'not help file: substitute()')
  "call VUAssertTrue(getline('.') =~ '\*substitute()\*', 'substitute()')
  "bdelete
endfunction " }}}

" vim:ft=vim:fdm=marker
