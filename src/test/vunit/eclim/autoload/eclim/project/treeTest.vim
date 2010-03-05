" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for project/tree.vim
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
  let g:EclimProjectTreeSharedInstance = 0
endfunction " }}}

" TestProjectTree() {{{
function! TestProjectTree()
  ProjectTree eclim_unit_test
  call VUAssertEquals(expand('%'), 'ProjectTree_1')
  call VUAssertEquals(getline(1), 'eclim_unit_test/')
  call VUAssertEquals(getline(2), '  + files/')
  call VUAssertEquals(getline(3), '  + history/')
  call VUAssertEquals(getline(4), '  + vcs/')
  call VUAssertEquals(getline(5), '  + vim/')
  call VUAssertEquals(getline(6), '  + xml/')
  call VUAssertEquals(getline(7), '    test_root_file.txt')

  normal j
  call VUAssertEquals(line('.'), 2)
  call VUAssertEquals(col('.'), 3)

  normal o
  call VUAssertEquals(getline( 1), 'eclim_unit_test/')
  call VUAssertEquals(getline( 2), '  - files/')
  call VUAssertEquals(getline( 3), '        test1.txt')
  call VUAssertEquals(getline( 4), '        test2.txt')
  call VUAssertEquals(getline( 5), '        test3.txt')
  call VUAssertEquals(getline( 6), '  + history/')
  call VUAssertEquals(getline( 7), '  + vcs/')
  call VUAssertEquals(getline( 8), '  + vim/')
  call VUAssertEquals(getline( 9), '  + xml/')
  call VUAssertEquals(getline(10), '    test_root_file.txt')

  normal o
  call VUAssertEquals(foldclosed(2), 2, 'Wrong fold start line.')
  call VUAssertEquals(foldclosedend(2), 5, 'Wrong fold end line.')
  call VUAssertEquals(foldtextresult(2), '  - files/')

  normal o
  call VUAssertEquals(foldclosed(2), -1, 'Wrong open fold start line.')

  normal j
  call VUAssertEquals(line('.'), 3)
  call VUAssertEquals(col('.'), 8)
  exec "normal \<cr>"
  let name = substitute(expand('%'), '\', '/', 'g')
  call VUAssertEquals(name, 'eclim_unit_test/files/test1.txt')
  call VUAssertEquals(getline(1), 'test file 1')
  bdelete
  winc h

  normal o
  call VUAssertEquals(getline(1), 'Split')
  call VUAssertEquals(getline(2), 'Tab')
  call VUAssertEquals(getline(3), 'Edit')

  normal j
  exec "normal \<cr>"
  let name = substitute(expand('%'), '\', '/', 'g')
  call VUAssertTrue(name =~ 'eclim_unit_test/files/test1.txt$')
  call VUAssertEquals(tabpagenr(), 2)
  call VUAssertEquals(getline(1), 'test file 1')
  bdelete
  call VUAssertEquals(tabpagenr(), 1)
endfunction " }}}

" TestProjectTreeMultiple() {{{
function! TestProjectTreeMultiple()
  ProjectTree eclim_unit_test eclim_unit_test_java eclim_unit_test_web
  call VUAssertEquals(expand('%'), 'ProjectTree_1')
  call VUAssertEquals(getline(1), 'eclim_unit_test/')
  call VUAssertEquals(getline(2), 'eclim_unit_test_java/')
  call VUAssertEquals(getline(3), 'eclim_unit_test_web/')

  normal o
  call VUAssertEquals(getline(1), 'eclim_unit_test/')
  call VUAssertEquals(getline(2), '  + files/')
  call VUAssertEquals(getline(3), '  + history/')
  call VUAssertEquals(getline(4), '  + vcs/')
  call VUAssertEquals(getline(5), '  + vim/')
  call VUAssertEquals(getline(6), '  + xml/')
  call VUAssertEquals(getline(7), '    test_root_file.txt')
  call VUAssertEquals(getline(8), 'eclim_unit_test_java/')
  call VUAssertEquals(getline(9), 'eclim_unit_test_web/')

  normal 7j
  call VUAssertEquals(line('.'), 8)
  call VUAssertEquals(col('.'), 1)
  call VUAssertEquals(getline('.'), 'eclim_unit_test_java/')
  normal o
  call VUAssertEquals(getline( 1), 'eclim_unit_test/')
  call VUAssertEquals(getline( 2), '  + files/')
  call VUAssertEquals(getline( 3), '  + history/')
  call VUAssertEquals(getline( 4), '  + vcs/')
  call VUAssertEquals(getline( 5), '  + vim/')
  call VUAssertEquals(getline( 6), '  + xml/')
  call VUAssertEquals(getline( 7), '    test_root_file.txt')
  call VUAssertEquals(getline( 8), 'eclim_unit_test_java/')
  call VUAssertEquals(getline( 9), '  + bin/')
  call VUAssertEquals(getline(10), '  + doc/')
  call VUAssertEquals(getline(11), '  + log4j/')
  call VUAssertEquals(getline(12), '  + src/')
  call VUAssertEquals(getline(13), '  + src-javac/')
  call VUAssertEquals(getline(14), '  + webxml/')
  call VUAssertEquals(getline(15), '    build.xml')
  call VUAssertEquals(getline(16), '    checkstyle.xml')
  call VUAssertEquals(getline(17), '    pom.xml')
  call VUAssertEquals(getline(18), 'eclim_unit_test_web/')

  normal 10j
  call VUAssertEquals(line('.'), 18)
  call VUAssertEquals(col('.'), 1)
  call VUAssertEquals(getline('.'), 'eclim_unit_test_web/')
  normal o
  call VUAssertEquals(getline( 1), 'eclim_unit_test/')
  call VUAssertEquals(getline( 2), '  + files/')
  call VUAssertEquals(getline( 3), '  + history/')
  call VUAssertEquals(getline( 4), '  + vcs/')
  call VUAssertEquals(getline( 5), '  + vim/')
  call VUAssertEquals(getline( 6), '  + xml/')
  call VUAssertEquals(getline( 7), '    test_root_file.txt')
  call VUAssertEquals(getline( 8), 'eclim_unit_test_java/')
  call VUAssertEquals(getline( 9), '  + bin/')
  call VUAssertEquals(getline(10), '  + doc/')
  call VUAssertEquals(getline(11), '  + log4j/')
  call VUAssertEquals(getline(12), '  + src/')
  call VUAssertEquals(getline(13), '  + src-javac/')
  call VUAssertEquals(getline(14), '  + webxml/')
  call VUAssertEquals(getline(15), '    build.xml')
  call VUAssertEquals(getline(16), '    checkstyle.xml')
  call VUAssertEquals(getline(17), '    pom.xml')
  call VUAssertEquals(getline(18), 'eclim_unit_test_web/')
  call VUAssertEquals(getline(19), '  + css/')
  call VUAssertEquals(getline(20), '  + dtd/')
endfunction " }}}

" vim:ft=vim:fdm=marker
