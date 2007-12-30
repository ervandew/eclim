" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for classpath.vim
"
" License:
"
" Copyright (c) 2005 - 2008
"
" Licensed under the Apache License, Version 2.0 (the "License");
" you may not use this file except in compliance with the License.
" You may obtain a copy of the License at
"
"      http://www.apache.org/licenses/LICENSE-2.0
"
" Unless required by applicable law or agreed to in writing, software
" distributed under the License is distributed on an "AS IS" BASIS,
" WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
" See the License for the specific language governing permissions and
" limitations under the License.
"
" }}}

" SetUp() {{{
function! SetUp ()
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
endfunction " }}}

" TestUpdateClasspath() {{{
function! TestUpdateClasspath ()
  edit! .classpath
  call PeekRedir()

  NewJarEntry lib/blah.jar

  call VUAssertTrue(search('blah\.jar'), 'blah.jar not added.')

  update!
  call PeekRedir()
  let errors = getloclist(0)
  call VUAssertEquals(1, len(errors), 'Wrong number of errors.')
  let message = errors[0].text
  echom message
  call VUAssertTrue(message =~ 'missing required library:.*blah.jar',
    \ 'Wrong error message.')

  undo
  update!
  call PeekRedir()
  let errors = getloclist(0)
  call VUAssertEquals(0, len(errors), 'Still contains errors.')
endfunction " }}}

" TestVariableCreateDelete() {{{
function! TestVariableCreateDelete ()
  edit! .classpath
  call PeekRedir()

  let vars = eclim#java#classpath#GetVariableNames()
  call VUAssertFalse(eclim#util#ListContains(vars, '.*VUNIT_TEST_VAR.*'),
    \ 'VUNIT_TEST_VAR alread found in variable list.')

  VariableCreate VUNIT_TEST_VAR /home

  let vars = eclim#java#classpath#GetVariableNames()
  call VUAssertTrue(eclim#util#ListContains(vars, '.*VUNIT_TEST_VAR.*'),
    \ 'VUNIT_TEST_VAR not found in variable list.')

  VariableDelete VUNIT_TEST_VAR

  let vars = eclim#java#classpath#GetVariableNames()
  call VUAssertFalse(eclim#util#ListContains(vars, '.*VUNIT_TEST_VAR.*'),
    \ 'VUNIT_TEST_VAR still found in variable list.')
endfunction " }}}

" TestGetVariableNames() {{{
function! TestGetVariableNames ()
  edit! .classpath
  call PeekRedir()

  let results = eclim#java#classpath#GetVariableNames()
  call VUAssertTrue(len(results) > 1, 'Not enough var results.')
  call VUAssertTrue(eclim#util#ListContains(results, 'ECLIPSE_HOME'),
    \ 'Missing ECLIPSE_HOME var entry.')
  call VUAssertTrue(eclim#util#ListContains(results, 'USER_HOME'),
    \ 'Missing USER_HOME var entry.')
endfunction " }}}

" TestCommandCompleteVar() {{{
function! TestCommandCompleteVar()
  edit! .classpath
  call PeekRedir()

  let results = eclim#java#classpath#CommandCompleteVar('', 'VariableDelete ', 15)
  call VUAssertTrue(len(results) > 1, 'Not enough var results.')
  call VUAssertTrue(eclim#util#ListContains(results, 'ECLIPSE_HOME'),
    \ 'Missing ECLIPSE_HOME var entry.')
  call VUAssertTrue(eclim#util#ListContains(results, 'JRE_LIB'),
    \ 'Missing JRE_LIB var entry.')
  call VUAssertTrue(eclim#util#ListContains(results, 'USER_HOME'),
    \ 'Missing USER_HOME var entry.')

  let results = eclim#java#classpath#CommandCompleteVar('JRE', 'VariableDelete JRE', 18)
  call VUAssertTrue(len(results) > 1, 'Not enough var results.')
  for result in results
    call VUAssertTrue(result =~ '^JRE', 'Var does not begin with JRE.')
  endfor
endfunction " }}}

" TestCommandCompleteVarPath() {{{
function! TestCommandCompleteVarPath()
  edit! .classpath
  call PeekRedir()

  let results = eclim#java#classpath#CommandCompleteVarPath('', 'NewVarEntry ', 12)
  call VUAssertTrue(len(results) > 1, 'Not enough var results.')
  for result in results
    if result !~ '^JRE'
      call VUAssertTrue(result =~ '.*/$', 'Var path does not have trailing slash.')
    endif
  endfor
  call VUAssertTrue(eclim#util#ListContains(results, 'ECLIPSE_HOME/'),
    \ 'Missing ECLIPSE_HOME/ var path entry.')
  call VUAssertTrue(eclim#util#ListContains(results, 'USER_HOME/'),
    \ 'Missing USER_HOME/ var path entry.')

  let results = eclim#java#classpath#CommandCompleteVarPath(
    \ 'ECLIPSE_HOME/e', 'NewVarEntry ECLIPSE_HOME/e', 26)
  call VUAssertTrue(len(results) > 1, 'Not enough var results.')
  for result in results
    call VUAssertTrue(result =~ '^ECLIPSE_HOME/e',
      \ 'Var path result did not start with ECLIPSE_HOME/e.')
  endfor
  call VUAssertTrue(eclim#util#ListContains(results, 'ECLIPSE_HOME/eclipse'),
    \ 'Missing ECLIPSE_HOME/eclipse var path entry.')
endfunction " }}}

" TestCommandCompleteVarAndDir() {{{
function! TestCommandCompleteVarAndDir()
  edit! .classpath
  call PeekRedir()

  let results = eclim#java#classpath#CommandCompleteVarAndDir(
    \ '', 'VariableCreate /hom', 15)
  call VUAssertTrue(len(results) > 1, 'Not enough var results.')
  call VUAssertTrue(eclim#util#ListContains(results, 'ECLIPSE_HOME'),
    \ 'Missing ECLIPSE_HOME var entry.')
  call VUAssertTrue(eclim#util#ListContains(results, 'JRE_LIB'),
    \ 'Missing JRE_LIB var entry.')
  call VUAssertTrue(eclim#util#ListContains(results, 'USER_HOME'),
    \ 'Missing USER_HOME var entry.')

  let results = eclim#java#classpath#CommandCompleteVarAndDir(
    \ 'JRE', 'VariableCreate JRE /hom', 18)
  call VUAssertTrue(len(results) > 1, 'Not enough var results.')
  for result in results
    call VUAssertTrue(result =~ '^JRE', 'Var does not begin with JRE.')
  endfor

  let results = eclim#java#classpath#CommandCompleteVarAndDir(
    \ 'JRE', 'VariableCreate JRE /b', 23)
  call VUAssertTrue(len(results) > 1, 'Not enough dir results.')
  call VUAssertTrue(eclim#util#ListContains(results, '/boot/'),
    \ 'Missing /boot/ entry.')
  call VUAssertTrue(eclim#util#ListContains(results, '/bin/'),
    \ 'Missing /bin/ entry.')
endfunction " }}}

" vim:ft=vim:fdm=marker
