" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for import.vim
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
  let g:EclimJavaImportExclude = ['^com\.sun\..*', '^sunw\?\..*']
endfunction " }}}

" TestImport() {{{
function! TestImport()
  edit! src/org/eclim/test/include/TestImportVUnit.java
  call vunit#PeekRedir()

  call cursor(5, 11)
  JavaImport
  call vunit#AssertFalse(search('^import .*TestUnusedImportVUnit;'),
    \ 'TestUnusedImportVUnit imported.')

  call cursor(6, 11)
  JavaImport
  call vunit#AssertFalse(search('^import .*String;'), 'String imported.')

  call cursor(7, 11)
  JavaImport
  call vunit#AssertTrue(search('^import java\.util\.ArrayList;'),
    \ 'ArrayList not imported.')
endfunction " }}}

" TestImportMissing() {{{
function! TestImportMissing()
  edit! src/org/eclim/test/include/TestImportMissingVUnit.java
  call vunit#PeekRedir()

  call vunit#AssertFalse(search('^import java\.util\.ArrayList;'),
    \ 'ArrayList already imported.')
  call vunit#AssertFalse(search('^import java\.util\.HashMap;'),
    \ 'HashMap already imported.')

  JavaImportMissing
  call vunit#AssertTrue(search('^import java\.util\.ArrayList;'),
    \ 'ArrayList not imported.')
  call vunit#AssertTrue(search('^import java\.util\.HashMap;'),
    \ 'HashMap not imported.')
endfunction " }}}

