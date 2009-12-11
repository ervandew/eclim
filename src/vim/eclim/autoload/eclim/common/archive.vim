" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Plugin for archive related functionality.
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
  if !exists('g:EclimArchiveActions')
    let g:EclimArchiveActions = [
        \ {'pattern': '.*', 'name': 'Split', 'action': 'split'},
        \ {'pattern': '.*', 'name': 'Tab', 'action': 'tablast | tabnew'},
        \ {'pattern': '.*', 'name': 'Edit', 'action': 'edit'},
      \ ]
  endif
" }}}

" Script Variables {{{
let s:command_list = '-command archive_list -f "<file>"'
let s:command_list_all = '-command archive_list_all -f "<file>"'
let s:command_read = '-command archive_read -f "<file>"'
let s:command_read_class = '-command java_class_prototype -c <class>'

let s:urls = {
    \ 'jar:': ['.jar', '.ear', '.war'],
    \ 'tar:': ['.tar'],
    \ 'tgz:': ['.tgz', '.tar.gz'],
    \ 'tbz2:': ['.tbz2', '.tar.bz2'],
    \ 'zip:': ['.zip', '.egg'],
  \ }

let s:file_regex =
  \ '\(.\{-}\)\s*[0-9]\+\s\+[0-9]\{4}-[0-9]\{2}-[0-9]\{2} [0-9]\{2}:[0-9]\{2}:[0-9]\{2}$'
" }}}

