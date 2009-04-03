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

if !exists('g:eclim_vcs_web_loaded')
  let g:eclim_vcs_web_loaded = 1
else
  finish
endif

" GetVcsWebFunction(type, func_name) {{{
" Gets a reference to the proper vcs web function.
" Ex. let GetLogUrl = eclim#vcs#web#GetVcsWebFunction('viewvc', 'GetLogUrl')
function eclim#vcs#web#GetVcsWebFunction(type, func_name)
  if a:type == 'viewvc'
    runtime autoload/eclim/vcs/impl/viewvc.vim
  elseif a:type == 'trac'
    runtime autoload/eclim/vcs/impl/trac.vim
  elseif a:type == 'redmine'
    runtime autoload/eclim/vcs/impl/redmine.vim
  elseif a:type == 'hgcgi'
    runtime autoload/eclim/vcs/impl/hgcgi.vim
  elseif a:type == 'hgserve'
    runtime autoload/eclim/vcs/impl/hgserve.vim
  elseif a:type == 'gitweb'
    runtime autoload/eclim/vcs/impl/gitweb.vim
  endif

  try
    return function('eclim#vcs#impl#' . a:type . '#' . a:func_name)
  catch /E700:.*/
    call eclim#util#EchoError('This function is not supported by "' . a:type . '".')
    return
  endtry
endfunction " }}}

" VcsWeb(url_func, ...) {{{
function eclim#vcs#web#VcsWeb(url_func, ...)
  let type = eclim#project#util#GetProjectSetting('org.eclim.project.vcs.web.viewer')
  let root = eclim#project#util#GetProjectSetting('org.eclim.project.vcs.web.url')

  if type(type) == 0 || type(root) == 0 || type == '' || root == ''
    call eclim#util#EchoWarning(
      \ "VcsWeb commands requires the following project settings\n" .
      \ "  org.eclim.project.vcs.web.viewer\n" .
      \ "  org.eclim.project.vcs.web.url")
    return
  endif

  if root =~ '/$'
    let root = root[:-2]
  elseif type(root) == 0 && root == 0
    return
  endif

  let file = expand('%:p')
  let dir = fnamemodify(file, ':h')
  let cwd = getcwd()
  exec 'lcd ' . dir
  try
    let GetVcsWebPath = eclim#vcs#util#GetVcsFunction('GetVcsWebPath')
    if type(GetVcsWebPath) != 2
      return
    endif
    let path = GetVcsWebPath()
  finally
    exec 'lcd ' . cwd
  endtry

  if path == ''
    call eclim#util#EchoError('Current file is not under a supported version control.')
    return
  endif

  let GetUrl = eclim#vcs#web#GetVcsWebFunction(type, a:url_func)
  if type(GetUrl) != 2
    return
  endif
  call eclim#web#OpenUrl(GetUrl(root, path, a:000))
endfunction " }}}

" VcsWebLog() {{{
" View the vcs web log.
function eclim#vcs#web#VcsWebLog()
  call eclim#vcs#web#VcsWeb('GetLogUrl', eclim#vcs#util#GetRevision())
endfunction " }}}

" VcsWebChangeSet(revision) {{{
" View the revision info for the supplied or current revision of the
" current file.
function eclim#vcs#web#VcsWebChangeSet(revision)
  let revision = a:revision
  if revision == ''
    let revision = eclim#vcs#util#GetRevision()
  endif

  call eclim#vcs#web#VcsWeb('GetChangeSetUrl', revision)
endfunction " }}}

" VcsWebAnnotate(revision) {{{
" View annotated version of the file.
function eclim#vcs#web#VcsWebAnnotate(revision)
  let revision = a:revision
  if revision == ''
    let revision = eclim#vcs#util#GetRevision()
  endif

  call eclim#vcs#web#VcsWeb('GetAnnotateUrl', revision)
endfunction " }}}

" VcsWebDiff(revision1, revision2) {{{
" View diff between two revisions.
function eclim#vcs#web#VcsWebDiff(...)
  let args = a:000
  if len(args) == 1
    let args = split(args[0])
  endif

  if len(args) > 2
    call eclim#util#EchoWarning(":VcsWebDiff accepts at most 2 revision arguments.")
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

  call eclim#vcs#web#VcsWeb('GetDiffUrl', revision1, revision2)
endfunction " }}}

" vim:ft=vim:fdm=marker
