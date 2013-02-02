" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for project/tree.vim
"
" License:
"
" Copyright (C) 2005 - 2013  Eric Van Dewoestine
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
  let g:EclimProjectTreeSharedInstance = 0
endfunction " }}}

function! TestProjectTree() " {{{
  let workspace = eclim#project#util#GetProjectWorkspace('eclim_unit_test')
  ProjectTree eclim_unit_test
  call vunit#AssertEquals(expand('%'), 'ProjectTree_1')
  call vunit#AssertEquals(getline(1), 'eclim_unit_test/')
  call vunit#AssertEquals(getline(2), '  + bin/')
  call vunit#AssertEquals(getline(3), '  + files/')
  call vunit#AssertEquals(getline(4), '  + history/')
  call vunit#AssertEquals(getline(5), '  + linked -> ' . workspace . '/eclim_unit_test_linked/other/')
  call vunit#AssertEquals(getline(6), '  + src/')
  call vunit#AssertEquals(getline(7), '  + vim/')
  call vunit#AssertEquals(getline(8), '  + xml/')
  call vunit#AssertEquals(getline(9), '    test_root_file.txt')

  normal jj
  call vunit#AssertEquals(line('.'), 3)
  call vunit#AssertEquals(col('.'), 3)

  normal o
  call vunit#AssertEquals(getline( 1), 'eclim_unit_test/')
  call vunit#AssertEquals(getline( 2), '  + bin/')
  call vunit#AssertEquals(getline( 3), '  - files/')
  call vunit#AssertEquals(getline( 4), '        test1.txt')
  call vunit#AssertEquals(getline( 5), '        test2.txt')
  call vunit#AssertEquals(getline( 6), '        test3.txt')
  call vunit#AssertEquals(getline( 7), '  + history/')
  call vunit#AssertEquals(getline( 8), '  + linked -> ' . workspace . '/eclim_unit_test_linked/other/')
  call vunit#AssertEquals(getline( 9), '  + src/')
  call vunit#AssertEquals(getline(10), '  + vim/')
  call vunit#AssertEquals(getline(11), '  + xml/')
  call vunit#AssertEquals(getline(12), '    test_root_file.txt')

  call cursor(8, 5)
  normal o
  call vunit#AssertEquals(getline( 1), 'eclim_unit_test/')
  call vunit#AssertEquals(getline( 2), '  + bin/')
  call vunit#AssertEquals(getline( 3), '  - files/')
  call vunit#AssertEquals(getline( 4), '        test1.txt')
  call vunit#AssertEquals(getline( 5), '        test2.txt')
  call vunit#AssertEquals(getline( 6), '        test3.txt')
  call vunit#AssertEquals(getline( 7), '  + history/')
  call vunit#AssertEquals(getline( 8), '  - linked -> ' . workspace . '/eclim_unit_test_linked/other/')
  call vunit#AssertEquals(getline( 9), '      + foo/')
  call vunit#AssertEquals(getline(10), '  + src/')
  call vunit#AssertEquals(getline(11), '  + vim/')
  call vunit#AssertEquals(getline(12), '  + xml/')
  call vunit#AssertEquals(getline(13), '    test_root_file.txt')

  normal j
  normal o
  call vunit#AssertEquals(getline( 1), 'eclim_unit_test/')
  call vunit#AssertEquals(getline( 2), '  + bin/')
  call vunit#AssertEquals(getline( 3), '  - files/')
  call vunit#AssertEquals(getline( 4), '        test1.txt')
  call vunit#AssertEquals(getline( 5), '        test2.txt')
  call vunit#AssertEquals(getline( 6), '        test3.txt')
  call vunit#AssertEquals(getline( 7), '  + history/')
  call vunit#AssertEquals(getline( 8), '  - linked -> ' . workspace . '/eclim_unit_test_linked/other/')
  call vunit#AssertEquals(getline( 9), '      - foo/')
  call vunit#AssertEquals(getline(10), '            bar.txt')
  call vunit#AssertEquals(getline(11), '  + src/')
  call vunit#AssertEquals(getline(12), '  + vim/')
  call vunit#AssertEquals(getline(13), '  + xml/')
  call vunit#AssertEquals(getline(14), '    test_root_file.txt')

  call cursor(3, 5)
  normal o
  call vunit#AssertEquals(foldclosed(3), 3, 'Wrong fold start line.')
  call vunit#AssertEquals(foldclosedend(3), 6, 'Wrong fold end line.')
  call vunit#AssertEquals(foldtextresult(3), '  - files/')

  normal o
  call vunit#AssertEquals(foldclosed(3), -1, 'Wrong open fold start line.')

  normal j
  call vunit#AssertEquals(line('.'), 4)
  call vunit#AssertEquals(col('.'), 8)
  exec "normal \<cr>"
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(name, 'eclim_unit_test/files/test1.txt')
  call vunit#AssertEquals(getline(1), 'test file 1')
  bdelete
  winc h

  normal o
  call vunit#AssertEquals(getline(1), 'Split')
  call vunit#AssertEquals(getline(2), 'VSplit')
  call vunit#AssertEquals(getline(3), 'Tab')
  call vunit#AssertEquals(getline(4), 'Edit')

  normal 2j
  exec "normal \<cr>"
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertTrue(name =~ 'eclim_unit_test/files/test1.txt$')
  call vunit#AssertEquals(tabpagenr(), 2)
  call vunit#AssertEquals(getline(1), 'test file 1')
  bdelete
  call vunit#AssertEquals(tabpagenr(), 1)
endfunction " }}}

" vim:ft=vim:fdm=marker
