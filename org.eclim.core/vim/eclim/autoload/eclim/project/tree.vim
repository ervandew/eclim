" Author:  Eric Van Dewoestine
"
" Description: {{{
"
" License:
"
" Copyright (C) 2005 - 2012  Eric Van Dewoestine
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
        \ {'pattern': '.*', 'name': 'VSplit', 'action': 'vsplit'},
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

function! eclim#project#tree#ProjectTree(...) " {{{
  " Open a tree view of the current or specified projects.

  " no project dirs supplied, use current project
  if len(a:000) == 0
    let name = eclim#project#util#GetCurrentProjectName()
    let names = [name]
    if name == ''
      if exists('t:cwd')
        let names = [t:cwd]
      else
        call eclim#project#util#UnableToDetermineProject()
        return
      endif
    endif

  " list of project names supplied
  elseif type(a:000[0]) == g:LIST_TYPE
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
    if dir == ''
      let dir = expand(name, ':p')
      if !isdirectory(dir)
        call eclim#util#EchoWarning('Project not found: ' . name)
        call remove(names_copy, index)
        continue
      endif
      let names_copy[index] = fnamemodify(substitute(dir, '/$', '', ''), ':t')
    endif
    call add(dirs, dir)
    let index += 1
  endfor
  let names = names_copy

  if len(dirs) == 0
    return
  endif

  " for session reload
  let g:Eclim_project_tree_names = join(names, '|')

  let display = len(names) == 1 ?
    \ 'Project: ' . names[0] :
    \ 'Projects: ' . join(names, ', ')

  call eclim#project#tree#ProjectTreeClose()
  call eclim#project#tree#ProjectTreeOpen(display, names, dirs)
endfunction " }}}

function! eclim#project#tree#ProjectTreeToggle() " {{{
  let title = s:GetTreeTitle()
  let bufnum = bufnr(title)
  let winnum = bufwinnr(title)
  if bufnum == -1 || winnum == -1
    call eclim#project#tree#ProjectTree()
  else
    exec winnum . 'winc w'
    close
    winc p
  endif
endfunction " }}}

function! eclim#project#tree#ProjectTreeOpen(display, names, dirs) " {{{
  let expandDir = ''
  if g:EclimProjectTreeExpandPathOnOpen
    let expandDir = substitute(expand('%:p:h'), '\', '/', 'g')
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
  exec 'setlocal statusline=' . escape(a:display, ' ')

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

function! eclim#project#tree#ProjectTreeClose() " {{{
  if exists('t:project_tree_name') || exists('t:project_tree_id')
    let winnr = bufwinnr(s:GetTreeTitle())
    if winnr != -1
      exec winnr . 'winc w'
      close
    endif
  endif
endfunction " }}}

function! eclim#project#tree#Restore() " {{{
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

function! s:GetTreeTitle() " {{{
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

function! s:GetSharedTreeBuffer(names) " {{{
  let instance_names = join(a:names, '_')
  let instance_names = substitute(instance_names, '\W', '_', 'g')
  if g:EclimProjectTreeSharedInstance &&
   \ has_key(s:shared_instances_by_names, instance_names)
    return s:shared_instances_by_names[instance_names]
  endif
  return -1
endfunction " }}}

function! s:Mappings() " {{{
  nnoremap <buffer> <silent> E :call <SID>OpenFile('edit')<cr>
  nnoremap <buffer> <silent> S :call <SID>OpenFile('split')<cr>
  nnoremap <buffer> <silent> \| :call <SID>OpenFile('vsplit')<cr>
  nnoremap <buffer> <silent> T :call <SID>OpenFile('tablast \| tabnew')<cr>
  nnoremap <buffer> <silent> F :call <SID>OpenFileName()<cr>
  nnoremap <buffer> <silent> Y :call <SID>YankFileName()<cr>

  " assign to buffer var to get around weird vim issue passing list containing
  " a string w/ a '<' in it on execution of mapping.
  let b:project_tree_help = [
      \ '<cr> - open/close dir, open file',
      \ 'o - toggle dir fold, choose file open action',
      \ 'E - open with :edit',
      \ 'S - open in a new split window',
      \ '| (pipe) - open in a new vertical split window',
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
      \ 'Y - yank current file/dir path to the clipboard',
      \ 'A - toggle hide/view hidden files',
      \ ':CD <dir> - set the root to <dir>',
    \ ]
  nnoremap <buffer> <silent> ?
    \ :call eclim#help#BufferHelp(b:project_tree_help, 'horizontal', 10)<cr>
endfunction " }}}

function! s:InfoLine() " {{{
  setlocal modifiable
  let pos = getpos('.')
  if len(b:roots) == 1
    let lnum = line('$') - 1
    if getline(lnum) =~ '^"'
      exec lnum . ',' . lnum . 'delete _'
    endif

    let info = ''
    try
      let info = function('vcs#util#GetInfo')(b:roots[0])
    catch /E\(117\|700\)/
      " fall back to fugitive
      try
        " make sure fugitive has the git dir for the current project
        if !exists('b:git_dir') || (b:git_dir !~ '^\M' . b:roots[0])
          let cwd = ''
          if getcwd() . '/' != b:roots[0]
            let cwd = getcwd()
            exec 'lcd ' . escape(b:roots[0], ' ')
          endif

          if exists('b:git_dir')
            unlet b:git_dir
          endif
          silent! doautocmd fugitive BufReadPost %

          if cwd != ''
            exec 'lcd ' . escape(cwd, ' ')
          endif
        endif

        let info = function('fugitive#statusline')()
        if info != ''
          let branch = substitute(info, '^\[\Git(\(.*\))\]$', '\1', 'g')
          if branch != info
            let info = 'git:' . branch
          endif
        endif
      catch /E\(117\|700\)/
        " noop if the neither function was found
      endtry
    endtry

    " &modifiable check for silly side effect of fugitive autocmd
    if info != '' && &modifiable
      call append(line('$') - 1, '" ' . info)
    endif
  endif
  call setpos('.', pos)
  setlocal nomodifiable
endfunction " }}}

function! s:PathEcho() " {{{
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

function! s:OpenFile(action) " {{{
  let path = eclim#tree#GetPath()
  if path !~ '/$'
    if !filereadable(path)
      echo "File is not readable or has been deleted."
      return
    endif

    call eclim#tree#ExecuteAction(path,
      \ "call eclim#project#tree#OpenProjectFile('" . a:action . "', '<file>')")
  endif
