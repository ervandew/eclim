" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for classpath.vim
"
" License:
"
" Copyright (C) 2005 - 2017  Eric Van Dewoestine
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

" TestUpdateClasspath() {{{
function! TestUpdateClasspath()
  edit! .classpath
  call vunit#PeekRedir()

  NewJarEntry lib/blah.jar

  call vunit#AssertTrue(search('blah\.jar'), 'blah.jar not added.')

  update!
  call vunit#PeekRedir()
  let errors = getloclist(0)
  call vunit#AssertEquals(1, len(errors), 'Wrong number of errors.')
  let message = errors[0].text
  echom message
  call vunit#AssertTrue(message =~ 'missing required library:.*blah.jar',
    \ 'Wrong error message.')

  undo
  update!
  call vunit#PeekRedir()
  let errors = getloclist(0)
  call vunit#AssertEquals(0, len(errors), 'Still contains errors.')
endfunction " }}}

" TestVariableCreateDelete() {{{
function! TestVariableCreateDelete()
  edit! .classpath
  call vunit#PeekRedir()

  let vars = eclim#java#classpath#GetVariableNames()
  call vunit#AssertFalse(eclim#util#ListContains(vars, '.*VUNIT_TEST_VAR.*'),
    \ 'VUNIT_TEST_VAR alread found in variable list.')

  VariableCreate VUNIT_TEST_VAR /home

  let vars = eclim#java#classpath#GetVariableNames()
  call vunit#AssertTrue(eclim#util#ListContains(vars, '.*VUNIT_TEST_VAR.*'),
    \ 'VUNIT_TEST_VAR not found in variable list.')

  VariableDelete VUNIT_TEST_VAR

  let vars = eclim#java#classpath#GetVariableNames()
  call vunit#AssertFalse(eclim#util#ListContains(vars, '.*VUNIT_TEST_VAR.*'),
    \ 'VUNIT_TEST_VAR still found in variable list.')
endfunction " }}}

" TestGetVariableNames() {{{
function! TestGetVariableNames()
  edit! .classpath
  call vunit#PeekRedir()

  let results = eclim#java#classpath#GetVariableNames()
  call vunit#AssertTrue(len(results) > 1, 'Not enough var results.')
  call vunit#AssertTrue(eclim#util#ListContains(results, 'ECLIPSE_HOME'),
    \ 'Missing ECLIPSE_HOME var entry.')
  call vunit#AssertTrue(eclim#util#ListContains(results, 'USER_HOME'),
    \ 'Missing USER_HOME var entry.')
endfunction " }}}

" TestCommandCompleteVar() {{{
function! TestCommandCompleteVar()
  edit! .classpath
  call vunit#PeekRedir()

  let results = eclim#java#classpath#CommandCompleteVar('', 'VariableDelete ', 15)
  call vunit#AssertTrue(len(results) > 1, 'Not enough var results.')
  call vunit#AssertTrue(eclim#util#ListContains(results, 'ECLIPSE_HOME'),
    \ 'Missing ECLIPSE_HOME var entry.')
  call vunit#AssertTrue(eclim#util#ListContains(results, 'JRE_LIB'),
    \ 'Missing JRE_LIB var entry.')
  call vunit#AssertTrue(eclim#util#ListContains(results, 'USER_HOME'),
    \ 'Missing USER_HOME var entry.')

  let results = eclim#java#classpath#CommandCompleteVar('JRE', 'VariableDelete JRE', 18)
  call vunit#AssertTrue(len(results) > 1, 'Not enough var results.')
  for result in results
    call vunit#AssertTrue(result =~ '^JRE', 'Var does not begin with JRE.')
  endfor
endfunction " }}}

" TestCommandCompleteVarPath() {{{
function! TestCommandCompleteVarPath()
  edit! .classpath
  call vunit#PeekRedir()

  let results = eclim#java#classpath#CommandCompleteVarPath('', 'NewVarEntry ', 12)
  call vunit#AssertTrue(len(results) > 1, 'Not enough var results.')
  for result in results
    if result =~ '^\(ECLIPSE_HOME\|USER_HOME\)'
      call vunit#AssertTrue(result =~ '.*/$', 'Var path does not have trailing slash.')
    endif
  endfor
  call vunit#AssertTrue(eclim#util#ListContains(results, 'ECLIPSE_HOME/'),
    \ 'Missing ECLIPSE_HOME/ var path entry.')
  call vunit#AssertTrue(eclim#util#ListContains(results, 'USER_HOME/'),
    \ 'Missing USER_HOME/ var path entry.')

  let results = eclim#java#classpath#CommandCompleteVarPath(
    \ 'ECLIPSE_HOME/e', 'NewVarEntry ECLIPSE_HOME/e', 26)
  call vunit#AssertTrue(len(results) > 1, 'Not enough var results.')
  for result in results
    call vunit#AssertTrue(result =~ '^ECLIPSE_HOME/e',
      \ 'Var path result did not start with ECLIPSE_HOME/e.')
  endfor
  call vunit#AssertTrue(eclim#util#ListContains(results, 'ECLIPSE_HOME/eclim'),
    \ 'Missing ECLIPSE_HOME/eclim var path entry.')
endfunction " }}}

" TestCommandCompleteVarAndDir() {{{
function! TestCommandCompleteVarAndDir()
  edit! .classpath
  call vunit#PeekRedir()

  let results = eclim#java#classpath#CommandCompleteVarAndDir(
    \ '', 'VariableCreate /hom', 15)
  call vunit#AssertTrue(len(results) > 1, 'Not enough var results.')
  call vunit#AssertTrue(eclim#util#ListContains(results, 'ECLIPSE_HOME'),
    \ 'Missing ECLIPSE_HOME var entry.')
  call vunit#AssertTrue(eclim#util#ListContains(results, 'JRE_LIB'),
    \ 'Missing JRE_LIB var entry.')
  call vunit#AssertTrue(eclim#util#ListContains(results, 'USER_HOME'),
    \ 'Missing USER_HOME var entry.')

  let results = eclim#java#classpath#CommandCompleteVarAndDir(
    \ 'JRE', 'VariableCreate JRE /hom', 18)
  call vunit#AssertTrue(len(results) > 1, 'Not enough var results.')
  for result in results
    call vunit#AssertTrue(result =~ '^JRE', 'Var does not begin with JRE.')
  endfor

  let results = eclim#java#classpath#CommandCompleteVarAndDir(
    \ 'JRE', 'VariableCreate JRE /b', 23)
  call vunit#AssertTrue(len(results) > 1, 'Not enough dir results.')
  call vunit#AssertTrue(eclim#util#ListContains(results, '/boot/'),
    \ 'Missing /boot/ entry.')
  call vunit#AssertTrue(eclim#util#ListContains(results, '/bin/'),
    \ 'Missing /bin/ entry.')
endfunction " }}}

" vim:ft=vim:fdm=marker