" TestImportMissing2() {{{
function! TestImportMissing2()
  edit! src/org/eclim/test/include/TestImportMissing2VUnit.java
  call vunit#PeekRedir()

  call vunit#AssertEquals(getline(1), 'package org.eclim.test.include;', 'line 1')
  call vunit#AssertEquals(getline(2), '', 'line 2')
  call vunit#AssertEquals(getline(3), 'import java.awt.Component;', 'line 3')
  call vunit#AssertEquals(getline(4), '', 'line 4')
  call vunit#AssertEquals(getline(5), 'import java.util.ArrayList;', 'line 5')
  call vunit#AssertEquals(getline(6), 'import java.util.List;', 'line 6')
  call vunit#AssertEquals(getline(7), '', 'line 7')
  call vunit#AssertEquals(getline(8), 'import java.util.regex.Pattern;', 'line 8')
  call vunit#AssertEquals(getline(9), '', 'line 9')
  call vunit#AssertEquals(getline(10), 'import javax.swing.JComponent;', 'line 10')
  call vunit#AssertEquals(getline(11), 'import javax.swing.JTree;', 'line 11')
  call vunit#AssertEquals(getline(12), '', 'line 12')
  call vunit#AssertEquals(getline(13), 'import org.apache.commons.io.IOUtils;', 'line 13')
  call vunit#AssertEquals(getline(14), '', 'line 14')
  call vunit#AssertEquals(getline(15), 'import org.apache.commons.lang.StringUtils;', 'line 15')
  call vunit#AssertEquals(getline(16), '', 'line 16')
  call vunit#AssertEquals(getline(17), 'public class TestImportMissing2VUnit', 'line 17')

  let g:EclimJavaImportPackageSeparationLevel = 0
  JavaImportMissing

  call vunit#AssertEquals(getline(1), 'package org.eclim.test.include;', 'line 1')
  call vunit#AssertEquals(getline(2), '', 'line 2')
  call vunit#AssertEquals(getline(3), 'import java.awt.AWTError;', 'line 3')
  call vunit#AssertEquals(getline(4), 'import java.awt.Component;', 'line 4')
  call vunit#AssertEquals(getline(5), '', 'line 5')
  call vunit#AssertEquals(getline(6), 'import java.util.ArrayList;', 'line 6')
  call vunit#AssertEquals(getline(7), 'import java.util.HashMap;', 'line 7')
  call vunit#AssertEquals(getline(8), 'import java.util.List;', 'line 8')
  call vunit#AssertEquals(getline(9), 'import java.util.Map;', 'line 9')
  call vunit#AssertEquals(getline(10), 'import java.util.concurrent.SynchronousQueue;', 'line 10')
  call vunit#AssertEquals(getline(11), '', 'line 11')
  call vunit#AssertEquals(getline(12), 'import java.util.regex.Pattern;', 'line 12')
  call vunit#AssertEquals(getline(13), '', 'line 13')
  call vunit#AssertEquals(getline(14), 'import javax.swing.JComponent;', 'line 14')
  call vunit#AssertEquals(getline(15), 'import javax.swing.JList;', 'line 15')
  call vunit#AssertEquals(getline(16), 'import javax.swing.JTree;', 'line 16')
  call vunit#AssertEquals(getline(17), '', 'line 17')
  call vunit#AssertEquals(getline(18), 'import org.apache.commons.io.IOUtils;', 'line 18')
  call vunit#AssertEquals(getline(19), '', 'line 19')
  call vunit#AssertEquals(getline(20), 'import org.apache.commons.lang.StringUtils;', 'line 20')
  call vunit#AssertEquals(getline(21), 'import org.eclim.test.bean.TestBean;', 'line 21')
  call vunit#AssertEquals(getline(22), '', 'line 23')
  call vunit#AssertEquals(getline(23), 'public class TestImportMissing2VUnit', 'line 24')

  undo
  let g:EclimJavaImportPackageSeparationLevel = -1
  JavaImportMissing

  call vunit#AssertEquals(getline(1), 'package org.eclim.test.include;', 'line 1')
  call vunit#AssertEquals(getline(2), '', 'line 2')
  call vunit#AssertEquals(getline(3), 'import java.awt.AWTError;', 'line 3')
  call vunit#AssertEquals(getline(4), 'import java.awt.Component;', 'line 4')
  call vunit#AssertEquals(getline(5), '', 'line 5')
  call vunit#AssertEquals(getline(6), 'import java.util.ArrayList;', 'line 6')
  call vunit#AssertEquals(getline(7), 'import java.util.HashMap;', 'line 7')
  call vunit#AssertEquals(getline(8), 'import java.util.List;', 'line 8')
  call vunit#AssertEquals(getline(9), 'import java.util.Map;', 'line 9')
  call vunit#AssertEquals(getline(10), '', 'line 10')
  call vunit#AssertEquals(getline(11), 'import java.util.concurrent.SynchronousQueue;', 'line 11')
  call vunit#AssertEquals(getline(12), '', 'line 12')
  call vunit#AssertEquals(getline(13), 'import java.util.regex.Pattern;', 'line 13')
  call vunit#AssertEquals(getline(14), '', 'line 14')
  call vunit#AssertEquals(getline(15), 'import javax.swing.JComponent;', 'line 15')
  call vunit#AssertEquals(getline(16), 'import javax.swing.JList;', 'line 16')
  call vunit#AssertEquals(getline(17), 'import javax.swing.JTree;', 'line 17')
  call vunit#AssertEquals(getline(18), '', 'line 18')
  call vunit#AssertEquals(getline(19), 'import org.apache.commons.io.IOUtils;', 'line 19')
  call vunit#AssertEquals(getline(20), '', 'line 20')
  call vunit#AssertEquals(getline(21), 'import org.apache.commons.lang.StringUtils;', 'line 21')
  call vunit#AssertEquals(getline(22), '', 'line 22')
  call vunit#AssertEquals(getline(23), 'import org.eclim.test.bean.TestBean;', 'line 23')
  call vunit#AssertEquals(getline(24), '', 'line 24')
  call vunit#AssertEquals(getline(25), 'public class TestImportMissing2VUnit', 'line 25')

  undo
  let g:EclimJavaImportPackageSeparationLevel = 2
  JavaImportMissing

  call vunit#AssertEquals(getline(1), 'package org.eclim.test.include;', 'line 1')
  call vunit#AssertEquals(getline(2), '', 'line 2')
  call vunit#AssertEquals(getline(3), 'import java.awt.AWTError;', 'line 3')
  call vunit#AssertEquals(getline(4), 'import java.awt.Component;', 'line 4')
  call vunit#AssertEquals(getline(5), '', 'line 5')
  call vunit#AssertEquals(getline(6), 'import java.util.ArrayList;', 'line 6')
  call vunit#AssertEquals(getline(7), 'import java.util.HashMap;', 'line 7')
  call vunit#AssertEquals(getline(8), 'import java.util.List;', 'line 8')
  call vunit#AssertEquals(getline(9), 'import java.util.Map;', 'line 9')
  call vunit#AssertEquals(getline(10), 'import java.util.concurrent.SynchronousQueue;', 'line 10')
  call vunit#AssertEquals(getline(11), '', 'line 11')
  call vunit#AssertEquals(getline(12), 'import java.util.regex.Pattern;', 'line 12')
  call vunit#AssertEquals(getline(13), '', 'line 13')
  call vunit#AssertEquals(getline(14), 'import javax.swing.JComponent;', 'line 14')
  call vunit#AssertEquals(getline(15), 'import javax.swing.JList;', 'line 15')
  call vunit#AssertEquals(getline(16), 'import javax.swing.JTree;', 'line 16')
  call vunit#AssertEquals(getline(17), '', 'line 17')
  call vunit#AssertEquals(getline(18), 'import org.apache.commons.io.IOUtils;', 'line 18')
  call vunit#AssertEquals(getline(19), '', 'line 19')
  call vunit#AssertEquals(getline(20), 'import org.apache.commons.lang.StringUtils;', 'line 20')
  call vunit#AssertEquals(getline(21), '', 'line 21')
  call vunit#AssertEquals(getline(22), 'import org.eclim.test.bean.TestBean;', 'line 22')
  call vunit#AssertEquals(getline(23), '', 'line 23')
  call vunit#AssertEquals(getline(24), 'public class TestImportMissing2VUnit', 'line 24')
