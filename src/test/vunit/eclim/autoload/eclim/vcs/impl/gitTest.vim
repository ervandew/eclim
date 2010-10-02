" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for impl/git.vim
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
  let s:test_dir = g:TestEclimWorkspace . 'eclim_unit_test/vcs/git/unittest/test'
  exec 'cd ' . s:test_dir
endfunction " }}}

" TestInfo() {{{
function! TestInfo()
  view file1.txt
  call PushRedir('@"')
  VcsInfo
  call PopRedir()
  let info = split(@", '\n')
  call VUAssertEquals(info[0], 'commit 101e4be405fdf4f4c38e5b0e3726e937559037f3')
  call VUAssertEquals(info[1], 'Author: ervandew <ervandew@gmail.com>')
  call VUAssertEquals(info[2], 'Date:   Sat Sep 27 18:05:24 2008 -0700')
  call VUAssertEquals(info[3], '')
  call VUAssertEquals(info[4], '    changed some files and leaving a multi line comment')
  call VUAssertEquals(info[5], '    ')
  call VUAssertEquals(info[6], '    - file 1')
  call VUAssertEquals(info[7], '    - file 2')

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
    \ 'df552e0239d91c2a814023735ad300c0f2e2c889 (2008-09-27 13:49:08 -0700) ervandew')

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

  call VUAssertEquals(
    \ expand('%'), 'vcs_101e4be_file1.txt')
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
  call VUAssertTrue(
    \ getline(3) =~
    \ '+ 101e4be ervandew (.* ago) changed some files and leaving a multi line comment')
  call VUAssertTrue(
    \ getline(4) =~
    \ '+ 08c4100 ervandew (.* ago) added 2nd revision content to file1.txt')
  call VUAssertTrue(
    \ getline(5) =~
    \ '+ df552e0 ervandew (.* ago) adding some test files')

  " toggle
  call cursor(4, 1)
  exec "normal \<cr>"
  call VUAssertEquals(line('$'), 9)
  call VUAssertTrue(
    \ getline(4) =~
    \ '- 08c4100 ervandew (.* ago) 2008-09-27 15:01:49 -0700')
  call VUAssertEquals(
    \ getline(5),
    \ '  |view| |annotate| |diff working copy| |diff previous|')
  call VUAssertEquals(getline(6), '  added 2nd revision content to file1.txt')
  call VUAssertEquals(getline(7), '')
  call VUAssertEquals(getline(8), '  + files')

  exec "normal \<cr>"
  call VUAssertEquals(line('$'), 5)
  call VUAssertTrue(
    \ getline(4) =~
    \ '+ 08c4100 ervandew (.* ago) added 2nd revision content to file1.txt')

  exec "normal \<cr>"

  " view
  call cursor(5, 4)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(expand('%'), 'vcs_08c4100_file1.txt')
  bdelete
  VcsLog

  " annotate
  call cursor(5, 1)
  exec "normal \<cr>"
  call VUAssertEquals(getline(6), '  |view| |annotate| |diff working copy|')
  call cursor(6, 11)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(expand('%'), 'vcs_df552e0_file1.txt')
  call VUAssertEquals(
    \ b:vcs_annotations[0],
    \ 'df552e0239d91c2a814023735ad300c0f2e2c889 (2008-09-27 13:49:08 -0700) ervandew')
  bdelete
  VcsLog

  " diff previous
  call cursor(3, 1)
  exec "normal \<cr>"
  call cursor(4, 42)
  exec "normal \<cr>"
  call PeekRedir()
  call VUAssertEquals(expand('%'), 'vcs_101e4be_file1.txt')
  call VUAssertEquals(line('$'), 4)
  winc l
  call VUAssertEquals(expand('%'), 'vcs_08c4100_file1.txt')
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
  call VUAssertEquals(expand('%'), 'vcs_df552e0_file1.txt')
  call VUAssertEquals(line('$'), 2)
endfunction " }}}

" TestLogFiles() {{{
function! TestLogFiles()
  view file2.txt
  call PeekRedir()
  VcsLog
  call VUAssertEquals(expand('%'), '[vcs_log]')
  call VUAssertEquals(getline(1), 'test/file2.txt')
  call VUAssertEquals(line('$'), 5)
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
  call VUAssertEquals(expand('%'), 'vcs_ee5a562_file2.txt')
  call VUAssertEquals(line('$'), 4)
  winc l
  call VUAssertEquals(expand('%'), 'vcs_101e4be_file2.txt')
  call VUAssertEquals(line('$'), 3)
  bdelete
  bdelete
  winc j

  " new file
  call cursor(9, 6)
  exec "normal \<cr>"
  call VUAssertEquals(expand('%'), 'vcs_ee5a562_file3.txt')
  call VUAssertEquals(line('$'), 4)
  bdelete
  winc j

  " moved file
  call cursor(10, 6)
  exec "normal \<cr>"
  call VUAssertEquals(expand('%'), 'vcs_ee5a562_file5.txt')
  call VUAssertEquals(line('$'), 1)
  winc l
  call VUAssertEquals(expand('%'), 'vcs_35a1f6a_file4.txt')
  call VUAssertEquals(line('$'), 1)
  bdelete
  bdelete
endfunction " }}}

" vim:ft=vim:fdm=marker
