" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Plugin for archive related functionality.
"
" License:
"
" Copyright (c) 2005 - 2008
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

" Script Variables {{{
let s:command_list = '-command archive_list -f "<file>"'
let s:command_read = '-command archive_read -f "<file>"'
let s:command_read_class =
  \ '-command java_class_prototype -p "<project>" -c <class>'

let s:urls = {
    \ 'jar:': ['.jar', '.ear', '.war'],
    \ 'tar:': ['.tar'],
    \ 'tgz:': ['.tgz', '.tar.gz'],
    \ 'tbz2:': ['.tbz2', '.tar.bz2'],
    \ 'zip:': ['.zip', '.egg'],
  \ }
" }}}

" List() {{{
" Lists the contents of the archive.
function! eclim#common#archive#List ()
  let b:urls = {}
  let root = expand('%:t') . '/'
  let b:urls[root] = s:FileUrl(expand('%:p'))
  call setline(1, root)
  call eclim#common#archive#ExpandDir()

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
function! eclim#common#archive#ReadFile ()
  let file = substitute(expand('%'), '\', '/', 'g')
  if file =~ '.class$'
    let class = substitute(file, '.*!\(.*\)\.class', '\1', '')
    let class = substitute(class, '/', '.', 'g')

    let project = exists('g:EclimLastProject') ?
      \ g:EclimLastProject : eclim#project#util#GetCurrentProjectName()

    if project == ''
      call eclim#util#EchoError(
        \ 'Could not open archive file: Unable to determine project.')
      return
    endif

    let command = s:command_read_class
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<class>', class, '')
  else
    let command = substitute(s:command_read, '<file>', file, '')
  endif

  let file = eclim#ExecuteEclim(command)

  if string(file) != '0'
    let bufnum = bufnr('%')
    silent exec "keepjumps edit! " . file

    exec 'bdelete ' . bufnum

    " alternate solution, that keeps the archive url as the buffer's filename,
    " but prevents taglist from being able to parse tags.
    "setlocal noreadonly
    "setlocal modifiable
    "silent! exec "read " . file
    "let saved = @"
    "1,1delete
    "let @" = saved

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
function eclim#common#archive#Execute (alt)
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
    noautocmd exec 'split ' . b:urls[getline('.')]
    call eclim#common#archive#ReadFile()
  endif
endfunction " }}}

" ExpandDir() {{{
function eclim#common#archive#ExpandDir ()
  let path = expand('%:p')
  let dir = b:urls[getline('.')]
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
  let temp_urls = {}
  for entry in results
    let info = split(entry, '|')
    let name = info[0]
    let url = info[1]
    let type = info[2]
    let temp_urls[name] = url
    if type == 'folder' || s:IsArchive(name)
      call add(dirs, name . '/')
    else
      call add(files, name)
    endif
  endfor

  let content = eclim#tree#WriteContents('^', dirs, files)
  " hacky, but works
  for key in sort(keys(temp_urls))
    let index = 0
    for line in content
      if line =~ '\<' . key . '\>'
        let b:urls[line] = temp_urls[key]
        call remove(content, index)
        continue
      endif
      let index += 1
    endfor
  endfor
endfunction " }}}

" s:FileUrl(file) {{{
function! s:FileUrl (file)
  let url = a:file
  for key in keys(s:urls)
    for ext in s:urls[key]
      if url =~ ext . '$'
        let url = key . url
        break
      endif
    endfor
  endfor
  return url
endfunction " }}}

" s:IsArchive(file) {{{
function! s:IsArchive (file)
  let url = a:file
  for key in keys(s:urls)
    for ext in s:urls[key]
      if url =~ ext . '$'
        return 1
      endif
    endfor
  endfor
  return 0
endfunction " }}}

" s:Mappings() {{{
function s:Mappings ()
  nmap <buffer> <silent> <cr> :call eclim#common#archive#Execute(0)<cr>
  nmap <buffer> <silent> o    :call eclim#common#archive#Execute(1)<cr>

  nmap <buffer> <silent> j    j:call eclim#tree#Cursor(line('.'))<cr>
  nmap <buffer> <silent> k    k:call eclim#tree#Cursor(line('.'))<cr>
  nmap <buffer> <silent> p    :call eclim#tree#MoveToParent()<cr>
  nmap <buffer> <silent> P    :call eclim#tree#MoveToLastChild()<cr>
endfunction " }}}

" vim:ft=vim:fdm=marker
