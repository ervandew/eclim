" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for archive.vim
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

" SetUp() {{{
function! SetUp()
  exec 'cd ' . g:TestEclimWorkspace
endfunction " }}}

" TestArchiveList() {{{
function! TestArchiveList()
  edit eclim_unit_test/test_archive.tar.gz
  call VUAssertEquals(line('$'), 4, 'Wrong number of lines in tree: test_archive.tar.gz')
  call VUAssertEquals(getline(1), 'test_archive.tar.gz/')
  call VUAssertEquals(getline(2), '  + test_archive/')

  AsList

  call VUAssertEquals(line('$'), 6, 'Wrong number of lines in list: test_archive.tar.gz')
  call cursor(4, 1)
  exec "normal \<cr>"

  let name = substitute(expand('%:p'), '\', '/', 'g')
  call VUAssertEquals(name, g:EclimTempDir . '/test_archive/dir1/file1.txt')
  call VUAssertEquals(line('$'), 2)
  call VUAssertEquals(getline(1), 'file')
  call VUAssertEquals(getline(2), 'one')
  close

  AsTree

  call VUAssertEquals(line('$'), 4, 'Wrong number of lines in tree: test_archive.tar.gz')
  call VUAssertEquals(getline(1), 'test_archive.tar.gz/')
  call VUAssertEquals(getline(2), '  + test_archive/')
endfunction " }}}

" TestArchiveTree() {{{
function! TestArchiveTree()
  edit eclim_unit_test/test_archive.tar.gz
  call VUAssertEquals(line('$'), 4, 'Wrong number of lines: test_archive.tar.gz')
  call VUAssertEquals(getline(1), 'test_archive.tar.gz/')
  call VUAssertEquals(getline(2), '  + test_archive/')

  call cursor(2, 3)
  exec "normal \<cr>"

  call VUAssertEquals(line('$'), 7, 'Wrong number of lines: test_archive.tar.gz')
  call VUAssertEquals(getline(1), 'test_archive.tar.gz/')
  call VUAssertEquals(getline(2), '  - test_archive/')
  call VUAssertEquals(getline(3), '      + dir1/')
  call VUAssertEquals(getline(4), '      + dir2/')
  call VUAssertEquals(getline(5), '        root_file.txt')

  call cursor(3, 7)
  exec "normal \<cr>"

  call VUAssertEquals(line('$'), 9, 'Wrong number of lines: test_archive.tar.gz')
  call VUAssertEquals(getline(1), 'test_archive.tar.gz/')
  call VUAssertEquals(getline(2), '  - test_archive/')
  call VUAssertEquals(getline(3), '      - dir1/')
  call VUAssertEquals(getline(4), '          + child1/')
  call VUAssertEquals(getline(5), '            file1.txt')
  call VUAssertEquals(getline(6), '      + dir2/')
  call VUAssertEquals(getline(7), '        root_file.txt')

  call cursor(4, 11)
  exec "normal \<cr>"

  call VUAssertEquals(line('$'), 10, 'Wrong number of lines: test_archive.tar.gz')
  call VUAssertEquals(getline(1), 'test_archive.tar.gz/')
  call VUAssertEquals(getline(2), '  - test_archive/')
  call VUAssertEquals(getline(3), '      - dir1/')
  call VUAssertEquals(getline(4), '          - child1/')
  call VUAssertEquals(getline(5), '                child_file1.txt')
  call VUAssertEquals(getline(6), '            file1.txt')
  call VUAssertEquals(getline(7), '      + dir2/')
  call VUAssertEquals(getline(8), '        root_file.txt')

  call cursor(6, 15)
  exec "normal \<cr>"

  let name = substitute(expand('%:p'), '\', '/', 'g')
  call VUAssertEquals(name, g:EclimTempDir . '/test_archive/dir1/file1.txt')
  call VUAssertEquals(line('$'), 2)
  call VUAssertEquals(getline(1), 'file')
  call VUAssertEquals(getline(2), 'one')
endfunction " }}}

" vim:ft=vim:fdm=marker
