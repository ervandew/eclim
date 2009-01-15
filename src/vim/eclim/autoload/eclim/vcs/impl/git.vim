" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/common/vcs.html
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

if !exists('g:eclim_vcs_git_loaded')
  let g:eclim_vcs_git_loaded = 1
else
  finish
endif

" Script Variables {{{
  let s:trackerIdPattern = join(eclim#vcs#command#EclimVcsTrackerIdPatterns, '\|')
" }}}

" GetAnnotations(revision) {{{
function! eclim#vcs#impl#git#GetAnnotations(revision)
  if exists('b:vcs_props')
    if filereadable(b:vcs_props.path)
      let file = fnamemodify(b:vcs_props.path, ':t')
    else
      let file = b:vcs_props.svn_root_url . b:vcs_props.path
    endif
  else
    let file = expand('%')
  endif

  let cmd = 'annotate -l'
  let revision = ''
  if a:revision != ''
    let revision = ' ' . substitute(a:revision, '.*:', '', '')
  endif
  let result = eclim#vcs#impl#git#Git(cmd . ' "' . file . '"' . revision)
  if type(result) == 0
    return
  endif

  let annotations = split(result, '\n')
  call map(annotations,
      \ "substitute(v:val, '\\(.\\{-}\\)\\s\\+(\\s*\\(.\\{-}\\)\\s\\+\\(.\\{-}\\)\\s\\+[0-9]\\+).*', '\\1 (\\3) \\2', '')")
  " substitute(v:val, '\\(.\\{-}\\)\\s\\+(.*', '\\2 (\\3) \\1', '')")

  return annotations
endfunction " }}}

" GetRelativePath(dir, file) {{{
function eclim#vcs#impl#git#GetRelativePath(dir, file)
  let root = eclim#vcs#impl#git#GetRoot()
  if type(root) == 0
    return
  endif
  let dir = substitute(a:dir, '\', '/', 'g')
  return substitute(dir, root, '', '') . '/' . a:file
endfunction " }}}

" GetPreviousRevision([file, revision]) {{{
function eclim#vcs#impl#git#GetPreviousRevision(...)
  let revision = ''
  if len(a:000)
    let path = fnamemodify(a:000[0], ':t')
    let revision = a:000[1]
  else
    let path = expand('%:t')
  endif

  let cmd = 'log --pretty=oneline -2 ' . revision . ' "' . path . '"'
  let log = eclim#vcs#impl#git#Git(cmd)
  if type(log) == 0
    return
  endif
  let revisions = split(log, '\n')
  if len(revisions) > 1
    return substitute(revisions[1], '\(.\{-}\)\s.*', '\1', '')
  endif
  return 0
endfunction " }}}

" GetRevision(file) {{{
function eclim#vcs#impl#git#GetRevision(file)
  " for some reason, in some contexts (git commit buffer), the git command
  " will fail if not run from root of the repos.
  let root = eclim#vcs#impl#git#GetRoot()
  let path = eclim#vcs#impl#git#GetRelativePath(
    \ fnamemodify(a:file, ':p:h'), fnamemodify(a:file, ':p:t'))
  let path = substitute(path, '^/', '', '')
  exec 'lcd ' . root
  let log = eclim#vcs#impl#git#Git('log --pretty=oneline -1 "' . path . '"')
  if type(log) == 0
    return
  endif
  return substitute(log, '\(.\{-}\)\s.*', '\1', '')
endfunction " }}}

" GetRevisions() {{{
function eclim#vcs#impl#git#GetRevisions()
  let log = eclim#vcs#impl#git#Git('log --pretty=oneline "' . expand('%:t') . '"')
  if type(log) == 0
    return
  endif
  let revisions = split(log, '\n')
  call map(revisions, "substitute(v:val, '\\(.\\{-}\\)\\s.*', '\\1', '')")
  return revisions
endfunction " }}}

