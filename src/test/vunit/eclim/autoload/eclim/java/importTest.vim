" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for import.vim
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

" SetUp() {{{
function! SetUp()
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
endfunction " }}}

" TestImport() {{{
function! TestImport()
  edit! src/org/eclim/test/include/TestImportVUnit.java
  call PeekRedir()

  call cursor(5, 11)
  JavaImport
  call VUAssertFalse(search('^import .*TestUnusedImportVUnit;'),
    \ 'TestUnusedImportVUnit imported.')

  call cursor(6, 11)
  JavaImport
  call VUAssertFalse(search('^import .*String;'), 'String imported.')

  call cursor(7, 11)
  JavaImport
  call VUAssertTrue(search('^import java\.util\.ArrayList;'),
    \ 'ArrayList not imported.')
endfunction " }}}

" TestImportMissing() {{{
function! TestImportMissing()
  edit! src/org/eclim/test/include/TestImportMissingVUnit.java
  call PeekRedir()

  call VUAssertFalse(search('^import java\.util\.ArrayList;'),
    \ 'ArrayList already imported.')
  call VUAssertFalse(search('^import java\.util\.HashMap;'),
    \ 'HashMap already imported.')

  JavaImportMissing
  call VUAssertTrue(search('^import java\.util\.ArrayList;'),
    \ 'ArrayList not imported.')
  call VUAssertTrue(search('^import java\.util\.HashMap;'),
    \ 'HashMap not imported.')
endfunction " }}}

