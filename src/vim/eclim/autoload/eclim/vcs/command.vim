" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Functions for working with version control systems.
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

runtime autoload/eclim/vcs/util.vim

" Global Variables {{{
  if !exists('g:EclimVcsLogMaxEntries')
    let g:EclimVcsLogMaxEntries = 0
  endif

  if !exists('g:EclimVcsDiffOrientation')
    let g:EclimVcsDiffOrientation = 'vertical'
  endif

  if !exists('g:EclimVcsTrackerIdPatterns')
    let g:EclimVcsTrackerIdPatterns = ['#\(\d\+\)']
  endif
" }}}

" Script Variables {{{
  let eclim#vcs#command#EclimVcsTrackerIdPatterns = g:EclimVcsTrackerIdPatterns
  let s:trackerIdPattern = join(eclim#vcs#command#EclimVcsTrackerIdPatterns, '\|')
" }}}

" Annotate([revision]) {{{
function! eclim#vcs#command#Annotate(...)
  if exists('b:vcs_annotations')
    call s:AnnotateOff()
    return
  endif

  let path = exists('b:vcs_props') ? b:vcs_props.path :
    \ eclim#vcs#util#GetRelativePath(expand('%:p'))
  let revision = len(a:000) > 0 ? a:000[0] : ''

  " let the vcs annotate the current working version so that the results line
  " up with the contents (assuming the underlying vcs supports it).
  "if revision == ''
  "  let revision = eclim#vcs#util#GetRevision()
  "endif

  let cwd = eclim#vcs#util#LcdRoot()
  try
    let Annotate = eclim#vcs#util#GetVcsFunction('GetAnnotations')
    if type(Annotate) != 2
      call eclim#util#EchoError(
        \ 'Current file is not under hg or git version control.')
      return
    endif
    let annotations = Annotate(path, revision)
  finally
    exec 'lcd ' . cwd
  endtry

  call s:ApplyAnnotations(annotations)
endfunction " }}}

" Diff(revision) {{{
" Diffs the current file against the current or supplied revision.
function! eclim#vcs#command#Diff(revision)
  let path = expand('%:p')
  let relpath = eclim#vcs#util#GetRelativePath(expand('%:p'))
  let revision = a:revision
  if revision == ''
    let revision = eclim#vcs#util#GetRevision(relpath)
    if revision == ''
      call eclim#util#Echo('Unable to determine file revision.')
      return
    endif
  elseif revision == 'prev'
    let revision = eclim#vcs#util#GetPreviousRevision(relpath)
  endif

  let filename = expand('%:p')
  let buf1 = bufnr('%')

  let orien = g:EclimVcsDiffOrientation == 'horizontal' ? '' : 'vertical'
  call eclim#vcs#command#ViewFileRevision(path, revision, 'bel ' . orien . ' split')
  diffthis

  let b:filename = filename
  let b:vcs_diff_temp = 1
  augroup vcs_diff
    autocmd! BufWinLeave <buffer>
    call eclim#util#GoToBufferWindowRegister(b:filename)
    autocmd BufWinLeave <buffer> diffoff
  augroup END

  call eclim#util#GoToBufferWindow(buf1)
  diffthis
endfunction " }}}

" Info() {{{
" Retrieves and echos info on the current file.
function! eclim#vcs#command#Info()
  let path = eclim#vcs#util#GetRelativePath(expand('%:p'))
  let cwd = eclim#vcs#util#LcdRoot()
  try
    let Info = eclim#vcs#util#GetVcsFunction('Info')
    if type(Info) == 2
      call Info(path)
    endif
  finally
    exec 'lcd ' . cwd
  endtry
endfunction " }}}

