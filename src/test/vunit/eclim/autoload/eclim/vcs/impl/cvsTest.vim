" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for impl/cvs.vim
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
  let s:test_dir = g:TestEclimWorkspace . 'eclim_unit_test/vcs/cvs/unittest'
  exec 'cd ' . s:test_dir
endfunction " }}}

" TestInfo() {{{
function! TestInfo()
  view test/file1.txt
  call PushRedir('@"')
  VcsInfo
  call PopRedir()
  let info = split(@", '\n')
  call VUAssertEquals(info[0], 'Status: Locally Modified')
  call VUAssertTrue(info[1] =~ 'Working revision: 1.3 .*')
endfunction " }}}

" TestAnnotate() {{{
function! TestAnnotate()
  view test/file1.txt
  call PeekRedir()
  call PushRedir('@"')
  VcsAnnotate
  call PopRedir()
  let existing = eclim#display#signs#GetExisting()
  call PeekRedir()
  call VUAssertEquals(len(existing), 4)
  call VUAssertEquals(existing[0].name, 'vcs_annotate_ervand')

  call VUAssertEquals(b:vcs_annotations[0], '1.1 (10-Jul-09) ervandew')

  call PushRedir('@"')
  VcsAnnotate
  call PopRedir()
  let existing = eclim#display#signs#GetExisting()
  call PeekRedir()
  call VUAssertEquals(len(existing), 0)
endfunction " }}}

" TestDiff() {{{
function! TestDiff()
  view test/file1.txt
  call PeekRedir()
  VcsDiff
  call VUAssertEquals(expand('%'), 'test/file1.txt')
  call VUAssertEquals(line('$'), 5)

  winc l

  call VUAssertEquals(expand('%'), 'vcs_1.3_file1.txt')
  call VUAssertEquals(line('$'), 4)
endfunction " }}}

" TestLog() {{{
function! TestLog()
  view test/file1.txt
  call PeekRedir()
  VcsLog
  call VUAssertEquals(expand('%'), '[vcs_log]')
  call VUAssertEquals(
    \ getline(1), '|unittest| / |test| / file1.txt')
  call VUAssertEquals(getline(4), 'Revision: 1.3 |view| |annotate|')
  call VUAssertEquals(getline(13), 'Revision: 1.2 |view| |annotate|')
  call VUAssertEquals(getline(20), 'Revision: 1.1 |view| |annotate|')

  " dir listing
  call cursor(1, 6)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(getline(1), '/')
  call VUAssertEquals(getline(3), '|test/|')
  call VUAssertEquals(getline(4), '|file.txt|')
  bdelete
  VcsLog

  " view
  call cursor(13, 16)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(expand('%'), 'vcs_1.2_file1.txt')
  bdelete
  VcsLog

  " annotate
  call cursor(20, 23)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(expand('%'), 'vcs_1.1_file1.txt')
  call VUAssertEquals(b:vcs_annotations[0], '1.1 (10-Jul-09) ervandew')
  bdelete
  VcsLog

  " diff previous
  call cursor(6, 8)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(expand('%'), 'vcs_1.3_file1.txt')
  call VUAssertEquals(line('$'), 4)
  winc l
  call VUAssertEquals(expand('%'), 'vcs_1.2_file1.txt')
  call VUAssertEquals(line('$'), 3)
  bdelete
  bdelete
  VcsLog

  " diff working copy
  call cursor(22, 27)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(expand('%'), 'test/file1.txt')
  call VUAssertEquals(line('$'), 5)
  winc l
  call VUAssertEquals(expand('%'), 'vcs_1.1_file1.txt')
  call VUAssertEquals(line('$'), 2)
endfunction " }}}

" vim:ft=vim:fdm=marker