" TestImportMissing2() {{{
function! TestImportMissing()
  edit! src/org/eclim/test/include/TestImportMissing2VUnit.java
  call PeekRedir()

  call VUAssertEquals(getline(1), 'package org.eclim.test.include;', 'line 1')
  call VUAssertEquals(getline(2), '', 'line 2')
  call VUAssertEquals(getline(3), 'import java.awt.Component;', 'line 3')
  call VUAssertEquals(getline(4), '', 'line 4')
  call VUAssertEquals(getline(5), 'import java.util.ArrayList;', 'line 5')
  call VUAssertEquals(getline(6), 'import java.util.List;', 'line 6')
  call VUAssertEquals(getline(7), '', 'line 7')
  call VUAssertEquals(getline(8), 'import java.util.regex.Pattern;', 'line 8')
  call VUAssertEquals(getline(9), '', 'line 9')
  call VUAssertEquals(getline(10), 'import javax.swing.JComponent;', 'line 10')
  call VUAssertEquals(getline(11), 'import javax.swing.JTree;', 'line 11')
  call VUAssertEquals(getline(12), '', 'line 12')
  call VUAssertEquals(getline(13), 'import org.apache.commons.io.IOUtils;', 'line 13')
  call VUAssertEquals(getline(14), '', 'line 14')
  call VUAssertEquals(getline(15), 'import org.apache.commons.lang.StringUtils;', 'line 15')
  call VUAssertEquals(getline(16), '', 'line 16')
  call VUAssertEquals(getline(17), 'public class TestImportMissing2VUnit', 'line 17')

  let g:EclimJavaImportPackageSeparationLevel = 0
  JavaImportMissing

  call VUAssertEquals(getline(1), 'package org.eclim.test.include;', 'line 1')
  call VUAssertEquals(getline(2), '', 'line 2')
  call VUAssertEquals(getline(3), 'import java.awt.AWTError;', 'line 3')
  call VUAssertEquals(getline(4), 'import java.awt.Component;', 'line 4')
  call VUAssertEquals(getline(5), '', 'line 5')
  call VUAssertEquals(getline(6), 'import java.util.ArrayList;', 'line 6')
  call VUAssertEquals(getline(7), 'import java.util.HashMap;', 'line 7')
  call VUAssertEquals(getline(8), 'import java.util.List;', 'line 8')
  call VUAssertEquals(getline(9), 'import java.util.Map;', 'line 9')
  call VUAssertEquals(getline(10), 'import java.util.concurrent.SynchronousQueue;', 'line 10')
  call VUAssertEquals(getline(11), '', 'line 11')
  call VUAssertEquals(getline(12), 'import java.util.regex.Pattern;', 'line 12')
  call VUAssertEquals(getline(13), '', 'line 13')
  call VUAssertEquals(getline(14), 'import javax.swing.JComponent;', 'line 14')
  call VUAssertEquals(getline(15), 'import javax.swing.JList;', 'line 15')
  call VUAssertEquals(getline(16), 'import javax.swing.JTree;', 'line 16')
  call VUAssertEquals(getline(17), '', 'line 17')
  call VUAssertEquals(getline(18), 'import org.apache.commons.io.IOUtils;', 'line 18')
  call VUAssertEquals(getline(19), '', 'line 19')
  call VUAssertEquals(getline(20), 'import org.apache.commons.lang.StringUtils;', 'line 20')
  call VUAssertEquals(getline(21), 'import org.eclim.test.bean.TestBean;', 'line 21')
  call VUAssertEquals(getline(22), '', 'line 23')
  call VUAssertEquals(getline(23), 'public class TestImportMissing2VUnit', 'line 24')

  undo
  let g:EclimJavaImportPackageSeparationLevel = -1
  JavaImportMissing

  call VUAssertEquals(getline(1), 'package org.eclim.test.include;', 'line 1')
  call VUAssertEquals(getline(2), '', 'line 2')
  call VUAssertEquals(getline(3), 'import java.awt.AWTError;', 'line 3')
  call VUAssertEquals(getline(4), 'import java.awt.Component;', 'line 4')
  call VUAssertEquals(getline(5), '', 'line 5')
  call VUAssertEquals(getline(6), 'import java.util.ArrayList;', 'line 6')
  call VUAssertEquals(getline(7), 'import java.util.HashMap;', 'line 7')
  call VUAssertEquals(getline(8), 'import java.util.List;', 'line 8')
  call VUAssertEquals(getline(9), 'import java.util.Map;', 'line 9')
  call VUAssertEquals(getline(10), '', 'line 10')
  call VUAssertEquals(getline(11), 'import java.util.concurrent.SynchronousQueue;', 'line 11')
  call VUAssertEquals(getline(12), '', 'line 12')
  call VUAssertEquals(getline(13), 'import java.util.regex.Pattern;', 'line 13')
  call VUAssertEquals(getline(14), '', 'line 14')
  call VUAssertEquals(getline(15), 'import javax.swing.JComponent;', 'line 15')
  call VUAssertEquals(getline(16), 'import javax.swing.JList;', 'line 16')
  call VUAssertEquals(getline(17), 'import javax.swing.JTree;', 'line 17')
  call VUAssertEquals(getline(18), '', 'line 18')
  call VUAssertEquals(getline(19), 'import org.apache.commons.io.IOUtils;', 'line 19')
  call VUAssertEquals(getline(20), '', 'line 20')
  call VUAssertEquals(getline(21), 'import org.apache.commons.lang.StringUtils;', 'line 21')
  call VUAssertEquals(getline(22), '', 'line 22')
  call VUAssertEquals(getline(23), 'import org.eclim.test.bean.TestBean;', 'line 23')
  call VUAssertEquals(getline(24), '', 'line 24')
  call VUAssertEquals(getline(25), 'public class TestImportMissing2VUnit', 'line 25')

  undo
  let g:EclimJavaImportPackageSeparationLevel = 2
  JavaImportMissing

  call VUAssertEquals(getline(1), 'package org.eclim.test.include;', 'line 1')
  call VUAssertEquals(getline(2), '', 'line 2')
  call VUAssertEquals(getline(3), 'import java.awt.AWTError;', 'line 3')
  call VUAssertEquals(getline(4), 'import java.awt.Component;', 'line 4')
  call VUAssertEquals(getline(5), '', 'line 5')
  call VUAssertEquals(getline(6), 'import java.util.ArrayList;', 'line 6')
  call VUAssertEquals(getline(7), 'import java.util.HashMap;', 'line 7')
  call VUAssertEquals(getline(8), 'import java.util.List;', 'line 8')
  call VUAssertEquals(getline(9), 'import java.util.Map;', 'line 9')
  call VUAssertEquals(getline(10), 'import java.util.concurrent.SynchronousQueue;', 'line 10')
  call VUAssertEquals(getline(11), '', 'line 11')
  call VUAssertEquals(getline(12), 'import java.util.regex.Pattern;', 'line 12')
  call VUAssertEquals(getline(13), '', 'line 13')
  call VUAssertEquals(getline(14), 'import javax.swing.JComponent;', 'line 14')
  call VUAssertEquals(getline(15), 'import javax.swing.JList;', 'line 15')
  call VUAssertEquals(getline(16), 'import javax.swing.JTree;', 'line 16')
  call VUAssertEquals(getline(17), '', 'line 17')
  call VUAssertEquals(getline(18), 'import org.apache.commons.io.IOUtils;', 'line 18')
  call VUAssertEquals(getline(19), '', 'line 19')
  call VUAssertEquals(getline(20), 'import org.apache.commons.lang.StringUtils;', 'line 20')
  call VUAssertEquals(getline(21), '', 'line 21')
  call VUAssertEquals(getline(22), 'import org.eclim.test.bean.TestBean;', 'line 22')
  call VUAssertEquals(getline(23), '', 'line 23')
  call VUAssertEquals(getline(24), 'public class TestImportMissing2VUnit', 'line 24')
endfunction " }}}

