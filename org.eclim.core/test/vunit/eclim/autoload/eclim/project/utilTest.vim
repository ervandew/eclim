" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for project/util.vim
"
" License:
"
" Copyright (C) 2005 - 2011  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace
  let g:EclimProjectRenamePrompt = 0

  ProjectDelete unit_test_rename_pre
  ProjectDelete unit_test_rename_post

  ProjectCreate unit_test_rename_pre -n none

  edit unit_test_rename_pre/test1.txt
  call setline(1, 'test1')
  split unit_test_rename_pre/test2.txt
  call setline(1, 'test2')
  call vunit#PeekRedir()

  call vunit#AssertTrue(
    \ isdirectory(g:TestEclimWorkspace . 'unit_test_rename_pre'),
    \ "initial project directory doesn't exist")
  cd unit_test_rename_pre
  let cwd = substitute(getcwd(), '\', '/', 'g')
  call vunit#AssertEquals(
    \ cwd, g:TestEclimWorkspace . 'unit_test_rename_pre', 'initial cwd is incorrect')

  ProjectRename unit_test_rename_post
  call vunit#PeekRedir()

  try
    call vunit#AssertFalse(
      \ isdirectory(g:TestEclimWorkspace . 'unit_test_rename_pre'),
      \ "initial project directory still exists")
    call vunit#AssertTrue(
      \ isdirectory(g:TestEclimWorkspace . 'unit_test_rename_post'),
      \ "renamed project directory doesn't exist")
    let cwd = substitute(getcwd(), '\', '/', 'g')
    call vunit#AssertEquals(
      \ cwd, g:TestEclimWorkspace . 'unit_test_rename_post',
      \ 'post rename cwd is incorrect')
    let name = substitute(expand('%:p'), '\', '/', 'g')
    call vunit#AssertEquals(name,
      \ g:TestEclimWorkspace . 'unit_test_rename_post/test2.txt',
      \ 'wrong file name test2 file')
    bdelete
    call vunit#AssertEquals(
      \ expand('%'), 'test1.txt', 'wrong file name for test1 file')
  finally
    ProjectDelete unit_test_rename_post
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

  view ../../eclim_unit_test_linked/other/foo/bar.txt
  let name = eclim#project#util#GetCurrentProjectName()
  call vunit#AssertEquals(
    \ 'eclim_unit_test', name, "Wrong project name for linked resource.")
endfunction " }}}

" TestGetCurrentProjectRoot() {{{
function! TestGetCurrentProjectRoot()
  let dir = eclim#project#util#GetCurrentProjectRoot()
  call vunit#AssertEquals(g:TestEclimWorkspace . 'eclim_unit_test', dir,
    \ "Wrong project dir.")

  view ../../eclim_unit_test_linked/other/foo/bar.txt
  let dir = eclim#project#util#GetCurrentProjectRoot()
  call vunit#AssertEquals(g:TestEclimWorkspace . 'eclim_unit_test', dir,
    \ "Wrong project dir for linked resource.")
endfunction " }}}

" TestGetProjectRelativeFilePath() {{{
function! TestGetProjectRelativeFilePath()
  let path = eclim#project#util#GetProjectRelativeFilePath(
    \ g:TestEclimWorkspace . 'eclim_unit_test/files/test1.txt')
  call vunit#AssertEquals('files/test1.txt', path, "Wrong project file path.")

  let path = eclim#project#util#GetProjectRelativeFilePath(
    \ g:TestEclimWorkspace . 'eclim_unit_test_linked/other/foo/bar.txt')
  call vunit#AssertEquals(
    \ 'linked/foo/bar.txt', path,
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

  call vunit#AssertEquals('eclim_unit_test', results[0])
endfunction " }}}

" vim:ft=vim:fdm=marker
