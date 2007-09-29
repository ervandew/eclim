" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Functions for working with version control systems.
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

" ChangeSet(revision, dir) {{{
function! eclim#vcs#log#ChangeSet (revision, dir)
  let url = eclim#vcs#util#GetSvnUrl(a:dir, '')
  let log = split(system('svn log -vr ' . a:revision . ' ' . url), '\n')

  let lines = []
  let entry = {}
  call s:ParseSvnInfo(entry, log[1])
  call add(lines, 'Revision: ' . entry.revision)
  call add(lines, 'Modified: ' . entry.date . ' by ' . entry.author)
  let files = map(log[2:-2], 'substitute(v:val, "\\s*M\\s*\\(.*\\)", "  |M| |\\1|", "")')
  let files = map(files, 'substitute(v:val, "\\s*A\\s*\\(.*\\)", "   A  |\\1|", "")')
  let files = map(files, 'substitute(v:val, "\\s*D\\s*\\(.*\\)", "   D  |\\1|", "")')
  let files = map(files, 'substitute(v:val, "\\(.*\\)\\( (.*)\\)\\(.*\\)", "\\1\\3\\2", "")')
  call extend(lines, files)

  call s:TempWindow(lines)
  let b:vcs_view = 'changeset'
endfunction " }}}

" Log(type, file) {{{
function! eclim#vcs#log#Log (type, file)
  let dir = fnamemodify(a:file, ':h')
  let file = fnamemodify(a:file, ':t')
  let local_dir = dir

  if a:type == 'cvs'
    if exists('b:vcs_local_dir')
      let local_dir = b:vcs_local_dir
      let dir = strpart(
        \ b:vcs_local_dir, 0, stridx(b:vcs_local_dir, dir) + len(dir))
    endif
    let lines = [s:Breadcrumb(dir, ''), '']

    let cwd = getcwd()
    exec 'lcd ' . dir
    try
      let result = system('cvs log -l "' . file . '"')
      let log = s:ParseCvsLog(split(result, '\n'))
    finally
      exec 'lcd ' . cwd
    endtry
  elseif a:type == 'svn'
    if exists('b:vcs_local_dir')
      let local_dir = b:vcs_local_dir
      let url = eclim#vcs#util#GetSvnReposUrl(b:vcs_local_dir) . dir . '/' . file

      let path = split(dir, '/')
      let head = map(path, '"|" . v:val . "|"')
      let breadcrumb = join(head, ' / ') . ' / ' . file
      let lines = [breadcrumb, '']
    else
      let url = eclim#vcs#util#GetSvnUrl(dir, file)
      let lines = [s:Breadcrumb(dir, file), '']
    endif
    let result = system('svn log "' . url . '"')
    let log = s:ParseSvnLog(split(result, '\n'))
  else
    call eclim#util#EchoError('Current file is not under cvs or svn version control.')
    return
  endif

  let index = 0
  for entry in log
    let index += 1
    call add(lines, '------------------------------------------')
    if a:type == 'cvs'
      call add(lines, 'Revision: ' . entry.revision . ' |view| |annotate|')
    else
      call add(lines, 'Revision: |' . entry.revision . '| |view| |annotate|')
    endif
    call add(lines, 'Modified: ' . entry.date . ' by ' . entry.author)
    if index < len(log)
      call add(lines, 'Diff: |previous ' . log[index].revision . '| |working copy|')
    else
      call add(lines, 'Diff: |working copy|')
    endif
    call add(lines, '')
    let lines += entry.comment
    if lines[-1] !~ '^\s*$' && index != len(log)
      call add(lines, '')
    endif
  endfor

  call s:TempWindow(lines)
  let b:vcs_view = 'log'
  let b:vcs_local_dir = local_dir
  let b:vcs_type = a:type

  call s:LogSyntax()

  nnoremap <silent> <buffer> <cr> :call <SID>FollowLink()<cr>
endfunction " }}}

