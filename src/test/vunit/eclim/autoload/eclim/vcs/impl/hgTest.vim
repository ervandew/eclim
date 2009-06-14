" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for impl/hg.vim
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
  let s:test_dir = g:TestEclimWorkspace . 'eclim_unit_test/vcs/mercurial/unittest'
  exec 'cd ' . s:test_dir
endfunction " }}}

" TestInfo() {{{
function! TestInfo()
  view test/file1.txt
  call PushRedir('@"')
  VcsInfo
  call PopRedir()
  let info = split(@", '\n')
  call VUAssertEquals(info[0], 'changeset:   2:5f0911d194b1')
  call VUAssertEquals(info[1], 'tag:         tip')
  call VUAssertEquals(info[2], 'user:        ervandew')
  call VUAssertEquals(info[3], 'date:        Sat Sep 27 22:31:45 2008 -0700')
  call VUAssertEquals(info[4], 'summary:     test a multi line comment')
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

  call VUAssertEquals(
    \ b:vcs_annotations[0],
    \ '0 (Sat Sep 27 22:26:55 2008 -0700) ervandew')

  call PushRedir('@"')
  VcsAnnotate
  call PopRedir()
  let existing = eclim#display#signs#GetExisting()
  call PeekRedir()
  call VUAssertEquals(len(existing), 0)
endfunction " }}}

" TestChangeSet() {{{
function! TestChangeSet()
  view test/file1.txt
  call PeekRedir()
  VcsChangeSet
  call VUAssertEquals(expand('%'), '[vcs_log]')
  call VUAssertEquals(getline(1), 'Revision: 2:5f0911d194b1')
  call VUAssertEquals(getline(4), '  A/M |test/file1.txt|')
  call VUAssertEquals(getline(5), '  A/M |test/file2.txt|')
  call VUAssertEquals(getline(7), 'test a multi line comment')
  call VUAssertEquals(getline(8), '  - file 1')
  call VUAssertEquals(getline(9), '  - file 2')

  call cursor(5, 8)
  exec "normal \<cr>"
  call VUAssertEquals(getline(1), 'unittest / test / file2.txt')
endfunction " }}}

" TestDiff() {{{
function! TestDiff()
  view test/file1.txt
  call PeekRedir()
  VcsDiff
  call VUAssertEquals(expand('%'), 'test/file1.txt')
  call VUAssertEquals(line('$'), 5)

  winc l

  call VUAssertEquals(expand('%'), 'vcs_2:5f0911d194b1_file1.txt')
  call VUAssertEquals(line('$'), 4)
endfunction " }}}

" TestLog() {{{
function! TestLog()
  view test/file1.txt
  call PeekRedir()
  VcsLog
  call VUAssertEquals(expand('%'), '[vcs_log]')
  call VUAssertEquals(getline(1), 'unittest / test / file1.txt')
  call VUAssertEquals(
    \ getline(4),
    \ 'Revision: |2:5f0911d194b1| |view| |annotate|')
  call VUAssertEquals(
    \ getline(13),
    \ 'Revision: |1:9247ff7b10e3| |view| |annotate|')
  call VUAssertEquals(
    \ getline(20),
    \ 'Revision: |0:6a95632ba43d| |view| |annotate|')

  " view
  call cursor(13, 29)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(expand('%'), 'vcs_1:9247ff7b10e3_file1.txt')
  bdelete
  VcsLog

  " annotate
  call cursor(20, 36)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(expand('%'), 'vcs_0:6a95632ba43d_file1.txt')
  call VUAssertEquals(
    \ b:vcs_annotations[0], '0 (Sat Sep 27 22:26:55 2008 -0700) ervandew')
  bdelete
  VcsLog

  " diff previous
  call cursor(6, 8)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(expand('%'), 'vcs_2:5f0911d194b1_file1.txt')
  call VUAssertEquals(line('$'), 4)
  winc l
  call VUAssertEquals(expand('%'), 'vcs_1:9247ff7b10e3_file1.txt')
  call VUAssertEquals(line('$'), 3)
  bdelete
  bdelete
  VcsLog

  " diff working copy
  call cursor(22, 8)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(expand('%'), 'test/file1.txt')
  call VUAssertEquals(line('$'), 5)
  winc l
  call VUAssertEquals(expand('%'), 'vcs_0_file1.txt')
  call VUAssertEquals(line('$'), 2)
endfunction " }}}

" vim:ft=vim:fdm=marker
