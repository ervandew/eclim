" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for project/run.vim
"
" License:
"
" Copyright (C) 2014  Eric Van Dewoestine
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

function! SetUp() " {{{
  exec 'cd ' . g:TestEclimWorkspace

  " gross, but we want to be able to test ProjectProblems without triggering
  " an eclipse dialog attempting to run a project with problems.
  edit! eclim_unit_test/src/org/eclim/test/Test.java
  let s:test_content = readfile(expand('%'))
  1,$delete
  call append(0, ['package org.eclim.test;', 'public class Test {}'])
  write
endfunction " }}}

function! TearDown() " {{{
  " restore orginal Test.java content.
  edit! eclim_unit_test/src/org/eclim/test/Test.java
  1,$delete
  call append(0, s:test_content)
  $,$delete
  write
endfunction " }}}

function! TestProjectRun() " {{{
  " not currently supported on Windows.
  if has('win32') || has('win64')
    return
  endif

  edit! eclim_unit_test/src/org/eclim/test/Main.java

  call vunit#AssertEquals(winnr('$'), 1)

  call vunit#PeekRedir()
  ProjectRun

  " wait on the output window for up to 10 seconds
  " Note: the sleep itself slows down the run process since all the ui updates
  " wait on the sleep as well.
  let tries = 0
  while winnr('$') == 1 && tries < 100
    sleep 100m
    let tries += 1
  endwhile

  call vunit#AssertEquals(winnr('$'), 2)
  winc w

  " wait on the program output for up to 3 seconds
  let tries = 0
  let output = s:Output()
  while len(output) < 3 && tries < 30
    sleep 100m
    let tries += 1
    let output = s:Output()
  endwhile

  call vunit#PeekRedir()
  echo "Output: " . string(output)
  call vunit#AssertEquals(output[0], 'out>Test Project')
  call vunit#AssertEquals(output[1], 'err>sample error message')
  call vunit#AssertEquals(output[2], 'out><terminated>')
endfunction " }}}

function! s:Output() " {{{
  let output = getline(1, line('$'))
  return filter(output, 'v:val != "" && v:val !~ "^\\(err\\|out\\)>$"')
endfunction " }}}

" vim:ft=vim:fdm=marker
