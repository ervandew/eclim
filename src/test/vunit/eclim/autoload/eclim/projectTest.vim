" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for project.vim
"
" License:
"
" Copyright (c) 2005 - 2006
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
  let s:test_dir = g:TestEclimWorkspace . 'eclim_unit_test_java/src/org/eclim'
  exec 'cd ' . s:test_dir
endfunction " }}}

" TestProjectCD() {{{
function! TestProjectCD ()
  call VUAssertEquals(s:test_dir, getcwd(), "Setup failed.")

  call eclim#project#util#ProjectCD(0)
  call VUAssertEquals(g:TestEclimWorkspace . 'eclim_unit_test_java', getcwd(),
    \ "Project cd failed.")
endfunction " }}}

" TestProjectSettings() {{{
function! TestProjectSettings ()
  call eclim#project#util#ProjectSettings('eclim_unit_test_java')
  call VUAssertEquals('eclim_unit_test_java_settings', expand('%'),
    \ "Didn't open settings window.")
  close
endfunction " }}}

" TestGetCurrentProjectName() {{{
function! TestGetCurrentProjectName ()
  let name = eclim#project#util#GetCurrentProjectName()
  call VUAssertEquals('eclim_unit_test_java', name, "Wrong project name.")
endfunction " }}}

" TestGetCurrentProjectRoot() {{{
function! TestGetCurrentProjectRoot ()
  let dir = eclim#project#util#GetCurrentProjectRoot()
  call VUAssertEquals(g:TestEclimWorkspace . 'eclim_unit_test_java', dir,
    \ "Wrong project dir.")
endfunction " }}}

" TestIsCurrentFileInProject() {{{
function! TestIsCurrentFileInProject ()
  call VUAssertTrue(eclim#project#util#IsCurrentFileInProject(0), "Wrong result.")
  cd ~
  call VUAssertFalse(eclim#project#util#IsCurrentFileInProject(0), "Wrong result.")
endfunction " }}}

" TestCommandCompleteProject() {{{
function! TestCommandCompleteProject ()
  let results = eclim#project#util#CommandCompleteProject(
    \ 'eclim_', 'ProjectRefresh eclim_', 21)

  call VUAssertEquals(2, len(results), "Wrong number of results.")
  call VUAssertEquals('eclim_unit_test_java', results[0], "Wrong result.")
  call VUAssertEquals('eclim_unit_test_php', results[1], "Wrong result.")
endfunction " }}}

" vim:ft=vim:fdm=marker
