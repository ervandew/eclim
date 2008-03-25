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
if !exists(":TracLog")
  command -nargs=? -complete=customlist,eclim#project#util#CommandCompleteProjectRelative
    \ TracLog :call eclim#project#trac#Log('<args>')
endif
if !exists(":TracChangeSet")
  command -nargs=? -complete=customlist,eclim#vcs#util#CommandCompleteRevision
    \ TracChangeSet :call eclim#project#trac#ChangeSet(<q-args>)
endif
if !exists(":TracAnnotate")
  command -nargs=? -complete=customlist,eclim#vcs#util#CommandCompleteRevision
    \ TracAnnotate :call eclim#project#trac#Annotate(<q-args>)
endif
if !exists(":TracDiff")
  command -nargs=* -complete=customlist,eclim#vcs#util#CommandCompleteRevision
    \ TracDiff :call eclim#project#trac#Diff(<q-args>)
endif
" }}}

" vim:ft=vim:fdm=marker
