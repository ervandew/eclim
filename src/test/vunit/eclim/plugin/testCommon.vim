" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for common.vim
"
" License:
"
" Copyright (C) 2005 - 2009  Eric Van Dewoestine
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

" TestSplit() {{{
function! TestSplit()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test/test_root_file.txt

  Split eclim_unit_test/files/test1.txt eclim_unit_test/files/test2.txt

  call VUAssertEquals(winnr('$'), 3)
  call VUAssertTrue(bufwinnr('eclim_unit_test/files/test1.txt') > -1,
    \ 'Did not open test1.txt.')
  call VUAssertTrue(bufwinnr('eclim_unit_test/files/test2.txt') > -1,
    \ 'Did not open test2.txt.')
endfunction " }}}

" TestTabnew() {{{
function! TestTabnew()
  exec 'cd ' . g:TestEclimWorkspace

  Tabnew eclim_unit_test/files/test1.txt eclim_unit_test/files/test2.txt

  call VUAssertEquals(tabpagenr('$'), 3)
  tabnext 2
  call VUAssertTrue(bufwinnr('eclim_unit_test/files/test1.txt') > -1,
    \ 'Did not open test1.txt.')
  tabnext 3
  call VUAssertTrue(bufwinnr('eclim_unit_test/files/test2.txt') > -1,
    \ 'Did not open test2.txt.')
endfunction " }}}

" TestEditRelative() {{{
function! TestEditRelative()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test/test_root_file.txt

  EditRelative files/test1.txt

  call VUAssertEquals(winnr('$'), 1)
  call VUAssertTrue(bufwinnr('eclim_unit_test/files/test1.txt') > -1,
    \ 'Did not open test1.txt.')
  call VUAssertEquals(getline(1), 'test file 1')
endfunction " }}}

" TestSplitRelative() {{{
function! TestSplitRelative()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test/test_root_file.txt

  SplitRelative files/test1.txt

  call VUAssertEquals(winnr('$'), 2)
  call VUAssertTrue(bufwinnr('eclim_unit_test/files/test1.txt') > -1,
    \ 'Did not open test1.txt.')
  call VUAssertEquals(getline(1), 'test file 1')
endfunction " }}}

" TestTabnewRelative() {{{
function! TestTabnewRelative()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test/test_root_file.txt

  TabnewRelative files/test1.txt files/test2.txt

  call VUAssertEquals(tabpagenr('$'), 3)
  tabnext 2
  call VUAssertTrue(bufwinnr('eclim_unit_test/files/test1.txt') > -1,
    \ 'Did not open test1.txt.')
  call VUAssertEquals(getline(1), 'test file 1')

  tabnext 3
  call VUAssertTrue(bufwinnr('eclim_unit_test/files/test2.txt') > -1,
    \ 'Did not open test2.txt.')
  call VUAssertEquals(getline(1), 'test file 2')
endfunction " }}}

" TestReadRelative() {{{
function! TestReadRelative()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test/test_root_file.txt
  call cursor(line('$'), 1)

  ReadRelative files/test2.txt

  call VUAssertTrue(getline(2) =~ 'test file 2')
endfunction " }}}

" TestArgsRelative() {{{
function! TestArgsRelative()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test/test_root_file.txt

  Buffers
  call VUAssertEquals(line('$'), 1, string(getline(1, line('$'))))
  close

  ArgsRelative files/test2.txt files/test3.txt

  Buffers
  call VUAssertEquals(line('$'), 3)
endfunction " }}}

" TestOnly() {{{
function! TestOnly()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test/test_root_file.txt
  Tlist

  Split eclim_unit_test/files/test1.txt eclim_unit_test/files/test2.txt

  call VUAssertEquals(winnr('$'), 4)
  call VUAssertTrue(bufwinnr('eclim_unit_test/files/test1.txt') > -1,
    \ 'Did not open test1.txt.')
  call VUAssertTrue(bufwinnr('eclim_unit_test/files/test2.txt') > -1,
    \ 'Did not open test2.txt.')

  Only
  call VUAssertEquals(winnr('$'), 2)
  call VUAssertTrue(bufwinnr('__Tag_List__') > -1, 'Taglist not open.')
  call VUAssertTrue(bufwinnr('eclim_unit_test/files/test2.txt') > -1,
    \ 'test2.txt not open.')
endfunction " }}}

" TestBuffers() {{{
function! TestBuffers()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test/test_root_file.txt

  Buffers
  call VUAssertEquals(line('$'), 1, string(getline(1, line('$'))))
  close

  argadd eclim_unit_test/files/test1.txt eclim_unit_test/files/test2.txt

  Buffers
  call VUAssertEquals(line('$'), 3)
  call VUAssertTrue(getline(1) =~ 'hidden\s\+test1.txt', 'test1.txt not found 1')
  call VUAssertTrue(getline(2) =~ 'hidden\s\+test2.txt', 'test2.txt not found 1')
  call VUAssertTrue(getline(3) =~ 'active\s\+test_root_file.txt',
        \ 'test_root_file.txt not found 1')

  " test edit
  call cursor(2, 1)
  normal E
  call VUAssertEquals(winnr('$'), 1, 'wrong number of windows after edit')
  Buffers
  call VUAssertEquals(line('$'), 3)
  call VUAssertTrue(getline(1) =~ 'hidden\s\+test1.txt', 'test1.txt not found 2')
  call VUAssertTrue(getline(2) =~ 'active\s\+test2.txt', 'test2.txt not found 2')
  call VUAssertTrue(getline(3) =~ 'hidden\s\+test_root_file.txt',
        \ 'test_root_file.txt not found 2')

  " test split
  call cursor(2, 1)
  normal S
  call VUAssertEquals(winnr('$'), 1, 'wrong number of windows after split existing')
  Buffers
  call cursor(1, 1)
  normal S
  call VUAssertEquals(winnr('$'), 2, 'wrong number of windows after split')
  Buffers
  call VUAssertEquals(line('$'), 3)
  call VUAssertTrue(getline(1) =~ 'active\s\+test1.txt', 'test1.txt not found 3')
  call VUAssertTrue(getline(2) =~ 'active\s\+test2.txt', 'test2.txt not found 3')
  call VUAssertTrue(getline(3) =~ 'hidden\s\+test_root_file.txt',
        \ 'test_root_file.txt not found 3')

  " test tabnew
  call cursor(3, 1)
  normal T
  call VUAssertEquals(winnr('$'), 1, 'wrong number of windows after tabnew')
  call VUAssertEquals(tabpagenr('$'), 2, 'wrong number of tabs after tabnew')
  Buffers
  call VUAssertEquals(line('$'), 3)
  call VUAssertTrue(getline(1) =~ 'active\s\+test1.txt', 'test1.txt not found 4')
  call VUAssertTrue(getline(2) =~ 'active\s\+test2.txt', 'test2.txt not found 4')
  call VUAssertTrue(getline(3) =~ 'active\s\+test_root_file.txt',
        \ 'test_root_file.txt not found 4')
endfunction " }}}

" vim:ft=vim:fdm=marker
