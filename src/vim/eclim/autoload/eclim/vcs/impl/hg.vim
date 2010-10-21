" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/common/vcs.html
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

if !exists('g:eclim_vcs_hg_loaded')
  let g:eclim_vcs_hg_loaded = 1
else
  finish
endif

" Script Variables {{{
  let s:trackerIdPattern = join(eclim#vcs#command#EclimVcsTrackerIdPatterns, '\|')
" }}}

" GetAnnotations(path, revision) {{{
function! eclim#vcs#impl#hg#GetAnnotations(path, revision)
  let cmd = 'annotate -udc'
  if a:revision != ''
    let revision = substitute(a:revision, '.*:', '', '')
    let cmd .= ' -r ' . revision
  endif
  let result = eclim#vcs#impl#hg#Hg(cmd . ' "' . a:path . '"')
  if type(result) == 0
    return
  endif

  let annotations = split(result, '\n')
  call map(annotations,
      \ "substitute(v:val, '^\\s*\\(.\\{-}\\)\\s\\(\\w\\+\\)\\s\\(.\\{-}\\):\\s.*', '\\2 (\\3) \\1', '')")

  return annotations
endfunction " }}}

" GetPreviousRevision(path, [revision]) {{{
function eclim#vcs#impl#hg#GetPreviousRevision(path, ...)
  let cmd = 'log -q --template "{node|short}\n"'
  if len(a:000) > 0 && a:000[0] != ''
    let cmd .= ' -r' . a:000[0] . ':1'
  endif
  let log = eclim#vcs#impl#hg#Hg(cmd . ' --limit 2 "' . a:path . '"')
  if type(log) == 0
    return
  endif
  let revisions = split(log, '\n')
  return len(revisions) > 1 ? revisions[1] : 0
endfunction " }}}

" GetRevision(path) {{{
function eclim#vcs#impl#hg#GetRevision(path)
  let log = eclim#vcs#impl#hg#Hg('log -q --template "{node|short}\n" --limit 1 "' . a:path . '"')
  if type(log) == 0
    return
  endif
  return substitute(log, '\n', '', '')
endfunction " }}}

" GetRevisions(path) {{{
function eclim#vcs#impl#hg#GetRevisions(path)
  let log = eclim#vcs#impl#hg#Hg('log -q --template "{node|short}\n" "' . a:path . '"')
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

" GetInfo() {{{
function eclim#vcs#impl#hg#GetInfo()
  let branch = substitute(eclim#vcs#impl#hg#Hg('branch'), '\n$', '', '')
  if branch == '0'
    return ''
  endif

  let bmarks = split(eclim#vcs#impl#hg#Hg('bookmarks'), '\n')
  let bmarks = filter(bmarks, 'v:val =~ "^\\s*\\*"')
  let bmark = len(bmarks) == 1 ?
    \ substitute(bmarks[0], '^\s*\*\s*\(\w\+\)\s.*', '\1', '') : ''
  let info = 'hg:' . branch . (bmark != '' ? (':' . bmark) : '')
  return info
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

" Info(path) {{{
function eclim#vcs#impl#hg#Info(path)
  let result = eclim#vcs#impl#hg#Hg('log --limit 1 "' . a:path . '"')
  if type(result) == 0
    return
  endif
  call eclim#util#Echo(result)
endfunction " }}}

" Log(path) {{{
function eclim#vcs#impl#hg#Log(path)
  " Note: tags are space separated, so if the user has a space in their tag
  " name, that tag will be screwed in the log.
  let logcmd = 'log --template "{node|short}|{author}|{date|age}|{tags}|{desc|firstline}\n"'
  if g:EclimVcsLogMaxEntries > 0
    let logcmd .= ' --limit ' . g:EclimVcsLogMaxEntries
  endif

  let result = eclim#vcs#impl#hg#Hg(logcmd . ' "' . a:path . '"')
  if type(result) == 0
    return
  endif
  let log = []
  for line in split(result, '\n')
    let values = split(line, '|')
    call add(log, {
        \ 'revision': values[0],
        \ 'author': values[1],
        \ 'age': values[2],
        \ 'refs': split(values[3]),
        \ 'comment': values[4],
     \ })
  endfor
  let root_dir = exists('b:vcs_props') ?
    \ b:vcs_props.root_dir : eclim#vcs#impl#hg#GetRoot()
  return {'log': log, 'props': {'root_dir': root_dir}}
endfunction " }}}

" LogDetail(revision) {{{
function eclim#vcs#impl#hg#LogDetail(revision)
  let basedir = EclimBaseDir()
  let logcmd = 'log "--template=' .
    \ '{node|short}|{author}|{date|age}|{date|isodate}|{tags}|{desc|firstline}|{desc}"'
  let result = eclim#vcs#impl#hg#Hg(logcmd . ' -r ' . a:revision)
  if type(result) == 0
    return
  endif
  let values = split(result, '|')
  return {
      \ 'revision': values[0],
      \ 'author': values[1],
      \ 'age': values[2],
      \ 'date': values[3],
      \ 'refs': split(values[4]),
      \ 'comment': values[5],
      \ 'description': values[6],
   \ }
endfunction " }}}

" LogFiles(revision) {{{
function eclim#vcs#impl#hg#LogFiles(revision)
  let basedir = substitute(EclimBaseDir(), '\', '', 'g')
  let logcmd = 'log --copies "--style=' . basedir .
    \ '/eclim/autoload/eclim/vcs/impl/hg_log_files.style" '
  let result = eclim#vcs#impl#hg#Hg(logcmd . '-r ' . a:revision)
  if type(result) == 0
    return
  endif
  let files = []
  let deletes = []
  for result in split(result, '\n')
    if result =~ 'R'
      let [status, old, new] = split(result, '\t')
      " filter out copies (the --copies arg shows cp and mv ops)
      if index(deletes, old) == -1
        continue
      else
        call remove(files, index(files, {'status': 'D', 'file': old}))
        call remove(files, index(files, {'status': 'A', 'file': new}))
      endif
      call add(files, {'status': status, 'old': old, 'new': new})
    else
      let [status, file] = split(result, '\t')
      " keep this list for filtering out copies
      if status == 'D'
        call add(deletes, file)
      endif
      call add(files, {'status': status, 'file': file})
    endif
  endfor
  return files
endfunction " }}}

" ViewFileRevision(path, revision) {{{
function! eclim#vcs#impl#hg#ViewFileRevision(path, revision)
  let revision = substitute(a:revision, '.\{-}:', '', '')
  let result = eclim#vcs#impl#hg#Hg('cat -r ' . revision . ' "' . a:path . '"')
  return split(result, '\n')
endfunction " }}}

" Hg(args) {{{
" Executes 'hg' with the supplied args.
function eclim#vcs#impl#hg#Hg(args)
  return eclim#vcs#util#Vcs('hg', a:args)
endfunction " }}}

" vim:ft=vim:fdm=marker
