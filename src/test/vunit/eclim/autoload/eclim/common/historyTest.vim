" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for history.vim
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test'
endfunction " }}}

" TestHistory() {{{
function! TestHistory()
  edit! history/sample_vunit.txt
  call PeekRedir()

  HistoryClear!

  call VUAssertEquals(getline(1), 'line 1', 'Wrong first line.')
  call VUAssertEquals(line('$'), 1, 'Wrong number of lines.')

  History

  call VUAssertEquals(expand('%'), '[History]', 'Wrong history buffer name.')
  call VUAssertEquals(line('$'), 3, 'Wrong number of history lines.')
  call VUAssertEquals(line(1), 'history/sample_vunit.txt', 'Wrong file name.')
  call VUAssertEquals(line(2), '')
  call VUAssertEquals(line(3), 'v: view  d: diff  r: revert  c: clear')
  bdelete

  set modified
  write
  sleep
  call append(1, 'line 2')
  write

  History

  call VUAssertEquals(expand('%'), '[History]', 'Wrong history buffer name.')
  call VUAssertEquals(line('$'), 5, 'Wrong number of history lines.')
  call VUAssertTrue(getline(2) =~ '^\s\+\d\d:\d\d \w\{3} \w\{3} \d\d \d\{4} (\d\+ \(millis\|seconds\?\) ago)', 'Invalid entry 1.')
  call VUAssertTrue(getline(3) =~ '^\s\+\d\d:\d\d \w\{3} \w\{3} \d\d \d\{4} (\d\+ .* ago)', 'Invalid entry 2.')
  call VUAssertEquals(getline(4), '')
  call VUAssertEquals(getline(5), 'v: view  d: diff  r: revert  c: clear')

  call cursor(3, 1)
  normal v
  call VUAssertTrue(expand('%') =~ 'history/sample_vunit.txt_\d\+', 'Wrong view buffer name.')
  call VUAssertEquals(getline(1), 'line 1', 'Wrong first line.')
  call VUAssertEquals(line('$'), 1, 'Wrong number of lines.')
  call VUAssertEquals(winnr('$'), 3, 'Wrong number of windows.')
  bdelete

  History

  call cursor(3, 1)
  normal d
  call VUAssertTrue(expand('%') =~ 'history/sample_vunit.txt', 'Wrong buffer.')
  call VUAssertEquals(line('$'), 2, 'Wrong number of lines.')
  call VUAssertEquals(&diff, 1, 'Diff not enabled.')
  winc w
  call VUAssertTrue(expand('%') =~ 'history/sample_vunit.txt_\d\+', 'Wrong diff buffer name.')
  call VUAssertEquals(getline(1), 'line 1', 'Wrong first line.')
  call VUAssertEquals(line('$'), 1, 'Wrong number of lines.')
  call VUAssertEquals(&diff, 1, 'Diff not enabled.')
  bdelete
  call VUAssertTrue(expand('%') =~ 'history/sample_vunit.txt', 'Wrong buffer.')
  call VUAssertEquals(line('$'), 2, 'Wrong number of lines.')
  call VUAssertEquals(&diff, 0, 'Diff not disabled.')

  History

  call cursor(3, 1)
  normal r
  call VUAssertTrue(expand('%') =~ 'history/sample_vunit.txt', 'Wrong buffer.')
  call VUAssertEquals(getline(1), 'line 1', 'Wrong first line.')
  call VUAssertEquals(line('$'), 1, 'Wrong number of lines.')
  call VUAssertEquals(&modified, 1, 'Buffer not marked modified.')
endfunction " }}}

" vim:ft=vim:fdm=marker
