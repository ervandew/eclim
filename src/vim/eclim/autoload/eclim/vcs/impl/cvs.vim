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

if !exists('g:eclim_vcs_cvs_loaded')
  let g:eclim_vcs_cvs_loaded = 1
else
  finish
endif

" Script Variables {{{
  let s:trackerIdPattern = join(eclim#vcs#command#EclimVcsTrackerIdPatterns, '\|')
" }}}

" GetAnnotations(revision) {{{
function! eclim#vcs#impl#cvs#GetAnnotations(revision)
  if exists('b:vcs_props')
    if filereadable(b:vcs_props.path)
      let file = fnamemodify(b:vcs_props.path, ':t')
    else
      let file = b:vcs_props.svn_root_url . b:vcs_props.path
    endif
  else
    let file = expand('%')
  endif

  let cmd = 'annotate'
  if a:revision != ''
    let cmd .= ' -r ' . a:revision
  endif

  let result = eclim#vcs#impl#cvs#Cvs(cmd . ' "' . file . '"')
  let annotations = split(result, '\n')
  call filter(annotations, 'v:val =~ "^[0-9]"')
  call map(annotations,
    \ "substitute(v:val, '^\\s*\\([0-9.]\\+\\)\\s*(\\(.\\{-}\\)\\s\\+\\(.\\{-}\\)).*', '\\1 (\\3) \\2', '')")

  if v:shell_error
    call eclim#util#EchoError(result)
    return
  endif

  return annotations
endfunction " }}}

" GetRelativePath(dir, file) {{{
function eclim#vcs#impl#cvs#GetRelativePath(dir, file)
  let lines = readfile(escape(a:dir . '/CVS/Repository', ' '), '', 1)
  return '/' . lines[0] . '/' . a:file
endfunction " }}}

" GetPreviousRevision() {{{
function eclim#vcs#impl#cvs#GetPreviousRevision()
  let log = eclim#vcs#impl#cvs#Cvs('log ' . expand('%:t'))
  let lines = split(log, '\n')
  call filter(lines, 'v:val =~ "^revision [0-9.]\\+\\s*$"')
  if len(lines) >= 2
    return substitute(lines[1], '^revision \([0-9.]\+\)\s*.*', '\1', '')
  endif

  return
endfunction " }}}

" GetRevision(file) {{{
function eclim#vcs#impl#cvs#GetRevision(file)
  let status = eclim#vcs#impl#cvs#Cvs('status ' . a:file)
  let pattern = '.*Working revision:\s*\([0-9.]\+\)\s*.*'
  if status =~ pattern
    return substitute(status, pattern, '\1', '')
  endif

  return
endfunction " }}}

" GetRevisions() {{{
function eclim#vcs#impl#cvs#GetRevisions()
  let log = eclim#vcs#impl#cvs#Cvs('log ' . expand('%:t'))
  let lines = split(log, '\n')
  call filter(lines, 'v:val =~ "^revision [0-9.]\\+\\s*$"')
  call map(lines, 'substitute(v:val, "^revision \\([0-9.]\\+\\)\\s*$", "\\1", "")')
  return lines
endfunction " }}}

" GetRoot() {{{
function eclim#vcs#impl#cvs#GetRoot()
  let dir = substitute(getcwd(), '\', '/', 'g')
  let lines = readfile(dir . '/CVS/Repository', '', 1)
  let remove = len(split(lines[0], '/'))
  let root = join(split(dir, '/')[:0 - remove], '/')
  if has('unix')
    return '/' . root
  endif
  return root
endfunction " }}}

" GetEditorFile() {{{
function eclim#vcs#impl#cvs#GetEditorFile()
  let line = getline('.')
  if line =~ '^CVS:\s\+.*'
    if !s:IsReadOnlySupported()
      call eclim#util#EchoWarning(
        \ 'Due to locking issues, viewing file diffs in the cvs ' .
        \ 'editor is only supported with cvs version 1.12.1 or higher.')
      return ''
    endif

    " FIXME: cvs will put more than one file on a line if they fit
    let file = substitute(line, '^CVS:\s\+\(.\{-}\)\(\s.*\|$\)', '\1', '')

    if col('.') > (len(file) + len(substitute(line, '^\(CVS:\s\+\).*', '\1', '')))
      let file = substitute(
        \ line, '^.*\s\+\(.*\%' . col('.') . 'c.\{-}\)\(\s.*\|$\)', '\1', '')
    endif

    let dirfound = search('^CVS:\s\+Committing in', 'nw')
    if dirfound && eclim#vcs#impl#cvs#GetRoot() == getcwd()
      let line = getline(dirfound)
      let dir = substitute(line, '^CVS:\s\+Committing in\s\+\(.*\)', '\1', '')
      let file = dir . '/' . file
    endif

    if filereadable(file)
      return file
    endif
  endif
  return ''
endfunction " }}}

" GetModifiedFiles() {{{
function eclim#vcs#impl#cvs#GetModifiedFiles()
  call eclim#util#EchoError('Sorry, this function is not yet supported for cvs.')
  return []
endfunction " }}}

" GetVcsWebPath() {{{
function eclim#vcs#impl#cvs#GetVcsWebPath()
  let dir = substitute(getcwd(), '\', '/', 'g')
  let lines = readfile(dir . '/CVS/Repository', '', 1)
  return lines[0] . '/' . expand('%')
endfunction " }}}

" Info() {{{
" Retrieves and echos info on the current file.
function eclim#vcs#impl#cvs#Info()
  let result = eclim#vcs#impl#cvs#Cvs('status "' . expand('%:t') . '"')
  if type(result) == 0
    return
  endif
  let info = split(result, "\n")
  call filter(info, 'v:val =~ "^\\(File\\|\\s\\+\\)"')
  call map(info, "substitute(v:val, '^\\s\\+', '', '')")
  call map(info, "substitute(v:val, '\\t', ' ', 'g')")
  let info[0] = substitute(info[0], '.\{-}\(Status:.*\)', '\1', '')
  call eclim#util#Echo(join(info, "\n"))
endfunction " }}}

" ListDir([path]) {{{
function eclim#vcs#impl#cvs#ListDir(...)
  let lines = [s:Breadcrumb(a:000[0]), '']
  " alternate solution if Entries doesn't have dirs in it
  "let dirs = split(globpath('.', '*/CVS'), '\n')
  "let dirs = dirs + split( globpath('.', '.*/CVS'), '\n')
  "call map(dirs, 'split(v:val, "/")[1] . "/"')
  "call filter(dirs, 'v:val !~ "\\./"')
  let contents = readfile('CVS/Entries')
  let dirs = filter(copy(contents), 'v:val =~ "^D/"')
  let files = filter(copy(contents), 'v:val =~ "^/"')
  call map(dirs, 'substitute(v:val, "^D/\\(.\\{-}/\\).*", "|\\1|", "")')
  call map(files, 'substitute(v:val, "^/\\(.\\{-}\\)/.*", "|\\1|", "")')
  call sort(dirs)
  call sort(files)
  call extend(lines, dirs)
  call extend(lines, files)

  let root_dir = exists('b:vcs_props') ?
    \ b:vcs_props.root_dir : eclim#vcs#impl#cvs#GetRoot()
  return {'list': lines, 'props': {'root_dir': root_dir}}
endfunction " }}}

" Log([file]) {{{
function eclim#vcs#impl#cvs#Log(...)
  if len(a:000) > 0
    let path = fnamemodify(a:000[0], ':t')
  else
    let path = expand('%:t')
  endif

  let result = eclim#vcs#impl#cvs#Cvs('log "' . path . '"')
  if type(result) == 0
    return
  endif
  let log = s:ParseCvsLog(split(result, '\n'))

  let index = 0
  let lines = [s:Breadcrumb(path), '']
  for entry in log
    let index += 1
    call add(lines, '------------------------------------------')
    call add(lines, 'Revision: ' . entry.revision . ' |view| |annotate|')
    call add(lines, 'Modified: ' . entry.date . ' by ' . entry.author)
    let working_copy = ''
    if (isdirectory(path) || filereadable(path)) &&
     \ (!exists('b:filename') || b:filename =~ path . '$')
      let working_copy = ' |working copy|'
    endif
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
  let root_dir = exists('b:vcs_props') ?
    \ b:vcs_props.root_dir : eclim#vcs#impl#cvs#GetRoot()
  return {'log': lines, 'props': {'root_dir': root_dir}}
endfunction " }}}

" ViewFileRevision(path, revision) {{{
function! eclim#vcs#impl#cvs#ViewFileRevision(path, revision)
  let path = fnamemodify(a:path, ':t')
  let result = eclim#vcs#impl#cvs#Cvs('annotate -r ' . a:revision . ' "' . path . '"')
  let content = split(result, '\n')
  call filter(content, 'v:val =~ "^[0-9]"')
  call map(content, 'substitute(v:val, "^.\\{-}):\\s", "", "")')
  return content
endfunction " }}}

" Cvs(args) {{{
" Executes 'cvs' with the supplied args.
function eclim#vcs#impl#cvs#Cvs(args)
  " Note: use -R (cvs 1.12.1 or highter) which pretends that the repos is read
  " only, which avoid waiting on locks, which we don't need to since we only
  " run read only operations.  Issue occurs trying to view diffs of files in
  " the cvs editor.
  let args = (s:IsReadOnlySupported() ? '-R ' : '') . a:args
  return eclim#vcs#util#Vcs('cvs', args)
endfunction " }}}

" s:GetVersion() {{{
function s:GetVersion()
  if !exists('s:cvs_version')
    let s:cvs_version = eclim#vcs#util#Vcs('cvs', '--version')
    let s:cvs_version =
      \ substitute(s:cvs_version, '.\{-}\s\(\d\+\.\d\+\.\d\+\)\s.*', '\1', '')
  endif
  return s:cvs_version
endfunction " }}}

" s:IsReadOnlySupported() {{{
function s:IsReadOnlySupported()
  let ver = s:GetVersion()
  exec 'let major = ' . substitute(ver, '^\(\d\+\)\..*', '\1', '')
  exec 'let minor = ' . substitute(ver, '^\d\+\.\(\d\+\)\..*', '\1', '')
  return (major >= 1 && minor >= 12)
endfunction " }}}

" s:Breadcrumb(path) {{{
function! s:Breadcrumb(path)
  if a:path == ''
    return '/'
  endif

  let file = exists('b:vcs_props') && isdirectory(b:vcs_props.root_dir . '/' . a:path) ?
    \ '' : a:path
  let path = split(eclim#vcs#impl#cvs#GetRelativePath('.', file), '/')
  let dirs = map(path[:-2], '"|" . v:val . "|"')
  return join(dirs, ' / ') . ' / ' . path[-1]
endfunction " }}}

" s:ParseCvsLog(lines) {{{
function! s:ParseCvsLog(lines)
  let log = []
  let section = 'head'
  let index = 0
  for line in a:lines
    let index += 1
    if line =~ '^=\+$'
      break
    elseif line =~ '^-\+$'
      let section = 'info'
      let entry = {'comment': []}
      call add(log, entry)
      continue
    elseif section == 'head'
      continue
    elseif section == 'info'
      let entry.revision = substitute(line, 'revision ', '', '')
      let section = 'date_author'
    elseif section == 'date_author'
      let entry.date = substitute(line, 'date: \(.\{-}\);.*', '\1', '')
      let entry.author = substitute(line, '.*author: \(.\{-}\);.*', '\1', '')
      let section = 'comment'
    elseif section == 'comment'
      let line = substitute(
        \ line, '\(' . s:trackerIdPattern . '\)', '|\1|', 'g')
      call add(entry.comment, line)
    endif
  endfor
  return log
endfunction " }}}

" vim:ft=vim:fdm=marker
