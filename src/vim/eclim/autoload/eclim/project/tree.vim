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
  if !exists('g:EclimProjectTreeActions')
    let g:EclimProjectTreeActions = [
        \ {'pattern': '.*', 'name': 'Split', 'action': 'split'},
        \ {'pattern': '.*', 'name': 'Tab', 'action': 'tablast | tabnew'},
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
function! eclim#project#tree#ProjectTree(...)
  " no project dirs supplied, use current project
  if len(a:000) == 0
    let name = eclim#project#util#GetCurrentProjectName()
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
  let names_copy = copy(names)
  for name in names
    if name == 'CURRENT'
      let name = eclim#project#util#GetCurrentProjectName()
      let names_copy[index] = name
    endif

    let dir = eclim#project#util#GetProjectRoot(name)
    if dir != ''
      call add(dirs, dir)
    else
      call remove(names_copy, name)
    endif
    let index += 1
  endfor
  let names = names_copy

  " for session reload
  let g:Eclim_project_tree_names = join(names, '|')

  if len(dirs) == 0
    "call eclim#util#Echo('ProjectTree: No directories found for requested projects.')
    return
  endif

  let dir_list = string(dirs)

  call s:CloseTreeWindow()
  call s:OpenTree(names, dirs)
  normal! zs

  call s:Mappings()
endfunction " }}}

" Restore() " {{{
function! eclim#project#tree#Restore()
  if exists('t:project_tree_restoring')
    return
  endif
  let t:project_tree_restoring = 1

  " prevent auto open from firing after session is loaded.
  augroup project_tree_autoopen
    autocmd!
  augroup END

  let title = s:GetTreeTitle()
  let winnum = bufwinnr(title)
  if winnum != -1
    if exists('g:Eclim_project_tree_names')
      let projects = split(g:Eclim_project_tree_names, '|')
      call map(projects, 'escape(v:val, " ")')
      let names = join(projects, ' ')
      call eclim#util#DelayedCommand(
        \ 'let bufnum = bufnr("%") | ' .
        \ 'exec "ProjectTree ' . names . '" | ' .
        \ 'exec bufwinnr(bufnum) . "winc w" | ' .
        \ 'unlet t:project_tree_restoring')
    else
      exec 'bd ' . bufnr(title)
    endif
  endif
endfunction " }}}

" s:CloseTreeWindow() " {{{
function! s:CloseTreeWindow()
  let winnr = bufwinnr(s:GetTreeTitle())
  if winnr != -1
    exec winnr . 'winc w'
    close
  endif
endfunction " }}}

" s:GetTreeTitle() {{{
function! s:GetTreeTitle()
  if !exists('t:project_tree_id')
    let t:project_tree_id = s:project_tree_ids + 1
    let s:project_tree_ids += 1
  endif
  return g:EclimProjectTreeTitle . t:project_tree_id
endfunction " }}}

" s:Mappings() " {{{
function! s:Mappings()
  nnoremap <buffer> E :call <SID>OpenFile('edit')<cr>
  nnoremap <buffer> S :call <SID>OpenFile('split')<cr>
  nnoremap <buffer> T :call <SID>OpenFile('tablast \| tabnew')<cr>
endfunction " }}}

" s:OpenFile(action) " {{{
function! s:OpenFile(action)
  let path = eclim#tree#GetPath()
  if path !~ '/$'
    if !filereadable(path)
      echo "File is not readable or has been deleted."
      return
    endif

    call eclim#tree#ExecuteAction(path,
      \ "call eclim#project#tree#OpenProjectFile('" . a:action . "', '<cwd>', '<file>')")
  endif
endfunction " }}}

" s:OpenTree(names, dirs) " {{{
function! s:OpenTree(names, dirs)
  let expandDir = ''
  if g:EclimProjectTreeExpandPathOnOpen
    let expandDir = substitute(expand('%:p:h'), '\', '/', 'g')
  endif

  call eclim#display#window#VerticalToolWindowOpen(s:GetTreeTitle(), 9)
  " command used to navigate to a content window before executing a command.
  if !exists('g:EclimProjectTreeContentWincmd')
    if g:VerticalToolWindowSide == 'right'
      let g:EclimProjectTreeContentWincmd = 'winc h'
    else
      let g:EclimProjectTreeContentWincmd = 'winc l'
    endif
  endif

  if !s:project_tree_loaded
    " remove any settings related to usage of tree as an external filesystem
    " explorer.
    if exists('g:TreeSettingsFunction')
      unlet g:TreeSettingsFunction
    endif
  endif

  let expand = len(a:dirs) == 1
  call eclim#tree#Tree(s:GetTreeTitle(), a:dirs, a:names, expand, [])

  if !s:project_tree_loaded
    for action in g:EclimProjectTreeActions
      call eclim#tree#RegisterFileAction(action.pattern, action.name,
        \ "call eclim#project#tree#OpenProjectFile('" . action.action . "', '<cwd>', '<file>')")
    endfor

    let s:project_tree_loaded = 1
  endif

  setlocal bufhidden=hide

  if expand && expandDir != ''
    call eclim#util#DelayedCommand(
      \ 'call eclim#tree#ExpandPath("' . s:GetTreeTitle() . '", "' . expandDir . '")')
  endif
endfunction " }}}

" OpenProjectFile(cmd, cwd, file) {{{
" Execute the supplied command in one of the main content windows.
function! eclim#project#tree#OpenProjectFile(cmd, cwd, file)
  let cmd = a:cmd
  let cwd = substitute(getcwd(), '\', '/', 'g')
  let cwd = escape(cwd, ' &')

  "exec 'cd ' . escape(a:cwd, ' ')
  exec g:EclimProjectTreeContentWincmd

  " if the buffer is a no name and action is split, use edit instead.
  if bufname('%') == '' && cmd == 'split'
    let cmd = 'edit'
  endif

  exec cmd . ' ' . cwd . '/' . a:file
endfunction " }}}

" HorizontalContentWindow() {{{
" Command for g:EclimProjectTreeContentWincmd used when relative to a
" horizontal taglist window.
function! eclim#project#tree#HorizontalContentWindow()
  winc k
  if exists('g:TagList_title') && bufname(bufnr('%')) == g:TagList_title
    winc k
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