" ListDir(dir) {{{
function! eclim#vcs#log#ListDir (type, dir)
  let dir = a:dir
  let local_dir = dir

  if a:type == 'cvs'
    if exists('b:vcs_local_dir')
      let local_dir = b:vcs_local_dir
      let dir = strpart(
        \ b:vcs_local_dir, 0, stridx(b:vcs_local_dir, dir) + len(dir))
    endif
    let lines = [s:Breadcrumb(dir, ''), '']

    let cwd = getcwd()
    exec 'lcd ' . dir
    try
      let listing = readfile('CVS/Entries')
      let dirs = sort(filter(listing[:], 'v:val =~ "^D/"'))
      call map(dirs, 'substitute(v:val, "^D/\\(.\\{-}/\\).*", "\\1", "")')
      let files = sort(filter(listing[:], 'v:val =~ "^/"'))
      call map(files, 'substitute(v:val, "^/\\(.\\{-}\\)/.*", "\\1", "")')
    finally
      exec 'lcd ' . cwd
    endtry
  elseif a:type == 'svn'
    if exists('b:vcs_local_dir')
      let local_dir = b:vcs_local_dir
      let url = eclim#vcs#util#GetSvnReposUrl(b:vcs_local_dir) . dir

      let path = split(dir, '/')
      let head = map(path[:-2], '"|" . v:val . "|"')
      let breadcrumb = join(head, ' / ') . ' / ' . path[-1]
      let lines = [breadcrumb, '']
    else
      let url = eclim#vcs#util#GetSvnUrl(dir, '')
      let lines = [s:Breadcrumb(dir, ''), '']
    endif
    let listing = split(system('svn list ' . url), '\n')
    let dirs = sort(filter(listing[:], 'v:val =~ "/$"'))
    let files = sort(filter(listing[:], 'v:val =~ "[^/]$"'))
  else
    call eclim#util#EchoError('Current file is not under cvs or svn version control.')
    return
  endif

  call map(dirs, '"|" . v:val . "|"')
  call map(files, '"|" . v:val . "|"')
  call extend(lines, dirs)
  call extend(lines, files)

  call s:TempWindow(lines)
  let b:vcs_view = 'dir'
  let b:vcs_type = a:type
  let b:vcs_local_dir = local_dir

  call s:LogSyntax()

  nnoremap <silent> <buffer> <cr> :call <SID>FollowLink()<cr>
endfunction " }}}

" ViewFileRevision(type, file, revision, split) {{{
function! eclim#vcs#log#ViewFileRevision (type, file, revision, split)
  let file = a:file
  let split = a:split
  if split == ''
    let split = 'split'
  endif

  if a:type == 'cvs'
    if exists('b:vcs_url')
      let file = b:vcs_url
    elseif exists('b:vcs_local_dir')
      let file = b:vcs_local_dir . '/' . fnamemodify(file, ':t')
    endif
    let url = file

    let cwd = getcwd()
    exec 'lcd ' . fnamemodify(file, ':h')
    try
      let file = fnamemodify(file, ':t')
      let result = system('cvs annotate -r ' . a:revision . ' "' . file . '"')
      let lines = split(result, '\n')
      call filter(lines, 'v:val =~ "^[0-9]"')
      call map(lines, "substitute(v:val, '^.\\{-}: ', '', '')")
    finally
      exec 'lcd ' . cwd
    endtry
  elseif a:type == 'svn'
    if exists('b:vcs_url')
      let url = b:vcs_url
    elseif exists('b:vcs_local_dir')
      let url = eclim#vcs#util#GetSvnReposUrl(b:vcs_local_dir) . file
    else
      let url = eclim#vcs#util#GetSvnUrl(fnamemodify(file, ':h'), fnamemodify(file, ':t'))
    endif
  endif

  if exists('b:filename')
    call eclim#util#GoToBufferWindow(b:filename)
  endif
  let svn_file = a:type . '_' . a:revision . '_' . fnamemodify(file, ':t')
  call eclim#util#GoToBufferWindowOrOpen(svn_file, split)

  setlocal noreadonly
  setlocal modifiable
  let saved = @"
  silent 1,$delete
  let @" = saved

  " load in content
  if a:type == 'cvs'
    call append(1, lines)
  elseif a:type == 'svn'
    exec 'silent read !svn cat -r ' . a:revision . ' ' . url
  endif

  silent 1,1delete
  call cursor(1, 1)

  setlocal nomodified
  setlocal readonly
  setlocal nomodifiable
  setlocal noswapfile

  let b:vcs_url = url
  let b:vcs_revision = a:revision
