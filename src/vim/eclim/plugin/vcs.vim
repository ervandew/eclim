" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Commands for working with version control systems.
"
" License:
"
" Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
if !exists(":VcsAnnotate")
  command VcsAnnotate :call eclim#vcs#command#Annotate()
endif
if !exists(":VcsLog")
  command VcsLog
    \ :if s:CheckWindow() |
    \    call eclim#vcs#command#Log(expand('%:p')) |
    \  endif
endif
if !exists(":VcsChangeSet")
  command -nargs=? VcsChangeSet
    \ :if s:CheckWindow() |
    \    call eclim#vcs#command#ChangeSet(expand('%:p'), '<args>') |
    \  endif
endif
if !exists(":VcsDiff")
  command -nargs=? VcsDiff
    \ :if s:CheckWindow() |
    \    call eclim#vcs#command#Diff(expand('%:p'), '<args>') |
    \  endif
endif
if !exists(":VcsCat")
  command -nargs=? VcsCat
    \ :if s:CheckWindow() |
    \    call eclim#vcs#command#ViewFileRevision(expand('%:p'), '<args>', 'split') |
    \  endif
endif
if !exists(":VcsInfo")
  command -nargs=0 VcsInfo
    \ :if s:CheckWindow() |
    \    call eclim#vcs#command#Info() |
    \  endif
endif

if !exists(":Viewvc")
  command -nargs=? -complete=customlist,eclim#project#util#CommandCompleteProjectRelative
    \ Viewvc :call eclim#vcs#viewvc#Viewvc('<args>', 'view=log')
endif
if !exists(":ViewvcChangeSet")
  command -nargs=? -complete=customlist,eclim#vcs#util#CommandCompleteRevision
    \ ViewvcChangeSet :call eclim#vcs#viewvc#ViewvcChangeSet(<q-args>)
endif
if !exists(":ViewvcAnnotate")
  command -nargs=? -complete=customlist,eclim#vcs#util#CommandCompleteRevision
    \ ViewvcAnnotate :call eclim#vcs#viewvc#ViewvcAnnotate(<q-args>)
endif
if !exists(":ViewvcDiff")
  command -nargs=* -complete=customlist,eclim#vcs#util#CommandCompleteRevision
    \ ViewvcDiff :call eclim#vcs#viewvc#ViewvcDiff(<q-args>)
endif

function s:CheckWindow ()
  return !exists('b:vcs_props')
endfunction

" }}}

" vim:ft=vim:fdm=marker
