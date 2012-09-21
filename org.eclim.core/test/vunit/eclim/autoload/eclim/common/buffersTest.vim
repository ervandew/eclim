" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for buffers.vim
"
" License:
"
" Copyright (C) 2005 - 2012  Eric Van Dewoestine
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

function! SetUp() " {{{
  exec 'cd ' . g:TestEclimWorkspace
endfunction " }}}

function! TestOnly() " {{{
  call vunit#AssertEquals(winnr('$'), 1)
  edit! eclim_unit_test/test_root_file.txt
  ProjectTree
  winc w

  split eclim_unit_test/files/test1.txt
  split eclim_unit_test/files/test2.txt

  call vunit#AssertEquals(winnr('$'), 4)
  call vunit#AssertTrue(bufwinnr('eclim_unit_test/files/test1.txt') > -1,
    \ 'Did not open test1.txt.')
  call vunit#AssertTrue(bufwinnr('eclim_unit_test/files/test2.txt') > -1,
    \ 'Did not open test2.txt.')

  Only
  call vunit#AssertEquals(winnr('$'), 2, 'Too many windows after :Only')
  call vunit#AssertTrue(bufwinnr('ProjectTree_1') > -1, 'Project tree not open.')
  call vunit#AssertTrue(bufwinnr('eclim_unit_test/files/test2.txt') > -1,
    \ 'test2.txt not open.')
endfunction " }}}

function! TestBuffers() " {{{
  edit! eclim_unit_test/test_root_file.txt

  Buffers
  call vunit#AssertEquals(line('$'), 3, string(getline(1, line('$'))))
  close

  argadd eclim_unit_test/files/test1.txt eclim_unit_test/files/test2.txt

  Buffers
  call vunit#AssertEquals(line('$'), 5)
  call vunit#AssertTrue(getline(1) =~ 'hidden\s\+test1.txt', 'test1.txt not found 1')
  call vunit#AssertTrue(getline(2) =~ 'hidden\s\+test2.txt', 'test2.txt not found 1')
  call vunit#AssertTrue(getline(3) =~ 'active\s\+test_root_file.txt',
        \ 'test_root_file.txt not found 1')
  call vunit#AssertTrue(getline(4) == '', 'empty line before help text not found')
  call vunit#AssertTrue(getline(5) == '" use ? to view help', 'help text not found')

  " test edit
  call cursor(2, 1)
  normal E
  call vunit#AssertEquals(winnr('$'), 1, 'wrong number of windows after edit')
  Buffers
  call vunit#AssertEquals(line('$'), 5)
  call vunit#AssertTrue(getline(1) =~ 'hidden\s\+test1.txt', 'test1.txt not found 2')
  call vunit#AssertTrue(getline(2) =~ 'active\s\+test2.txt', 'test2.txt not found 2')
  call vunit#AssertTrue(getline(3) =~ 'hidden\s\+test_root_file.txt',
        \ 'test_root_file.txt not found 2')

  " test split
  call cursor(2, 1)
  normal S
  call vunit#AssertEquals(winnr('$'), 1, 'wrong number of windows after split existing')
  Buffers
  call cursor(1, 1)
  normal S
  call vunit#AssertEquals(winnr('$'), 2, 'wrong number of windows after split')
  Buffers
  call vunit#AssertEquals(line('$'), 5)
  call vunit#AssertTrue(getline(1) =~ 'active\s\+test1.txt', 'test1.txt not found 3')
  call vunit#AssertTrue(getline(2) =~ 'active\s\+test2.txt', 'test2.txt not found 3')
  call vunit#AssertTrue(getline(3) =~ 'hidden\s\+test_root_file.txt',
        \ 'test_root_file.txt not found 3')

  " test tabnew
  call cursor(3, 1)
  normal T
  call vunit#AssertEquals(winnr('$'), 1, 'wrong number of windows after tabnew')
  call vunit#AssertEquals(tabpagenr('$'), 2, 'wrong number of tabs after tabnew')
  Buffers
  call vunit#AssertEquals(line('$'), 3, 'wrong number Buffers after tabnew')
  call vunit#AssertTrue(getline(1) =~ 'active\s\+test_root_file.txt',
        \ 'test_root_file.txt not found 4')
  Buffers!
  call vunit#AssertEquals(line('$'), 5, 'wrong number of Buffers! after tabnew')
  call vunit#AssertTrue(getline(1) =~ 'active\s\+test1.txt', 'test1.txt not found 4')
  call vunit#AssertTrue(getline(2) =~ 'active\s\+test2.txt', 'test2.txt not found 4')
  call vunit#AssertTrue(getline(3) =~ 'active\s\+test_root_file.txt',
        \ 'test_root_file.txt not found 4')
endfunction " }}}

" vim:ft=vim:fdm=marker
