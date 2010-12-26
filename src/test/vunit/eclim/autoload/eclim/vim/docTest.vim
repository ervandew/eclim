" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for doc.vim
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

" TestFindDoc() {{{
function! TestFindDoc()
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test'
  edit! vim/test.vim
  call vunit#PeekRedir()

  call cursor(3, 4)
" This test case may be crashing vim !!
  "call eclim#vim#doc#FindDoc('')
  "call vunit#AssertTrue(&ft == 'help', 'not help file: v:version')
  "call vunit#AssertTrue(getline('.') =~ '\*v:version\*', 'v:version')
  "bdelete

  "call cursor(4, 4)
  "call eclim#vim#doc#FindDoc('')
  "call vunit#AssertTrue(&ft == 'help', 'not help file: :command')
  "call vunit#AssertTrue(getline('.') =~ '\*:command\*', ':command')
  "bdelete

  "call cursor(4, 11)
  "call eclim#vim#doc#FindDoc('')
  "call vunit#AssertTrue(&ft == 'help', 'not help file: -nargs')
  "call vunit#AssertTrue(getline('.') =~ '\*E175\*', '-nargs')
  "bdelete

  "call cursor(4, 20)
  "call eclim#vim#doc#FindDoc('')
  "call vunit#AssertTrue(&ft == 'help', 'not help file: -complete')
  "call vunit#AssertTrue(getline('.') =~ '\*:command-completion\*', '-complete')
  "bdelete

  "call cursor(4, 30)
  "call eclim#vim#doc#FindDoc('')
  "call vunit#AssertTrue(&ft == 'help', 'not help file: customlist')
  "call vunit#AssertTrue(getline('.') =~ '\*:command-completion-customlist\*', 'customlist')
  "bdelete

  "call cursor(4, 65)
  "call eclim#vim#doc#FindDoc('')
  "call vunit#AssertTrue(&ft == 'help', 'not help file: :call')
  "call vunit#AssertTrue(getline('.') =~ '\*:call\*', ':call')
  "bdelete

  "call cursor(4, 56)
  "call eclim#vim#doc#FindDoc('')
  "call vunit#AssertEquals('vim/test.vim', expand('%'))

  "call cursor(6, 24)
  "call eclim#vim#doc#FindDoc('')
  "call vunit#AssertTrue(&ft == 'help', 'not help file: substitute()')
  "call vunit#AssertTrue(getline('.') =~ '\*substitute()\*', 'substitute()')
endfunction " }}}

" vim:ft=vim:fdm=marker
