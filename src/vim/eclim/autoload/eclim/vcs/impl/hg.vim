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

if !exists('g:eclim_vcs_hg_loaded')
  let g:eclim_vcs_hg_loaded = 1
else
  finish
endif

" Script Variables {{{
  let s:trackerIdPattern = join(eclim#vcs#command#EclimVcsTrackerIdPatterns, '\|')
" }}}

" GetAnnotations(revision) {{{
function! eclim#vcs#impl#hg#GetAnnotations(revision)
  if exists('b:vcs_props') && filereadable(b:vcs_props.path)
    let file = fnamemodify(b:vcs_props.path, ':t')
  else
    let file = expand('%')
  endif

  let cmd = 'annotate -udn'
  if a:revision != ''
    let revision = substitute(a:revision, '.*:', '', '')
    let cmd .= ' -r ' . revision
  endif
  let result = eclim#vcs#impl#hg#Hg(cmd . ' "' . file . '"')
  if type(result) == 0
    return
  endif

  let annotations = split(result, '\n')
  call map(annotations,
      \ "substitute(v:val, '^\\(.\\{-}\\)\\s\\([0-9]\\+\\)\\s\\(.\\{-}\\):\\s.*', '\\2 (\\3) \\1', '')")

  return annotations
endfunction " }}}

" GetRelativePath(dir, file) {{{
function eclim#vcs#impl#hg#GetRelativePath(dir, file)
  let root = eclim#vcs#impl#hg#GetRoot()
  if type(root) == 0
    return
  endif
  let root = fnamemodify(root, ':h')
  return substitute(a:dir, root, '', '') . '/' . a:file
endfunction " }}}

" GetPreviousRevision([file, revision]) {{{
function eclim#vcs#impl#hg#GetPreviousRevision(...)
  let file = len(a:000) ? fnamemodify(a:000[0], ':t') : expand('%:t')
  let cmd = 'log -q'
  if len(a:000) > 1 && a:000[1] != ''
    let cmd .= '-r' . a:000[1] . ':1'
  endif
  let log = eclim#vcs#impl#hg#Hg(cmd . ' --limit 2 "' . file . '"')
  if type(log) == 0
    return
  endif
  let revisions = split(log, '\n')
  return len(revisions) > 1 ? revisions[1] : 0
endfunction " }}}

" GetRevision(file) {{{
function eclim#vcs#impl#hg#GetRevision(file)
  let log = eclim#vcs#impl#hg#Hg('log -q --limit 1 "' . a:file . '"')
  if type(log) == 0
    return
  endif
  return substitute(log, '\n', '', '')
endfunction " }}}

" GetRevisions() {{{
function eclim#vcs#impl#hg#GetRevisions()
  let log = eclim#vcs#impl#hg#Hg('log -q "' . expand('%:t') . '"')
  if type(log) == 0
    return
  endif
  return split(log, '\n')
endfunction " }}}

" GetRoot() {{{
function eclim#vcs#impl#hg#GetRoot()
  let root = eclim#vcs#impl#hg#Hg('root')
  if type(root) == 0
    return
  endif
  let root = substitute(root, '\n', '', '')
  let root = substitute(root, '\', '/', 'g')
  return root
endfunction " }}}

" GetEditorFile() {{{
function eclim#vcs#impl#hg#GetEditorFile()
  let line = getline('.')
  let file = ''
  if line =~ '^HG: changed .*'
    let file = substitute(line, '^HG: changed\s\+\(.*\)\s*', '\1', '')
  elseif line =~ '^HG: added .*'
    let file = substitute(line, '^HG: added\s\+\(.*\)\s*', '\1', '')
  endif
  return file
endfunction " }}}

" GetModifiedFiles() {{{
function eclim#vcs#impl#hg#GetModifiedFiles()
  let status = eclim#vcs#impl#hg#Hg('status -m -a -u -n')
  let root = eclim#vcs#impl#hg#GetRoot()
  return map(split(status, "\n"), 'root . "/" . v:val')
endfunction " }}}

" GetVcsWebPath() {{{
function eclim#vcs#impl#hg#GetVcsWebPath()
  let path = substitute(expand('%:p'), '\', '/', 'g')
  let path = substitute(path, eclim#vcs#impl#hg#GetRoot(), '', '')
  if path =~ '^/'
    let path = path[1:]
  endif
  return path
endfunction " }}}

" ChangeSet(revision) {{{
function eclim#vcs#impl#hg#ChangeSet(revision)
  let result = eclim#vcs#impl#hg#Hg('log -vr ' . a:revision)
  if type(result) == 0
    return
  endif
  let results = split(result, '\n')
  let log = {}
  let comment = []
  for line in results[:-3]
    if line =~ '^[a-z]\+:'
      let name = substitute(line, '^\(.\{-}\):.*', '\1', '')
      let line = substitute(line, '^.\{-}:\s*\(.*\)', '\1', '')
      let log[name] = line
    else
      call add(comment, line)
    endif
  endfor
  let author = substitute(log.user, '^user:\s\+', '', '')
  let date = substitute(log.date, '^date:\s\+', '', '')
  let root = eclim#vcs#impl#hg#GetRoot()
  let files = split(substitute(log.files, '^files:\s\+', '', ''))
  call map(files, 'filereadable(root . "/" . v:val) ? "  A/M |" . v:val . "|" : "  R   |" . v:val . "|"')
  let lines = []
  call add(lines, 'Revision: ' . a:revision)
  call add(lines, 'Modified: ' . date . ' by ' . author)
  call add(lines, 'Changed paths:')
  let lines += files
  call add(lines, '')
  let lines += comment

  let root_dir = exists('b:vcs_props') ?
    \ b:vcs_props.root_dir : eclim#vcs#impl#hg#GetRoot()
  return {'changeset': lines, 'props': {'root_dir': root_dir}}
endfunction " }}}

" Info() {{{
function eclim#vcs#impl#hg#Info()
  let result = eclim#vcs#impl#hg#Hg('log --limit 1 "' . expand('%:t') . '"')
  if type(result) == 0
    return
  endif
  call eclim#util#Echo(result)
endfunction " }}}

" Log([file]) {{{
function eclim#vcs#impl#hg#Log(...)
  if len(a:000) > 0
    let dir = fnamemodify(a:000[0], ':h')
    let root = eclim#vcs#impl#hg#GetRoot()
    if dir !~ '^' . root
      let dir = eclim#vcs#impl#hg#GetRoot() . '/' . dir
    endif
    let file = fnamemodify(a:000[0], ':t')
  else
    let dir = expand('%:h')
    let file = expand('%:t')
  endif

  let logcmd = 'log -v'
  if g:EclimVcsLogMaxEntries > 0
    let logcmd .= ' --limit ' . g:EclimVcsLogMaxEntries
  endif

  let result = eclim#vcs#impl#hg#Hg(logcmd . ' "' . file . '"')
  if type(result) == 0
    return
  endif
  let log = s:ParseHgLog(split(result, '\n'))

  let index = 0
  let lines = [s:Breadcrumb(dir, file), '']
  for entry in log
    let index += 1
    call add(lines, '--------------------------------------------------')
    call add(lines, 'Revision: |' . entry.revision . '| |view| |annotate|')
    call add(lines, 'Modified: ' . entry.date . ' by ' . entry.author)
    let working_copy = isdirectory(file) || filereadable(file) ? ' |working copy|' : ''
    if index < len(log)
      call add(lines, 'Diff: |previous ' . log[index].revision . '|' . working_copy)
    elseif working_copy != ''
      call add(lines, 'Diff: |working copy|')
    endif
    call add(lines, '')
    let lines += entry.comment[:-3]
    if lines[-1] !~ '^\s*$' && index != len(log)
      call add(lines, '')
    endif
  endfor
  let root_dir = exists('b:vcs_props') ?
    \ b:vcs_props.root_dir : eclim#vcs#impl#hg#GetRoot()
  return {'log': lines, 'props': {'root_dir': root_dir}}
endfunction " }}}

" ViewFileRevision(path, revision) {{{
function! eclim#vcs#impl#hg#ViewFileRevision(path, revision)
  let revision = substitute(a:revision, '.\{-}:', '', '')
  let path = fnamemodify(a:path, ':t')
  let result = eclim#vcs#impl#hg#Hg('cat -r ' . revision . ' "' . path . '"')
  return split(result, '\n')
endfunction " }}}

" Hg(args) {{{
" Executes 'hg' with the supplied args.
function eclim#vcs#impl#hg#Hg(args)
  return eclim#vcs#util#Vcs('hg', a:args)
endfunction " }}}

" s:Breadcrumb(dir, file) {{{
function! s:Breadcrumb(dir, file)
  let path = split(eclim#vcs#impl#hg#GetRelativePath(a:dir, a:file), '/')
  return join(path, ' / ' )
endfunction " }}}

" s:ParseHgLog(lines) {{{
function! s:ParseHgLog(lines)
  let log = []
  let section = 'header'
  let index = 0
  for line in a:lines
    let index += 1
    if line =~ '^changeset:\s\+[0-9]\+:[0-9a-z]\+'
      let entry = {'revision': substitute(line, 'changeset:\s\+', '', ''), 'comment': []}
      call add(log, entry)
      let section = 'header'
    elseif line =~ '^user:\s\+'
      let entry.author = substitute(line, 'user:\s\+', '', '')
    elseif line =~ '^date:\s\+'
      let entry.date = substitute(line, 'date:\s\+', '', '')
    elseif line =~ '^description:'
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
