" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for impl/svn.vim
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
  let s:test_dir = g:TestEclimWorkspace . 'eclim_unit_test/vcs/subversion/unittest'
  exec 'cd ' . s:test_dir
endfunction " }}}

" TestInfo() {{{
function! TestInfo()
  view test/file1.txt
  call PushRedir('@"')
  VcsInfo
  call PopRedir()
  let info = split(@", '\n')
  call VUAssertEquals(info[1], 'Last Changed Author: ervandew')
  call VUAssertEquals(info[2], 'Last Changed Rev: 4')
  call VUAssertEquals(
    \ info[3], 'Last Changed Date: 2008-09-27 22:32:39 -0700 (Sat, 27 Sep 2008)')
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
    \ b:vcs_annotations[0], '2 (Sat, 27 Sep 2008 22:28:05) ervandew')

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
  call VUAssertEquals(getline(1), 'Revision: 4')
  call VUAssertEquals(getline(4), '  |M| |/unittest/trunk/test/file1.txt|')
  call VUAssertEquals(getline(5), '  |M| |/unittest/trunk/test/file2.txt|')
  call VUAssertEquals(getline(7), 'test multi line comment')
  call VUAssertEquals(getline(8), '  - file 1')
  call VUAssertEquals(getline(9), '  - file 2')

  " test log on |test/file2.txt|
  call cursor(5, 8)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(
    \ getline(1), '|repos| / |unittest| / |trunk| / |test| / file2.txt')

  bdelete
  VcsChangeSet

  " test diff on |M|
  call cursor(4, 4)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(expand('%'), 'vcs_4_file1.txt')
  call VUAssertEquals(line('$'), 4)
  winc l
  call VUAssertEquals(expand('%'), 'vcs_3_file1.txt')
  call VUAssertEquals(line('$'), 3)
endfunction " }}}

" TestDiff() {{{
function! TestDiff()
  view test/file1.txt
  call PeekRedir()
  VcsDiff
  call VUAssertEquals(expand('%'), 'test/file1.txt')
  call VUAssertEquals(line('$'), 5)

  winc l

  call VUAssertEquals(expand('%'), 'vcs_4_file1.txt')
  call VUAssertEquals(line('$'), 4)
endfunction " }}}

" TestLog() {{{
function! TestLog()
  view test/file1.txt
  call PeekRedir()
  VcsLog
  call VUAssertEquals(expand('%'), '[vcs_log]')
  call VUAssertEquals(
    \ getline(1), '|repos| / |unittest| / |trunk| / |test| / file1.txt')
  call VUAssertEquals(getline(4), 'Revision: |4| |view| |annotate|')
  call VUAssertEquals(getline(13), 'Revision: |3| |view| |annotate|')
  call VUAssertEquals(getline(20), 'Revision: |2| |view| |annotate|')

  " dir listing
  call cursor(1, 12)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(getline(1), '|repos| / unittest')
  call VUAssertEquals(getline(3), '|branches/|')
  call VUAssertEquals(getline(4), '|tags/|')
  call VUAssertEquals(getline(5), '|trunk/|')
  bdelete
  VcsLog

  " view
  call cursor(13, 16)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(expand('%'), 'vcs_3_file1.txt')
  bdelete
  VcsLog

  " annotate
  call cursor(20, 23)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(expand('%'), 'vcs_2_file1.txt')
  call VUAssertEquals(
    \ b:vcs_annotations[0], '2 (Sat, 27 Sep 2008 22:28:05) ervandew')
  bdelete
  VcsLog

  " diff previous
  call cursor(6, 8)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(expand('%'), 'vcs_4_file1.txt')
  call VUAssertEquals(line('$'), 4)
  winc l
  call VUAssertEquals(expand('%'), 'vcs_3_file1.txt')
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
  call VUAssertEquals(expand('%'), 'vcs_2_file1.txt')
  call VUAssertEquals(line('$'), 2)
endfunction " }}}

" vim:ft=vim:fdm=marker