" GetRoot() {{{
function eclim#vcs#impl#git#GetRoot()
  let root = finddir('.git', getcwd() . ';')
  if root == ''
    return
  endif
  let root = fnamemodify(root, ':p:h:h')
  let root = substitute(root, '\', '/', 'g')
  return root
endfunction " }}}

" GetEditorFile() {{{
function eclim#vcs#impl#git#GetEditorFile()
  let line = getline('.')
  if line =~ '^#\s*modified:.*'
    return substitute(line, '^#\s*modified:\s\+\(.*\)\s*', '\1', '')
  endif
  return ''
endfunction " }}}

" GetVcsWebPath() {{{
function eclim#vcs#impl#git#GetVcsWebPath()
  let path = substitute(expand('%:p'), '\', '/', 'g')
  let path = substitute(path, eclim#vcs#impl#git#GetRoot(), '', '')
  if path =~ '^/'
    let path = path[1:]
  endif
  return path
endfunction " }}}

" ChangeSet(revision) {{{
function eclim#vcs#impl#git#ChangeSet(revision)
  let result = eclim#vcs#impl#git#Git('log -1 --name-status ' . a:revision)
  if type(result) == 0
    return
  endif
  let results = split(result, '\n')
  let log = {}
  let comment = []
  let files = []
  let in_comment = 0
  for line in results[1:]
    if line =~ '^[A-Z][a-z]\+:'
      let name = substitute(line, '^\(.\{-}\):.*', '\1', '')
      let line = substitute(line, '^.\{-}:\s*\(.*\)', '\1', '')
      let log[tolower(name)] = line
    elseif line=~ '^[A-Z]\s\+'
      if in_comment
        let in_comment = 0
        call remove(comment, -1)
      endif
      let line = substitute(line, '^A\s\+\(.*\)', ' A  |\1|', '')
      let line = substitute(line, '^D\s\+\(.*\)', ' D   \1', '')
      let line = substitute(line, '^M\s\+\(.*\)', '|M| |\1|', '')
      call add(files, line)
    elseif line =~ '^\s*$' && !in_comment
      continue
    else
      let in_comment = 1
      call add(comment, substitute(line, '^\s\{4}', '', ''))
    endif
  endfor
  let author = substitute(log.author, '^user:\s\+', '', '')
  let date = substitute(log.date, '^date:\s\+', '', '')
  let lines = []
  call add(lines, 'Revision: ' . a:revision)
  call add(lines, 'Modified: ' . date . ' by ' . author)
  call add(lines, 'Changed paths:')
  let lines += files
  call add(lines, '')
  let lines += comment

  let root_dir = exists('b:vcs_props') ?
    \ b:vcs_props.root_dir : eclim#vcs#impl#git#GetRoot()
  return {'changeset': lines, 'props': {'root_dir': root_dir}}
endfunction " }}}

" Info() {{{
function eclim#vcs#impl#git#Info()
  let result = eclim#vcs#impl#git#Git('log -1 "' . expand('%:t') . '"')
  if type(result) == 0
    return
  endif
  call eclim#util#Echo(result)
endfunction " }}}

" Log([file]) {{{
function eclim#vcs#impl#git#Log(...)
  if len(a:000) > 0
    let dir = fnamemodify(a:000[0], ':h')
    let file = fnamemodify(a:000[0], ':t')
  else
    let dir = expand('%:h')
    let file = expand('%:t')
  endif

  let logcmd = 'log'
  if g:EclimVcsLogMaxEntries > 0
    let logcmd .= ' -' . g:EclimVcsLogMaxEntries
  endif

  let result = eclim#vcs#impl#git#Git(logcmd . ' "' . file . '"')
  if type(result) == 0
    return
  endif
  let log = s:ParseGitLog(split(result, '\n'))

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
    let lines += entry.comment[:-1]
    if lines[-1] !~ '^\s*$' && index != len(log)
      call add(lines, '')
    endif
  endfor
  let root_dir = exists('b:vcs_props') ?
    \ b:vcs_props.root_dir : eclim#vcs#impl#git#GetRoot()
  return {'log': lines, 'props': {'root_dir': root_dir}}
endfunction " }}}

" ViewFileRevision(path, revision) {{{
function! eclim#vcs#impl#git#ViewFileRevision(path, revision)
  " for some reason, in some contexts (git commit buffer), the git command
  " will fail if not run from root of the repos.
  let path = eclim#vcs#impl#git#GetRelativePath(
    \ fnamemodify(a:path, ':p:h'), fnamemodify(a:path, ':t'))
  let path = substitute(path, '^/', '', '')
  let root = eclim#vcs#impl#git#GetRoot()
  exec 'lcd ' . root
  let result = eclim#vcs#impl#git#Git('show "' . a:revision . ':' . path . '"')
  return split(result, '\n')
endfunction " }}}

" Git(args) {{{
" Executes 'git' with the supplied args.
function eclim#vcs#impl#git#Git(args)
  return eclim#vcs#util#Vcs('git', '--no-pager ' . a:args)
endfunction " }}}

" s:Breadcrumb(dir, file) {{{
function! s:Breadcrumb(dir, file)
  let path = split(eclim#vcs#impl#git#GetRelativePath(a:dir, a:file), '/')
  call insert(path, fnamemodify(eclim#vcs#impl#git#GetRoot(), ':t'), 0)
  return join(path, ' / ' )
endfunction " }}}

" s:ParseGitLog(lines) {{{
function! s:ParseGitLog(lines)
  let log = []
  let section = 'header'
  for line in a:lines
    if line =~ '^commit\s[0-9a-z]\+$'
      let entry = {'revision': substitute(line, 'commit\s', '', ''), 'comment': []}
      call add(log, entry)
      let section = 'header'
    elseif line =~ '^Author:\s\+'
      let entry.author = substitute(line, 'Author:\s\+', '', '')
    elseif line =~ '^Date:\s\+'
      let entry.date = substitute(line, 'Date:\s\+', '', '')
    elseif line =~ '^$' && section == 'header'
      let section = 'comment'
    elseif section == 'comment'
      let line = substitute(line, '^\s\{4}', '', '')
      let line = substitute(
        \ line, '\(' . s:trackerIdPattern . '\)', '|\1|', 'g')
      call add(entry.comment, line)
    endif
  endfor
  return log
endfunction " }}}

" vim:ft=vim:fdm=marker