" Log(path) {{{
" Opens a buffer with the contents of the log for the supplied url.
function! eclim#vcs#command#Log(path)
  if a:path == ''
    call eclim#util#EchoError('File is not under version control.')
    return
  endif

  let cwd = eclim#vcs#util#LcdRoot(a:path)
  let path = eclim#vcs#util#GetRelativePath(a:path)
  try
    let Log = eclim#vcs#util#GetVcsFunction('Log')
    if type(Log) != 2
      return
    endif
    let info = Log(path)
  finally
    exec 'lcd ' . cwd
  endtry
  let info.props = has_key(info, 'props') ? info.props : {}
  let info.props.path = path

  " if annotations are on, jump to the revision for the current line
  let jumpto = ''
  if exists('b:vcs_annotations') && len(b:vcs_annotations) >= line('.')
    let jumpto = split(b:vcs_annotations[line('.') - 1])[0]
  endif

  let content = [path, '']
  for entry in info.log
    call add(content, s:LogLine(entry))
  endfor

  if g:EclimVcsLogMaxEntries > 0 && len(info.log) == g:EclimVcsLogMaxEntries
    call add(content, '------------------------------------------')
    call add(content, 'Note: entries limited to ' . g:EclimVcsLogMaxEntries . '.')
    call add(content, '      let g:EclimVcsLogMaxEntries = ' . g:EclimVcsLogMaxEntries)
  endif

  call s:TempWindow(info.props, content)
  call s:LogSyntax()
  call s:LogMappings()

  " continuation of annotation support
  if jumpto != ''
    " in the case of git, the annotate hash is longer than the log hash, so
    " perform a little extra work to line them up.
    let line = search('^[+-] \w\+', 'n')
    if line != -1
      let hash = substitute(getline(line), '^[+-] \(\w\+\) .*', '\1', '')
      let jumpto = jumpto[:len(hash)-1]
    endif

    call search('^[+-] ' . jumpto)
    normal! z
  endif
endfunction " }}}

" ViewFileRevision(path, revision, open_cmd) {{{
" Open a read only view for the revision of the supplied version file.
function! eclim#vcs#command#ViewFileRevision(path, revision, open_cmd)
  let path = eclim#vcs#util#GetRelativePath(a:path)
  let revision = a:revision
  if revision == ''
    let revision = eclim#vcs#util#GetRevision(path)
    if revision == ''
      call eclim#util#Echo('Unable to determine file revision.')
      return
    endif
  elseif revision == 'prev'
    let revision = eclim#vcs#util#GetPreviousRevision(path)
  endif

  let props = exists('b:vcs_props') ? b:vcs_props : {}

  if exists('b:filename')
    call eclim#util#GoToBufferWindow(b:filename)
  endif
  let vcs_file = 'vcs_' . revision . '_' . fnamemodify(path, ':t')

  let cwd = eclim#vcs#util#LcdRoot()
  let orig_buf = bufnr('%')
  let g:EclimTemplateTempIgnore = 1
  try
    " load in content
    let ViewFileRevision = eclim#vcs#util#GetVcsFunction('ViewFileRevision')
    if type(ViewFileRevision) != 2
      return
    endif
    let lines = ViewFileRevision(path, revision)
  finally
    " switch back to the original cwd for both the original + new buffer.
    let cur_buf = bufnr('%')
    call eclim#util#GoToBufferWindow(orig_buf)
    exec 'lcd ' . cwd
    call eclim#util#GoToBufferWindow(cur_buf)
    exec 'lcd ' . cwd

    unlet g:EclimTemplateTempIgnore
  endtry

  let open_cmd = a:open_cmd != '' ? a:open_cmd : 'split'
  if has('win32') || has('win64')
    let vcs_file = substitute(vcs_file, ':', '_', 'g')
  endif
  call eclim#util#GoToBufferWindowOrOpen(vcs_file, open_cmd)

  setlocal noreadonly
  setlocal modifiable
  silent 1,$delete _
  call append(1, lines)
  silent 1,1delete
  call cursor(1, 1)
  setlocal nomodified
  setlocal readonly
  setlocal nomodifiable
  setlocal noswapfile
  setlocal nobuflisted
  setlocal buftype=nofile
  setlocal bufhidden=delete
  doautocmd BufReadPost

  let b:vcs_props = copy(props)
endfunction " }}}

