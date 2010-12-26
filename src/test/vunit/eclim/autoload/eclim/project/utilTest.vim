" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for project/util.vim
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
  let s:test_dir = g:TestEclimWorkspace . 'eclim_unit_test/files'
  exec 'cd ' . s:test_dir
endfunction " }}}

" TestProjectRename() {{{
function! TestProjectRename()
  let g:EclimProjectRenamePrompt = 0

  edit! test1.txt
  split ../test_root_file.txt
  call vunit#PeekRedir()

  call vunit#AssertTrue(
    \ isdirectory(g:TestEclimWorkspace . 'eclim_unit_test'),
    \ "initial project directory doesn't exist")
  let cwd = substitute(getcwd(), '\', '/', 'g')
  call vunit#AssertEquals(cwd, s:test_dir, 'initial cwd is incorrect')

  ProjectRename eclim_unit_test_rename
  call vunit#PeekRedir()

  try
    call vunit#AssertFalse(
      \ isdirectory(g:TestEclimWorkspace . 'eclim_unit_test'),
      \ "initial project directory still exists")
    call vunit#AssertTrue(
      \ isdirectory(g:TestEclimWorkspace . 'eclim_unit_test_rename'),
      \ "renamed project directory doesn't exist")
    let cwd = substitute(getcwd(), '\', '/', 'g')
    call vunit#AssertEquals(cwd,
      \ substitute(s:test_dir, 'eclim_unit_test', 'eclim_unit_test_rename', ''),
      \ 'post rename cwd is incorrect')
    let name = substitute(expand('%:p'), '\', '/', 'g')
    call vunit#AssertEquals(name,
      \ g:TestEclimWorkspace . 'eclim_unit_test_rename/test_root_file.txt',
      \ 'wrong file name for root file')
    bdelete
    call vunit#AssertEquals(expand('%'), 'test1.txt', 'wrong file name for test1 file')
  finally
    ProjectRename eclim_unit_test
  endtry
endfunction " }}}

" TestProjectCD() {{{
function! TestProjectCD()
  let cwd = substitute(getcwd(), '\', '/', 'g')
  call vunit#AssertEquals(cwd, s:test_dir, "Setup failed.")

  call eclim#project#util#ProjectCD(0)
  let cwd = substitute(getcwd(), '\', '/', 'g')
  call vunit#AssertEquals(cwd, g:TestEclimWorkspace . 'eclim_unit_test',
    \ "Project cd failed.")
endfunction " }}}

" TestProjectSettings() {{{
function! TestProjectSettings()
  call eclim#project#util#ProjectSettings('eclim_unit_test')
  call vunit#AssertEquals('eclim_unit_test_settings', expand('%'),
    \ "Didn't open settings window.")
  close
endfunction " }}}

" TestGetCurrentProjectName() {{{
function! TestGetCurrentProjectName()
  let name = eclim#project#util#GetCurrentProjectName()
  call vunit#AssertEquals('eclim_unit_test', name, "Wrong project name.")

  view ../../eclim_unit_test_java_linked/src/org/eclim/test/TestLinked.java
  let name = eclim#project#util#GetCurrentProjectName()
  call vunit#AssertEquals(
    \ 'eclim_unit_test_java', name, "Wrong project name for linked resource.")
endfunction " }}}

" TestGetCurrentProjectRoot() {{{
function! TestGetCurrentProjectRoot()
  let dir = eclim#project#util#GetCurrentProjectRoot()
  call vunit#AssertEquals(g:TestEclimWorkspace . 'eclim_unit_test', dir,
    \ "Wrong project dir.")

  view ../../eclim_unit_test_java_linked/src/org/eclim/test/TestLinked.java
  let dir = eclim#project#util#GetCurrentProjectRoot()
  call vunit#AssertEquals(g:TestEclimWorkspace . 'eclim_unit_test_java', dir,
    \ "Wrong project dir for linked resource.")
endfunction " }}}

" TestGetProjectRelativeFilePath() {{{
function! TestGetProjectRelativeFilePath()
  let path = eclim#project#util#GetProjectRelativeFilePath(
    \ g:TestEclimWorkspace . 'eclim_unit_test/files/test1.txt')
  call vunit#AssertEquals('files/test1.txt', path, "Wrong project file path.")

  let path = eclim#project#util#GetProjectRelativeFilePath(
    \ g:TestEclimWorkspace .
    \ 'eclim_unit_test_java_linked/src/org/eclim/test/TestLinked.java')
  call vunit#AssertEquals(
    \ 'src-linked/org/eclim/test/TestLinked.java', path,
    \ "Wrong project file path for linked resource.")
endfunction " }}}

" TestIsCurrentFileInProject() {{{
function! TestIsCurrentFileInProject()
  call vunit#AssertTrue(eclim#project#util#IsCurrentFileInProject(0), "Wrong result.")
  cd ~
  call vunit#AssertFalse(eclim#project#util#IsCurrentFileInProject(0), "Wrong result.")
endfunction " }}}

" TestCommandCompleteProject() {{{
function! TestCommandCompleteProject()
  let results = eclim#project#util#CommandCompleteProject(
    \ 'eclim_', 'ProjectRefresh eclim_', 21)

  call vunit#AssertEquals(7, len(results), "Wrong number of results.")
  call vunit#AssertEquals('eclim_unit_test', results[0])
  call vunit#AssertEquals('eclim_unit_test_c', results[1])
  call vunit#AssertEquals('eclim_unit_test_java', results[2])
  call vunit#AssertEquals('eclim_unit_test_php', results[3])
  call vunit#AssertEquals('eclim_unit_test_python', results[4])
  call vunit#AssertEquals('eclim_unit_test_ruby', results[5])
  call vunit#AssertEquals('eclim_unit_test_web', results[6])
endfunction " }}}

" vim:ft=vim:fdm=marker
