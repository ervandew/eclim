" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for impl/git.vim
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
  let s:test_dir = g:TestEclimWorkspace . 'eclim_unit_test/vcs/git/unittest'
  exec 'cd ' . s:test_dir
endfunction " }}}

" TestInfo() {{{
function! TestInfo()
  view test/file1.txt
  call PushRedir('@"')
  VcsInfo
  call PopRedir()
  let info = split(@", '\n')
  call VUAssertEquals(info[0], 'commit 80376080596559eb6b6072f09f95be921ab6fe68')
  call VUAssertEquals(info[1], 'Author: ervandew <ervandew@gmail.com>')
  call VUAssertEquals(info[2], 'Date:   Sat Sep 27 18:05:24 2008 -0700')
  call VUAssertEquals(info[3], '')
  call VUAssertEquals(info[4], '    changed some files and leaving a multi line comment')
  call VUAssertEquals(info[5], '      - file 1')
  call VUAssertEquals(info[6], '      - file 2')
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
  call VUAssertEquals(existing[0].name, 'ervandew')

  call VUAssertEquals(
    \ b:vcs_annotations[0],
    \ 'df552e0239d91c2a814023735ad300c0f2e2c889 (2008-09-27 13:49:08 -0700) ervandew')

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
  call VUAssertEquals(getline(1), 'Revision: 80376080596559eb6b6072f09f95be921ab6fe68')
  call VUAssertEquals(getline(4), '|M| |test/file1.txt|')
  call VUAssertEquals(getline(5), '|M| |test/file2.txt|')
  call VUAssertEquals(getline(7), 'changed some files and leaving a multi line comment')
  call VUAssertEquals(getline(8), '  - file 1')
  call VUAssertEquals(getline(9), '  - file 2')

  " test log on |test/file2.txt|
  call cursor(5, 6)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(getline(1), 'unittest / test / file2.txt')

  exec "normal \<c-o>"
  call PeekRedir()

  " test diff on |M|
  call cursor(4, 2)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(
    \ expand('%'),
    \ 'vcs_80376080596559eb6b6072f09f95be921ab6fe68_file1.txt')
  call VUAssertEquals(line('$'), 4)
  winc l
  call VUAssertEquals(
    \ expand('%'),
    \ 'vcs_08c4100b146fa19f4950882dc44ab6902d2e5d23_file1.txt')
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

  call VUAssertEquals(
    \ expand('%'), 'vcs_80376080596559eb6b6072f09f95be921ab6fe68_file1.txt')
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
    \ 'Revision: |80376080596559eb6b6072f09f95be921ab6fe68| |view| |annotate|')
  call VUAssertEquals(
    \ getline(13),
    \ 'Revision: |08c4100b146fa19f4950882dc44ab6902d2e5d23| |view| |annotate|')
  call VUAssertEquals(
    \ getline(20),
    \ 'Revision: |df552e0239d91c2a814023735ad300c0f2e2c889| |view| |annotate|')

  " view
  call cursor(13, 55)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(
    \ expand('%'),
    \ 'vcs_08c4100b146fa19f4950882dc44ab6902d2e5d23_file1.txt')
  bdelete
  VcsLog

  " annotate
  call cursor(20, 62)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(
    \ expand('%'),
    \ 'vcs_df552e0239d91c2a814023735ad300c0f2e2c889_file1.txt')
  call VUAssertEquals(
    \ b:vcs_annotations[0],
    \ 'df552e0239d91c2a814023735ad300c0f2e2c889 (2008-09-27 13:49:08 -0700) ervandew')
  bdelete
  VcsLog

  " diff previous
  call cursor(6, 8)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(
    \ expand('%'),
    \ 'vcs_80376080596559eb6b6072f09f95be921ab6fe68_file1.txt')
  call VUAssertEquals(line('$'), 4)
  winc l
  call VUAssertEquals(
    \ expand('%'),
    \ 'vcs_08c4100b146fa19f4950882dc44ab6902d2e5d23_file1.txt')
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
  call VUAssertEquals(
    \ expand('%'),
    \ 'vcs_df552e0239d91c2a814023735ad300c0f2e2c889_file1.txt')
  call VUAssertEquals(line('$'), 2)
endfunction " }}}

" vim:ft=vim:fdm=marker
