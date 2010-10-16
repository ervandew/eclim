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
  edit eclim_unit_test/vcs/git.tar.gz
  call VUAssertEquals(line('$'), 4, 'Wrong number of lines in tree: git.tar.gz')
  call VUAssertEquals(getline(1), 'git.tar.gz/')
  call VUAssertEquals(getline(2), '  + git/')

  AsList

  call VUAssertEquals(line('$'), 65, 'Wrong number of lines in list: git.tar.gz')
  call cursor(62, 1)
  exec "normal \<cr>"

  let name = substitute(expand('%:p'), '\', '/', 'g')
  call VUAssertEquals(name, g:EclimTempDir . '/git/unittest/test/file1.txt')
  call VUAssertEquals(line('$'), 5)
  call VUAssertEquals(getline(1), 'file 1')
  call VUAssertEquals(getline(2), 'some first revision content')
  close

  AsTree

  call VUAssertEquals(line('$'), 4, 'Wrong number of lines in tree: git.tar.gz')
  call VUAssertEquals(getline(1), 'git.tar.gz/')
  call VUAssertEquals(getline(2), '  + git/')
endfunction " }}}

" TestArchiveTree() {{{
function! TestArchiveTree()
  edit eclim_unit_test/vcs/git.tar.gz
  call VUAssertEquals(line('$'), 4, 'Wrong number of lines: git.tar.gz')
  call VUAssertEquals(getline(1), 'git.tar.gz/')
  call VUAssertEquals(getline(2), '  + git/')

  call cursor(2, 3)
  exec "normal \<cr>"

  call VUAssertEquals(line('$'), 5, 'Wrong number of lines: git.tar.gz')
  call VUAssertEquals(getline(1), 'git.tar.gz/')
  call VUAssertEquals(getline(2), '  - git/')
  call VUAssertEquals(getline(3), '      + unittest/')

  call cursor(3, 7)
  exec "normal \<cr>"

  call VUAssertEquals(line('$'), 7, 'Wrong number of lines: git.tar.gz')
  call VUAssertEquals(getline(1), 'git.tar.gz/')
  call VUAssertEquals(getline(2), '  - git/')
  call VUAssertEquals(getline(3), '      - unittest/')
  call VUAssertEquals(getline(4), '          + .git/')
  call VUAssertEquals(getline(5), '          + test/')

  call cursor(5, 11)
  exec "normal \<cr>"

  call VUAssertEquals(line('$'), 11, 'Wrong number of lines: git.tar.gz')
  call VUAssertEquals(getline(1), 'git.tar.gz/')
  call VUAssertEquals(getline(2), '  - git/')
  call VUAssertEquals(getline(3), '      - unittest/')
  call VUAssertEquals(getline(4), '          + .git/')
  call VUAssertEquals(getline(5), '          - test/')
  call VUAssertEquals(getline(6), '                file1.txt')
  call VUAssertEquals(getline(7), '                file2.txt')

  call cursor(6, 15)
  exec "normal \<cr>"

  let name = substitute(expand('%:p'), '\', '/', 'g')
  call VUAssertEquals(name, g:EclimTempDir . '/git/unittest/test/file1.txt')
  call VUAssertEquals(line('$'), 5)
  call VUAssertEquals(getline(1), 'file 1')
  call VUAssertEquals(getline(2), 'some first revision content')
endfunction " }}}

" vim:ft=vim:fdm=marker
