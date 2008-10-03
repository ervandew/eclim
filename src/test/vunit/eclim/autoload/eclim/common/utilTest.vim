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

" TestDiffLastSaved() {{{
function! TestDiffLastSaved ()
  exec 'cd ' . g:TestEclimWorkspace
  edit eclim_unit_test/test_root_file.txt
  call append(1, 'some new content')
  DiffLastSaved

  call VUAssertEquals(&diff, 1)
  call VUAssertEquals(line('$'), 2)
  call VUAssertEquals(getline(1), 'file in project root')
  call VUAssertEquals(getline(2), 'some new content')

  winc l

  call VUAssertEquals(&diff, 1)
  call VUAssertEquals(line('$'), 1)
  call VUAssertEquals(getline(1), 'file in project root')

  bdelete!
  bdelete!
endfunction " }}}

" TestOpenRelative() {{{
function! TestOpenRelative ()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test/test_root_file.txt

  call eclim#common#util#OpenRelative('edit', 'files/test1.txt', 1)
  call VUAssertTrue(bufwinnr('eclim_unit_test/files/test1.txt') > -1,
    \ 'Did not open test1.txt.')
endfunction " }}}

" TestOpenFiles() {{{
function! TestOpenFiles ()
  exec 'cd ' . g:TestEclimWorkspace
  call eclim#common#util#OpenFiles('split',
    \ 'eclim_unit_test/files/test1.txt eclim_unit_test/files/test2.txt')
  call VUAssertTrue(bufwinnr('eclim_unit_test/files/test1.txt') > -1,
    \ 'Did not open test1.txt.')
  call VUAssertTrue(bufwinnr('eclim_unit_test/files/test2.txt') > -1,
    \ 'Did not open test2.txt.')
endfunction " }}}

" TestSwapWords() {{{
function! TestSwapWords ()
  call setline(1, 'one, two')
  call cursor(1, 1)
  call eclim#common#util#SwapWords()
  call VUAssertEquals('two, one', getline(1), "Words not swaped correctly.")
endfunction " }}}

" TestCommandCompleteRelative() {{{
function! TestCommandCompleteRelative ()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test/test_root_file.txt
  let results = eclim#common#util#CommandCompleteRelative('p', 'SplitRelative f', 15)
  call VUAssertEquals(1, len(results), "Wrong number of results.")
  call VUAssertEquals('files/', results[0], "Wrong result.")
endfunction " }}}

" vim:ft=vim:fdm=marker
