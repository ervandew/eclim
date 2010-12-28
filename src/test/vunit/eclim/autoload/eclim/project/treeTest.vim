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
  call vunit#AssertEquals(expand('%'), 'ProjectTree_1')
  call vunit#AssertEquals(getline(1), 'eclim_unit_test/')
  call vunit#AssertEquals(getline(2), '  + files/')
  call vunit#AssertEquals(getline(3), '  + history/')
  call vunit#AssertEquals(getline(4), '  + vim/')
  call vunit#AssertEquals(getline(5), '  + xml/')
  call vunit#AssertEquals(getline(6), '    test_root_file.txt')

  normal j
  call vunit#AssertEquals(line('.'), 2)
  call vunit#AssertEquals(col('.'), 3)

  normal o
  call vunit#AssertEquals(getline(1), 'eclim_unit_test/')
  call vunit#AssertEquals(getline(2), '  - files/')
  call vunit#AssertEquals(getline(3), '        test1.txt')
  call vunit#AssertEquals(getline(4), '        test2.txt')
  call vunit#AssertEquals(getline(5), '        test3.txt')
  call vunit#AssertEquals(getline(6), '  + history/')
  call vunit#AssertEquals(getline(7), '  + vim/')
  call vunit#AssertEquals(getline(8), '  + xml/')
  call vunit#AssertEquals(getline(9), '    test_root_file.txt')

  normal o
  call vunit#AssertEquals(foldclosed(2), 2, 'Wrong fold start line.')
  call vunit#AssertEquals(foldclosedend(2), 5, 'Wrong fold end line.')
  call vunit#AssertEquals(foldtextresult(2), '  - files/')

  normal o
  call vunit#AssertEquals(foldclosed(2), -1, 'Wrong open fold start line.')

  normal j
  call vunit#AssertEquals(line('.'), 3)
  call vunit#AssertEquals(col('.'), 8)
  exec "normal \<cr>"
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertEquals(name, 'eclim_unit_test/files/test1.txt')
  call vunit#AssertEquals(getline(1), 'test file 1')
  bdelete
  winc h

  normal o
  call vunit#AssertEquals(getline(1), 'Split')
  call vunit#AssertEquals(getline(2), 'Tab')
  call vunit#AssertEquals(getline(3), 'Edit')

  normal j
  exec "normal \<cr>"
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertTrue(name =~ 'eclim_unit_test/files/test1.txt$')
  call vunit#AssertEquals(tabpagenr(), 2)
  call vunit#AssertEquals(getline(1), 'test file 1')
  bdelete
  call vunit#AssertEquals(tabpagenr(), 1)
endfunction " }}}

