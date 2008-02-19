" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Commands for working with version control systems.
"
" License:
"
" Copyright (c) 2005 - 2008
"
" Licensed under the Apache License, Version 2.0 (the "License");
" you may not use this file except in compliance with the License.
" You may obtain a copy of the License at
"
"      http://www.apache.org/licenses/LICENSE-2.0
"
" Unless required by applicable law or agreed to in writing, software
" distributed under the License is distributed on an "AS IS" BASIS,
" WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
" See the License for the specific language governing permissions and
" limitations under the License.
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
