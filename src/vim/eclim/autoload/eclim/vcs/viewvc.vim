" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/common/vcs.html
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

" GetViewvcUrl (file) {{{
function eclim#vcs#viewvc#GetViewvcUrl (file)
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
function eclim#vcs#viewvc#Viewvc (file, view_args)
  let url = eclim#vcs#viewvc#GetViewvcUrl(a:file)
  if url == '0'
    return
  endif
  call eclim#web#OpenUrl(url . '?' . a:view_args)
endfunction " }}}

" ViewvcChangeSet(revision) {{{
" View the viewvc revision info for the supplied or current revision of the
" current file.
function eclim#vcs#viewvc#ViewvcChangeSet (revision)
  let revision = a:revision
  if revision == ''
    let revision = eclim#vcs#util#GetRevision()
  endif

  call eclim#vcs#viewvc#Viewvc('', 'view=rev&revision=' . revision)
endfunction " }}}

" ViewvcAnnotate(revision) {{{
" View annotated version of the file in viewvc.
function eclim#vcs#viewvc#ViewvcAnnotate (revision)
  let revision = a:revision
  if revision == ''
    let revision = eclim#vcs#util#GetRevision()
  endif

  call eclim#vcs#viewvc#Viewvc('', 'annotate=' . revision)
endfunction " }}}

" ViewvcDiff(revision1, revision2) {{{
" View diff between two revisions in viewvc.
function eclim#vcs#viewvc#ViewvcDiff (...)
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
    let revision1 = eclim#vcs#util#GetRevision()
  endif

  let revision2 = len(args) > 1 ? args[1] : ''
  if revision2 == ''
    let revision2 = len(args) == 1 ?
      \ eclim#vcs#util#GetRevision() : eclim#vcs#util#GetPreviousRevision()
    if revision2 == '0'
      call eclim#util#EchoWarning(
        \ "File '" . expand('%') . "' has no previous revision to diff.")
      return
    endif
  endif

  call eclim#vcs#viewvc#Viewvc('', 'r1=' . revision1 . '&r2=' . revision2)
endfunction " }}}

" vim:ft=vim:fdm=marker
