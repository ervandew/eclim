" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for doc.vim
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