" TestProjectTreeWithEclipseLinks() {{{
function! TestProjectTree()
  let workspace = eclim#project#util#GetProjectWorkspace('eclim_unit_test_java')
  ProjectTree eclim_unit_test_java
  call vunit#AssertEquals(getline( 1), 'eclim_unit_test_java/')
  call vunit#AssertEquals(getline( 2), '  + bin/')
  call vunit#AssertEquals(getline( 3), '  + doc/')
  call vunit#AssertEquals(getline( 4), '  + log4j/')
  call vunit#AssertEquals(getline( 5), '  + src/')
  call vunit#AssertEquals(getline( 6), '  + src-javac/')
  call vunit#AssertEquals(getline( 7), '  + src-linked -> ' . workspace . 'eclim_unit_test_java_linked/src/')
  call vunit#AssertEquals(getline( 8), '  + webxml/')
  call vunit#AssertEquals(getline( 9), '  + zlink -> ' . workspace . 'eclim_unit_test_java_linked/other/')
  call vunit#AssertEquals(getline(10), '    build.xml')
  call vunit#AssertEquals(getline(11), '    checkstyle.xml')
  call vunit#AssertEquals(getline(12), '    pom.xml')

  normal 6jo
  call vunit#AssertEquals(getline( 1), 'eclim_unit_test_java/')
  call vunit#AssertEquals(getline( 2), '  + bin/')
  call vunit#AssertEquals(getline( 3), '  + doc/')
  call vunit#AssertEquals(getline( 4), '  + log4j/')
  call vunit#AssertEquals(getline( 5), '  + src/')
  call vunit#AssertEquals(getline( 6), '  + src-javac/')
  call vunit#AssertEquals(getline( 7), '  - src-linked -> ' . workspace . 'eclim_unit_test_java_linked/src/')
  call vunit#AssertEquals(getline( 8), '      + org/')
  call vunit#AssertEquals(getline( 9), '  + webxml/')
  call vunit#AssertEquals(getline(10), '  + zlink -> ' . workspace . 'eclim_unit_test_java_linked/other/')
  call vunit#AssertEquals(getline(11), '    build.xml')
  call vunit#AssertEquals(getline(12), '    checkstyle.xml')
  call vunit#AssertEquals(getline(13), '    pom.xml')

  normal jo
  call vunit#AssertEquals(getline( 1), 'eclim_unit_test_java/')
  call vunit#AssertEquals(getline( 2), '  + bin/')
  call vunit#AssertEquals(getline( 3), '  + doc/')
  call vunit#AssertEquals(getline( 4), '  + log4j/')
  call vunit#AssertEquals(getline( 5), '  + src/')
  call vunit#AssertEquals(getline( 6), '  + src-javac/')
  call vunit#AssertEquals(getline( 7), '  - src-linked -> ' . workspace . 'eclim_unit_test_java_linked/src/')
  call vunit#AssertEquals(getline( 8), '      - org/')
  call vunit#AssertEquals(getline( 9), '          + eclim/')
  call vunit#AssertEquals(getline(10), '  + webxml/')
  call vunit#AssertEquals(getline(11), '  + zlink -> ' . workspace . 'eclim_unit_test_java_linked/other/')
  call vunit#AssertEquals(getline(12), '    build.xml')
  call vunit#AssertEquals(getline(13), '    checkstyle.xml')
  call vunit#AssertEquals(getline(14), '    pom.xml')

  normal 3jo
  call vunit#AssertEquals(getline( 1), 'eclim_unit_test_java/')
  call vunit#AssertEquals(getline( 2), '  + bin/')
  call vunit#AssertEquals(getline( 3), '  + doc/')
  call vunit#AssertEquals(getline( 4), '  + log4j/')
  call vunit#AssertEquals(getline( 5), '  + src/')
  call vunit#AssertEquals(getline( 6), '  + src-javac/')
  call vunit#AssertEquals(getline( 7), '  - src-linked -> ' . workspace . 'eclim_unit_test_java_linked/src/')
  call vunit#AssertEquals(getline( 8), '      - org/')
  call vunit#AssertEquals(getline( 9), '          + eclim/')
  call vunit#AssertEquals(getline(10), '  + webxml/')
  call vunit#AssertEquals(getline(11), '  - zlink -> ' . workspace . 'eclim_unit_test_java_linked/other/')
  call vunit#AssertEquals(getline(12), '      + foo/')
  call vunit#AssertEquals(getline(13), '    build.xml')
  call vunit#AssertEquals(getline(14), '    checkstyle.xml')
  call vunit#AssertEquals(getline(15), '    pom.xml')

  normal ggR
  call vunit#AssertEquals(getline( 1), 'eclim_unit_test_java/')
  call vunit#AssertEquals(getline( 2), '  + bin/')
  call vunit#AssertEquals(getline( 3), '  + doc/')
  call vunit#AssertEquals(getline( 4), '  + log4j/')
  call vunit#AssertEquals(getline( 5), '  + src/')
  call vunit#AssertEquals(getline( 6), '  + src-javac/')
  call vunit#AssertEquals(getline( 7), '  - src-linked -> ' . workspace . 'eclim_unit_test_java_linked/src/')
  call vunit#AssertEquals(getline( 8), '      - org/')
  call vunit#AssertEquals(getline( 9), '          + eclim/')
  call vunit#AssertEquals(getline(10), '  + webxml/')
  call vunit#AssertEquals(getline(11), '  - zlink -> ' . workspace . 'eclim_unit_test_java_linked/other/')
  call vunit#AssertEquals(getline(12), '      + foo/')
  call vunit#AssertEquals(getline(13), '    build.xml')
  call vunit#AssertEquals(getline(14), '    checkstyle.xml')
  call vunit#AssertEquals(getline(15), '    pom.xml')
endfunction " }}}