endfunction " }}}

" TestImportSort() {{{
function! TestImportSort()
  edit! src/org/eclim/test/include/TestImportSortVUnit.java
  call vunit#PeekRedir()

  call vunit#AssertEquals(getline(1), 'package org.eclim.test.include;', 'line 1')
  call vunit#AssertEquals(getline(3), 'import org.apache.commons.io.IOUtils;', 'line 3')
  call vunit#AssertEquals(getline(4), 'import java.util.ArrayList;', 'line 4')
  call vunit#AssertEquals(getline(5), 'import org.apache.commons.lang.StringUtils;', 'line 5')
  call vunit#AssertEquals(getline(6), 'import java.util.List;', 'line 6')
  call vunit#AssertEquals(getline(7), 'import java.awt.Component;', 'line 7')
  call vunit#AssertEquals(getline(8), 'import com.eclim.test.TestCom;', 'line 8')
  call vunit#AssertEquals(getline(9), 'import static net.eclim.test.TestNet.FOO;', 'line 9')
  call vunit#AssertEquals(getline(10), 'import javax.swing.JComponent;', 'line 10')
  call vunit#AssertEquals(getline(11), 'import java.util.regex.Pattern;', 'line 11')
  call vunit#AssertEquals(getline(12), 'import net.eclim.test.TestNet;', 'line 12')
  call vunit#AssertEquals(getline(13), 'import javax.swing.JTree;', 'line 13')
  call vunit#AssertEquals(getline(14), 'import static net.eclim.test.TestNet.BAR;', 'line 14')
  call vunit#AssertEquals(getline(16), 'public class TestImportSortVUnit', 'line 16')

  let g:EclimJavaImportPackageSeparationLevel = 0
  JavaImportSort

  call vunit#AssertEquals(getline(1), 'package org.eclim.test.include;', 'line 1')
  call vunit#AssertEquals(getline(3), 'import java.awt.Component;', 'line 3')
  call vunit#AssertEquals(getline(4), 'import java.util.ArrayList;', 'line 4')
  call vunit#AssertEquals(getline(5), 'import java.util.List;', 'line 5')
  call vunit#AssertEquals(getline(6), 'import java.util.regex.Pattern;', 'line 6')
  call vunit#AssertEquals(getline(7), 'import javax.swing.JComponent;', 'line 7')
  call vunit#AssertEquals(getline(8), 'import javax.swing.JTree;', 'line 8')
  call vunit#AssertEquals(getline(9), 'import org.apache.commons.io.IOUtils;', 'line 9')
  call vunit#AssertEquals(getline(10), 'import org.apache.commons.lang.StringUtils;', 'line 10')
  call vunit#AssertEquals(getline(11), 'import com.eclim.test.TestCom;', 'line 11')
  call vunit#AssertEquals(getline(12), 'import net.eclim.test.TestNet;', 'line 12')
  call vunit#AssertEquals(getline(13), 'import static net.eclim.test.TestNet.BAR;', 'line 13')
  call vunit#AssertEquals(getline(14), 'import static net.eclim.test.TestNet.FOO;', 'line 14')
  call vunit#AssertEquals(getline(16), 'public class TestImportSortVUnit', 'line 16')

  undo
  let g:EclimJavaImportPackageSeparationLevel = -1
  JavaImportSort

  call vunit#AssertEquals(getline(1), 'package org.eclim.test.include;', 'line 1')
  call vunit#AssertEquals(getline(3), 'import java.awt.Component;', 'line 3')
  call vunit#AssertEquals(getline(4), '', 'line 4')
  call vunit#AssertEquals(getline(5), 'import java.util.ArrayList;', 'line 5')
  call vunit#AssertEquals(getline(6), 'import java.util.List;', 'line 6')
  call vunit#AssertEquals(getline(7), '', 'line 7')
  call vunit#AssertEquals(getline(8), 'import java.util.regex.Pattern;', 'line 8')
  call vunit#AssertEquals(getline(9), '', 'line 9')
  call vunit#AssertEquals(getline(10), 'import javax.swing.JComponent;', 'line 10')
  call vunit#AssertEquals(getline(11), 'import javax.swing.JTree;', 'line 11')
  call vunit#AssertEquals(getline(12), '', 'line 12')
  call vunit#AssertEquals(getline(13), 'import org.apache.commons.io.IOUtils;', 'line 13')
  call vunit#AssertEquals(getline(14), '', 'line 14')
  call vunit#AssertEquals(getline(15), 'import org.apache.commons.lang.StringUtils;', 'line 15')
  call vunit#AssertEquals(getline(16), '', 'line 16')
  call vunit#AssertEquals(getline(17), 'import com.eclim.test.TestCom;', 'line 17')
  call vunit#AssertEquals(getline(18), '', 'line 18')
  call vunit#AssertEquals(getline(19), 'import net.eclim.test.TestNet;', 'line 19')
  call vunit#AssertEquals(getline(20), 'import static net.eclim.test.TestNet.BAR;', 'line 20')
  call vunit#AssertEquals(getline(21), 'import static net.eclim.test.TestNet.FOO;', 'line 21')
  call vunit#AssertEquals(getline(23), 'public class TestImportSortVUnit', 'line 23')

  undo
  let g:EclimJavaImportPackageSeparationLevel = 2
  JavaImportSort

  call vunit#AssertEquals(getline(1), 'package org.eclim.test.include;', 'line 1')
  call vunit#AssertEquals(getline(3), 'import java.awt.Component;', 'line 3')
  call vunit#AssertEquals(getline(4), '', 'line 4')
  call vunit#AssertEquals(getline(5), 'import java.util.ArrayList;', 'line 5')
  call vunit#AssertEquals(getline(6), 'import java.util.List;', 'line 6')
  call vunit#AssertEquals(getline(7), 'import java.util.regex.Pattern;', 'line 7')
  call vunit#AssertEquals(getline(8), '', 'line 8')
  call vunit#AssertEquals(getline(9), 'import javax.swing.JComponent;', 'line 9')
  call vunit#AssertEquals(getline(10), 'import javax.swing.JTree;', 'line 10')
  call vunit#AssertEquals(getline(11), '', 'line 11')
  call vunit#AssertEquals(getline(12), 'import org.apache.commons.io.IOUtils;', 'line 12')
  call vunit#AssertEquals(getline(13), 'import org.apache.commons.lang.StringUtils;', 'line 13')
  call vunit#AssertEquals(getline(14), '', 'line 14')
  call vunit#AssertEquals(getline(15), 'import com.eclim.test.TestCom;', 'line 15')
  call vunit#AssertEquals(getline(16), '', 'line 16')
  call vunit#AssertEquals(getline(17), 'import net.eclim.test.TestNet;', 'line 17')
  call vunit#AssertEquals(getline(18), 'import static net.eclim.test.TestNet.BAR;', 'line 18')
  call vunit#AssertEquals(getline(19), 'import static net.eclim.test.TestNet.FOO;', 'line 19')
  call vunit#AssertEquals(getline(21), 'public class TestImportSortVUnit', 'line 21')
