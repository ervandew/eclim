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

" ChangeSet(repos_url, url, revision) {{{
" Opens a buffer with change set info for the supplied revision.
function! eclim#vcs#log#ChangeSet (repos_url, url, revision)
  if a:repos_url == ''
    call eclim#util#EchoError('Current file is not under svn version control.')
    return
  endif

  let revision = a:revision
  if revision == ''
    let revision = eclim#vcs#util#GetSvnRevision(a:url)
    if revision == ''
      call eclim#util#Echo('Unable to determine file revision.')
      return
    endif
  endif

  let key = a:url . '_' . revision
  let cached = eclim#cache#Get(key)
  if has_key(cached, 'content')
    let lines = cached.content
  else
    let result = eclim#vcs#util#Svn('log -vr ' . revision . ' "' . a:url . '"')
    if result == '0'
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

    call eclim#cache#Set(key, lines, {'url': a:url, 'revision': revision})
  endif

  call s:TempWindow(lines)
  call s:LogSyntax()
  call s:LogMappings()

  let b:vcs_view = 'changeset'
  let b:vcs_repos_url = a:repos_url

  call s:HistoryPush('eclim#vcs#log#ChangeSet', [a:repos_url, a:url, a:revision])
endfunction " }}}

" Diff(repos_url, url, revision) {{{
" Diffs the current file against the current or supplied revision.
function! eclim#vcs#log#Diff (repos_url, url, revision)
  if a:repos_url == ''
    call eclim#util#EchoError('Current file is not under svn version control.')
    return
  endif

  let revision = a:revision
  if revision == ''
    let revision = eclim#vcs#util#GetSvnRevision(a:url)
    if revision == ''
      call eclim#util#Echo('Unable to determine file revision.')
      return
    endif
  endif

  let filename = expand('%:p')
  let buf1 = bufnr('%')

  call eclim#vcs#log#ViewFileRevision(
    \ a:repos_url, a:url, revision, 'bel vertical split')
  diffthis
  exec bufwinnr(buf1) . 'winc w'
  diffthis

  let b:filename = filename
  augroup vcs_diff
    autocmd! BufUnload <buffer>
    call eclim#util#GoToBufferWindowRegister(b:filename)
    autocmd BufUnload <buffer> diffoff
  augroup END
endfunction " }}}

" Log(repos_url, url) {{{
" Opens a buffer with the contents of the log for the supplied url.
function! eclim#vcs#log#Log (repos_url, url)
  if a:repos_url == ''
    call eclim#util#EchoError('Current file is not under svn version control.')
    return
  endif

  let cached = eclim#cache#Get(a:url, function('eclim#vcs#util#IsCacheValid'))
  if has_key(cached, 'content')
    let lines = cached.content
  else
    let result = eclim#vcs#util#Svn('log "' . a:url . '"')
    if result == '0'
      return
    endif
    let log = s:ParseSvnLog(split(result, '\n'))

    let index = 0
    let lines = [s:Breadcrumb(a:repos_url, a:url), '']
    for entry in log
      let index += 1
      call add(lines, '------------------------------------------')
      call add(lines, 'Revision: |' . entry.revision . '| |view| |annotate|')
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

    call eclim#cache#Set(a:url, lines, {
        \ 'url': a:url,
        \ 'revision': eclim#vcs#util#GetSvnRevision(a:url)
      \ })
  endif

  call s:TempWindow(lines)
  call s:LogSyntax()
  call s:LogMappings()

  let b:vcs_view = 'log'
  let b:vcs_repos_url = a:repos_url
  let b:vcs_url = a:url

  call s:HistoryPush('eclim#vcs#log#Log', [a:repos_url, a:url])
endfunction " }}}

