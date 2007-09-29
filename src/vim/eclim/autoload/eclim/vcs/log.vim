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

" ChangeSet(repos_url, revision) {{{
" Opens a buffer with change set info for the supplied revision.
function! eclim#vcs#log#ChangeSet (repos_url, revision)
  let log = split(system('svn log -vr ' . a:revision . ' ' . a:repos_url), '\n')

  let lines = []
  let entry = {}
  call s:ParseSvnInfo(entry, log[1])
  call add(lines, 'Revision: ' . entry.revision)
  call add(lines, 'Modified: ' . entry.date . ' by ' . entry.author)
  let files = map(log[2:-2], 'substitute(v:val, "\\s*M\\s*\\(.*\\)", "  |M| |\\1|", "")')
  let files = map(files, 'substitute(v:val, "\\s*A\\s*\\(.*\\)", "   A  |\\1|", "")')
  let files = map(files, 'substitute(v:val, "\\s*D\\s*\\(.*\\)", "   D  |\\1|", "")')
  let files = map(files,
    \ 'substitute(v:val, "\\(.*\\)\\( (.*)\\)\\(.*\\)", "\\1\\3\\2", "")')
  call extend(lines, files)

  call s:TempWindow(lines)
  call s:LogSyntax()

  let b:vcs_view = 'changeset'
  let b:vcs_repos_url = a:repos_url

  nnoremap <silent> <buffer> <cr> :call <SID>FollowLink()<cr>
endfunction " }}}

" Log(repos_url, url) {{{
" Opens a buffer with the contents of the log for the supplied url.
function! eclim#vcs#log#Log (repos_url, url)
  if a:repos_url == ''
    call eclim#util#EchoError('Current file is not under svn version control.')
    return
  endif

  let result = system('svn log "' . a:url . '"')
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

  call s:TempWindow(lines)
  call s:LogSyntax()

  let b:vcs_view = 'log'
  let b:vcs_repos_url = a:repos_url
  let b:vcs_url = a:url

  nnoremap <silent> <buffer> <cr> :call <SID>FollowLink()<cr>
endfunction " }}}

" ListDir(repos_url, url) {{{
" Opens a buffer with the directory listing for the supplied url.
function! eclim#vcs#log#ListDir (repos_url, url)
  if a:repos_url == ''
    call eclim#util#EchoError('Current file is not under svn version control.')
    return
  endif
  let listing = split(system('svn list ' . a:url), '\n')
  let dirs = sort(filter(listing[:], 'v:val =~ "/$"'))
  let files = sort(filter(listing[:], 'v:val =~ "[^/]$"'))

  call map(dirs, '"|" . v:val . "|"')
  call map(files, '"|" . v:val . "|"')

  let lines = [s:Breadcrumb(a:repos_url, a:url), '']
  call extend(lines, dirs)
  call extend(lines, files)

  call s:TempWindow(lines)
  call s:LogSyntax()

  let b:vcs_view = 'dir'
  let b:vcs_repos_url = a:repos_url
  let b:vcs_url = a:url

  nnoremap <silent> <buffer> <cr> :call <SID>FollowLink()<cr>
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

  if exists('b:filename')
    call eclim#util#GoToBufferWindow(b:filename)
  endif
  let svn_file = 'svn_' . a:revision . '_' . fnamemodify(a:url, ':t')
  call eclim#util#GoToBufferWindowOrOpen(svn_file, split)

  setlocal noreadonly
  setlocal modifiable
  let saved = @"
  silent 1,$delete
  let @" = saved

  " load in content
  exec 'silent read !svn cat -r ' . a:revision . ' ' . a:url

  silent 1,1delete
  call cursor(1, 1)

  setlocal nomodified
  setlocal readonly
  setlocal nomodifiable
  setlocal noswapfile
endfunction " }}}

" s:Breadcrumb(repos_url, url) {{{
function! s:Breadcrumb (repos_url, url)
  let path = split(substitute(a:url, a:repos_url, '', ''), '/')
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
  " link to folder
  if line('.') == 1
    let path = substitute(
      \ getline('.'), '\(.*|.\{-}\%' . col('.') . 'c.\{-}\)|.*', '\1', '')
    let path = substitute(path, '\(| / |\||\)', '/', 'g')

    call eclim#vcs#log#ListDir(b:vcs_repos_url, b:vcs_repos_url . path)

  " link to file or dir in directory listing view.
  elseif exists('b:vcs_view') && b:vcs_view == 'dir'
    let path = substitute(getline(1), '\(| / |\||\)', '/', 'g')
    let path = substitute(path, ' / ', '', 'g')
    let path .= '/' . link

    if path =~ '/$'
      call eclim#vcs#log#ListDir(b:vcs_repos_url, b:vcs_repos_url . path)
    else
      call eclim#vcs#log#Log(b:vcs_repos_url, b:vcs_repos_url . path)
    endif

  " link to file or dir in change set view.
  elseif exists('b:vcs_view') && b:vcs_view == 'changeset'
    if link == 'M'
      let file = substitute(line, '\s*|M|\s*|\(.\{-}\)|.*', '\1', '')
      let repos_url = b:vcs_repos_url
      let r1 = substitute(getline(1), 'Revision:\s*', '', '')

      let cmd = 'svn log -qr ' . r1 . ':1 --limit 2 ' . repos_url . file
      let info = split(system(cmd), '\n')
      " TODO: error handling for this
      let r2 = substitute(info[-2], '^r\([0-9]\+\).*', '\1', '')

      call eclim#vcs#log#ViewFileRevision(repos_url, repos_url . file, r1, '')
      diffthis
      call eclim#vcs#log#ViewFileRevision(
        \ repos_url, repos_url . file, r2, 'vertical split')
      diffthis
    else
      call eclim#vcs#log#Log(b:vcs_repos_url, b:vcs_repos_url . link)
    endif

  " link to view a change set
  elseif link =~ '^[0-9.]\+$'
    call eclim#vcs#log#ChangeSet(b:vcs_repos_url, link)

  " link to view / annotate a file
  elseif link == 'view' || link == 'annotate'
    let file = substitute(getline(1), '\(| / |\||\)', '/', 'g')
    let file = substitute(file, ' / ', '', 'g')
    let revision = substitute(line, '.\{-}|\?\([0-9.]\+\)|\?.*', '\1', '')
    let repos_url = b:vcs_repos_url
    call eclim#vcs#log#ViewFileRevision(repos_url, repos_url . file, revision, '')

    if link == 'annotate'
      let annotations = eclim#vcs#annotate#GetSvnAnnotations(repos_url . file, revision)
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

    call eclim#vcs#log#ViewFileRevision(repos_url, repos_url . file, r1, '')
    diffthis
    call eclim#vcs#log#ViewFileRevision(repos_url, repos_url . file, r2, 'vertical split')
    diffthis

  " link to diff against working copy
  elseif link == 'working copy'
    let file = substitute(getline(1), '\(| / |\||\)', '/', 'g')
    let file = substitute(file, ' / ', '', 'g')
    let revision = substitute(
      \ getline(line('.') - 2), 'Revision: |\?\([0-9.]\+\)|\?.*', '\1', '')
    let repos_url = b:vcs_repos_url

    let filename = b:filename
    call eclim#vcs#log#ViewFileRevision(
      \ repos_url, repos_url . file, revision, 'vertical split')
    diffthis
    " TODO: added autocommand that turns off diffmode after closing buffer
    call eclim#util#GoToBufferWindow(filename)
    diffthis
  endif
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
  let a:entry['author'] = substitute(a:line, '.\{-}|\s*\(\w\+\)\s*|.*', '\1', '')
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
