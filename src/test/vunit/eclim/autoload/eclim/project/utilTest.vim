" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for project/util.vim
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
  let s:test_dir = g:TestEclimWorkspace . 'eclim_unit_test/files'
  exec 'cd ' . s:test_dir
endfunction " }}}

" TestProjectCD() {{{
function! TestProjectCD()
  call VUAssertEquals(s:test_dir, getcwd(), "Setup failed.")

  call eclim#project#util#ProjectCD(0)
  call VUAssertEquals(g:TestEclimWorkspace . 'eclim_unit_test', getcwd(),
    \ "Project cd failed.")
endfunction " }}}

" TestProjectSettings() {{{
function! TestProjectSettings()
  call eclim#project#util#ProjectSettings('eclim_unit_test')
  call VUAssertEquals('eclim_unit_test_settings', expand('%'),
    \ "Didn't open settings window.")
  close
endfunction " }}}

" TestGetCurrentProjectName() {{{
function! TestGetCurrentProjectName()
  let name = eclim#project#util#GetCurrentProjectName()
  call VUAssertEquals('eclim_unit_test', name, "Wrong project name.")
endfunction " }}}

" TestGetCurrentProjectRoot() {{{
function! TestGetCurrentProjectRoot()
  let dir = eclim#project#util#GetCurrentProjectRoot()
  call VUAssertEquals(g:TestEclimWorkspace . 'eclim_unit_test', dir,
    \ "Wrong project dir.")
endfunction " }}}

" TestIsCurrentFileInProject() {{{
function! TestIsCurrentFileInProject()
  call VUAssertTrue(eclim#project#util#IsCurrentFileInProject(0), "Wrong result.")
  cd ~
  call VUAssertFalse(eclim#project#util#IsCurrentFileInProject(0), "Wrong result.")
endfunction " }}}

" TestCommandCompleteProject() {{{
function! TestCommandCompleteProject()
  let results = eclim#project#util#CommandCompleteProject(
    \ 'eclim_', 'ProjectRefresh eclim_', 21)

  call VUAssertEquals(7, len(results), "Wrong number of results.")
  call VUAssertEquals('eclim_unit_test', results[0])
  call VUAssertEquals('eclim_unit_test_c', results[1])
  call VUAssertEquals('eclim_unit_test_java', results[2])
  call VUAssertEquals('eclim_unit_test_php', results[3])
  call VUAssertEquals('eclim_unit_test_python', results[4])
  call VUAssertEquals('eclim_unit_test_ruby', results[5])
  call VUAssertEquals('eclim_unit_test_web', results[6])
endfunction " }}}

" vim:ft=vim:fdm=marker
