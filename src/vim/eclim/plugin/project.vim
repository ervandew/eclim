" Author:  Eric Van Dewoestine
"
" Description: {{{
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

" Global Variables {{{
if !exists("g:EclimProjectRefreshFiles")
  let g:EclimProjectRefreshFiles = 1
endif

if !exists("g:EclimProjectKeepLocalHistory")
  let g:EclimProjectKeepLocalHistory = 1
endif

if !exists("g:EclimProjectProblemsUpdateOnSave")
  let g:EclimProjectProblemsUpdateOnSave = 1
endif

let g:EclimProjectTreeTitle = 'ProjectTree_'

if !exists('g:EclimProjectTreeAutoOpen')
  let g:EclimProjectTreeAutoOpen = 0
endif

if !exists('g:EclimProjectTreeExpandPathOnOpen')
  let g:EclimProjectTreeExpandPathOnOpen = 0
endif

if !exists('g:EclimProjectTreeSharedInstance')
  let g:EclimProjectTreeSharedInstance = 0
endif

if g:EclimProjectTreeAutoOpen && !exists('g:EclimProjectTreeAutoOpenProjects')
  let g:EclimProjectTreeAutoOpenProjects = ['CURRENT']
endif
" }}}

" Auto Commands {{{

" w/ external vim refresh is optional, w/ embedded gvim it is mandatory
" disabling at all though is discouraged.
if g:EclimProjectRefreshFiles || has('netbeans_intg')
  augroup eclim_refresh_files
    autocmd!
    autocmd BufWritePre * call eclim#project#util#RefreshFileBootstrap()
  augroup END
endif

if g:EclimProjectKeepLocalHistory
  augroup eclim_history_add
    autocmd!
    autocmd BufWritePre * call eclim#common#history#AddHistory()
  augroup END
endif

if g:EclimProjectTreeAutoOpen
  augroup project_tree_autoopen
    autocmd!
    autocmd VimEnter *
      \ if eclim#project#util#GetCurrentProjectRoot() != '' |
      \   call eclim#project#tree#ProjectTree(copy(g:EclimProjectTreeAutoOpenProjects)) |
      \   exec g:EclimProjectTreeContentWincmd |
      \ endif
  augroup END

  autocmd BufWinEnter *
    \ if tabpagenr() > 1 &&
    \     !exists('t:project_tree_auto_opened') &&
    \     !exists('g:SessionLoad') &&
    \     eclim#project#util#GetCurrentProjectRoot() != '' |
    \   call eclim#project#tree#ProjectTree(copy(g:EclimProjectTreeAutoOpenProjects)) |
    \   exec g:EclimProjectTreeContentWincmd |
    \   let t:project_tree_auto_opened = 1 |
    \ endif
endif

autocmd SessionLoadPost * call eclim#project#tree#Restore()
" }}}

