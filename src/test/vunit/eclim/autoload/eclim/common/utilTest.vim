" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for common.vim
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

" TestDiffLastSaved() {{{
function! TestDiffLastSaved()
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

" TestLocateFile() {{{
function! TestLocateFile()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test/test_root_file.txt

  LocateFile test.vim
  call PeekRedir()
  let name = substitute(expand('%'), '\', '/', 'g')
  call VUAssertEquals(name, 'eclim_unit_test/vim/test.vim')
  bdelete

  LocateFile vcs/merc*/**/file1.txt
  call PeekRedir()
  let name = substitute(expand('%'), '\', '/', 'g')
  call VUAssertEquals(name, 'eclim_unit_test/vcs/mercurial/unittest/test/file1.txt')
  bdelete

  LocateFile
  call PeekRedir()
  call VUAssertEquals(expand('%'), '[Locate in eclim_unit_test]')
  call VUAssertEquals(bufname(b:results_bufnum), '[Locate Results]')
  call setline(1, "> file.txt")
  doautocmd CursorMovedI <buffer>
  doautocmd CursorHoldI <buffer>
  let results = getbufline(b:results_bufnum, 1, '$')
  call VUAssertEquals(len(results), 9)
  call VUAssertEquals(results[0],
    \ 'test_root_file.txt  /eclim_unit_test/test_root_file.txt')
  call VUAssertEquals(sort(results[1:])[0],
    \ 'file1.txt  /eclim_unit_test/vcs/git/unittest/test/file1.txt')
  exec "normal \<esc>"
  doautocmd InsertLeave <buffer>
  call VUAssertNotEquals(expand('%'), '[Locate in eclim_unit_test]')

  LocateFile
  call PeekRedir()
  call VUAssertEquals(expand('%'), '[Locate in eclim_unit_test]')
  call VUAssertEquals(bufname(b:results_bufnum), '[Locate Results]')
  call setline(1, "> vcs/merc/file.txt")
  doautocmd CursorMovedI <buffer>
  doautocmd CursorHoldI <buffer>
  let results = sort(getbufline(b:results_bufnum, 1, '$'))
  call VUAssertEquals(len(results), 4)
  call VUAssertEquals(results[0],
    \ 'file1.txt  /eclim_unit_test/vcs/mercurial/unittest/test/file1.txt')
  call VUAssertEquals(results[1],
    \ 'file2.txt  /eclim_unit_test/vcs/mercurial/unittest/test/file2.txt')
  call VUAssertEquals(results[2],
    \ 'file3.txt  /eclim_unit_test/vcs/mercurial/unittest/test/file3.txt')
  call VUAssertEquals(results[3],
    \ 'file5.txt  /eclim_unit_test/vcs/mercurial/unittest/test/file5.txt')
  exec "normal \<esc>"
endfunction " }}}

" TestOpenRelative() {{{
function! TestOpenRelative()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test/test_root_file.txt

  call eclim#common#util#OpenRelative('edit', 'files/test1.txt', 1)
  call VUAssertTrue(bufwinnr('eclim_unit_test/files/test1.txt') > -1,
    \ 'Did not open test1.txt.')
endfunction " }}}

" TestOpenFiles() {{{
function! TestOpenFiles()
  exec 'cd ' . g:TestEclimWorkspace
  call eclim#common#util#OpenFiles('split',
    \ 'eclim_unit_test/files/test1.txt eclim_unit_test/files/test2.txt')
  call VUAssertTrue(bufwinnr('eclim_unit_test/files/test1.txt') > -1,
    \ 'Did not open test1.txt.')
  call VUAssertTrue(bufwinnr('eclim_unit_test/files/test2.txt') > -1,
    \ 'Did not open test2.txt.')
endfunction " }}}

" TestSwapWords() {{{
function! TestSwapWords()
  call setline(1, 'one, two')
  call cursor(1, 1)
  call eclim#common#util#SwapWords()
  call VUAssertEquals('two, one', getline(1), "Words not swaped correctly.")
endfunction " }}}

" TestCommandCompleteRelative() {{{
function! TestCommandCompleteRelative()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test/test_root_file.txt
  let results = eclim#common#util#CommandCompleteRelative('p', 'SplitRelative f', 15)
  call VUAssertEquals(1, len(results), "Wrong number of results.")
  call VUAssertEquals('files/', results[0], "Wrong result.")
endfunction " }}}

" vim:ft=vim:fdm=marker