" s:ApplyAnnotations(annotations) {{{
function! s:ApplyAnnotations(annotations)
  let existing = {}
  let existing_annotations = {}
  for exists in eclim#display#signs#GetExisting()
    if exists.name !~ '^\(vcs_annotate_\|placeholder\)'
      let existing[exists.line] = exists
    else
      let existing_annotations[exists.line] = exists
    endif
  endfor

  let defined = eclim#display#signs#GetDefined()
  let index = 1
  for annotation in a:annotations
    if annotation == 'uncommitted'
      if has_key(existing_annotations, index)
        call eclim#display#signs#Unplace(existing_annotations[index].id)
      endif
      let index += 1
      continue
    endif

    if has_key(existing, index)
      let index += 1
      continue
    endif

    let user = substitute(annotation, '^.\{-})\s\+\(.\{-}\)\s*$', '\1', '')
    let user_abbrv = user[:1]
    let name_parts = split(user)
    " if the user name appears to be in the form of First Last, then try using
    " using the first letter of each as initials
    if len(name_parts) > 1 && name_parts[0] =~ '^\w' && name_parts[1] =~ '^\w'
      let user_abbrv = name_parts[0][0] . name_parts[1][0]
    endif
    let sign_name = 'vcs_annotate_' . substitute(user[:5], ' ', '_', 'g')
    if index(defined, sign_name) == -1
      call eclim#display#signs#Define(sign_name, user_abbrv, g:EclimInfoHighlight)
      call add(defined, sign_name)
    endif
    call eclim#display#signs#Place(sign_name, index)
    let index += 1
  endfor

  let b:vcs_annotations = a:annotations
  call s:AnnotateInfo()

  augroup vcs_annotate
    autocmd!
    autocmd CursorMoved <buffer> call <SID>AnnotateInfo()
    autocmd BufWritePost <buffer>
      \ if !eclim#util#WillWrittenBufferClose() |
      \   if exists('b:vcs_annotations') |
      \     unlet b:vcs_annotations |
      \   endif |
      \   call eclim#vcs#command#Annotate() |
      \ endif
  augroup END
endfunction " }}}

" s:AnnotateInfo() {{{
function! s:AnnotateInfo()
  if mode() != 'n'
    return
  endif

  if exists('b:vcs_annotations') && len(b:vcs_annotations) >= line('.')
    call eclim#util#WideMessage('echo', b:vcs_annotations[line('.') - 1])
  endif
endfunction " }}}

" s:AnnotateOff() {{{
function! s:AnnotateOff()
  if exists('b:vcs_annotations')
    let defined = eclim#display#signs#GetDefined()
    for annotation in b:vcs_annotations
      let user = substitute(annotation, '^.\{-})\s\+\(.\{-}\)\s*$', '\1', '')
      let sign_name = 'vcs_annotate_' . substitute(user[:5], ' ', '_', 'g')
      if index(defined, sign_name) != -1
        let signs = eclim#display#signs#GetExisting(sign_name)
        for sign in signs
          call eclim#display#signs#Unplace(sign.id)
        endfor
        call eclim#display#signs#Undefine(sign_name)
        call remove(defined, index(defined, sign_name))
      endif
    endfor
    unlet b:vcs_annotations
  endif
  augroup vcs_annotate
    autocmd!
  augroup END
endfunction " }}}

