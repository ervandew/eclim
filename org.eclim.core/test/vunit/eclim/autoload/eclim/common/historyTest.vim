" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for history.vim
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test'
  let g:EclimProjectKeepLocalHistory = 1
  runtime plugin/project.vim
endfunction " }}}

" TestHistory() {{{
function! TestHistory()
  edit! history/sample_vunit.txt
  call vunit#PeekRedir()

  HistoryClear!

  call vunit#AssertEquals(getline(1), 'line 1', 'Wrong first line.')
  call vunit#AssertEquals(line('$'), 1, 'Wrong number of lines.')

  History

  call vunit#AssertEquals(expand('%'), '[History]', 'Wrong history buffer name.')
  call vunit#AssertEquals(line('$'), 3, 'Wrong number of history lines.')
  call vunit#AssertEquals(line(1), 'history/sample_vunit.txt', 'Wrong file name.')
  call vunit#AssertEquals(line(2), '')
  call vunit#AssertEquals(line(3), '" use ? to view help')
  bdelete

  set modified
  write
  sleep
  call append(1, 'line 2')
  write

  History

  call vunit#AssertEquals(expand('%'), '[History]', 'Wrong history buffer name.')
  call vunit#AssertEquals(line('$'), 5, 'Wrong number of history lines.')
  call vunit#AssertTrue(getline(2) =~ '^\s\+\d\d:\d\d \w\{3} \w\{3} \d\d \d\{4} (\d\+ \(millis\|seconds\?\) ago)', 'Invalid entry 1.')
  call vunit#AssertTrue(getline(3) =~ '^\s\+\d\d:\d\d \w\{3} \w\{3} \d\d \d\{4} (\d\+ .* ago)', 'Invalid entry 2.')
  call vunit#AssertEquals(getline(4), '')
  call vunit#AssertEquals(getline(5), '" use ? to view help')

  call cursor(3, 1)
  exec "normal \<cr>"
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertTrue(name =~ 'history/sample_vunit.txt_\d\+', 'Wrong view buffer name.')
  call vunit#AssertEquals(getline(1), 'line 1', 'Wrong first line.')
  call vunit#AssertEquals(line('$'), 1, 'Wrong number of lines.')
  call vunit#AssertEquals(winnr('$'), 3, 'Wrong number of windows.')
  bdelete

  History

  call cursor(3, 1)
  normal d
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertTrue(name =~ 'history/sample_vunit.txt', 'Wrong buffer.')
  call vunit#AssertEquals(line('$'), 2, 'Wrong number of lines.')
  call vunit#AssertEquals(&diff, 1, 'Diff not enabled.')
  winc w
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertTrue(name =~ 'history/sample_vunit.txt_\d\+', 'Wrong diff buffer name.')
  call vunit#AssertEquals(getline(1), 'line 1', 'Wrong first line.')
  call vunit#AssertEquals(line('$'), 1, 'Wrong number of lines.')
  call vunit#AssertEquals(&diff, 1, 'Diff not enabled.')
  bdelete
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertTrue(name =~ 'history/sample_vunit.txt', 'Wrong buffer.')
  call vunit#AssertEquals(line('$'), 2, 'Wrong number of lines.')
  call vunit#AssertEquals(&diff, 0, 'Diff not disabled.')

  History

  call cursor(3, 1)
  normal r
  let name = substitute(expand('%'), '\', '/', 'g')
  call vunit#AssertTrue(name =~ 'history/sample_vunit.txt', 'Wrong buffer.')
  call vunit#AssertEquals(getline(1), 'line 1', 'Wrong first line.')
  call vunit#AssertEquals(line('$'), 1, 'Wrong number of lines.')
  call vunit#AssertEquals(&modified, 1, 'Buffer not marked modified.')
endfunction " }}}

" vim:ft=vim:fdm=marker