" TestProjectTreeMultiple() {{{
function! TestProjectTreeMultiple()
  let workspace = eclim#project#util#GetProjectWorkspace('eclim_unit_test_java')
  ProjectTree eclim_unit_test eclim_unit_test_java eclim_unit_test_web
  call vunit#AssertEquals(expand('%'), 'ProjectTree_1')
  call vunit#AssertEquals(getline(1), 'eclim_unit_test/')
  call vunit#AssertEquals(getline(2), 'eclim_unit_test_java/')
  call vunit#AssertEquals(getline(3), 'eclim_unit_test_web/')

  normal o
  call vunit#AssertEquals(getline(1), 'eclim_unit_test/')
  call vunit#AssertEquals(getline(2), '  + files/')
  call vunit#AssertEquals(getline(3), '  + history/')
  call vunit#AssertEquals(getline(4), '  + vim/')
  call vunit#AssertEquals(getline(5), '  + xml/')
  call vunit#AssertEquals(getline(6), '    test_root_file.txt')
  call vunit#AssertEquals(getline(7), 'eclim_unit_test_java/')
  call vunit#AssertEquals(getline(8), 'eclim_unit_test_web/')

  normal 6j
  call vunit#AssertEquals(line('.'), 7)
  call vunit#AssertEquals(col('.'), 1)
  call vunit#AssertEquals(getline('.'), 'eclim_unit_test_java/')
  normal o
  call vunit#AssertEquals(getline( 1), 'eclim_unit_test/')
  call vunit#AssertEquals(getline( 2), '  + files/')
  call vunit#AssertEquals(getline( 3), '  + history/')
  call vunit#AssertEquals(getline( 4), '  + vim/')
  call vunit#AssertEquals(getline( 5), '  + xml/')
  call vunit#AssertEquals(getline( 6), '    test_root_file.txt')
  call vunit#AssertEquals(getline( 7), 'eclim_unit_test_java/')
  call vunit#AssertEquals(getline( 8), '  + bin/')
  call vunit#AssertEquals(getline( 9), '  + doc/')
  call vunit#AssertEquals(getline(10), '  + log4j/')
  call vunit#AssertEquals(getline(11), '  + src/')
  call vunit#AssertEquals(getline(12), '  + src-javac/')
  call vunit#AssertEquals(getline(13), '  + src-linked -> ' . workspace . 'eclim_unit_test_java_linked/src/')
  call vunit#AssertEquals(getline(14), '  + webxml/')
  call vunit#AssertEquals(getline(15), '  + zlink -> ' . workspace . 'eclim_unit_test_java_linked/other/')
  call vunit#AssertEquals(getline(16), '    build.xml')
  call vunit#AssertEquals(getline(17), '    checkstyle.xml')
  call vunit#AssertEquals(getline(18), '    pom.xml')
  call vunit#AssertEquals(getline(19), 'eclim_unit_test_web/')

  normal 12j
  call vunit#AssertEquals(line('.'), 19)
  call vunit#AssertEquals(col('.'), 1)
  call vunit#AssertEquals(getline('.'), 'eclim_unit_test_web/')
  normal o
  call vunit#AssertEquals(getline( 1), 'eclim_unit_test/')
  call vunit#AssertEquals(getline( 2), '  + files/')
  call vunit#AssertEquals(getline( 3), '  + history/')
  call vunit#AssertEquals(getline( 4), '  + vim/')
  call vunit#AssertEquals(getline( 5), '  + xml/')
  call vunit#AssertEquals(getline( 6), '    test_root_file.txt')
  call vunit#AssertEquals(getline( 7), 'eclim_unit_test_java/')
  call vunit#AssertEquals(getline( 8), '  + bin/')
  call vunit#AssertEquals(getline( 9), '  + doc/')
  call vunit#AssertEquals(getline(10), '  + log4j/')
  call vunit#AssertEquals(getline(11), '  + src/')
  call vunit#AssertEquals(getline(12), '  + src-javac/')
  call vunit#AssertEquals(getline(13), '  + src-linked -> ' . workspace . 'eclim_unit_test_java_linked/src/')
  call vunit#AssertEquals(getline(14), '  + webxml/')
  call vunit#AssertEquals(getline(15), '  + zlink -> ' . workspace . 'eclim_unit_test_java_linked/other/')
  call vunit#AssertEquals(getline(16), '    build.xml')
  call vunit#AssertEquals(getline(17), '    checkstyle.xml')
  call vunit#AssertEquals(getline(18), '    pom.xml')
  call vunit#AssertEquals(getline(19), 'eclim_unit_test_web/')
  call vunit#AssertEquals(getline(20), '  + css/')
  call vunit#AssertEquals(getline(21), '  + dtd/')
endfunction " }}}

" vim:ft=vim:fdm=marker
