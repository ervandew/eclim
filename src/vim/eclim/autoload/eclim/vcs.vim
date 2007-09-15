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

" Annotate() {{{
function! eclim#vcs#Annotate ()
  let file = expand('%:p')
  let dir = expand('%:p:h')
  if isdirectory(dir . '/CVS')
    let result = system('cvs annotate "' . file . '"')
  elseif isdirectory(dir . '/.svn')
    let result = system('svn blame "' . file . '"')
  else
    call eclim#util#EchoError('Current file is not under cvs or svn version control.')
    return
  endif

  if v:shell_error
    call eclim#util#EchoError(result)
    return
  endif

  let annotations = split(result, '\n')
  call map(annotations,
      \ "substitute(v:val, '^\\s*\\(.\\{-}\\s\\+.\\{-}\\)\\s\\+.*', '\\1', '')")
  let defined = eclim#display#signs#GetDefined()
  let index = 1
  for annotation in annotations
    let user = substitute(annotation, '^.\{-}\s\+\(.*\)', '\1', '')
    let user_abbrv = user[:1]
    if index(defined, user) == -1
      call eclim#display#signs#Define(user, user_abbrv, g:EclimInfoHighlight)
      call add(defined, user_abbrv)
    endif
    call eclim#display#signs#Place(user, index)
    let index += 1
  endfor
  let b:vcs_annotations = annotations

  augroup vcs_annotate
    autocmd!
    autocmd CursorHold <buffer> call eclim#vcs#AnnotateInfo()
  augroup END
endfunction " }}}

" AnnotateOff() {{{
function! eclim#vcs#AnnotateOff ()
  if exists('b:vcs_annotations')
    let defined = eclim#display#signs#GetDefined()
    for annotation in b:vcs_annotations
      let user = substitute(annotation, '^.\{-}\s\+\(.*\)', '\1', '')
      if index(defined, user) != -1
        let signs = eclim#display#signs#GetExisting(user)
        for sign in signs
          call eclim#display#signs#Unplace(sign.id)
        endfor
        call eclim#display#signs#Undefine(user)
        call remove(defined, index(defined, user))
      endif
    endfor
    unlet b:vcs_annotations
  endif
  augroup vcs_annotate
    autocmd!
  augroup END
endfunction " }}}

" AnnotateInfo() {{{
function! eclim#vcs#AnnotateInfo ()
  if exists('b:vcs_annotations')
    echo b:vcs_annotations[line('.') - 1]
  endif
endfunction " }}}

" GetRevision() {{{
" Gets the current revision of the current file.
function eclim#vcs#GetRevision ()
  let revision = '0'

  let cwd = getcwd()
  let dir = expand('%:p:h')
  exec 'lcd ' . dir
  try
    if isdirectory(dir . '/CVS')
      let status = system('cvs status ' . expand('%:t'))
      let pattern = '.*Working revision:\s*\([0-9.]\+\)\s*.*'
      if status =~ pattern
        let revision = substitute(status, pattern, '\1', '')
      endif
    elseif isdirectory(dir . '/.svn')
      let info = system('svn info ' . expand('%:t'))
      let pattern = '.*Last Changed Rev:\s*\([0-9]\+\)\s*.*'
      if info =~ pattern
        let revision = substitute(info, pattern, '\1', '')
      endif
    endif
  finally
    exec 'lcd ' . cwd
  endtry

  return revision
endfunction " }}}

" GetPreviousRevision() {{{
" Gets the previous revision of the current file.
function eclim#vcs#GetPreviousRevision ()
  let revision = '0'

  let cwd = getcwd()
  let dir = expand('%:p:h')
  exec 'lcd ' . dir
  try
    if isdirectory(dir . '/CVS')
      let log = system('cvs log ' . expand('%:t'))
      let lines = split(log, '\n')
      call filter(lines, 'v:val =~ "^revision [0-9.]\\+\\s*$"')
      if len(lines) >= 2
        let revision = substitute(lines[1], '^revision \([0-9.]\+\)\s*.*', '\1', '')
      endif
    elseif isdirectory(dir . '/.svn')
      let log = system('svn log -q --limit 2 ' . expand('%:t'))
      let lines = split(log, '\n')
      if len(lines) == 5 && lines[1] =~ '^r[0-9]\+' && lines[3] =~ '^r[0-9]\+'
        let revision = substitute(lines[3], '^r\([0-9]\+\)\s.*', '\1', '')
      endif
    endif
  finally
    exec 'lcd ' . cwd
  endtry

  return revision
endfunction " }}}

" GetRevisions() {{{
" Gets a list of revision numbers for the current file.
function eclim#vcs#GetRevisions ()
  let revisions = []

  let cwd = getcwd()
  let dir = expand('%:p:h')
  exec 'lcd ' . dir
  try
    if isdirectory(dir . '/CVS')
      let log = system('cvs log ' . expand('%:t'))
      let lines = split(log, '\n')
      call filter(lines, 'v:val =~ "^revision [0-9.]\\+\\s*$"')
      call map(lines, 'substitute(v:val, "^revision \\([0-9.]\\+\\)\\s*$", "\\1", "")')
      let revisions = lines
    elseif isdirectory(dir . '/.svn')
      let log = system('svn log -q ' . expand('%:t'))
      let lines = split(log, '\n')
      call filter(lines, 'v:val =~ "^r[0-9]\\+\\s.*"')
      call map(lines, 'substitute(v:val, "^r\\([0-9]\\+\\)\\s.*", "\\1", "")')
      let revisions = lines
    endif
  finally
    exec 'lcd ' . cwd
  endtry

  return revisions
