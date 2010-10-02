" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for impl/hg.vim
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
  let s:test_dir = g:TestEclimWorkspace . 'eclim_unit_test/vcs/mercurial/unittest/test'
  exec 'cd ' . s:test_dir
endfunction " }}}

" TestInfo() {{{
function! TestInfo()
  view file1.txt
  call PushRedir('@"')
  VcsInfo
  call PopRedir()
  let info = split(@", '\n')
  call VUAssertEquals(info[0], 'changeset:   2:5f0911d194b1')
  call VUAssertEquals(info[1], 'user:        ervandew')
  call VUAssertEquals(info[2], 'date:        Sat Sep 27 22:31:45 2008 -0700')
  call VUAssertEquals(info[3], 'summary:     test a multi line comment')
endfunction " }}}

" TestAnnotate() {{{
function! TestAnnotate()
  view file1.txt
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
    \ '6a95632ba43d (Sat Sep 27 22:26:55 2008 -0700) ervandew')

  call PushRedir('@"')
  VcsAnnotate
  call PopRedir()
  let existing = eclim#display#signs#GetExisting()
  call PeekRedir()
  call VUAssertEquals(len(existing), 0)
endfunction " }}}

" TestDiff() {{{
function! TestDiff()
  view file1.txt
  call PeekRedir()
  VcsDiff
  let name = substitute(expand('%'), '\', '/', 'g')
  call VUAssertEquals(name, 'file1.txt')
  call VUAssertEquals(line('$'), 5)

  winc l

  call VUAssertEquals(expand('%'), 'vcs_5f0911d194b1_file1.txt')
  call VUAssertEquals(line('$'), 4)
endfunction " }}}

" TestLog() {{{
function! TestLog()
  view file1.txt
  call PeekRedir()
  VcsLog
  call VUAssertEquals(expand('%'), '[vcs_log]')
  call VUAssertEquals(getline(1), 'test/file1.txt')
  call VUAssertEquals(line('$'), 5)
  call VUAssertEquals(getline(3), '+ 5f0911d194b1 ervandew (2008-09-27) test a multi line comment')
  call VUAssertEquals(getline(4), '+ 9247ff7b10e3 ervandew (2008-09-27) second revision of files')
  call VUAssertEquals(getline(5), '+ 6a95632ba43d ervandew (2008-09-27) adding 2 files')

  " toggle
  call cursor(4, 1)
  exec "normal \<cr>"
  call VUAssertEquals(line('$'), 9)
  call VUAssertEquals(getline(4), '- 9247ff7b10e3 ervandew (2008-09-27) 2008-09-27 22:30 -0700')
  call VUAssertEquals(getline(5),'  |view| |annotate| |diff working copy| |diff previous|')
  call VUAssertEquals(getline(6), '  second revision of files')
  call VUAssertEquals(getline(7), '')
  call VUAssertEquals(getline(8), '  + files')

  exec "normal \<cr>"
  call VUAssertEquals(line('$'), 5)
  call VUAssertEquals(getline(4), '+ 9247ff7b10e3 ervandew (2008-09-27) second revision of files')

  exec "normal \<cr>"

  " view
  call cursor(5, 4)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(expand('%'), 'vcs_9247ff7b10e3_file1.txt')
  bdelete
  VcsLog

  " annotate
  call cursor(5, 1)
  exec "normal \<cr>"
  call VUAssertEquals(getline(6), '  |view| |annotate| |diff working copy|')
  call cursor(6, 11)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(expand('%'), 'vcs_6a95632ba43d_file1.txt')
  call VUAssertEquals(
    \ b:vcs_annotations[0],
    \ '6a95632ba43d (Sat Sep 27 22:26:55 2008 -0700) ervandew')
  bdelete
  VcsLog

  " diff previous
  call cursor(3, 1)
  exec "normal \<cr>"
  call cursor(4, 42)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(expand('%'), 'vcs_5f0911d194b1_file1.txt')
  call VUAssertEquals(line('$'), 4)
  winc l
  call VUAssertEquals(expand('%'), 'vcs_9247ff7b10e3_file1.txt')
  call VUAssertEquals(line('$'), 3)
  bdelete
  bdelete
  VcsLog

  " diff working copy
  call cursor(5, 1)
  exec "normal \<cr>"
  call cursor(6, 27)
  exec "normal \<cr>"
  call PeekRedir()
  let name = substitute(expand('%'), '\', '/', 'g')
  call VUAssertEquals(name, 'file1.txt')
  call VUAssertEquals(line('$'), 5)
  winc l
  call VUAssertEquals(expand('%'), 'vcs_6a95632ba43d_file1.txt')
  call VUAssertEquals(line('$'), 2)
endfunction " }}}

" TestLogFiles() {{{
function! TestLogFiles()
  view file2.txt
  call PeekRedir()
  VcsLog
  call VUAssertEquals(expand('%'), '[vcs_log]')
  call VUAssertEquals(getline(1), 'test/file2.txt')
  call VUAssertEquals(line('$'), 6)
  call cursor(3, 1)
  exec "normal \<cr>"
  call VUAssertEquals(getline(7), '  + files')
  call cursor(7, 1)
  exec "normal \<cr>"

  call VUAssertEquals(getline( 7), '  - files')
  call VUAssertEquals(getline( 8), '    |M| test/file2.txt')
  call VUAssertEquals(getline( 9), '    |A| test/file3.txt')
  call VUAssertEquals(getline(10), '    |R| test/file4.txt -> test/file5.txt')

  " modified file
  call cursor(8, 6)
  exec "normal \<cr>"
  call VUAssertEquals(expand('%'), 'vcs_571c289b2787_file2.txt')
  call VUAssertEquals(line('$'), 5)
  winc l
  call VUAssertEquals(expand('%'), 'vcs_5f0911d194b1_file2.txt')
  call VUAssertEquals(line('$'), 4)
  bdelete
  bdelete
  winc j

  " new file
  call cursor(9, 6)
  exec "normal \<cr>"
  call VUAssertEquals(expand('%'), 'vcs_571c289b2787_file3.txt')
  call VUAssertEquals(line('$'), 5)
  bdelete
  winc j

  " moved file
  call cursor(10, 6)
  exec "normal \<cr>"
  call VUAssertEquals(expand('%'), 'vcs_571c289b2787_file5.txt')
  call VUAssertEquals(line('$'), 2)
  winc l
  call VUAssertEquals(expand('%'), 'vcs_96e609aeceb3_file4.txt')
  call VUAssertEquals(line('$'), 1)
  bdelete
  bdelete
endfunction " }}}

" vim:ft=vim:fdm=marker