endfunction " }}}

" TestImportOrder() {{{
function! TestImportOrder()
  edit! src/org/eclim/test/include/TestImportSortVUnit.java
  call vunit#PeekRedir()

  ProjectSettings

  call search('jdt\.ui\.importorder=', 'w')
  let setting = getline('.')
  let setting = substitute(
    \ setting, '\(.*jdt\.ui\.importorder=\).*', '\1java;javax;net;org;com', '')
  call setline(line('.'), setting)
  write
  bdelete

  let g:EclimJavaImportPackageSeparationLevel = 0
  JavaImportSort

  call vunit#AssertEquals(getline(1), 'package org.eclim.test.include;', 'line 1')
  call vunit#AssertEquals(getline(3), 'import java.awt.Component;', 'line 3')
  call vunit#AssertEquals(getline(4), 'import java.util.ArrayList;', 'line 4')
  call vunit#AssertEquals(getline(5), 'import java.util.List;', 'line 5')
  call vunit#AssertEquals(getline(6), 'import java.util.regex.Pattern;', 'line 6')
  call vunit#AssertEquals(getline(7), 'import javax.swing.JComponent;', 'line 7')
  call vunit#AssertEquals(getline(8), 'import javax.swing.JTree;', 'line 8')
  call vunit#AssertEquals(getline(9), 'import net.eclim.test.TestNet;', 'line 9')
  call vunit#AssertEquals(getline(10), 'import static net.eclim.test.TestNet.BAR;', 'line 10')
  call vunit#AssertEquals(getline(11), 'import static net.eclim.test.TestNet.FOO;', 'line 11')
  call vunit#AssertEquals(getline(12), 'import org.apache.commons.io.IOUtils;', 'line 12')
  call vunit#AssertEquals(getline(13), 'import org.apache.commons.lang.StringUtils;', 'line 13')
  call vunit#AssertEquals(getline(14), 'import com.eclim.test.TestCom;', 'line 14')
  call vunit#AssertEquals(getline(16), 'public class TestImportSortVUnit', 'line 16')
