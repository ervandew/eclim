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
    if exists('g:Tlist_Use_Horiz_Window') && g:Tlist_Use_Horiz_Window
      let g:EclimProjectTreeWincmd = 'botright'
    elseif exists('g:Tlist_Use_Right_Window') && g:Tlist_Use_Right_Window
      let g:EclimProjectTreeWincmd = 'botright vertical'
    else
      let g:EclimProjectTreeWincmd = 'topleft vertical'
    endif
  endif

  " command used to navigate to a content window before executing a command.
  if !exists('g:EclimProjectTreeContentWincmd')
    if exists('g:Tlist_Use_Horiz_Window') && g:Tlist_Use_Horiz_Window
      let g:EclimProjectTreeContentWincmd =
        \ 'call eclim#project#tree#HorizontalContentWindow()'
    elseif exists('g:Tlist_Use_Right_Window') && g:Tlist_Use_Right_Window
      let g:EclimProjectTreeContentWincmd = 'winc h'
    else
      let g:EclimProjectTreeContentWincmd = 'winc l'
    endif
  endif

  if !exists('g:EclimProjectTreeTaglistRelation')
    let g:EclimProjectTreeTaglistRelation = 'below'
  endif

  if !exists('g:EclimProjectTreeWidth')
    if exists('g:Tlist_WinWidth')
      let g:EclimProjectTreeWidth = g:Tlist_WinWidth
    else
      let g:EclimProjectTreeWidth = 30
    endif
  endif

  if !exists('g:EclimProjectTreeHeight')
    if exists('g:Tlist_WinHeight')
      let g:EclimProjectTreeHeight = g:Tlist_WinHeight
    else
      let g:EclimProjectTreeHeight = 10
    endif
  endif

  if !exists('g:EclimProjectTreeActions')
    let g:EclimProjectTreeActions = [
        \ {'pattern': '.*', 'name': 'Split', 'action': 'split'},
        \ {'pattern': '.*', 'name': 'Tab', 'action': 'tabnew'},
        \ {'pattern': '.*', 'name': 'Edit', 'action': 'edit'},
      \ ]
  endif
" }}}

" Script Variables {{{
  let s:project_tree_loaded = 0
  let s:project_tree_ids = 0
" }}}

" ProjectTree(...) {{{
" Open a tree view of the current or specified projects.
function! eclim#project#tree#ProjectTree (...)
  " no project dirs supplied, use current project
  if len(a:000) == 0
    let name = eclim#project#GetCurrentProjectName()
    if name == ''
      call eclim#util#Echo('Unable to determine project.')
      return
    endif
    let names = [name]

  " list of project names supplied
  elseif type(a:000[0]) == 3
    let names = a:000[0]
    if len(names) == 1 && (names[0] == '0' || names[0] == '')
      return
    endif

  " list or project names
  else
    let names = a:000
  endif

  let dirs = []
  let index = 0
  for name in names
    if name == 'CURRENT'
      let name = eclim#project#GetCurrentProjectName()
      let names[index] = name
    endif

    let dir = eclim#project#GetProjectRoot(name)
    if dir != ''
      call add(dirs, dir)
    else
      call remove(names, name)
    endif
    let index += 1
  endfor

  if len(dirs) == 0
    "call eclim#util#Echo('ProjectTree: No directories found for requested projects.')
    return
  endif

  let dir_list = string(dirs)

  call s:CloseTreeWindow()

  if bufwinnr(s:GetTreeTitle()) == -1
    call s:OpenTreeWindow()
  endif

  call s:OpenTree(names, dirs)
  normal zs

  augroup project_tree
    autocmd!
    autocmd BufEnter * call eclim#project#tree#CloseIfLastWindow()
  augroup END
  if exists('g:EclimProjectTreeTaglistRelation')
    augroup project_tree
      autocmd BufWinEnter __Tag_List__ call eclim#project#tree#ReopenTree()
    augroup END
  endif
endfunction " }}}

