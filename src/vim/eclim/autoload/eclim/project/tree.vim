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
      call eclim#project#util#UnableToDetermineProject()
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

  if len(dirs) == 0
    "call eclim#util#Echo('ProjectTree: No directories found for requested projects.')
    return
  endif

  " for session reload
  let g:Eclim_project_tree_names = join(names, '|')

  call s:CloseTreeWindow()
  call s:OpenTree(names, dirs)
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

" s:GetSharedTreeBuffer(names) {{{
function! s:GetSharedTreeBuffer(names)
  let instance_names = join(a:names, '_')
  if g:EclimProjectTreeSharedInstance &&
   \ exists('g:eclim_project_tree_instance{instance_names}')
    return g:eclim_project_tree_instance{instance_names}
  endif
  return -1
endfunction " }}}

" s:Mappings() " {{{
function! s:Mappings()
  nnoremap <buffer> <silent> E :call <SID>OpenFile('edit')<cr>
  nnoremap <buffer> <silent> S :call <SID>OpenFile('split')<cr>
  nnoremap <buffer> <silent> T :call <SID>OpenFile('tablast \| tabnew')<cr>
  nnoremap <buffer> <silent> F :call <SID>OpenFileName()<cr>

  " assign to buffer var to get around weird vim issue passing list containing
  " a string w/ a '<' in it on execution of mapping.
  let b:project_tree_help = [
      \ '<cr> - open/close dir, open file',
      \ 'o - toggle dir fold, choose file open action',
      \ 'E - open with :edit',
      \ 'S - open in a new split window',
      \ 'T - open in a new tab',
      \ 'R - refresh directory',
      \ 'i - view file info',
      \ 's - open shell at directory',
      \ 'p - move cursor to parent dir',
      \ 'P - move cursor to last child of dir',
      \ 'C - set root to dir under the cursor',
      \ 'B - set root up one dir',
      \ '~ - set root to home dir',
      \ 'K - set root to top most dir',
      \ 'F - open/create a file by name',
      \ 'D - create a new directory',
      \ 'A - toggle hide/view hidden files',
      \ ':CD <dir> - set the root to <dir>',
    \ ]
  nnoremap <buffer> <silent> ?
    \ :call eclim#help#BufferHelp(b:project_tree_help, 'horizontal', 10)<cr>
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

" s:OpenFileName() " {{{
function! s:OpenFileName()
  let path = eclim#tree#GetPath()
  if !isdirectory(path)
    let path = fnamemodify(path, ':h') . '/'
  endif

  let response = input('file: ', path, 'file')
  call eclim#tree#ExecuteAction(response,
    \ "call eclim#project#tree#OpenProjectFile('split', '<cwd>', '<file>')")
endfunction " }}}

" s:OpenTree(names, dirs) " {{{
function! s:OpenTree(names, dirs)
  let expandDir = ''
  if g:EclimProjectTreeExpandPathOnOpen
    let expandDir = substitute(expand('%:p:h'), '\', '/', 'g')
  endif

  call eclim#display#window#VerticalToolWindowOpen(s:GetTreeTitle(), 9)

  let shared = s:GetSharedTreeBuffer(a:names)
  if shared != -1 && bufloaded(shared)
    exec 'buffer ' . shared
    if line('$') > 1 || getline(1) !~ '^\s*$'
      setlocal nowrap nonumber
      setlocal foldmethod=manual foldtext=getline(v:foldstart)
      exec 'let t:project_tree_id = ' .
        \ substitute(bufname(shared), g:EclimProjectTreeTitle . '\(\d\+\)', '\1', '')
      return
    endif
  endif

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

  if !exists('b:project_tree_loaded')
    for action in g:EclimProjectTreeActions
      call eclim#tree#RegisterFileAction(action.pattern, action.name,
        \ "call eclim#project#tree#OpenProjectFile('" .
        \   action.action . "', '<cwd>', '<file>')",
        \ bufnr('%'))
    endfor

    let b:project_tree_loaded = 1
  endif

  setlocal bufhidden=hide

  if expand && expandDir != ''
    call eclim#util#DelayedCommand(
      \ 'call eclim#tree#ExpandPath("' . s:GetTreeTitle() . '", "' . expandDir . '")')
  endif

  normal! zs

  let instance_names = join(a:names, '_')
  let g:eclim_project_tree_instance{instance_names} = bufnr('%')

  call s:Mappings()
  setlocal modifiable
  call append(line('$'), ['', '" use ? to view help'])
  setlocal nomodifiable
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