endfunction " }}}

function! s:OpenFileName() " {{{
  let path = eclim#tree#GetPath()
  if !isdirectory(path)
    let path = fnamemodify(path, ':h') . '/'
  endif

  let response = input('file: ', path, 'file')
  if response != ''
    let actions = eclim#tree#GetFileActions(response)
    call eclim#tree#ExecuteAction(response, actions[0].action)
  endif
endfunction " }}}

function! s:YankFileName() " {{{
  let path = eclim#tree#GetPath()
  let [@*, @+, @"] = [path, path, path]
  call eclim#util#Echo('Copied path to clipboard: ' . path)
endfunction " }}}

function! eclim#project#tree#ProjectTreeSettings() " {{{
  for action in g:EclimProjectTreeActions
    call eclim#tree#RegisterFileAction(action.pattern, action.name,
      \ "call eclim#project#tree#OpenProjectFile('" . action.action . "', '<file>')")
  endfor

  call eclim#tree#RegisterDirAction(function('eclim#project#tree#InjectLinkedResources'))

  if exists('s:TreeSettingsFunction')
    let l:Settings = function(s:TreeSettingsFunction)
    call l:Settings()
  endif

  augroup eclim_tree
    autocmd User <buffer> call <SID>InfoLine()
    if g:EclimProjectTreePathEcho
      autocmd CursorMoved <buffer> call <SID>PathEcho()
    endif
  augroup END
endfunction " }}}

" OpenProjectFile(cmd, file) {{{
" Execute the supplied command in one of the main content windows.
function! eclim#project#tree#OpenProjectFile(cmd, file)
  if eclim#util#GoToBufferWindow(a:file)
    return
  endif

  let file = a:file
  let cmd = a:cmd
  let cwd = getcwd()

  exec g:EclimProjectTreeContentWincmd

  " if the buffer is a no name and action is split, use edit instead.
  if cmd =~ 'split' && expand('%') == '' &&
   \ !&modified && line('$') == 1 && getline(1) == ''
    let cmd = 'edit'
  endif

  " current file doesn't share same cwd as the project tree
  let lcwd = getcwd()
  if lcwd != cwd && !filereadable(file)
    let file = escape(substitute(cwd, '\', '/', 'g'), ' &') . '/' . file
  endif

  try
    exec cmd . ' ' file
  catch /E325/
    " ignore attention error since the user should be prompted to handle it.
  finally
    if lcwd != cwd
      exec 'lcd ' . escape(cwd, ' ')
    endif
  endtry
endfunction " }}}

function! eclim#project#tree#InjectLinkedResources(dir, contents) " {{{
  let project = eclim#project#util#GetProject(a:dir)
  if len(project) == 0
    return
  endif

  " listing the project root, so inject our project links
  if len(get(project, 'links', {})) &&
   \ substitute(a:dir, '/$', '', '') == project.path
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

    for link in links
      call add(a:contents, a:dir . link . '/')
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