endfunction " }}}

" s:LogSyntax() {{{
function! s:LogSyntax ()
  set ft=vcs_log
  hi link VcsDivider Constant
  hi link VcsHeader Identifier
  hi link VcsLink Label
  " TODO: highlight path and make linkable
  hi link VcsPathLink Label
  syntax match VcsDivider /^-\+$/
  syntax match VcsLink /|.\{-}|/
  syntax match VcsHeader /^\(Revision\|Modified\|Diff\|Changed paths\):/
endfunction " }}}

" s:ParseCvsLog() {{{
" Parse the cvs log.
function! s:ParseCvsLog (lines)
  let log = []
  let section = 'head'
  for line in a:lines
    if line =~ '^=\+$'
      continue
    elseif line =~ '^-\+$'
      let section = 'info'
      let entry = {'comment': []}
      call add(log, entry)
      continue
    elseif section == 'head'
      continue
    elseif section == 'info'
      if line =~ '^revision'
        let entry['revision'] = substitute(line, 'revision\s\(.*\)', '\1', '')
      elseif line =~ '^date:'
        let entry['author'] = substitute(line, '.*author:\s*\(.\{-}\);.*', '\1', '')
        let entry['date'] = substitute(line, '.*date:\s*\(.\{-}\);.*', '\1', '')
        let entry['date'] = substitute(entry.date, '/', '-', 'g')
        let section = 'comment'
      endif
    elseif section == 'comment'
      call add(entry.comment, line)
    endif
  endfor
  return log
endfunction " }}}

" s:ParseSvnLog() {{{
" Parse the svn log.
function! s:ParseSvnLog (lines)
  let log = []
  let section = 'head'
  let index = 0
  for line in a:lines
    let index += 1
    if line =~ '^-\+$' && index == len(a:lines)
      " get rid of empty line at the end of last entry's comment
      if exists('l:entry') && entry.comment[-1] =~ '^\s*$'
        let entry.comment = entry.comment[:-2]
      endif
      continue
    elseif line =~ '^-\+$'
      " get rid of empty line at the end of entry's comment
      if exists('l:entry') && entry.comment[-1] =~ '^\s*$'
        let entry.comment = entry.comment[:-2]
      endif
      let section = 'info'
      let entry = {'comment': []}
      call add(log, entry)
      continue
    elseif section == 'head'
      continue
    elseif section == 'info'
      call s:ParseSvnInfo(entry, line)
      let section = 'comment'
    elseif section == 'comment'
      " ignore leading blank line of comment section
      if len(entry.comment) == 0 && line =~ '^\s*$'
        continue
      endif
      call add(entry.comment, line)
    endif
  endfor
  return log
endfunction " }}}

" s:ParseSvnInfo(entry, line) {{{
" Parse the svn info line of the log.
function! s:ParseSvnInfo (entry, line)
  let a:entry['revision'] = substitute(a:line, '^r\(\w\+\).*', '\1', '')
  let a:entry['author'] = substitute(a:line, '.\{-}|\s*\(\w\+\)\s*|.*', '\1', '')
  let a:entry['date'] = substitute(a:line, '.\{-}|.\{-}|\s*\(.\{-}\)\s\+[+-].\{-}|.*', '\1', '')
endfunction " }}}