" List() {{{
" Lists the contents of the archive.
function! eclim#common#archive#List()
  if !eclim#PingEclim(0)
    call s:DefaultList()
    return
  endif
  if !exists('b:archive_loaded')
    for action in g:EclimArchiveActions
      call eclim#tree#RegisterFileAction(
        \ action.pattern, action.name, action.action, bufnr('%'))
    endfor

    let b:archive_loaded = 1
  endif

  let b:file_info = {}
  let file = substitute(expand('%:p'), '\', '/', 'g')
  let root = fnamemodify(file, ':t') . '/'
  let b:file_info[root] = {'url': s:FileUrl(file)}

  if exists('g:EclimArchiveLayout') && g:EclimArchiveLayout == 'list'
    set modifiable
    call eclim#common#archive#ListAll()
    let g:EclimArchiveLayout = 'list'
    call append(0, '" use ? to view help')
    set nomodifiable
  else
    set modifiable
    call setline(1, root)
    call eclim#common#archive#ExpandDir()
    let g:EclimArchiveLayout = 'tree'
    set modifiable
    call append(line('$'), ['', '" use ? to view help'])
    set nomodifiable
  endif

  setlocal ft=archive
  setlocal nowrap
  setlocal noswapfile
  setlocal nobuflisted
  setlocal buftype=nofile
  setlocal bufhidden=delete
  setlocal foldtext=getline(v:foldstart)

  call s:Mappings()
  call eclim#tree#Syntax()
endfunction " }}}

" ReadFile() {{{
" Reads the contents of an archived file.
function! eclim#common#archive#ReadFile()
  let file = substitute(expand('%'), '\', '/', 'g')
  if file =~ '.class$'
    let class = substitute(file, '.*![/]\?\(.*\)\.class', '\1', '')
    let class = substitute(class, '/', '.', 'g')

    let archive = substitute(file, '^\w\+:file://\(.\{-}\)!.*', '\1', '')
    let project = eclim#project#util#GetProject(archive)
    let project_name = get(project, 'name', '')

    let command = s:command_read_class
    let command = substitute(command, '<class>', class, '')

    if project_name != ''
      let command .= ' -p "' . project_name . '"'
    else
      let read = substitute(s:command_read, '<file>', file, '')
      let file = eclim#ExecuteEclim(read)
      if string(file) == '0'
        return
      endif
      let command .= ' -f "' . file . '"'
    endif
  else
    let command = substitute(s:command_read, '<file>', file, '')
  endif

  let file = eclim#ExecuteEclim(command)

  if string(file) != '0'
    let bufnum = bufnr('%')
    silent exec "keepjumps edit! " . escape(file, ' ')

    exec 'bdelete ' . bufnum

    " alternate solution, that keeps the archive url as the buffer's filename,
    " but prevents taglist from being able to parse tags.
    "setlocal noreadonly
    "setlocal modifiable
    "silent! exec "read " . file
    "1,1delete _

    silent exec "doautocmd BufReadPre " . file
    silent exec "doautocmd BufReadPost " . file

    setlocal readonly
    setlocal nomodifiable
    setlocal noswapfile
    " causes taglist.vim errors (fold then delete fails)
    "setlocal bufhidden=delete
  endif
endfunction " }}}

" Execute(alt) {{{
function eclim#common#archive#Execute(alt)
  if getline('.') =~ '^"\|^\s*$'
    return
  endif

  let path = eclim#tree#GetPath()

  " execute action on dir
  if path =~ '/$'
    if a:alt || foldclosed(line('.')) != -1
      call eclim#tree#ToggleFoldedDir(function('eclim#common#archive#ExpandDir'))
    else
      call eclim#tree#ToggleCollapsedDir(function('eclim#common#archive#ExpandDir'))
    endif

  " execute action on file
  else
    let url = s:GetFilePath()
    let actions = eclim#tree#GetFileActions(path)
    if a:alt
      call eclim#tree#DisplayActionChooser(
        \ url, actions, 'eclim#common#archive#ExecuteAction')
    else
      call eclim#common#archive#ExecuteAction(url, actions[0].action)
    endif
  endif
endfunction " }}}

" ExecuteAction(file, command) {{{
function eclim#common#archive#ExecuteAction(file, command)
  if a:command == 'edit'
    if !exists('b:archive_edit_window') ||
     \ getwinvar(b:archive_edit_window, 'archive_edit_window') == ''
      let bufnr = bufnr('%')
      new
      let w:archive_edit_window = 1
      call setbufvar(bufnr, 'archive_edit_window', winnr())
    else
      exec b:archive_edit_window . 'winc w'
    endif
  endif

  if exists('b:archive_edit_window') &&
   \ getwinvar(b:archive_edit_window, 'archive_edit_window') == 1
    exec b:archive_edit_window . 'winc w'
  endif

  noautocmd exec a:command . ' ' . escape(a:file, ' ')
  call eclim#common#archive#ReadFile()
endfunction " }}}

" ExpandDir() {{{
function eclim#common#archive#ExpandDir()
  let path = substitute(expand('%:p'), '\', '/', 'g')
  let dir = b:file_info[getline('.')].url
  if dir !~ path . '$' && s:IsArchive(dir)
    let dir = s:FileUrl(dir) . '!/'
  endif
  let command = s:command_list
  let command = substitute(command, '<file>', dir, '')
  let results = split(eclim#ExecuteEclim(command), '\n')
  if len(results) == 1 && results[0] == '0'
    return
  endif

  let dirs = []
  let files = []
  let temp_info = {}
  for entry in results
    let parsed = s:ParseEntry(entry)
    let temp_info[parsed.name] = parsed
    if parsed.type == 'folder' || s:IsArchive(parsed.name)
      call add(dirs, parsed.name . '/')
    else
      call add(files, parsed.name)
    endif
  endfor

  let content = eclim#tree#WriteContents('^', dirs, files)
  " hacky, but works
  for key in sort(keys(temp_info))
    let index = 0
    for line in content
      if line =~ '\s*+\?\s*' . escape(key, '.') . '/\?$'
        let b:file_info[line] = temp_info[key]
        call remove(content, index)
        continue
      endif
      let index += 1
    endfor
  endfor
endfunction " }}}

" ListAll() {{{
" Function for listing all the archive files (for 'list' layout).
function eclim#common#archive#ListAll()
  let path = substitute(expand('%:p'), '\', '/', 'g')
  let command = s:command_list_all
  let command = substitute(command, '<file>', path, '')
  let results = split(eclim#ExecuteEclim(command), '\n')
  if len(results) == 1 && results[0] == '0'
    return
  endif

  exec 'read ' . results[0]
  call delete(results[0])
endfunction " }}}

" s:GetFilePath() {{{
function! s:GetFilePath()
  if g:EclimArchiveLayout == 'list'
    let file = substitute(getline('.'), s:file_regex, '\1', '')
    let archive = substitute(expand('%:p'), '\', '/', 'g')
    let url = s:FileUrl(archive) . '!/' . file
  else
    let url = b:file_info[getline('.')].url
  endif
  return url
endfunction " }}}

" s:ParseEntry(entry) {{{
function! s:ParseEntry(entry)
  let info = split(a:entry, '|')
  let parsed = {}
  let parsed.path = info[0]
  let parsed.name = info[1]
  let parsed.url = info[2]
  let parsed.type = info[3]
  let parsed.size = info[4]
  let parsed.date = len(info) > 5 ? info[5] : ''
  return parsed
endfunction " }}}

" s:FileUrl(file) {{{
function! s:FileUrl(file)
  let url = a:file
  if url =~ '^[a-zA-Z]:'
    let url = '/' . url
  endif
  for key in keys(s:urls)
    for ext in s:urls[key]
      if url =~ escape(ext, '.') . '$'
        let url = key . url
        break
      endif
    endfor
  endfor
  return url
endfunction " }}}

" s:IsArchive(file) {{{
function! s:IsArchive(file)
  let url = a:file
  for key in keys(s:urls)
    for ext in s:urls[key]
      if url =~ escape(ext, '.') . '$'
        return 1
      endif
    endfor
  endfor
  return 0
endfunction " }}}

" s:ChangeLayout(layout) {{{
function! s:ChangeLayout(layout)
  if g:EclimArchiveLayout != a:layout
    let g:EclimArchiveLayout = a:layout
    set modifiable
    edit
  endif
endfunction " }}}

" s:OpenFile(action) " {{{
function! s:OpenFile(action)
  let path = s:GetFilePath()
  call eclim#common#archive#ExecuteAction(path, a:action)
endfunction " }}}

" s:FileInfo() {{{
function! s:FileInfo()
  let info = b:file_info[substitute(getline('.'), '^\(\s*\)-\(.*/$\)', '\1+\2', '')]
  if has_key(info, 'type') && info.type == 'file'
    call eclim#util#Echo(printf('%-15s', info.size) . info.date)
  endif
endfunction " }}}

" s:Mappings() {{{
function s:Mappings()
  nmap <buffer> <silent> <cr> :call eclim#common#archive#Execute(0)<cr>
  nmap <buffer> <silent> E :call <SID>OpenFile('edit')<cr>
  nmap <buffer> <silent> S :call <SID>OpenFile('split')<cr>
  nmap <buffer> <silent> T :call <SID>OpenFile('tablast \| tabnew')<cr>

  if g:EclimArchiveLayout == 'tree'
    let b:tree_mappings_active = 1
    let b:hierarchy_help = [
        \ '<cr> - open/close dir, open file',
        \ 'o - toggle dir fold, choose file open action',
        \ 'E - open with :edit',
        \ 'S - open in a new split window',
        \ 'T - open in a new tab',
        \ 'p - move cursor to parent dir',
        \ 'P - move cursor to last child of dir',
        \ 'i - view file info',
        \ ':AsList - switch to list view',
      \ ]
    nmap <buffer> <silent> o    :call eclim#common#archive#Execute(1)<cr>
    nmap <buffer> <silent> j    j:call eclim#tree#Cursor(line('.'))<cr>
    nmap <buffer> <silent> k    k:call eclim#tree#Cursor(line('.'))<cr>
    nmap <buffer> <silent> p    :call eclim#tree#MoveToParent()<cr>
    nmap <buffer> <silent> P    :call eclim#tree#MoveToLastChild()<cr>
    nmap <buffer> <silent> i    :call <SID>FileInfo()<cr>
    nmap <buffer> <silent> I    :call <SID>FileInfo()<cr>

    silent! delcommand AsTree
    command! -nargs=0 AsList :call <SID>ChangeLayout('list')
  else
    if exists('b:tree_mappings_active')
      unlet b:tree_mappings_active
      unmap <buffer> o
      unmap <buffer> j
      unmap <buffer> k
      unmap <buffer> p
      unmap <buffer> P
      unmap <buffer> i
      unmap <buffer> I
    endif

    let b:hierarchy_help = [
        \ '<cr> - open/close dir, open file',
        \ 'E - open with :edit',
        \ 'S - open in a new split window',
        \ 'T - open in a new tab',
        \ ':AsList - switch to tree view',
      \ ]

    silent! delcommand AsList
    command! -nargs=0 AsTree :call <SID>ChangeLayout('tree')
  endif

  nnoremap <buffer> <silent> ?
    \ :call eclim#help#BufferHelp(b:hierarchy_help, 'horizontal', 10)<cr>

endfunction " }}}

" s:DefaultList() {{{
function s:DefaultList()
  " once the tar and zip plugins are loaded, we must disable the eclim viewer
  " since they will conflict.
  let g:EclimArchiveViewerEnabled = 0
  augroup eclim_archive
    autocmd!
  augroup END

  if exists('g:loaded_tarPlugin') && g:loaded_tarPlugin == '1'
    unlet g:loaded_tarPlugin
    runtime plugin/tarPlugin.vim
  endif
  if exists('g:loaded_zipPlugin') && g:loaded_zipPlugin == '1'
    unlet g:loaded_zipPlugin
    runtime plugin/zipPlugin.vim
  endif
  silent doautocmd tar BufReadCmd
  silent doautocmd zip BufReadCmd
endfunction " }}}

" vim:ft=vim:fdm=marker
