" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for project.vim
"
" License:
"
" Copyright (C) 2012 - 2013  Eric Van Dewoestine
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

function! TestGetTargets() " {{{
  let targets = eclim#android#project#GetTargets(g:TestEclimWorkspace)
  call vunit#AssertTrue(len(targets) > 0, "No targets found.")
  for target in targets
    call vunit#AssertTrue(has_key(target, 'name'), "Missing name key.")
    call vunit#AssertTrue(has_key(target, 'hash'), "Missing hash key.")
    call vunit#AssertTrue(has_key(target, 'api'), "Missing api key.")
  endfor
endfunction " }}}

function! TestProjectCreatePre() " {{{
  let path = g:TestEclimWorkspace . '/eclim_unit_test_android_vunit'

  silent! ProjectDelete eclim_unit_test_android_vunit
  exec '!rm -r ' . escape(path, ' ')

  let targets = eclim#android#project#GetTargets(g:TestEclimWorkspace)
  call vunit#AssertTrue(len(targets) > 0, "No targets found.")

  if len(targets) > 1
    let g:EclimTestPromptQueue = [
        \ 0, 'org.test', 'Test VUnit App', 'n', 'y', 'TestVUnitAppActivity']
  else
    let g:EclimTestPromptQueue = [
        \ 'org.test', 'Test VUnit App', 'n', 'y', 'TestVUnitAppActivity']
  endif

  let g:result = ''
  call vunit#PushRedir('=> g:result')
  exec 'ProjectCreate ' . path . ' -n android'
  call vunit#PopRedir()
  let g:result = substitute(g:result, "^\n", '', '')
  call vunit#AssertEquals(g:result, "Created project 'eclim_unit_test_android_vunit'.")
  call vunit#AssertTrue(
    \ filereadable(path . '/src/org/test/TestVUnitAppActivity.java'),
    \ "Activity not created.")
endfunction " }}}

" vim:ft=vim:fdm=marker