" s:Breadcrumb(dir, file) {{{
function! s:Breadcrumb (dir, file)
  let path = split(eclim#vcs#util#GetPath(a:dir, a:file), '/')
  let head = map(path[:-2], '"|" . v:val . "|"')
  return join(head, ' / ') . ' / ' . path[-1]
endfunction " }}}

" s:FollowLink () {{{
function! s:FollowLink ()
  let line = getline('.')
  let link = substitute(
    \ getline('.'), '.*|\(.\{-}\%' . col('.') . 'c.\{-}\)|.*', '\1', '')
  if link == line
    return
  endif

  echom link
  " link to svn folder
  if line('.') == 1
    let path = substitute(
      \ getline('.'), '\(.*|.\{-}\%' . col('.') . 'c.\{-}\)|.*', '\1', '')
    let path = substitute(path, '\(| / |\||\)', '/', 'g')

    call eclim#vcs#log#ListDir(b:vcs_type, path)

  " link to view a change set
  elseif link =~ '^[0-9.]\+$'
    call eclim#vcs#log#ChangeSet(link, b:vcs_local_dir)

  " link to view / annotate a file
  elseif link == 'view' || link == 'annotate'
    let file = substitute(getline(1), '\(| / |\||\)', '/', 'g')
    let file = substitute(file, ' / ', '', 'g')
    let revision = substitute(line, '.\{-}|\?\([0-9.]\+\)|\?.*', '\1', '')
    let vcs_type = b:vcs_type
    call eclim#vcs#log#ViewFileRevision(b:vcs_type, file, revision, 'split')

    if link == 'annotate'
      if vcs_type == 'cvs'
        let annotations = eclim#vcs#annotate#GetCvsAnnotations(b:vcs_url, revision)
      elseif vcs_type == 'svn'
        let annotations = eclim#vcs#annotate#GetSvnAnnotations(b:vcs_url, revision)
      endif
      call eclim#vcs#annotate#ApplyAnnotations(annotations)
    endif

  " link to diff one version against previous
  elseif link =~ '^previous [0-9.]\+$'
    let file = substitute(getline(1), '\(| / |\||\)', '/', 'g')
    let file = substitute(file, ' / ', '', 'g')
    let r1 = substitute(
      \ getline(line('.') - 2), 'Revision: |\?\([0-9.]\+\)|\?.*', '\1', '')
    let r2 = substitute(link, 'previous \(.*\)', '\1', '')

    let vcs_type = b:vcs_type
    call eclim#vcs#log#ViewFileRevision(vcs_type, file, r1, 'split')
    diffthis
    call eclim#vcs#log#ViewFileRevision(vcs_type, file, r2, 'vertical split')
    diffthis

  " link to diff against working copy
  elseif link == 'working copy'
    let file = substitute(getline(1), '\(| / |\||\)', '/', 'g')
    let file = substitute(file, ' / ', '', 'g')
    let revision = substitute(
      \ getline(line('.') - 2), 'Revision: |\?\([0-9.]\+\)|\?.*', '\1', '')

    let filename = b:filename
    call eclim#vcs#log#ViewFileRevision(b:vcs_type, file, revision, 'vertical split')
    diffthis
    call eclim#util#GoToBufferWindow(filename)
    diffthis

  elseif exists('b:vcs_view') && b:vcs_view == 'dir'
    let path = substitute(getline(1), '\(| / |\||\)', '/', 'g')
    let path = substitute(path, ' / ', '', 'g')
    let path .= '/' . link

    if path =~ '/$'
      call eclim#vcs#log#ListDir(b:vcs_type, path)
    else
      call eclim#vcs#log#Log(b:vcs_type, path)
    endif
  endif
endfunction " }}}

" s:TempWindow (lines) {{{
function! s:TempWindow (lines)
  let filename = expand('%:p')
  if expand('%') == '[vcs_log]' && exists('b:filename')
    let filename = b:filename
  endif

  call eclim#util#TempWindow('[vcs_log]', a:lines)

  let b:filename = filename
  augroup temp_window
    autocmd! BufUnload <buffer>
    call eclim#util#GoToBufferWindowRegister(b:filename)
  augroup END
endfunction " }}}

" vim:ft=vim:fdm=marker
