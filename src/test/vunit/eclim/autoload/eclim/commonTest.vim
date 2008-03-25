" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for common.vim
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

" TestOpenRelative() {{{
function! TestOpenRelative ()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test_java/build.xml

  call eclim#common#util#OpenRelative('edit', 'pom.xml', 1)
  call VUAssertTrue(bufwinnr('eclim_unit_test_java/pom.xml') > -1,
    \ 'Did not open pom.xml.')
  bdelete
endfunction " }}}

" TestOpenFiles() {{{
function! TestOpenFiles ()
  exec 'cd ' . g:TestEclimWorkspace
  call eclim#common#util#OpenFiles('split',
    \ 'eclim_unit_test_java/build.xml eclim_unit_test_java/pom.xml')
  call VUAssertTrue(bufwinnr('eclim_unit_test_java/build.xml') > -1,
    \ 'Did not open build.xml.')
  call VUAssertTrue(bufwinnr('eclim_unit_test_java/pom.xml') > -1,
    \ 'Did not open pom.xml.')
  bdelete
  bdelete
endfunction " }}}

" TestSwapWords() {{{
function! TestSwapWords ()
  new
  call setline(1, 'one, two')
  call cursor(1, 1)
  call eclim#common#util#SwapWords()
  call VUAssertEquals('two, one', getline(1), "Words not swaped correctly.")
  bdelete!
endfunction " }}}

" TestCommandCompleteRelative() {{{
function! TestCommandCompleteRelative ()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test_java/build.xml
  let results = eclim#common#util#CommandCompleteRelative('p', 'SplitRelative p', 15)
  call VUAssertEquals(1, len(results), "Wrong number of results.")
  call VUAssertEquals('pom.xml', results[0], "Wrong result.")
  bdelete
endfunction " }}}

" vim:ft=vim:fdm=marker