endfunction " }}}

" TestUnusedImport() {{{
function! TestUnusedImport()
  edit! src/org/eclim/test/include/TestUnusedImportVUnit.java
  call vunit#PeekRedir()

  call vunit#AssertTrue(search('^import java\.lang\.Math;$'),
    \ 'Math import not found.')
  call vunit#AssertTrue(search('^import static java\.lang\.Math\.PI;$'),
    \ 'Math.PI import not found.')
  call vunit#AssertTrue(search('^import java\.util\.ArrayList;$'),
    \ 'ArrayList import not found.')
  call vunit#AssertTrue(search('^import java\.util\.List;$'),
    \ 'List import not found.')

  JavaImportClean

  call vunit#AssertFalse(search('^import java\.lang\.Math.PI;$'),
    \ 'Math.PI import still found.')
  call vunit#AssertFalse(search('^import java\.util\.ArrayList;$'),
    \ 'ArrayList import still found.')
  call vunit#AssertFalse(search('^import java\.util\.List;$'),
    \ 'List import still found.')
  call vunit#AssertTrue(search('^import java\.lang\.Math;$'),
    \ 'Math import not found.')
  call vunit#AssertTrue(search('^import java\.util\.Map;$'),
    \ 'Map import not found.')
  call vunit#AssertTrue(search('^import java\.util\.HashMap;$'),
    \ 'HashMap import not found.')
endfunction " }}}

" vim:ft=vim:fdm=marker
