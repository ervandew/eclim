" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"
" License:
"
" Copyright (c) 2005 - 2008
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

" GetUrl() {{{
" Gets the base url to trac or 0 if not set or eclim is not running.
" not running.
function! eclim#project#trac#GetUrl ()
  let url = eclim#project#util#GetProjectSetting('org.eclim.project.trac')
  if url == '0'
    return
  endif
  if url == ''
    call eclim#util#EchoWarning(
      \ "Trac commands require project setting 'org.eclim.project.trac'.")
    return
  elseif url =~ '/$'
    let url = url[:-2]
  elseif type(url) == 0 && url == 0
    return
  endif

  return url
endfunction " }}}

" GetFilePath(file) {{{
" Gets the vcs root relative path of the supplied file.
function eclim#project#trac#GetFilePath (file)
  let file = a:file != '' ? a:file : expand('%')
  let dir = fnamemodify(file, ':h')
  let cwd = getcwd()
  exec 'lcd ' . dir
  try
    let GetViewvcPath = eclim#vcs#util#GetVcsFunction('GetViewvcPath')
    if type(GetViewvcPath) != 2
      return
    endif
    let path = GetViewvcPath()
  finally
    exec 'lcd ' . cwd
  endtry

  if path == ''
    call eclim#util#EchoError('Current file is not under cvs or svn version control.')
    return
  endif
  return path
endfunction " }}}

" Log(file) {{{
function eclim#project#trac#Log (file)
  let url = eclim#project#trac#GetUrl()
  let path = eclim#project#trac#GetFilePath(a:file)
  if (type(url) == 0 && url == 0) || (type(path) == 0 && path == 0)
    return
  endif

  call eclim#web#OpenUrl(url . '/log' . path . '?verbose=on')
endfunction " }}}

" ChangeSet(revision) {{{
function eclim#project#trac#ChangeSet (revision)
  let url = eclim#project#trac#GetUrl()
  if type(url) == 0 && url == 0
    return
  endif

  let revision = a:revision
  if revision == ''
    let revision = eclim#vcs#util#GetRevision()
  endif

  call eclim#web#OpenUrl(url . '/changeset/' . revision)
endfunction " }}}

" Diff(revision1, revision2) {{{
function eclim#project#trac#Diff (...)
  let url = eclim#project#trac#GetUrl()
  let path = eclim#project#trac#GetFilePath('')
  if (type(url) == 0 && url == 0) || (type(path) == 0 && path == 0)
    return
  endif

  let args = a:000
  if len(args) == 1
    let args = split(args[0])
  endif

  if len(args) > 2
    call eclim#util#EchoWarning(":TracDiff accepts at most 2 revision arguments.")
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

  let urlpath = substitute(path, '/', '%2F', 'g')
  let r1 = urlpath . '%40' . revision1
  let r2 = urlpath . '%40' . revision2
  call eclim#web#OpenUrl(url . printf('/changeset?new=%s&old=%s', r1, r2))
endfunction " }}}

" Annotate(revision) {{{
" View annotated version of the file.
function eclim#project#trac#Annotate (revision)
  let url = eclim#project#trac#GetUrl()
  let path = eclim#project#trac#GetFilePath('')
  if (type(url) == 0 && url == 0) || (type(path) == 0 && path == 0)
    return
  endif

  let revision = a:revision
  if revision == ''
    let revision = eclim#vcs#util#GetRevision()
  endif

  call eclim#web#OpenUrl(url . path . '?annotate=1&rev=' . revision)
endfunction " }}}

" vim:ft=vim:fdm=marker
