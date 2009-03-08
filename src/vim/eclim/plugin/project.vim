" Author:  Eric Van Dewoestine
"
" Description: {{{
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

" Global Variables {{{
if !exists("g:EclimProjectRefreshFiles")
  let g:EclimProjectRefreshFiles = 1
endif

if !exists("g:EclimProjectKeepLocalHistory")
  let g:EclimProjectKeepLocalHistory = 1
endif

let g:EclimProjectTreeTitle = 'ProjectTree_'

if !exists('g:EclimProjectTreeAutoOpen')
  let g:EclimProjectTreeAutoOpen = 0
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
  autocmd VimEnter *
    \ if eclim#project#util#GetCurrentProjectRoot() != '' |
    \   call eclim#project#tree#ProjectTree(copy(g:EclimProjectTreeAutoOpenProjects)) |
    \   exec g:EclimProjectTreeContentWincmd |
    \ endif
  autocmd BufWinEnter *
    \ if tabpagenr() > 1 &&
    \     !exists('t:project_tree_auto_opened') &&
    \     eclim#project#util#GetCurrentProjectRoot() != '' |
    \   call eclim#util#DelayedCommand('call eclim#project#tree#ProjectTree(copy(g:EclimProjectTreeAutoOpenProjects)) | winc w') |
    \   let t:project_tree_auto_opened = 1 |
    \ endif
endif
" }}}

" Command Declarations {{{
if !exists(":ProjectCreate")
  command -nargs=+ -complete=customlist,eclim#project#util#CommandCompleteProjectCreate
    \ ProjectCreate :call eclim#project#util#ProjectCreate('<args>')
endif
if !exists(":ProjectDelete")
  command -nargs=1 -complete=customlist,eclim#project#util#CommandCompleteProject
    \ ProjectDelete :call eclim#project#util#ProjectDelete('<args>')
endif
if !exists(":ProjectRefresh")
  command -nargs=* -complete=customlist,eclim#project#util#CommandCompleteProject
    \ ProjectRefresh :call eclim#project#util#ProjectRefresh('<args>')
endif
if !exists(":ProjectRefreshAll")
  command ProjectRefreshAll :call eclim#project#util#ProjectRefreshAll()
endif
if !exists(":ProjectList")
  command ProjectList :call eclim#project#util#ProjectList()
endif
if !exists(":ProjectSettings")
  command -nargs=? -complete=customlist,eclim#project#util#CommandCompleteProject
    \ ProjectSettings :call eclim#project#util#ProjectSettings('<args>')
endif
if !exists(":ProjectInfo")
  command -nargs=? -complete=customlist,eclim#project#util#CommandCompleteProject
    \ ProjectInfo :call eclim#project#util#ProjectInfo('<args>')
endif
if !exists(":ProjectOpen")
  command -nargs=? -complete=customlist,eclim#project#util#CommandCompleteProject
    \ ProjectOpen :call eclim#project#util#ProjectOpen('<args>')
endif
if !exists(":ProjectClose")
  command -nargs=? -complete=customlist,eclim#project#util#CommandCompleteProject
    \ ProjectClose :call eclim#project#util#ProjectClose('<args>')
endif
if !exists(":ProjectNatures")
  command -nargs=? -complete=customlist,eclim#project#util#CommandCompleteProject
    \ ProjectNatures :call eclim#project#util#ProjectNatures('<args>')
endif
if !exists(":ProjectNatureAdd")
  command -nargs=+ -complete=customlist,eclim#project#util#CommandCompleteProjectNatureAdd
    \ ProjectNatureAdd :call eclim#project#util#ProjectNatureModify('add', '<args>')
endif
if !exists(":ProjectNatureRemove")
  command -nargs=+ -complete=customlist,eclim#project#util#CommandCompleteProjectNatureRemove
    \ ProjectNatureRemove :call eclim#project#util#ProjectNatureModify('remove', '<args>')
endif

if !exists(":ProjectTree")
  command -nargs=* -complete=customlist,eclim#project#util#CommandCompleteProject
    \ ProjectTree :call eclim#project#tree#ProjectTree(<f-args>)
endif
if !exists(":ProjectsTree")
  command -nargs=0 -complete=customlist,eclim#project#util#CommandCompleteProject
    \ ProjectsTree
    \ :call eclim#project#tree#ProjectTree(eclim#project#util#GetProjectNames())
endif

if !exists(":ProjectCD")
  command ProjectCD :call eclim#project#util#ProjectCD(0)
endif
if !exists(":ProjectLCD")
  command ProjectLCD :call eclim#project#util#ProjectCD(1)
endif

if !exists(":ProjectGrep")
  command -nargs=+ -complete=customlist,eclim#project#util#CommandCompleteProjectRelative
    \ ProjectGrep :call eclim#project#util#ProjectGrep('vimgrep', <q-args>)
endif
if !exists(":ProjectGrepAdd")
  command -nargs=+ -complete=customlist,eclim#project#util#CommandCompleteProjectRelative
    \ ProjectGrepAdd :call eclim#project#util#ProjectGrep('vimgrepadd', <q-args>)
endif
if !exists(":ProjectLGrep")
  command -nargs=+ -complete=customlist,eclim#project#util#CommandCompleteProjectRelative
    \ ProjectLGrep :call eclim#project#util#ProjectGrep('lvimgrep', <q-args>)
endif
if !exists(":ProjectGrepAdd")
  command -nargs=+ -complete=customlist,eclim#project#util#CommandCompleteProjectRelative
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
