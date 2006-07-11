" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"
" License:
"
" Copyright (c) 2005 - 2006
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

" Global Variables {{{
  let g:EclimProjectTreeTitle = 'ProjectTree_'

  if !exists('g:EclimProjectTreeAutoOpen')
    let g:EclimProjectTreeAutoOpen = 0
  endif

  if g:EclimProjectTreeAutoOpen && !exists('g:EclimProjectTreeAutoOpenProjects')
    let g:EclimProjectTreeAutoOpenProjects = ['CURRENT']
  endif
" }}}

" Auto Commands {{{
  if g:EclimProjectTreeAutoOpen
    autocmd VimEnter *
      \ if eclim#project#GetCurrentProjectRoot() != '' |
      \   call eclim#project#tree#ProjectTree(copy(g:EclimProjectTreeAutoOpenProjects)) |
      \   exec g:EclimProjectTreeContentWincmd |
      \ endif
    autocmd BufWinEnter *
      \ if tabpagenr() > 1 &&
      \     !exists('t:project_tree_auto_opened') &&
      \     eclim#project#GetCurrentProjectRoot() != '' |
      \   call eclim#project#tree#ProjectTree(copy(g:EclimProjectTreeAutoOpenProjects)) |
      \   let t:project_tree_auto_opened = 1 |
      \ endif
  endif
" }}}

" Command Declarations {{{
if !exists(":ProjectCreate")
  command -nargs=+ -complete=customlist,eclim#project#CommandCompleteProjectCreate
    \ ProjectCreate :call eclim#project#ProjectCreate('<args>')
endif
if !exists(":ProjectDelete")
  command -nargs=1 -complete=customlist,eclim#project#CommandCompleteProject
    \ ProjectDelete :call eclim#project#ProjectDelete('<args>')
endif
if !exists(":ProjectRefresh")
  command -nargs=* -complete=customlist,eclim#project#CommandCompleteProject
    \ ProjectRefresh :call eclim#project#ProjectRefresh('<args>')
endif
if !exists(":ProjectList")
  command ProjectList :call eclim#project#ProjectList()
endif
if !exists(":ProjectSettings")
  command -nargs=? -complete=customlist,eclim#project#CommandCompleteProject
    \ ProjectSettings :call eclim#project#ProjectSettings('<args>')
endif
if !exists(":ProjectOpen")
  command -nargs=1 -complete=customlist,eclim#project#CommandCompleteProject
    \ ProjectOpen :call eclim#project#ProjectOpen('<args>')
endif
if !exists(":ProjectClose")
  command -nargs=1 -complete=customlist,eclim#project#CommandCompleteProject
    \ ProjectClose :call eclim#project#ProjectClose('<args>')
endif

if !exists(":ProjectTree")
  command -nargs=* -complete=customlist,eclim#project#CommandCompleteProject
    \ ProjectTree :call eclim#project#tree#ProjectTree(<f-args>)
endif
if !exists(":ProjectsTree")
  command -nargs=0 -complete=customlist,eclim#project#CommandCompleteProject
    \ ProjectsTree
    \ :call eclim#project#tree#ProjectTree(eclim#project#GetProjectNames())
endif
" }}}

" vim:ft=vim:fdm=marker
