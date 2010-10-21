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

if !exists('g:eclim_vcs_git_loaded')
  let g:eclim_vcs_git_loaded = 1
else
  finish
endif

" Autocmds {{{

augroup vcs_git
  autocmd BufReadCmd index_blob_* call <SID>ReadIndex()
augroup END

" }}}

" Script Variables {{{
  let s:trackerIdPattern = join(eclim#vcs#command#EclimVcsTrackerIdPatterns, '\|')
" }}}

" GetAnnotations(path, revision) {{{
function! eclim#vcs#impl#git#GetAnnotations(path, revision)
  let cmd = 'annotate'
  let revision = ''
  if a:revision != ''
    let revision = ' ' . substitute(a:revision, '.*:', '', '')
  endif
  let result = eclim#vcs#impl#git#Git(cmd . ' "' . a:path . '"' . revision)
  if type(result) == 0
    return
  endif

  let annotations = split(result, '\n')
  call map(annotations,
      \ "substitute(v:val, '\\(.\\{-}\\)\\s\\+(\\s*\\(.\\{-}\\)\\s\\+\\(\\d\\{4}-\\d\\{2}\-\\d\\{2}\\s.\\{-}\\)\\s\\+[0-9]\\+).*', '\\1 (\\3) \\2', '')")
  " substitute(v:val, '\\(.\\{-}\\)\\s\\+(.*', '\\2 (\\3) \\1', '')")
  call map(annotations, "v:val =~ '^0\\{5,}' ? 'uncommitted' : v:val")

  return annotations
endfunction " }}}

" GetPreviousRevision(path, [revision]) {{{
function eclim#vcs#impl#git#GetPreviousRevision(path, ...)
  let revision = 'HEAD'
  if len(a:000)
    let revision = a:000[0]
  endif

  let cmd = 'rev-list --abbrev-commit -n 1 --skip=1 ' . revision . ' -- "' . a:path . '"'
  let prev = eclim#vcs#impl#git#Git(cmd)
  if type(prev) == 0
    return
  endif
  return substitute(prev, '\n', '', 'g')
endfunction " }}}

" GetRevision(path) {{{
function eclim#vcs#impl#git#GetRevision(path)
  " for some reason, in some contexts (git commit buffer), the git command
  " will fail if not run from root of the repos.
  let root = eclim#vcs#impl#git#GetRoot()
  exec 'lcd ' . escape(root, ' ')

  let path = a:path

  " kind of a hack to support diffs against git's staging (index) area.
  if path =~ '\<index_blob_[a-z0-9]\{40}_'
    let path = substitute(path, '\<index_blob_[a-z0-9]\{40}_', '', '')
  endif

  let rev = eclim#vcs#impl#git#Git('rev-list --abbrev-commit -n 1 HEAD -- "' . path . '"')
  if type(rev) == 0
    return
  endif
  return substitute(rev, '\n', '', '')
endfunction " }}}

" GetRevisions(path) {{{
function eclim#vcs#impl#git#GetRevisions(path)
  let revs = eclim#vcs#impl#git#Git('rev-list --abbrev-commit HEAD "' . a:path . '"')
  if type(revs) == 0
    return
  endif
  return split(revs, '\n')
endfunction " }}}

