" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for import.vim
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

" SetUp() {{{
function! SetUp()
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
endfunction " }}}

function! TestImport() " {{{
  edit! src/org/eclim/test/include/TestImportVUnit.java
  call vunit#PeekRedir()
  call vunit#AssertFalse(search('^import java\.util\.ArrayList;', 'n'),
    \ 'ArrayList already imported.')

  call cursor(5, 11)
  JavaImport
  call vunit#AssertFalse(search('^import .*String;', 'n'), 'String imported.')

  call cursor(6, 27)
  JavaImport
  call vunit#AssertTrue(search('^import java\.util\.ArrayList;', 'n'),
    \ 'ArrayList not imported.')

  call cursor(0, 14)
  let g:EclimTestPromptQueue = [1] " choose java.util.List
  JavaImport
  call vunit#AssertTrue(search('^import java\.util\.List;', 'n'),
    \ 'List not imported.')

  call search('Pattern')
  JavaImport
  call vunit#AssertTrue(search('^import java\.util\.regex\.Pattern;', 'n'),
    \ 'Pattern not imported.')
endfunction " }}}

function! TestImportOrganize() " {{{
  call eclim#project#util#SetProjectSetting(
    \ "org.eclim.java.import.package_separation_level", "1")

  edit! src/org/eclim/test/include/TestImportOrganizeVUnit.java
  call vunit#PeekRedir()
  call vunit#AssertFalse(search('^import java\.util\.regex\.Pattern;', 'n'),
    \ 'Pattern already imported.')

  let g:EclimTestPromptQueue = [1] " choose java.util.List
  JavaImportOrganize

  let imports = getline(3, 18)
  echom string(imports)
  call vunit#AssertEquals(imports[ 0], 'import static net.eclim.test.TestNet.BAR;')
  call vunit#AssertEquals(imports[ 1], 'import static net.eclim.test.TestNet.FOO;')
  call vunit#AssertEquals(imports[ 2], '')
  call vunit#AssertEquals(imports[ 3], 'import java.awt.AWTError;')
  call vunit#AssertEquals(imports[ 4], 'import java.util.ArrayList;')
  call vunit#AssertEquals(imports[ 5], 'import java.util.HashMap;')
  call vunit#AssertEquals(imports[ 6], 'import java.util.List;')
  call vunit#AssertEquals(imports[ 7], 'import java.util.Map;')
  call vunit#AssertEquals(imports[ 8], 'import java.util.concurrent.SynchronousQueue;')
  call vunit#AssertEquals(imports[ 9], 'import java.util.regex.Pattern;')
  call vunit#AssertEquals(imports[10], '')
  call vunit#AssertEquals(imports[11], 'import javax.swing.JComponent;')
  call vunit#AssertEquals(imports[12], 'import javax.swing.JList;')
  call vunit#AssertEquals(imports[13], 'import javax.swing.JTree;')
  call vunit#AssertEquals(imports[14], '')
  call vunit#AssertEquals(imports[15], 'import org.eclim.test.bean.TestBean;')

  call eclim#project#util#SetProjectSetting(
    \ "org.eclim.java.import.package_separation_level", "-1")
  JavaImportOrganize
  let imports = getline(3, 21)
  echom string(imports)
  call vunit#AssertEquals(imports[ 0], 'import static net.eclim.test.TestNet.BAR;')
  call vunit#AssertEquals(imports[ 1], 'import static net.eclim.test.TestNet.FOO;')
  call vunit#AssertEquals(imports[ 2], '')
  call vunit#AssertEquals(imports[ 3], 'import java.awt.AWTError;')
  call vunit#AssertEquals(imports[ 4], '')
  call vunit#AssertEquals(imports[ 5], 'import java.util.ArrayList;')
  call vunit#AssertEquals(imports[ 6], 'import java.util.HashMap;')
  call vunit#AssertEquals(imports[ 7], 'import java.util.List;')
  call vunit#AssertEquals(imports[ 8], 'import java.util.Map;')
  call vunit#AssertEquals(imports[ 9], '')
  call vunit#AssertEquals(imports[10], 'import java.util.concurrent.SynchronousQueue;')
  call vunit#AssertEquals(imports[11], '')
  call vunit#AssertEquals(imports[12], 'import java.util.regex.Pattern;')
  call vunit#AssertEquals(imports[13], '')
  call vunit#AssertEquals(imports[14], 'import javax.swing.JComponent;')
  call vunit#AssertEquals(imports[15], 'import javax.swing.JList;')
  call vunit#AssertEquals(imports[16], 'import javax.swing.JTree;')
  call vunit#AssertEquals(imports[17], '')
  call vunit#AssertEquals(imports[18], 'import org.eclim.test.bean.TestBean;')
endfunction " }}}

" vim:ft=vim:fdm=marker
