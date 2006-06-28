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
  " command used (minus the :split) to open the tree window.
  if !exists('g:EclimProjectTreeWincmd')
    let g:EclimProjectTreeWincmd = 'topleft vertical'
  endif
  " command used to navigate to a content window before executing a command.
  if !exists('g:EclimProjectTreeContentWincmd')
    let g:EclimProjectTreeContentWincmd = 'winc l'
  endif
" }}}

" ProjectTree(...) {{{
" Open a tree view of the current or specified projects.
function! eclim#project#tree#ProjectTree (...)
  " no project dirs supplied, use current project
  if len(a:000) == 0
    let dir = eclim#project#GetCurrentProjectRoot()
    if dir == ''
      call eclim#util#Echo('Unable to determine project.')
      return
    endif
    let dirs = [dir]

  " list of dirs supplied (all projects)
  elseif type(a:000[0]) == 3
    let dirs = a:000[0]

  " list or project names
  else
    let dirs = []
    for project in a:000
      call add(dirs, eclim#project#GetProjectRoot(project))
    endfor
  endif

  call s:OpenTreeWindow()
  call s:OpenTree(dirs)

  augroup project_tree
    autocmd!
    autocmd BufEnter * call eclim#project#tree#CloseIfLastWindow()
    autocmd BufWinEnter * echom "TAGLIST OPENED"
  augroup END
  "if exists('g:EclimProjectTreeTaglistRelation')
  "  augroup project_tree
  "  augroup END
  "endif
endfunction " }}}

" ProjectsTree() {{{
" Open a tree view of all the projects.
function! eclim#project#tree#ProjectsTree ()
  let dirs = eclim#project#GetProjectDirs()
  if !(len(dirs) == 1 && dirs[0] == '0')
    call eclim#project#tree#ProjectTree(dirs)
  endif
endfunction " }}}

" CloseIfLastWindow() {{{
function eclim#project#tree#CloseIfLastWindow ()
  if (winnr('$') == 1 && bufwinnr('project_tree') != -1) ||
      \  (winnr('$') == 2 &&
      \   bufwinnr(g:TagList_title) != -1 &&
      \   bufwinnr('project_tree') != -1)
    if tabpagenr('$') > 1
      tabclose
    else
      quitall
    endif
  endif
endfunction " }}}

" ReopenTree() {{{
function eclim#project#tree#ReopenTree ()
  echom "Reopen the tree"
  "let bufnr = bufnr('%')
  "call s:OpenTreeWindow()
  "let winnr = bufwinnr(bufnr)
  "exec 'buffer ' . bufnr

  "let curwinnr = winnr()

  "exec winnr . 'winc w'
  "close

  "exec curwinnr . 'winc w'
endfunction " }}}

" OpenTreeWindow() " {{{
function! s:OpenTreeWindow ()
  let taglist_window = bufwinnr(g:TagList_title)
  " taglist relative
  if taglist_window != -1 && exists('g:EclimProjectTreeTaglistRelation')
    let wincmd = taglist_window . 'winc w | ' . g:EclimProjectTreeTaglistRelation
  " absolute location
  else
    let wincmd = g:EclimProjectTreeWincmd
  endif

  "call eclim#util#ExecWithoutAutocmds(wincmd . ' split project_tree')
  exec wincmd . ' split project_tree'
endfunction " }}}

" OpenTree(dirs) " {{{
function! s:OpenTree (dirs)
  " remove any settings related to usage of tree as an external filesystem
  " explorer.
  if exists('g:TreeSettingsFunction')
    unlet g:TreeSettingsFunction
  endif

  call tree#Tree('project_tree', a:dirs, len(a:dirs) == 1, [])
  call tree#RegisterFileAction('.*', 'Split',
    \ "call eclim#project#tree#OpenProjectFile('split <file>')")
  call tree#RegisterFileAction('.*', 'Edit',
    \ "call eclim#project#tree#OpenProjectFile('edit <file>')")
  call tree#RegisterFileAction('.*', 'Tab', 'tabnew <file>')

  setlocal nowrap
  setlocal nonumber
  set winfixwidth
endfunction " }}}

" OpenProjectFile(cmd) {{{
" Execute the supplied command in one of the main content windows.
function! eclim#project#tree#OpenProjectFile (cmd)
  exec g:EclimProjectTreeContentWincmd
  exec a:cmd
endfunction " }}}

" vim:ft=vim:fdm=marker