" ListDir(repos_url, url) {{{
" Opens a buffer with the directory listing for the supplied url.
function! eclim#vcs#log#ListDir (repos_url, url)
  if a:repos_url == ''
    call eclim#util#EchoError('Current file is not under svn version control.')
    return
  endif
  let result = eclim#vcs#util#Svn('list "' . a:url . '"')
  if result == '0'
    return
  endif
  let listing = split(result, '\n')
  let dirs = sort(filter(listing[:], 'v:val =~ "/$"'))
  let files = sort(filter(listing[:], 'v:val =~ "[^/]$"'))

  call map(dirs, '"|" . v:val . "|"')
  call map(files, '"|" . v:val . "|"')

  let lines = [s:Breadcrumb(a:repos_url, a:url), '']
  call extend(lines, dirs)
  call extend(lines, files)

  call s:TempWindow(lines)
  call s:LogSyntax()
  call s:LogMappings()

  let b:vcs_view = 'dir'
  let b:vcs_repos_url = a:repos_url
  let b:vcs_url = a:url

  call s:HistoryPush('eclim#vcs#log#ListDir', [a:repos_url, a:url])
endfunction " }}}

" ViewFileRevision(repos_url, url, revision, split) {{{
" Open a read only view for the revision of the supplied url.
function! eclim#vcs#log#ViewFileRevision (repos_url, url, revision, split)
  if a:repos_url == ''
    call eclim#util#EchoError('Current file is not under svn version control.')
    return
  endif

  let split = a:split
  if split == ''
    let split = 'split'
  endif

  let revision = a:revision
  if revision == ''
    let revision = eclim#vcs#util#GetSvnRevision(a:url)
    if revision == ''
      call eclim#util#Echo('Unable to determine file revision.')
      return
    endif
  endif

  if exists('b:filename')
    call eclim#util#GoToBufferWindow(b:filename)
  endif
  let svn_file = 'svn_' . revision . '_' . fnamemodify(a:url, ':t')
  call eclim#util#GoToBufferWindowOrOpen(svn_file, split)

  setlocal noreadonly
  setlocal modifiable
  let saved = @"
  silent 1,$delete
  let @" = saved

  " load in content
  let key = a:url . '_' . revision
  let cached = eclim#cache#Get(key)
  if has_key(cached, 'content')
    let lines = cached.content
    call append(1, lines)
  else
    let result = eclim#vcs#util#Svn('info "' . a:url . '"')
    if result == '0'
      return
    endif
    exec 'silent read !svn cat -r ' . revision . ' "' . a:url . '"'
    let lines = getline(2, line('$'))
    call eclim#cache#Set(key, lines, {'url': a:url, 'revision': revision})
  endif

  silent 1,1delete
  call cursor(1, 1)

  setlocal nomodified
  setlocal readonly
  setlocal nomodifiable
  setlocal noswapfile
endfunction " }}}

