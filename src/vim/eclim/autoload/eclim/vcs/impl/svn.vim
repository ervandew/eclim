" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/common/vcs.html
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

if !exists('g:eclim_vcs_svn_loaded')
  let g:eclim_vcs_svn_loaded = 1
else
  finish
endif

" Script Variables {{{
  let s:trackerIdPattern = join(eclim#vcs#command#EclimVcsTrackerIdPatterns, '\|')
" }}}

" GetAnnotations(revision) {{{
function! eclim#vcs#impl#svn#GetAnnotations(revision)
  let cmd = 'annotate -v'
  if a:revision != ''
    let cmd .= ' -r ' . a:revision
  endif
  if exists('b:vcs_props')
    if filereadable(b:vcs_props.path)
      let file = fnamemodify(b:vcs_props.path, ':t')
    else
      let file = b:vcs_props.svn_root_url . b:vcs_props.path
    endif
  else
    let file = expand('%')
  endif
  let result = eclim#vcs#impl#svn#Svn(cmd . ' "' . file . '"')
  if type(result) == 0
    return
  endif

  let annotations = split(result, '\n')
  call map(annotations,
      \ "substitute(v:val, '^\\s*\\([0-9]\\+\\)\\s*\\(.\\{-}\\)\\s\\+.\\{-}\\s\\+\\(.\\{-}\\)\\s\\+.\\{-}(\\(.\\{-}\\)).*', '\\1 (\\4 \\3) \\2', '')")

  return annotations
endfunction " }}}

" GetRelativePath(dir, file) {{{
function eclim#vcs#impl#svn#GetRelativePath(dir, file)
  let info = eclim#vcs#impl#svn#Svn('info')
  if type(info) == 0
    return
  endif
  let url = substitute(info, '.\{-}URL:\s\+\(.\{-}\)\n.*', '\1', '')
  let dir = substitute(a:dir, '\', '/', 'g')
  let url_parts = split(url, '/')
  let dir_parts = split(dir, '/')
  let path_parts = []
  while url_parts[-1] == dir_parts[-1]
    call insert(path_parts, url_parts[-1], 0)
    let url_parts = url_parts[:-2]
    let dir_parts = dir_parts[:-2]
  endwhile
  let path = join(path_parts, '/')
  if a:file != ''
    let path .= '/' . a:file
  endif
  return '/' . path
endfunction " }}}

" GetPreviousRevision([file, revision]) {{{
function eclim#vcs#impl#svn#GetPreviousRevision(...)
  let path = exists('b:vcs_props') && has_key(b:vcs_props, 'svn_root_url') ?
    \ (b:vcs_props.svn_root_url . a:000[0]) : expand('%:t')

  let cmd = 'log -q'
  if len(a:000) > 1 && a:000[1] != ''
    let cmd .= 'r ' . a:000[1] . ':1'
  endif
  let log = eclim#vcs#impl#svn#Svn(cmd . ' --limit 2 "' . path . '"')
  if type(log) == 0
    return
  endif
  let lines = split(log, '\n')
  if len(lines) == 5 && lines[1] =~ '^r[0-9]\+' && lines[3] =~ '^r[0-9]\+'
    return substitute(lines[3], '^r\([0-9]\+\)\s.*', '\1', '')
  endif

  return
endfunction " }}}

" GetRevision(file) {{{
function eclim#vcs#impl#svn#GetRevision(file)
  let path = exists('b:vcs_props') ? b:vcs_props.svn_root_url . '/' . a:file : a:file
  let info = eclim#vcs#impl#svn#Svn('info "' . path . '"')
  if type(info) == 0
    return
  endif
  let pattern = '.*Last Changed Rev:\s*\([0-9]\+\)\s*.*'
  if info =~ pattern
    return substitute(info, pattern, '\1', '')
  endif

  return
endfunction " }}}

" GetRevisions() {{{
function eclim#vcs#impl#svn#GetRevisions()
  let log = eclim#vcs#impl#svn#Svn('log -q "' . expand('%:t') . '"')
  if type(log) == 0
    return
  endif
  let lines = split(log, '\n')
  call filter(lines, 'v:val =~ "^r[0-9]\\+\\s.*"')
  call map(lines, 'substitute(v:val, "^r\\([0-9]\\+\\)\\s.*", "\\1", "")')
  return lines
endfunction " }}}

" GetRoot() {{{
function eclim#vcs#impl#svn#GetRoot()
  let info = eclim#vcs#impl#svn#Svn('info')
  if type(info) == 0
    return
  endif
  let url = substitute(info, '.\{-}URL:\s\+\(.\{-}\)\n.*', '\1', '')
  let dir = substitute(getcwd(), '\', '/', 'g')
  let url_parts = split(url, '/')
  let dir_parts = split(dir, '/')
  while url_parts[-1] == dir_parts[-1]
    let url_parts = url_parts[:-2]
    let dir_parts = dir_parts[:-2]
  endwhile
  let dir = join(dir_parts, '/')
  if has('unix')
    return '/' . dir
  endif
  return dir
endfunction " }}}

" GetEditorFile() {{{
function eclim#vcs#impl#svn#GetEditorFile()
  let line = getline('.')
  let file = ''
  if line =~ '^M\s\+.*'
    let file = substitute(line, '^M\s\+\(.*\)\s*', '\1', '')
  elseif line =~ '^A\s\+.*'
    let file = substitute(line, '^A\s\+\(.*\)\s*', '\1', '')
  endif
  return file
endfunction " }}}

" GetModifiedFiles() {{{
function eclim#vcs#impl#svn#GetModifiedFiles()
  let root = eclim#vcs#impl#svn#GetRoot()
  let status = eclim#vcs#impl#svn#Svn('status')
  let files = []
  for file in split(status, "\n")
    if file !~ '^[?AM]\s\+'
      continue
    endif
    let file = substitute(file, '^[?AM]\s\+', '', '')
    call add(files, root . '/' . file)
  endfor
  return files
endfunction " }}}

" GetVcsWebPath() {{{
function eclim#vcs#impl#svn#GetVcsWebPath()
  let url_root = s:GetUrlAndRoot(expand('%'))
  let path = substitute(url_root.url, url_root.root, '', '')
  if path =~ '^/'
    let path = path[1:]
  endif
  return path
endfunction " }}}

" ChangeSet(revision) {{{
function eclim#vcs#impl#svn#ChangeSet(revision)
  if exists('b:vcs_props') && has_key(b:vcs_props, 'svn_root_url')
    let url_root = {'root': b:vcs_props.svn_root_url}
    if isdirectory(b:vcs_props.path) || filereadable(b:vcs_props.path)
      let url_root.url = fnamemodify(b:vcs_props.path, ':t')
    else
      let url_root.url = b:vcs_props.svn_root_url . '/' . b:vcs_props.path
    endif
    let root_dir = b:vcs_props.root_dir
  else
    let url_root = {'root': s:GetRootUrl(), 'url': ''}
    let root_dir = eclim#vcs#impl#svn#GetRoot()
  endif
  let result = eclim#vcs#impl#svn#Svn('log -vr ' . a:revision . ' ' . (url_root.url))
  if type(result) == 0
    return
  endif
  let log = split(result, '\n')

  let lines = []
  let entry = {}
  call s:ParseSvnInfo(entry, log[1])
  call add(lines, 'Revision: ' . entry.revision)
  call add(lines, 'Modified: ' . entry.date . ' by ' . entry.author)
  let files = map(log[2:-2], 'substitute(v:val, "\\s*M\\s*\\(.*\\)", "  |M| |\\1|", "")')
  let files = map(files, 'substitute(v:val, "\\s*A\\s*\\(.*\\)", "   A  |\\1|", "")')
  let files = map(files, 'substitute(v:val, "\\s*D\\s*\\(.*\\)", "   D   \\1", "")')
  let files = map(files,
    \ 'substitute(v:val, "\\(.*\\)\\( (.*)\\)\\(.*\\)", "\\1\\3\\2", "")')
  let files = map(files, 'substitute(v:val, "\\(#\\d\\+\\)", "|\\1|", "g")')
  call extend(lines, files)
  return {'changeset': lines,
    \ 'props': {'root_dir': root_dir, 'svn_root_url': url_root.root}}
endfunction " }}}

" Info() {{{
function eclim#vcs#impl#svn#Info()
  let result = eclim#vcs#impl#svn#Svn('info "' . expand('%:t') . '"')
  if type(result) == 0
    return
  endif
  let info = split(result, "\n")
  call filter(info, "v:val =~ '^\\(Last\\|URL\\)'")
  call eclim#util#Echo(join(info, "\n"))
endfunction " }}}

" ListDir([path]) {{{
function eclim#vcs#impl#svn#ListDir(...)
  if len(a:000) > 0
    if exists('b:vcs_props') && has_key(b:vcs_props, 'svn_root_url')
      let url_root = {'root': b:vcs_props.svn_root_url}
      if isdirectory(b:vcs_props.path)
        let url_root.url = s:GetUrl(b:vcs_props.path)
      else
        let url_root.url = b:vcs_props.svn_root_url . '/' . a:000[0]
      endif
      let root_dir = b:vcs_props.root_dir
    else
      let url_root = s:GetUrlAndRoot(a:000[0])
      let root_dir = eclim#vcs#impl#svn#GetRoot()
    endif
  else
    let url_root = s:GetUrlAndRoot('.')
    let root_dir = eclim#vcs#impl#svn#GetRoot()
  endif

  let result = eclim#vcs#impl#svn#Svn('list "' . url_root.url . '"')
  if type(result) == 0
    return
  endif
  let listing = split(result, '\n')
  let dirs = sort(filter(listing[:], 'v:val =~ "/$"'))
  let files = sort(filter(listing[:], 'v:val =~ "[^/]$"'))

  call map(dirs, '"|" . v:val . "|"')
  call map(files, '"|" . v:val . "|"')

  let lines = [s:Breadcrumb(url_root), '']
  call extend(lines, dirs)
  call extend(lines, files)

  return {'list': lines, 'props': {'root_dir': root_dir, 'svn_root_url': url_root.root}}
endfunction " }}}

" Log([file]) {{{
function eclim#vcs#impl#svn#Log(...)
  let root_dir = ''
  if len(a:000) > 0
    let path = a:000[0]
    if exists('b:vcs_props') && has_key(b:vcs_props, 'svn_root_url')
      let url_root = {'root': b:vcs_props.svn_root_url}
      if isdirectory(path) || filereadable(path)
        let url_root.url = s:GetUrl(fnamemodify(path, ':t'))
      else
        let url_root.url = b:vcs_props.svn_root_url . '/' . path
      endif
      let root_dir = b:vcs_props.root_dir
    else
      let url_root = s:GetUrlAndRoot(path)
      let root_dir = eclim#vcs#impl#svn#GetRoot()
    endif
  else
    let path = expand('%')
    let url_root = s:GetUrlAndRoot(path)
    let root_dir = eclim#vcs#impl#svn#GetRoot()
  endif

  let logcmd = 'log'
  if g:EclimVcsLogMaxEntries > 0
    let logcmd .= ' --limit ' . g:EclimVcsLogMaxEntries
  endif

  let result = eclim#vcs#impl#svn#Svn(logcmd . ' "' . url_root.url . '"')
  if type(result) == 0
    return
  endif
  let log = s:ParseSvnLog(split(result, '\n'))

  let index = 0
  let lines = [s:Breadcrumb(url_root), '']
  for entry in log
    let index += 1
    call add(lines, '------------------------------------------')
    call add(lines, 'Revision: |' . entry.revision . '| |view| |annotate|')
    call add(lines, 'Modified: ' . entry.date . ' by ' . entry.author)
    let working_copy = isdirectory(path) || filereadable(path) ? ' |working copy|' : ''
    if index < len(log)
      call add(lines, 'Diff: |previous ' . log[index].revision . '|' . working_copy)
    elseif working_copy != ''
      call add(lines, 'Diff: |working copy|')
    endif
    call add(lines, '')
    let lines += entry.comment
    if lines[-1] !~ '^\s*$' && index != len(log)
      call add(lines, '')
    endif
  endfor
  return {'log': lines, 'props': {'root_dir': root_dir, 'svn_root_url': url_root.root}}
endfunction " }}}

" ViewFileRevision(path, revision) {{{
function! eclim#vcs#impl#svn#ViewFileRevision(path, revision)
  let path = has_key(b:vcs_props, 'svn_root_url') ?
    \ b:vcs_props.svn_root_url . '/' . a:path : a:path
  let content = eclim#vcs#impl#svn#Svn('cat -r ' . a:revision . ' "' . path . '"')
  return split(content, '\n')
endfunction " }}}

" Svn(args) {{{
" Executes 'svn' with the supplied args.
function eclim#vcs#impl#svn#Svn(args)
  return eclim#vcs#util#Vcs('svn', a:args)
endfunction " }}}

" s:GetRootUrl() {{{
function s:GetRootUrl()
  let info = eclim#vcs#impl#svn#Svn('info')
  if type(info) == 0
    return
  endif
  let url = substitute(info, '.\{-}Repository Root:\s\+\(.\{-}\)\n.*', '\1', '')
  return url
endfunction " }}}

" s:GetUrl([file]) {{{
function s:GetUrl(...)
  let info = eclim#vcs#impl#svn#Svn('info ' . (len(a:000) > 0 ? a:000[0] : ''))
  if type(info) == 0
    return
  endif
  let url = substitute(info, '.\{-}URL:\s\+\(.\{-}\)\n.*', '\1', '')
  return url
endfunction " }}}

" s:GetUrlAndRoot([file]) {{{
function s:GetUrlAndRoot(...)
  let path = len(a:000) > 0 ? fnamemodify(a:000[0], ':t') : ''
  let info = eclim#vcs#impl#svn#Svn('info ' . path)
  if type(info) == 0
    return
  endif
  let root = substitute(info, '.\{-}Repository Root:\s\+\(.\{-}\)\n.*', '\1', '')
  let url = substitute(info, '.\{-}URL:\s\+\(.\{-}\)\n.*', '\1', '')
  return {'root': root, 'url': url}
endfunction " }}}

" s:Breadcrumb(url_root) {{{
function! s:Breadcrumb(url_root)
  if a:url_root.root . '/' == a:url_root.url
    return '/'
  endif

  let path = split(substitute(a:url_root.url, a:url_root.root, '', ''), '/')
  call insert(path, fnamemodify(a:url_root.root, ':t'))
  let dirs = map(path[:-2], '"|" . v:val . "|"')
  return join(dirs, ' / ') . ' / ' . path[-1]
endfunction " }}}

" s:ParseSvnInfo(entry, line) {{{
function! s:ParseSvnInfo(entry, line)
  let a:entry['revision'] = substitute(a:line, '^r\(\w\+\).*', '\1', '')
  let a:entry['author'] = substitute(a:line, '.\{-}|\s*\(\S\+\)\s*|.*', '\1', '')
  let a:entry['date'] = substitute(a:line, '.\{-}|.\{-}|\s*\(.\{-}\)\s\+[+-].\{-}|.*', '\1', '')
endfunction " }}}

" s:ParseSvnLog(lines) {{{
function! s:ParseSvnLog(lines)
  let log = []
  let section = 'head'
  let index = 0
  for line in a:lines
    let index += 1
    if line =~ '^-\+$' && index == len(a:lines)
      " get rid of empty line at the end of last entry's comment
      if exists('l:entry') && len(entry.comment) > 1 && entry.comment[-1] =~ '^\s*$'
        let entry.comment = entry.comment[:-2]
      endif
      continue
    elseif line =~ '^-\+$'
      " get rid of empty line at the end of entry's comment
      if exists('l:entry') && len(entry.comment) > 1 && entry.comment[-1] =~ '^\s*$'
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
      let line = substitute(
        \ line, '\(' . s:trackerIdPattern . '\)', '|\1|', 'g')
      call add(entry.comment, line)
    endif
  endfor
  return log
endfunction " }}}

" vim:ft=vim:fdm=marker
