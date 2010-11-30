" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Commands for working with version control systems.
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

" Command Declarations {{{
if !exists(":VcsLog")
  command -nargs=* VcsLog
    \ if s:CheckWindow() |
    \   call eclim#vcs#command#Log(<q-args>) |
    \ endif
  command -nargs=* VcsLogGrepMessage call eclim#vcs#command#LogGrep(<q-args>, 'message')
  command -nargs=* VcsLogGrepFiles call eclim#vcs#command#LogGrep(<q-args>, 'files')
  command -nargs=? VcsDiff
    \ if s:CheckWindow() |
    \   call eclim#vcs#command#Diff('<args>') |
    \ endif
  command -nargs=? VcsCat
    \ if s:CheckWindow() |
    \   call eclim#vcs#command#ViewFileRevision(expand('%:p'), '<args>', 'split') |
    \ endif
  command VcsAnnotate :call eclim#vcs#command#Annotate()
  command -nargs=0 VcsInfo
    \ if s:CheckWindow() |
    \   call eclim#vcs#command#Info() |
    \ endif
endif

if !exists(":VcsWebLog")
  command -nargs=? VcsWebLog call eclim#vcs#web#VcsWebLog('<args>')
  command -nargs=? -complete=customlist,eclim#vcs#util#CommandCompleteRevision
    \ VcsWebChangeSet call eclim#vcs#web#VcsWebChangeSet(<q-args>)
  command -nargs=? -complete=customlist,eclim#vcs#util#CommandCompleteRevision
    \ VcsWebAnnotate call eclim#vcs#web#VcsWebAnnotate(<q-args>)
  command -nargs=* -complete=customlist,eclim#vcs#util#CommandCompleteRevision
    \ VcsWebDiff call eclim#vcs#web#VcsWebDiff(<q-args>)
endif

function! s:CheckWindow()
  return &buftype == ''
endfunction

" }}}

" vim:ft=vim:fdm=marker