" s:Action() {{{
function! s:Action()
  try
    let line = getline('.')

    if line =~ '^\s\+[+-] files$'
      call s:ToggleFiles()
      return
    endif

    if line =~ '^[+-] \w\+'
      call s:ToggleDetail()
      return
    endif

    let link = substitute(
      \ getline('.'), '.*|\(.\{-}\%' . col('.') . 'c.\{-}\)|.*', '\1', '')
    if link == line
      return
    endif

    " link to view / annotate a file
    if link == 'view' || link == 'annotate'
      let file = s:GetFilePath()
      let revision = s:GetRevision()

      call eclim#vcs#command#ViewFileRevision(file, revision, '')
      if link == 'annotate'
        call eclim#vcs#command#Annotate(revision)
      endif

    " link to diff one version against previous
    elseif link =~ '^diff '
      let file = s:GetFilePath()
      let revision = s:GetRevision()
      let orien = g:EclimVcsDiffOrientation == 'horizontal' ? '' : 'vertical'

      if link =~ 'previous'
        let previous = s:GetPreviousRevision()
        if previous != ''
          call eclim#vcs#command#ViewFileRevision(file, revision, '')
          let buf1 = bufnr('%')
          call eclim#vcs#command#ViewFileRevision(file, previous, 'bel ' . orien . ' split')
          diffthis
          call eclim#util#GoToBufferWindow(buf1)
          diffthis
        endif
      else
        let filename = b:filename
        call eclim#vcs#command#ViewFileRevision(file, revision, 'bel ' . orien . ' split')
        diffthis

        let b:filename = filename
        augroup vcs_diff
          autocmd! BufWinLeave <buffer>
          call eclim#util#GoToBufferWindowRegister(b:filename)
          autocmd BufWinLeave <buffer> diffoff
        augroup END

        call eclim#util#GoToBufferWindow(filename)
        diffthis
      endif

    " link to bug / feature report
    elseif link =~ '^' . s:trackerIdPattern . '$'
      let cwd = getcwd()
      let dir = fnamemodify(b:filename, ':h')
      exec 'lcd ' . escape(dir, ' ')
      try
        let url = eclim#project#util#GetProjectSetting('org.eclim.project.tracker')
      finally
        exec 'lcd ' . escape(cwd, ' ')
      endtry

      if type(url) == 0
        return
      endif

      if url == ''
        call eclim#util#EchoWarning(
          \ "Link to bug report / feature request requires project setting " .
          \ "'org.eclim.project.tracker'.")
        return
      endif

      for pattern in g:EclimVcsTrackerIdPatterns
        if link =~ pattern
          let id = substitute(link, pattern, '\1', '')
          break
        endif
      endfor
      let url = substitute(url, '<id>', id, 'g')
      call eclim#web#OpenUrl(url)

    " added file
    elseif link == 'A'
      let file = substitute(line, '.*|A|\s*', '', '')
      let revision = s:GetRevision()
      call eclim#vcs#command#ViewFileRevision(file, revision, '')

    " modified or renamed file
    elseif link == 'M' || link == 'R'
      let revision = s:GetRevision()
      if link == 'M'
        let file = substitute(line, '.*|M|\s*', '', '')
        let old = file
        let previous = eclim#vcs#util#GetPreviousRevision(file, revision)
      else
        let file = substitute(line, '.*|R|.*->\s*', '', '')
        let old = substitute(line, '.*|R|\s*\(.*\)\s->.*', '\1', '')
        let previous = eclim#vcs#util#GetPreviousRevision(old)
      endif
      call eclim#vcs#command#ViewFileRevision(file, revision, '')
      let buf1 = bufnr('%')
      let orien = g:EclimVcsDiffOrientation == 'horizontal' ? '' : 'vertical'
      call eclim#vcs#command#ViewFileRevision(old, previous, 'bel ' . orien . ' split')
      diffthis
      call eclim#util#GoToBufferWindow(buf1)
      diffthis

    " deleted file
    elseif link == 'D'
      let file = substitute(line, '.*|D|\s*', '', '')
      let revision = s:GetRevision()
      let previous = eclim#vcs#util#GetPreviousRevision(file, revision)
      call eclim#vcs#command#ViewFileRevision(file, previous, '')

    endif
  catch /vcs error/
    " the error message is printed by eclim#vcs#util#Vcs
  endtry
endfunction " }}}

" s:LogLine(entry) {{{
function! s:LogLine(entry)
  let entry = a:entry
  let refs = ''
  if len(entry.refs)
    let refs = '(' . join(entry.refs, ', ') . ') '
  endif
  return printf('+ %s %s%s (%s) %s',
    \ entry.revision, refs, entry.author, entry.age, entry.comment)
endfunction " }}}

" s:ToggleDetail() {{{
function! s:ToggleDetail()
  let line = getline('.')
  let lnum = line('.')
  let revision = s:GetRevision()
  let log = s:LogDetail(revision)

  setlocal modifiable noreadonly
  if line =~ '^+'
    let open = substitute(line, '+ \(.\{-})\).*', '- \1 ' . log.date, '')
    call setline(lnum, open)
    let lines = []
    if lnum == line('$')
      call add(lines, "\t|view| |annotate| |diff working copy|")
    else
      call add(lines, "\t|view| |annotate| |diff working copy| |diff previous|")
    endif
    let desc = substitute(log.description, '\_s*$', '', '')
    let desc = substitute(desc, '\('. s:trackerIdPattern . '\)', '|\1|', 'g')
    let lines += map(split(desc, "\n"), '(v:val != "" ? "\t" : "") . v:val')
    call add(lines, '')
    call add(lines, "\t+ files")
    call append(lnum, lines)
    retab
  else
    let pos = getpos('.')
    call setline(lnum, s:LogLine(log))
    let end = search('^[+-] \w\+', 'nW') - 1
    if end == -1
      let end = line('$')
    endif
    exec lnum + 1 . ',' . end . 'delete _'
    call setpos('.', pos)
  endif
  setlocal nomodifiable readonly