" CloseIfLastWindow() {{{
function eclim#project#tree#CloseIfLastWindow ()
  if (winnr('$') == 1 && bufwinnr(s:GetTreeTitle()) != -1) ||
      \  (winnr('$') == 2 &&
      \   bufwinnr(g:TagList_title) != -1 &&
      \   bufwinnr(s:GetTreeTitle()) != -1)
    if tabpagenr('$') > 1
      tabclose
    else
      quitall
    endif
  endif
endfunction " }}}

" ReopenTree() {{{
function eclim#project#tree#ReopenTree ()
  let projectwin = bufwinnr(s:GetTreeTitle())
  if projectwin != -1
    exec projectwin . 'winc w'
    close
    call s:OpenTreeWindow()

    if g:EclimProjectTreeWincmd =~ 'vert'
      exec 'vertical resize ' . g:EclimProjectTreeWidth
    else
      exec 'resize ' . g:EclimProjectTreeHeight
    endif
  endif
endfunction " }}}

" CloseTreeWindow() " {{{
function! s:CloseTreeWindow ()
  let winnr = bufwinnr(s:GetTreeTitle())
  if winnr != -1
    exec winnr . 'winc w'
    close
  endif
endfunction " }}}

" OpenTreeWindow() " {{{
function! s:OpenTreeWindow ()
  let taglist_window = bufwinnr(g:TagList_title)
  " taglist relative
  if taglist_window != -1 && exists('g:EclimProjectTreeTaglistRelation')
    let wincmd = taglist_window . 'winc w | ' . g:EclimProjectTreeTaglistRelation . ' '

    if g:EclimProjectTreeWincmd !~ 'vert'
      let wincmd .= g:EclimProjectTreeHeight
    endif

  " absolute location
  else
    if g:EclimProjectTreeWincmd =~ 'vert'
      let wincmd = g:EclimProjectTreeWincmd . ' ' . g:EclimProjectTreeWidth
    else
      let wincmd = g:EclimProjectTreeWincmd . ' ' . g:EclimProjectTreeHeight
    endif
  endif

  silent call eclim#util#ExecWithoutAutocmds(wincmd . ' split ' . s:GetTreeTitle())
  if g:EclimProjectTreeWincmd =~ 'vert'
    set winfixwidth
  else
    set winfixheight
  endif

  setlocal nonumber
endfunction " }}}

" OpenTree(names, dirs) " {{{
function! s:OpenTree (names, dirs)
  if !s:project_tree_loaded
    " remove any settings related to usage of tree as an external filesystem
    " explorer.
    if exists('g:TreeSettingsFunction')
      unlet g:TreeSettingsFunction
    endif
  endif

  call eclim#tree#Tree(s:GetTreeTitle(), a:dirs, a:names, len(a:dirs) == 1, [])

  if !s:project_tree_loaded
    for action in g:EclimProjectTreeActions
      call eclim#tree#RegisterFileAction(action.pattern, action.name,
        \ "call eclim#project#tree#OpenProjectFile('" . action.action . "', '<cwd>', '<file>')")
    endfor

    let s:project_tree_loaded = 1
  endif

  setlocal bufhidden=hide
endfunction " }}}

" OpenProjectFile(cmd, cwd, file) {{{
" Execute the supplied command in one of the main content windows.
function! eclim#project#tree#OpenProjectFile (cmd, cwd, file)
  let cwd = substitute(getcwd(), '\', '/', 'g')
  exec 'cd ' . a:cwd
  exec g:EclimProjectTreeContentWincmd
  exec a:cmd . ' ' . cwd . '/' . a:file
endfunction " }}}

" HorizontalContentWindow() {{{
" Command for g:EclimProjectTreeContentWincmd used when relative to a
" horizontal taglist window.
function! eclim#project#tree#HorizontalContentWindow ()
  winc k
  if exists('g:TagList_title') && bufname(bufnr('%')) == g:TagList_title
    winc k
  endif
endfunction " }}}

" GetTreeTitle() {{{
function! s:GetTreeTitle ()
  if !exists('t:project_tree_id')
    let t:project_tree_id = s:project_tree_ids + 1
    let s:project_tree_ids += 1
  endif
  return g:EclimProjectTreeTitle . t:project_tree_id
endfunction " }}}

" vim:ft=vim:fdm=marker
