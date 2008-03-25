" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/common/vcs.html
"
" License:
"
" Copyright (C) 2005 - 2008  Eric Van Dewoestine
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

  let file = a:file != '' ? a:file : expand('%:p')
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

  let url = root . path
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