endfunction " }}}

" s:ToggleFiles() {{{
function! s:ToggleFiles()
  let line = getline('.')
  let lnum = line('.')
  let revision = s:GetRevision()

  setlocal modifiable noreadonly
  if line =~ '^\s\++'
    let open = substitute(line, '+', '-', '')
    call setline(lnum, open)
    let files = s:LogFiles(revision)
    let lines = []
    for file in files
      if file.status == 'R'
        call add(lines, "\t\t|" . file.status . "| " . file.old . ' -> ' . file.new)
      else
        call add(lines, "\t\t|" . file.status . "| " . file.file)
      endif
    endfor
    call append(lnum, lines)
    retab
  else
    let pos = getpos('.')
    let close = substitute(line, '-', '+', '')
    call setline(lnum, close)
    let start = lnum + 1
    let end = search('^[+-] \w\+', 'nW') - 1
    if end == -1
      let end = line('$')
    endif
    if end < start
      let end = start
    endif
    exec start . ',' . end . 'delete _'
    call setpos('.', pos)
  endif
  setlocal nomodifiable readonly
endfunction " }}}

" s:GetFilePath() {{{
function! s:GetFilePath()
  return getline(1)
endfunction " }}}

" s:GetRevision() {{{
function! s:GetRevision()
  let lnum = search('^[+-] \w\+', 'bcnW')
  return substitute(getline(lnum), '[+-] \(\w\+\) .*', '\1', '')
endfunction " }}}

" s:GetPreviousRevision() {{{
function! s:GetPreviousRevision()
  let lnum = search('^[+-] \w\+', 'nW')
  if lnum == 0
    call eclim#util#EchoWarning('Could not find the previous revision number')
    return ''
  endif
  return substitute(getline(lnum), '[+-] \(\w\+\) .*', '\1', '')
endfunction " }}}

" s:LogDetail(revision) {{{
function! s:LogDetail(revision)
  let LogDetail = eclim#vcs#util#GetVcsFunction('LogDetail')
  if type(LogDetail) != 2
    return
  endif
  return LogDetail(a:revision)
endfunction " }}}

" s:LogFiles(revision) {{{
function! s:LogFiles(revision)
  let LogFiles = eclim#vcs#util#GetVcsFunction('LogFiles')
  if type(LogFiles) != 2
    return
  endif
  return LogFiles(a:revision)
endfunction " }}}

" s:LogMappings() {{{
function! s:LogMappings()
  nnoremap <silent> <buffer> <cr> :call <SID>Action()<cr>
endfunction " }}}

" s:LogSyntax() {{{
function! s:LogSyntax()
  set ft=vcs_log
  hi link VcsRevision Identifier
  hi link VcsRefs Tag
  hi link VcsDate String
  hi link VcsLink Label
  hi link VcsFiles Comment
  syntax match VcsRevision /\(^[+-] \)\@<=\w\+/
  syntax match VcsRefs /\(^[+-] \w\+ \)\@<=(.\{-})/
  syntax match VcsDate /\(^[+-] \w\+ \((.\{-}) \)\?\w.\{-}\)\@<=(\d.\{-})/
  syntax match VcsLink /|\S.\{-}|/
  let indent = eclim#util#GetIndent(1)
  exec 'syntax match VcsFiles /\(^' . indent . '[+-] \)\@<=files$/'
endfunction " }}}

" s:TempWindow(props, lines) {{{
function! s:TempWindow(props, lines)
  let filename = expand('%:p')
  if expand('%') == '[vcs_log]' && exists('b:filename')
    let filename = b:filename
  endif

  call eclim#util#TempWindow('[vcs_log]', a:lines)

  let b:filename = filename
  let b:vcs_props = a:props
  exec 'lcd ' . escape(a:props.root_dir, ' ')

  augroup eclim_temp_window
    autocmd! BufWinLeave <buffer>
    call eclim#util#GoToBufferWindowRegister(b:filename)
  augroup END
endfunction " }}}

" vim:ft=vim:fdm=marker