" Command Declarations {{{
if !exists(":ProjectCreate")
  command -nargs=+
    \ -complete=customlist,eclim#project#util#CommandCompleteProjectCreate
    \ ProjectCreate :call eclim#project#util#ProjectCreate('<args>')
  command -nargs=1 -complete=dir
    \ ProjectImport :call eclim#project#util#ProjectImport('<args>')
  command -nargs=1
    \ -complete=customlist,eclim#project#util#CommandCompleteProject
    \ ProjectDelete :call eclim#project#util#ProjectDelete('<args>')
  command -nargs=+
    \ -complete=customlist,eclim#project#util#CommandCompleteProject
    \ ProjectRename :call eclim#project#util#ProjectRename('<args>')
  command -nargs=+
    \ -complete=customlist,eclim#project#util#CommandCompleteProjectMove
    \ ProjectMove :call eclim#project#util#ProjectMove('<args>')
  command -nargs=*
    \ -complete=customlist,eclim#project#util#CommandCompleteProject
    \ ProjectRefresh :call eclim#project#util#ProjectRefresh('<args>')
  command ProjectRefreshAll :call eclim#project#util#ProjectRefreshAll()
  command ProjectCacheClear :call eclim#project#util#ClearProjectsCache()
  command -nargs=? -complete=customlist,eclim#eclipse#CommandCompleteWorkspaces
    \ ProjectList :call eclim#project#util#ProjectList('<args>')
  command -nargs=?
    \ -complete=customlist,eclim#project#util#CommandCompleteProject
    \ ProjectSettings :call eclim#project#util#ProjectSettings('<args>')
  command -nargs=?
    \ -complete=customlist,eclim#project#util#CommandCompleteProject
    \ ProjectInfo :call eclim#project#util#ProjectInfo('<args>')
  command -nargs=?
    \ -complete=customlist,eclim#project#util#CommandCompleteProject
    \ ProjectOpen :call eclim#project#util#ProjectOpen('<args>')
  command -nargs=?
    \ -complete=customlist,eclim#project#util#CommandCompleteProject
    \ ProjectClose :call eclim#project#util#ProjectClose('<args>')
  command -nargs=?
    \ -complete=customlist,eclim#project#util#CommandCompleteProject
    \ ProjectNatures :call eclim#project#util#ProjectNatures('<args>')
  command -nargs=+
    \ -complete=customlist,eclim#project#util#CommandCompleteProjectNatureAdd
    \ ProjectNatureAdd
    \ :call eclim#project#util#ProjectNatureModify('add', '<args>')
  command -nargs=+
    \ -complete=customlist,eclim#project#util#CommandCompleteProjectNatureRemove
    \ ProjectNatureRemove
    \ :call eclim#project#util#ProjectNatureModify('remove', '<args>')
endif

if !exists(":ProjectProblems")
  command -nargs=?
    \ -complete=customlist,eclim#project#util#CommandCompleteProject
    \ ProjectProblems :call eclim#project#problems#Problems('<args>', 1)
endif

if !exists(":ProjectTree")
  command -nargs=*
    \ -complete=customlist,eclim#project#util#CommandCompleteProject
    \ ProjectTree :call eclim#project#tree#ProjectTree(<f-args>)
  command -nargs=0 ProjectsTree
    \ :call eclim#project#tree#ProjectTree(eclim#project#util#GetProjectNames())
  command -nargs=1
    \ -complete=customlist,eclim#project#util#CommandCompleteProject
    \ ProjectTab :call eclim#project#util#ProjectTab('<args>')
endif

if !exists(":ProjectCD")
  command ProjectCD :call eclim#project#util#ProjectCD(0)
  command ProjectLCD :call eclim#project#util#ProjectCD(1)
endif

if !exists(":ProjectGrep")
  command -nargs=+
    \ -complete=customlist,eclim#project#util#CommandCompleteProjectRelative
    \ ProjectGrep :call eclim#project#util#ProjectGrep('vimgrep', <q-args>)
  command -nargs=+
    \ -complete=customlist,eclim#project#util#CommandCompleteProjectRelative
    \ ProjectGrepAdd :call eclim#project#util#ProjectGrep('vimgrepadd', <q-args>)
  command -nargs=+
    \ -complete=customlist,eclim#project#util#CommandCompleteProjectRelative
    \ ProjectLGrep :call eclim#project#util#ProjectGrep('lvimgrep', <q-args>)
  command -nargs=+
    \ -complete=customlist,eclim#project#util#CommandCompleteProjectRelative
    \ ProjectLGrepAdd :call eclim#project#util#ProjectGrep('lvimgrepadd', <q-args>)
endif

if !exists(":Todo")
  command -nargs=0 Todo :call eclim#project#util#Todo()
endif
if !exists(":ProjectTodo")
  command -nargs=0 ProjectTodo :call eclim#project#util#ProjectTodo()
endif

if !exists(":TrackerTicket")
  command -nargs=1 TrackerTicket :call eclim#project#tracker#Ticket('<args>')
endif
" }}}

" Menu Items {{{
"if has('gui')
"  amenu <silent> &Plugin.&eclim.Projects.List :ProjectList<cr>
"endif
" }}}

" vim:ft=vim:fdm=marker