" GetRoot() {{{
function eclim#vcs#impl#git#GetRoot()
  let root = finddir('.git', escape(getcwd(), ' ') . ';')
  if root == ''
    return
  endif
  let root = fnamemodify(root, ':p:h:h')
  let root = substitute(root, '\', '/', 'g')
  return root
endfunction " }}}

" GetInfo() {{{
function eclim#vcs#impl#git#GetInfo()
  let info = eclim#vcs#impl#git#Git('branch')
  if info == '0'
    return ''
  endif
  let info = 'git:' . substitute(info, '.*\*\s*\(.\{-}\)\(\n.*\|$\)', '\1', 'g')
  return info
endfunction " }}}

" GetEditorFile() {{{
function eclim#vcs#impl#git#GetEditorFile()
  let line = getline('.')
  if line =~ '^#\s*modified:.*'
    let file = substitute(line, '^#\s*modified:\s\+\(.*\)\s*', '\1', '')
    if search('#\s\+Changed but not updated:', 'nw') > line('.')
      let result = eclim#vcs#impl#git#Git('diff --full-index --cached "' . file . '"')
      let lines = split(result, "\n")[:5]
      call filter(lines, 'v:val =~ "^index \\w\\+\\.\\.\\w\\+"')
      if len(lines)
        let index = substitute(lines[0], 'index \w\+\.\.\(\w\+\)\s.*', '\1', '')

        " kind of hacky but so far only git has a staging area, so return a
        " filename indicating the index blob version of the file which will
        " trigger an autocmd above that will populate the contents.
        let path = fnamemodify(file, ':h')
        let path .= path != '' ? '/' : ''
        return path . 'index_blob_' . index . '_' . fnamemodify(file, ':t')
      endif
    endif
    return file
  elseif line =~ '^#\s*new file:.*'
    return substitute(line, '^#\s*new file:\s\+\(.*\)\s*', '\1', '')
  endif
  return ''
endfunction " }}}

" GetModifiedFiles() {{{
function eclim#vcs#impl#git#GetModifiedFiles()
  let root = eclim#vcs#impl#git#GetRoot()
  let status = eclim#vcs#impl#git#Git('diff --name-status HEAD')
  let files = []
  for file in split(status, "\n")
    if file !~ '^[AM]\s\+'
      continue
    endif
    let file = substitute(file, '^[AM]\s\+', '', '')
    call add(files, root . '/' . file)
  endfor

  let untracked = eclim#vcs#impl#git#Git('ls-files --others --exclude-standard')
  let files += map(split(untracked, "\n"), 'root . "/" . v:val')

  return files
endfunction " }}}

" Info(path) {{{
function eclim#vcs#impl#git#Info(path)
  let result = eclim#vcs#impl#git#Git('log -1 "' . a:path . '"')
  if type(result) == 0
    return
  endif
  call eclim#util#Echo(result)
endfunction " }}}

" Log(path) {{{
function eclim#vcs#impl#git#Log(path)
  let logcmd = 'log --pretty=tformat:"%h|%cn|%cr|%d|%s|"'
  if g:EclimVcsLogMaxEntries > 0
    let logcmd .= ' -' . g:EclimVcsLogMaxEntries
  endif

  let result = eclim#vcs#impl#git#Git(logcmd . ' "' . a:path . '"')
  if type(result) == 0
    return
  endif
  let log = []
  for line in split(result, '\n')
    let values = split(line, '|')
    let refs = split(substitute(values[3], '^\s*(\|)\s*$', '', 'g'), ',\s*')
    call add(log, {
        \ 'revision': values[0],
        \ 'author': values[1],
        \ 'age': values[2],
        \ 'refs': refs,
        \ 'comment': values[4],
     \ })
  endfor
  let root_dir = exists('b:vcs_props') ?
    \ b:vcs_props.root_dir : eclim#vcs#impl#git#GetRoot()
  return {'log': log, 'props': {'root_dir': root_dir}}
endfunction " }}}

" LogDetail(revision) {{{
function eclim#vcs#impl#git#LogDetail(revision)
  let logcmd = 'log -1 --pretty=tformat:"%h|%cn|%cr|%ci|%d|%s|%s%n%n%b|" '
  let result = eclim#vcs#impl#git#Git(logcmd . a:revision)
  if type(result) == 0
    return
  endif
  let values = split(result, '|')
  let refs = split(substitute(values[4], '^\s*(\|)\s*$', '', 'g'), ',\s*')
  return {
      \ 'revision': values[0],
      \ 'author': values[1],
      \ 'age': values[2],
      \ 'date': values[3],
      \ 'refs': refs,
      \ 'comment': values[5],
      \ 'description': values[6],
   \ }
endfunction " }}}

" LogFiles(revision) {{{
function eclim#vcs#impl#git#LogFiles(revision)
  let logcmd = 'log -1 --name-status --pretty=tformat:"" '
  let result = eclim#vcs#impl#git#Git(logcmd . a:revision)
  if type(result) == 0
    return
  endif
  let results = filter(split(result, '\n'), 'v:val !~ "^$"')
  let files = []
  for result in results
    if result =~ '^R'
      let [status, old, new] = split(result, '\t')
      call add(files, {'status': status[0], 'old': old, 'new': new})
    else
      let [status, file] = split(result, '\t')
      call add(files, {'status': status, 'file': file})
    endif
  endfor
  return files
endfunction " }}}

" ViewFileRevision(path, revision) {{{
function! eclim#vcs#impl#git#ViewFileRevision(path, revision)
  let path = a:path

  " kind of a hack to support diffs against git's staging (index) area.
  if path =~ '\<index_blob_[a-z0-9]\{40}_'
    let path = substitute(path, '\<index_blob_[a-z0-9]\{40}_', '', '')
  endif

  let result = eclim#vcs#impl#git#Git('show "' . a:revision . ':' . path . '"')
  return split(result, '\n')
endfunction " }}}

" Git(args) {{{
" Executes 'git' with the supplied args.
function eclim#vcs#impl#git#Git(args)
  return eclim#vcs#util#Vcs('git', '--no-pager ' . a:args)
endfunction " }}}

" s:ReadIndex() {{{
" Used to read a file with the name index_blob_<index hash>_<filename>, for
" use by the git editor diff support.
function! s:ReadIndex()
  if !filereadable(expand('%'))
    let path = eclim#vcs#util#GetRelativePath(expand('%:p'))
    let path = substitute(path, '^/', '', '')
    let root = eclim#vcs#impl#git#GetRoot()
    exec 'lcd ' . escape(root, ' ')
    let index = substitute(path, '.*\<index_blob_\([a-z0-9]\{40}\)_.*', '\1', '')
    let path = substitute(path, '\<index_blob_[a-z0-9]\{40}_', '', '')
    let result = eclim#vcs#impl#git#Git('show ' . index)
    call append(1, split(result, "\n"))
  else
    read %
  endif
  1,1delete _
endfunction " }}}

" vim:ft=vim:fdm=marker