" s:Breadcrumb(repos_url, url) {{{
function! s:Breadcrumb (repos_url, url)
  if a:repos_url =~ '^' . a:url . '[/]\?$'
    return '/'
  endif

  let path = split(substitute(a:url, a:repos_url, '', ''), '/')
  call insert(path, fnamemodify(a:repos_url, ':h:t'))
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

  " link to folder
  if line('.') == 1
    let path = substitute(
      \ getline('.'), '\(.*|.\{-}\%' . col('.') . 'c.\{-}\)|.*', '\1', '')
    let path = substitute(path, '\(| / |\||\)', '/', 'g')
    let prefix = fnamemodify(b:vcs_repos_url, ':h:h')

    call eclim#vcs#log#ListDir(b:vcs_repos_url, prefix . path)

  " link to file or dir in directory listing view.
  elseif exists('b:vcs_view') && b:vcs_view == 'dir'
    let line = getline(1)
    let path = ''
    let prefix = b:vcs_repos_url
    if line != '/'
      let path = substitute(getline(1), '\(| / |\||\)', '/', 'g')
      let path = substitute(path, ' / ', '', 'g')
      let prefix = fnamemodify(b:vcs_repos_url, ':h:h')
    endif

    let path .= '/' . link

    if path =~ '/$'
      call eclim#vcs#log#ListDir(b:vcs_repos_url, prefix . path)
    else
      call eclim#vcs#log#Log(b:vcs_repos_url, prefix . path)
    endif

  " link to file or dir in change set view.
  elseif link !~ '^#' && exists('b:vcs_view') && b:vcs_view == 'changeset'
    if link == 'M'
      let file = substitute(line, '\s*|M|\s*|\(.\{-}\)|.*', '\1', '')
      let repos_url = b:vcs_repos_url
      let r1 = substitute(getline(1), 'Revision:\s*', '', '')

      let cmd = 'log -qr ' . r1 . ':1 --limit 2 "' . repos_url . file . '"'
      let result = eclim#vcs#util#Svn(cmd)
      if result == '0'
        return
      endif
      let info = split(result, '\n')
      " TODO: error handling for this
      let r2 = substitute(info[-2], '^r\([0-9]\+\).*', '\1', '')

      call eclim#vcs#log#ViewFileRevision(repos_url, repos_url . file, r1, '')
      let buf1 = bufnr('%')
      call eclim#vcs#log#ViewFileRevision(
        \ repos_url, repos_url . file, r2, 'bel vertical split')
      diffthis
      exec bufwinnr(buf1) . 'winc w'
      diffthis
    else
      call eclim#vcs#log#Log(b:vcs_repos_url, b:vcs_repos_url . link)
    endif

  " link to view a change set
  elseif link =~ '^[0-9.]\+$'
    let file = substitute(getline(1), '\(| / |\||\)', '/', 'g')
    let file = substitute(file, ' / ', '', 'g')
    let repos_url = b:vcs_repos_url
    let prefix = fnamemodify(b:vcs_repos_url, ':h:h')
    call eclim#vcs#log#ChangeSet(repos_url, prefix . file, link)

  " link to view / annotate a file
  elseif link == 'view' || link == 'annotate'
    let file = substitute(getline(1), '\(| / |\||\)', '/', 'g')
    let file = substitute(file, ' / ', '', 'g')
    let revision = substitute(line, '.\{-}|\?\([0-9.]\+\)|\?.*', '\1', '')
    let repos_url = b:vcs_repos_url
    let prefix = fnamemodify(b:vcs_repos_url, ':h:h')
    call eclim#vcs#log#ViewFileRevision(repos_url, prefix . file, revision, '')

    if link == 'annotate'
      let annotations = eclim#vcs#annotate#GetSvnAnnotations(prefix . file, revision)
      call eclim#vcs#annotate#ApplyAnnotations(annotations)
    endif

  " link to diff one version against previous
  elseif link =~ '^previous [0-9.]\+$'
    let file = substitute(getline(1), '\(| / |\||\)', '/', 'g')
    let file = substitute(file, ' / ', '', 'g')
    let r1 = substitute(
      \ getline(line('.') - 2), 'Revision: |\?\([0-9.]\+\)|\?.*', '\1', '')
    let r2 = substitute(link, 'previous \(.*\)', '\1', '')
    let repos_url = b:vcs_repos_url
    let prefix = fnamemodify(b:vcs_repos_url, ':h:h')

    call eclim#vcs#log#ViewFileRevision(repos_url, prefix . file, r1, '')
    let buf1 = bufnr('%')
    call eclim#vcs#log#ViewFileRevision(
      \ repos_url, prefix . file, r2, 'bel vertical split')
    diffthis
    exec bufwinnr(buf1) . 'winc w'
    diffthis

  " link to diff against working copy
  elseif link == 'working copy'
    let file = substitute(getline(1), '\(| / |\||\)', '/', 'g')
    let file = substitute(file, ' / ', '', 'g')
    let revision = substitute(
      \ getline(line('.') - 2), 'Revision: |\?\([0-9.]\+\)|\?.*', '\1', '')
    let repos_url = b:vcs_repos_url
    let prefix = fnamemodify(b:vcs_repos_url, ':h:h')

    let filename = b:filename
    call eclim#vcs#log#ViewFileRevision(
      \ repos_url, prefix . file, revision, 'bel vertical split')
    diffthis

    let b:filename = filename
    augroup vcs_diff
      autocmd! BufUnload <buffer>
      call eclim#util#GoToBufferWindowRegister(b:filename)
      autocmd BufUnload <buffer> diffoff
    augroup END

    call eclim#util#GoToBufferWindow(filename)
    diffthis

  " link to bug / feature report
  elseif link =~ '^#\d\+$'
    let url = eclim#project#util#GetProjectSetting('org.eclim.project.vcs.tracker')
    if url == '0'
      return
    endif

    if url == ''
      call eclim#util#EchoWarning(
        \ "Link to bug report / feature request requires project setting " .
        \ "'org.eclim.project.vcs.tracker'.")
      return
    elseif type(url) == 0 && url == 0
      return
    endif

    let url = substitute(url, '<id>', link[1:], 'g')
    call eclim#web#OpenUrl(url)
  endif
