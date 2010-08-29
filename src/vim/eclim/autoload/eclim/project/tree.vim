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
  if !exists('g:EclimProjectTreeActions')
    let g:EclimProjectTreeActions = [
        \ {'pattern': '.*', 'name': 'Split', 'action': 'split'},
        \ {'pattern': '.*', 'name': 'Tab', 'action': 'tablast | tabnew'},
        \ {'pattern': '.*', 'name': 'Edit', 'action': 'edit'},
      \ ]
  endif
  if !exists('g:EclimProjectTreePathEcho')
    let g:EclimProjectTreePathEcho = 1
  endif
" }}}

" Script Variables {{{
  let s:project_tree_ids = 0
  let s:shared_instances_by_buffer = {}
  let s:shared_instances_by_names = {}
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

  call eclim#project#tree#ProjectTreeClose()
  call eclim#project#tree#ProjectTreeOpen(names, dirs)
endfunction " }}}

" ProjectTreeOpen(names, dirs, [title]) " {{{
function! eclim#project#tree#ProjectTreeOpen(names, dirs, ...)
  let expandDir = ''
  if g:EclimProjectTreeExpandPathOnOpen
    let expandDir = substitute(expand('%:p:h'), '\', '/', 'g')
  endif

  " support supplied tree name
  if a:0 > 0 && a:1 != ''
    let t:project_tree_name = a:1
  endif

  " see if we should just use a shared tree
  let shared = s:GetSharedTreeBuffer(a:names)
  if shared != -1 && bufloaded(shared)
    call eclim#display#window#VerticalToolWindowOpen(bufname(shared), 9)
    "exec 'buffer ' . shared
    if line('$') > 1 || getline(1) !~ '^\s*$'
      setlocal nowrap nonumber
      setlocal foldmethod=manual foldtext=getline(v:foldstart)
      if !exists('t:project_tree_name')
        exec 'let t:project_tree_id = ' .
          \ substitute(bufname(shared), g:EclimProjectTreeTitle . '\(\d\+\)', '\1', '')
      endif
      return
    endif
  endif

  " clear the project tree id if we are replacing a shared tree instance
  if g:EclimProjectTreeSharedInstance && exists('t:project_tree_id')
    unlet t:project_tree_id
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

  let expand = len(a:dirs) == 1

  if exists('g:TreeSettingsFunction')
    let s:TreeSettingsFunction = g:TreeSettingsFunction
  endif
  let g:TreeSettingsFunction = 'eclim#project#tree#ProjectTreeSettings'

  try
    call eclim#tree#Tree(s:GetTreeTitle(), a:dirs, a:names, expand, [])
  finally
    if exists('s:TreeSettingsFunction')
      let g:TreeSettingsFunction = s:TreeSettingsFunction
    else
      unlet g:TreeSettingsFunction
    endif
  endtry

  setlocal bufhidden=hide

  if expand && expandDir != ''
    call eclim#util#DelayedCommand(
      \ 'call eclim#tree#ExpandPath("' . s:GetTreeTitle() . '", "' . expandDir . '")')
  endif

  normal! zs

  let instance_names = join(a:names, '_')
  let instance_names = substitute(instance_names, '\W', '_', 'g')

  " remove the old associated tree value if one exists
  silent! unlet s:shared_instances_by_names[s:shared_instances_by_buffer[bufnr('%')]]

  let s:shared_instances_by_buffer[bufnr('%')] = instance_names
  let s:shared_instances_by_names[instance_names] = bufnr('%')

  call s:Mappings()
  setlocal modifiable
  call append(line('$'), ['', '" use ? to view help'])
  call s:InfoLine()
  setlocal nomodifiable
endfunction " }}}

" ProjectTreeClose() " {{{
function! eclim#project#tree#ProjectTreeClose()
  if exists('t:project_tree_name') || exists('t:project_tree_id')
    let winnr = bufwinnr(s:GetTreeTitle())
    if winnr != -1
      exec winnr . 'winc w'
      close
    endif
  endif
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

" s:GetTreeTitle() {{{
function! s:GetTreeTitle()
  " support a custom name from an external plugin
  if exists('t:project_tree_name')
    return t:project_tree_name
  endif

  if !exists('t:project_tree_id')
    let t:project_tree_id = s:project_tree_ids + 1
    let s:project_tree_ids += 1
  endif
  return g:EclimProjectTreeTitle . t:project_tree_id
endfunction " }}}

" s:GetSharedTreeBuffer(names) {{{
function! s:GetSharedTreeBuffer(names)
  let instance_names = join(a:names, '_')
  let instance_names = substitute(instance_names, '\W', '_', 'g')
  if g:EclimProjectTreeSharedInstance &&
   \ has_key(s:shared_instances_by_names, instance_names)
    return s:shared_instances_by_names[instance_names]
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

" s:InfoLine() {{{
function! s:InfoLine()
  setlocal modifiable
  let pos = getpos('.')
  if len(b:roots) == 1
    let lnum = line('$') - 1
    if getline(lnum) =~ '^"'
      exec lnum . ',' . lnum . 'delete _'
    endif

    let info = eclim#vcs#util#GetInfo(b:roots[0])
    if info != ''
      call append(line('$') - 1, '" ' . info)
    endif
  endif
  call setpos('.', pos)
  setlocal nomodifiable
endfunction " }}}

" s:PathEcho() {{{
function! s:PathEcho()
  if mode() != 'n'
    return
  endif

  let path = eclim#tree#GetPath()
  let path = substitute(path, eclim#tree#GetRoot(), '', '')
  if path !~ '^"'
    call eclim#util#WideMessage('echo', path)
  else
    call eclim#util#WideMessage('echo', '')
  endif
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
  let actions = eclim#tree#GetFileActions(response)
  call eclim#tree#ExecuteAction(response, actions[0].action)
endfunction " }}}

" ProjectTreeSettings() {{{
function! eclim#project#tree#ProjectTreeSettings()
  for action in g:EclimProjectTreeActions
    call eclim#tree#RegisterFileAction(action.pattern, action.name,
      \ "call eclim#project#tree#OpenProjectFile('" .
      \   action.action . "', '<cwd>', '<file>')")
  endfor

  call eclim#tree#RegisterDirAction(function('eclim#project#tree#InjectLinkedResources'))

  if exists('s:TreeSettingsFunction')
    let Settings = function(s:TreeSettingsFunction)
    call Settings()
  endif

  augroup eclim_tree
    autocmd User <buffer> call <SID>InfoLine()
    if g:EclimProjectTreePathEcho
      autocmd CursorMoved <buffer> call <SID>PathEcho()
    endif
  augroup END
endfunction " }}}

" OpenProjectFile(cmd, cwd, file) {{{
" Execute the supplied command in one of the main content windows.
function! eclim#project#tree#OpenProjectFile(cmd, cwd, file)
  let cmd = a:cmd
  let cwd = substitute(getcwd(), '\', '/', 'g')
  let cwd = escape(cwd, ' &')

  "exec 'cd ' . escape(a:cwd, ' ')
  exec g:EclimProjectTreeContentWincmd

  let file = cwd . '/' . a:file

  if eclim#util#GoToBufferWindow(file)
    return
  endif

  " if the buffer is a no name and action is split, use edit instead.
  if cmd == 'split' && expand('%') == '' &&
   \ !&modified && line('$') == 1 && getline(1) == ''
    let cmd = 'edit'
  endif

  try
    exec cmd . ' ' file
  catch /E325/
    " ignore attention error since the use should be prompted to handle it.
  endtry
endfunction " }}}

" InjectLinkedResources(dir, contents) {{{
function! eclim#project#tree#InjectLinkedResources(dir, contents)
  let project = eclim#project#util#GetProject(a:dir)
  if len(project) == 0
    return
  endif

  " listing the project root, so inject our project links
  if len(project.links) && substitute(a:dir, '/$', '', '') == project.path
    if !exists('b:links')
      let b:links = {}
    endif
    call extend(b:links, project.links)

    let links = keys(project.links)
    call sort(links)

    let index = 0
    for entry in copy(a:contents)
      if !len(links)
        break
      endif

      while len(links) && links[0] < fnamemodify(entry, ':h:t')
        call insert(a:contents, a:dir . remove(links, 0) . '/', index)
      endwhile
      let index += 1
    endfor

    let index += 1
    for link in links
      call insert(a:contents, a:dir . link . '/', index)
    endfor
  endif
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