" TestImportSort() {{{
function! TestImportSort()
  edit! src/org/eclim/test/include/TestImportSortVUnit.java
  call PeekRedir()

  call VUAssertEquals(getline(1), 'package org.eclim.test.include;', 'line 1')
  call VUAssertEquals(getline(3), 'import org.apache.commons.io.IOUtils;', 'line 3')
  call VUAssertEquals(getline(4), 'import java.util.ArrayList;', 'line 4')
  call VUAssertEquals(getline(5), 'import org.apache.commons.lang.StringUtils;', 'line 5')
  call VUAssertEquals(getline(6), 'import java.util.List;', 'line 6')
  call VUAssertEquals(getline(7), 'import java.awt.Component;', 'line 7')
  call VUAssertEquals(getline(8), 'import javax.swing.JComponent;', 'line 8')
  call VUAssertEquals(getline(9), 'import java.util.regex.Pattern;', 'line 9')
  call VUAssertEquals(getline(10), 'import javax.swing.JTree;', 'line 10')
  call VUAssertEquals(getline(12), 'public class TestImportSortVUnit', 'line 12')

  let g:EclimJavaImportPackageSeparationLevel = 0
  JavaImportSort

  call VUAssertEquals(getline(1), 'package org.eclim.test.include;', 'line 1')
  call VUAssertEquals(getline(3), 'import java.awt.Component;', 'line 3')
  call VUAssertEquals(getline(4), 'import java.util.ArrayList;', 'line 4')
  call VUAssertEquals(getline(5), 'import java.util.List;', 'line 5')
  call VUAssertEquals(getline(6), 'import java.util.regex.Pattern;', 'line 6')
  call VUAssertEquals(getline(7), 'import javax.swing.JComponent;', 'line 7')
  call VUAssertEquals(getline(8), 'import javax.swing.JTree;', 'line 8')
  call VUAssertEquals(getline(9), 'import org.apache.commons.io.IOUtils;', 'line 9')
  call VUAssertEquals(getline(10), 'import org.apache.commons.lang.StringUtils;', 'line 10')
  call VUAssertEquals(getline(12), 'public class TestImportSortVUnit', 'line 12')

  undo
  let g:EclimJavaImportPackageSeparationLevel = -1
  JavaImportSort

  call VUAssertEquals(getline(1), 'package org.eclim.test.include;', 'line 1')
  call VUAssertEquals(getline(3), 'import java.awt.Component;', 'line 3')
  call VUAssertEquals(getline(4), '', 'line 4')
  call VUAssertEquals(getline(5), 'import java.util.ArrayList;', 'line 5')
  call VUAssertEquals(getline(6), 'import java.util.List;', 'line 6')
  call VUAssertEquals(getline(7), '', 'line 7')
  call VUAssertEquals(getline(8), 'import java.util.regex.Pattern;', 'line 8')
  call VUAssertEquals(getline(9), '', 'line 9')
  call VUAssertEquals(getline(10), 'import javax.swing.JComponent;', 'line 10')
  call VUAssertEquals(getline(11), 'import javax.swing.JTree;', 'line 11')
  call VUAssertEquals(getline(12), '', 'line 12')
  call VUAssertEquals(getline(13), 'import org.apache.commons.io.IOUtils;', 'line 13')
  call VUAssertEquals(getline(14), '', 'line 14')
  call VUAssertEquals(getline(15), 'import org.apache.commons.lang.StringUtils;', 'line 15')
  call VUAssertEquals(getline(17), 'public class TestImportSortVUnit', 'line 17')

  undo
  let g:EclimJavaImportPackageSeparationLevel = 2
  JavaImportSort

  call VUAssertEquals(getline(1), 'package org.eclim.test.include;', 'line 1')
  call VUAssertEquals(getline(3), 'import java.awt.Component;', 'line 3')
  call VUAssertEquals(getline(4), '', 'line 4')
  call VUAssertEquals(getline(5), 'import java.util.ArrayList;', 'line 5')
  call VUAssertEquals(getline(6), 'import java.util.List;', 'line 6')
  call VUAssertEquals(getline(7), 'import java.util.regex.Pattern;', 'line 7')
  call VUAssertEquals(getline(8), '', 'line 8')
  call VUAssertEquals(getline(9), 'import javax.swing.JComponent;', 'line 9')
  call VUAssertEquals(getline(10), 'import javax.swing.JTree;', 'line 10')
  call VUAssertEquals(getline(11), '', 'line 11')
  call VUAssertEquals(getline(12), 'import org.apache.commons.io.IOUtils;', 'line 12')
  call VUAssertEquals(getline(13), 'import org.apache.commons.lang.StringUtils;', 'line 13')
  call VUAssertEquals(getline(15), 'public class TestImportSortVUnit', 'line 15')
endfunction " }}}

" TestUnusedImport() {{{
function! TestUnusedImport()
  edit! src/org/eclim/test/include/TestUnusedImportVUnit.java
  call PeekRedir()

  call VUAssertTrue(search('^import java\.util\.ArrayList;$'),
    \ 'ArrayList import not found.')
  call VUAssertTrue(search('^import java\.util\.List;$'),
    \ 'List import not found.')

  JavaImportClean

  call VUAssertFalse(search('^import java\.util\.ArrayList;$'),
    \ 'ArrayList import still found.')
  call VUAssertFalse(search('^import java\.util\.List;$'),
    \ 'List import still found.')
endfunction " }}}

" vim:ft=vim:fdm=marker
