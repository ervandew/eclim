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

  call vunit#AssertEquals(&diff, 1)
  call vunit#AssertEquals(line('$'), 2)
  call vunit#AssertEquals(getline(1), 'file in project root')
  call vunit#AssertEquals(getline(2), 'some new content')

  winc l

  call vunit#AssertEquals(&diff, 1)
  call vunit#AssertEquals(line('$'), 1)
  call vunit#AssertEquals(getline(1), 'file in project root')

  bdelete!
  bdelete!
endfunction " }}}

" TestLocateFile() {{{
function! TestLocateFile()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test/test_root_file.txt

  LocateFile test.vim
  call vunit#PeekRedir()
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(name, 'eclim_unit_test/vim/test.vim')
  bdelete

  LocateFile f*/*1.txt
  call vunit#PeekRedir()
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(name, 'eclim_unit_test/files/test1.txt')
  bdelete

  LocateFile
  call vunit#PeekRedir()
  call vunit#AssertEquals(expand('%'), '[Locate in eclim_unit_test]')
  call vunit#AssertEquals(bufname(b:results_bufnum), '[Locate Results]')
  call setline(1, "> test.txt")
  doautocmd CursorMovedI <buffer>
  doautocmd CursorHoldI <buffer>
  let results = sort(getbufline(b:results_bufnum, 1, '$'))
  call vunit#AssertEquals(len(results), 4)
  call vunit#AssertEquals(results[0],
    \ 'test1.txt  /eclim_unit_test/files/test1.txt')
  call vunit#AssertEquals(results[1],
    \ 'test2.txt  /eclim_unit_test/files/test2.txt')
  call vunit#AssertEquals(results[2],
    \ 'test3.txt  /eclim_unit_test/files/test3.txt')
  call vunit#AssertEquals(results[3],
    \ 'test_root_file.txt  /eclim_unit_test/test_root_file.txt')
  exec "normal \<esc>"
  doautocmd InsertLeave <buffer>
  call vunit#AssertNotEquals(expand('%'), '[Locate in eclim_unit_test]')

  LocateFile
  call vunit#PeekRedir()
  call vunit#AssertEquals(expand('%'), '[Locate in eclim_unit_test]')
  call vunit#AssertEquals(bufname(b:results_bufnum), '[Locate Results]')
  call setline(1, "> file/test.txt")
  doautocmd CursorMovedI <buffer>
  doautocmd CursorHoldI <buffer>
  let results = sort(getbufline(b:results_bufnum, 1, '$'))
  call vunit#AssertEquals(len(results), 3)
  call vunit#AssertEquals(results[0],
    \ 'test1.txt  /eclim_unit_test/files/test1.txt')
  call vunit#AssertEquals(results[1],
    \ 'test2.txt  /eclim_unit_test/files/test2.txt')
  call vunit#AssertEquals(results[2],
    \ 'test3.txt  /eclim_unit_test/files/test3.txt')
  exec "normal \<esc>"
endfunction " }}}

" TestSwapWords() {{{
function! TestSwapWords()
  call setline(1, 'one, two')
  call cursor(1, 1)
  call eclim#common#util#SwapWords()
  call vunit#AssertEquals('two, one', getline(1), "Words not swaped correctly.")
endfunction " }}}

" vim:ft=vim:fdm=marker