endfunction " }}}

" GetViewvcUrl (file) {{{
function eclim#vcs#GetViewvcUrl (file)
  let root = eclim#project#util#GetProjectSetting('org.eclim.project.vcs.viewvc')
  if root == '0'
    return
  endif

  if root == ''
    call eclim#util#EchoWarning(
      \ ":Viewvc requires project setting 'org.eclim.project.vcs.viewvc'.")
    return
  elseif root =~ '/$'
    let root = root[:-2]
  elseif type(root) == 0 && root == 0
    return
  endif

  let project_root = eclim#project#util#GetCurrentProjectRoot()

  let file = a:file
  let dir = file
  if file == ''
    let file = expand('%:t')
    let dir = expand('%:p:h')
  elseif !isdirectory(project_root . '/' . file)
    let dir = fnamemodify(project_root . '/' . file, ':p:h')
    let file = fnamemodify(project_root . '/' . file, ':p:t')
  else
    let dir = fnamemodify(project_root . '/' . file, ':p')
    let file = ''
  endif

  let cmd = winrestcmd()
  if isdirectory(dir . '/CVS')
    silent exec 'sview ' . escape(dir . '/CVS/Repository', ' ')
    setlocal noswapfile
    setlocal bufhidden=delete

    let path = '/' . getline(1)

    silent close
  elseif isdirectory(dir . '/.svn')
    silent exec 'sview ' . escape(dir . '/.svn/entries', ' ')
    setlocal noswapfile
    setlocal bufhidden=delete

    " xml entries format < 1.4
    if getline(1) =~ '<?xml'
      call cursor(1, 1)
      let url = substitute(
        \ getline(search('^\s*url=')), '^\s*url="\(.*\)"', '\1', '')
      let repos = substitute(
        \ getline(search('^\s*repos=')), '^\s*repos="\(.*\)"', '\1', '')

    " entries format >= 1.4
    else
      " can't find official doc on the format, but lines 5 and 6 seem to
      " always have the necessary values.
      let url = getline(5)
      let repos = getline(6)
    endif

    let path = substitute(url, repos, '', '')

    silent close
  else
    call eclim#util#EchoError('Current file is not under cvs or svn version control.')
    return
  endif
  silent exec cmd

  let url = root . path . '/' . file
  echom url
  return url
endfunction " }}}

" Viewvc(file) {{{
" Convert file or directory to viewvc url with the supplied view parameters
" and open the url in the browser.
function eclim#vcs#Viewvc (file, view_args)
  let url = eclim#vcs#GetViewvcUrl(a:file)
  if url == '0'
    return
  endif
  call eclim#web#OpenUrl(url . '?' . a:view_args)
endfunction " }}}

" ViewvcChangeSet(revision) {{{
" View the viewvc revision info for the supplied or current revision of the
" current file.
function eclim#vcs#ViewvcChangeSet (revision)
  let revision = a:revision
  if revision == ''
    let revision = eclim#vcs#GetRevision()
  endif

  call eclim#vcs#Viewvc('', 'view=rev&revision=' . revision)
endfunction " }}}

" ViewvcAnnotate(revision) {{{
" View annotated version of the file in viewvc.
function eclim#vcs#ViewvcAnnotate (revision)
  let revision = a:revision
  if revision == ''
    let revision = eclim#vcs#GetRevision()
  endif

  call eclim#vcs#Viewvc('', 'annotate=' . revision)
endfunction " }}}

" ViewvcDiff(revision1, revision2) {{{
" View diff between two revisions in viewvc.
function eclim#vcs#ViewvcDiff (...)
  let args = a:000
  if len(args) == 1
    let args = split(args[0])
  endif

  if len(args) > 2
    call eclim#util#EchoWarning(":ViewvcDiff accepts at most 2 revision arguments.")
    return
  endif

  let revision1 = len(args) > 0 ? args[0] : ''
  if revision1 == ''
    let revision1 = eclim#vcs#GetRevision()
  endif

  let revision2 = len(args) > 1 ? args[1] : ''
  if revision2 == ''
    let revision2 = len(args) == 1 ?
      \ eclim#vcs#GetRevision() : eclim#vcs#GetPreviousRevision()
    if revision2 == '0'
      call eclim#util#EchoWarning(
        \ "File '" . expand('%') . "' has no previous revision to diff.")
      return
    endif
  endif

  call eclim#vcs#Viewvc('', 'r1=' . revision1 . '&r2=' . revision2)
endfunction " }}}

" CommandCompleteRevision(argLead, cmdLine, cursorPos) {{{
" Custom command completion for revision numbers out of viewvc.
function! eclim#vcs#CommandCompleteRevision (argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseArgs(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]

  let revisions = eclim#vcs#GetRevisions()
  call filter(revisions, 'v:val =~ "^' . argLead . '"')
  return revisions
endfunction " }}}

" vim:ft=vim:fdm=marker