endfunction " }}}

" s:HistoryPop() {{{
function! s:HistoryPop ()
  if exists('w:vcs_history') && len(w:vcs_history) > 1
    call remove(w:vcs_history, -1) " remove current page entry
    exec w:vcs_history[-1]
    call remove(w:vcs_history, -1) " remove entry added by going back
  endif
endfunction " }}}

" s:HistoryPush(command) {{{
function! s:HistoryPush (name, args)
  if !exists('w:vcs_history')
    let w:vcs_history = []
  endif

  let command = 'call ' . a:name . '('
  let index = 0
  for arg in a:args
    if index != 0
      let command .= ', '
    endif
    let command .= '"' . arg . '"'
    let index += 1
  endfor
  let command .= ')'
  call add(w:vcs_history, command)
endfunction " }}}

" s:LogMappings() {{{
function! s:LogMappings ()
  nnoremap <silent> <buffer> <cr> :call <SID>FollowLink()<cr>
  nnoremap <silent> <buffer> <c-o> :call <SID>HistoryPop()<cr>
endfunction " }}}

" s:LogSyntax() {{{
function! s:LogSyntax ()
  set ft=vcs_log
  hi link VcsDivider Constant
  hi link VcsHeader Identifier
  hi link VcsLink Label
  syntax match VcsDivider /^-\+$/
  syntax match VcsLink /|.\{-}|/
  syntax match VcsHeader /^\(Revision\|Modified\|Diff\|Changed paths\):/
endfunction " }}}

" s:ParseSvnInfo(entry, line) {{{
" Parse the svn info line of the log.
function! s:ParseSvnInfo (entry, line)
  let a:entry['revision'] = substitute(a:line, '^r\(\w\+\).*', '\1', '')
  let a:entry['author'] = substitute(a:line, '.\{-}|\s*\(\S\+\)\s*|.*', '\1', '')
  let a:entry['date'] = substitute(a:line, '.\{-}|.\{-}|\s*\(.\{-}\)\s\+[+-].\{-}|.*', '\1', '')
endfunction " }}}

" s:ParseSvnLog(lines) {{{
" Parse the svn log.
function! s:ParseSvnLog (lines)
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
      let line = substitute(line, '\(#\d\+\)', '|\1|', 'g')
      call add(entry.comment, line)
    endif
  endfor
  return log
endfunction " }}}

" s:TempWindow (lines) {{{
function! s:TempWindow (lines)
  let filename = expand('%:p')
  if expand('%') == '[vcs_log]' && exists('b:filename')
    let filename = b:filename
  endif

  call eclim#util#TempWindow('[vcs_log]', a:lines)

  let b:filename = filename
  augroup eclim_temp_window
    autocmd! BufUnload <buffer>
    call eclim#util#GoToBufferWindowRegister(b:filename)
  augroup END
endfunction " }}}

" vim:ft=vim:fdm=marker
