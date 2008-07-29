" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for common.vim
"
" License:
"
" Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
function! TestSplit ()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test_java/build.xml

  Split eclim_unit_test_java/pom.xml eclim_unit_test_java/css/complete.css

  call VUAssertEquals(winnr('$'), 3)
  call VUAssertTrue(bufwinnr('eclim_unit_test_java/pom.xml') > -1,
    \ 'Did not open pom.xml.')
  call VUAssertTrue(bufwinnr('eclim_unit_test_java/css/complete.css') > -1,
    \ 'Did not open complete.css.')

  bdelete
  bdelete
  bdelete
endfunction " }}}

" TestTabnew() {{{
function! TestTabnew ()
  exec 'cd ' . g:TestEclimWorkspace

  Tabnew eclim_unit_test_java/pom.xml eclim_unit_test_java/css/complete.css

  call VUAssertEquals(tabpagenr('$'), 3)
  tabnext 2
  call VUAssertTrue(bufwinnr('eclim_unit_test_java/pom.xml') > -1,
    \ 'Did not open pom.xml.')
  tabnext 3
  call VUAssertTrue(bufwinnr('eclim_unit_test_java/css/complete.css') > -1,
    \ 'Did not open complete.css.')

  bdelete
  bdelete
endfunction " }}}

" TestEditRelative() {{{
function! TestEditRelative ()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test_java/build.xml

  EditRelative pom.xml

  call VUAssertEquals(winnr('$'), 1)
  call VUAssertTrue(bufwinnr('eclim_unit_test_java/pom.xml') > -1,
    \ 'Did not open pom.xml.')
  call VUAssertTrue(getline(6) =~ '<project')

  bdelete
endfunction " }}}

" TestSplitRelative() {{{
function! TestSplitRelative ()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test_java/build.xml

  SplitRelative pom.xml

  call VUAssertEquals(winnr('$'), 2)
  call VUAssertTrue(bufwinnr('eclim_unit_test_java/pom.xml') > -1,
    \ 'Did not open pom.xml.')
  call VUAssertTrue(getline(6) =~ '<project')

  bdelete
endfunction " }}}

" TestTabnewRelative() {{{
function! TestTabnewRelative ()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test_java/build.xml

  TabnewRelative pom.xml css/complete.css

  call VUAssertEquals(tabpagenr('$'), 3)
  tabnext 2
  call VUAssertTrue(bufwinnr('eclim_unit_test_java/pom.xml') > -1,
    \ 'Did not open pom.xml.')
  call VUAssertTrue(getline(6) =~ '<project')

  tabnext 3
  call VUAssertTrue(bufwinnr('eclim_unit_test_java/css/complete.css') > -1,
    \ 'Did not open complete.css.')
  call VUAssertTrue(getline(4) =~ 'body {')

  bdelete
  bdelete
  bdelete
endfunction " }}}

" TestOnly() {{{
function! TestOnly ()
  exec 'cd ' . g:TestEclimWorkspace
  edit! eclim_unit_test_java/build.xml
  Tlist

  Split eclim_unit_test_java/pom.xml eclim_unit_test_java/css/complete.css

  call VUAssertEquals(winnr('$'), 4)
  call VUAssertTrue(bufwinnr('eclim_unit_test_java/pom.xml') > -1,
    \ 'Did not open pom.xml.')
  call VUAssertTrue(bufwinnr('eclim_unit_test_java/css/complete.css') > -1,
    \ 'Did not open complete.css.')

  Only
  call VUAssertEquals(winnr('$'), 2)
  call VUAssertTrue(bufwinnr('__Tag_List__') > -1, 'Taglist not open.')
  call VUAssertTrue(bufwinnr('eclim_unit_test_java/css/complete.css') > -1,
    \ 'complete.css not open.')

  Tlist
  bdelete
endfunction " }}}

" vim:ft=vim:fdm=marker
