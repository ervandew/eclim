" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/common/vcs.html
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

" Script Variables {{{
let s:vcs_viewers = {
    \ 'viewvc': 'http://${host}/viewvc/${path}',
    \ 'trac': 'http://${host}/${path}',
    \ 'redmine': 'http://${host}/repositories/<cmd>/${path}',
    \ 'hgcgi': 'http://${host}/${path}',
    \ 'hgserve': 'http://${host}/${path}',
    \ 'gitweb': 'http://${host}/git/gitweb.cgi?p=${path}',
    \ 'github': 'http://github.com/${username}/${project}',
    \ 'bitbucket': 'http://bitbucket.org/${username}/${project}',
    \ 'googlecode': 'http://code.google.com/p/${project}',
  \ }

let s:vcs_viewer_saved = {}
" }}}

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
  let vcs = eclim#vcs#util#GetVcsType()
  if vcs == ''
    return
  endif

  let type = eclim#project#util#GetProjectSetting('org.eclim.project.vcs.web.viewer')
  let root = eclim#project#util#GetProjectSetting('org.eclim.project.vcs.web.url')

  if type(type) == 0 || type(root) == 0 || type == '' || root == ''
    let type = get(s:vcs_viewer_saved, 'type', '')
    let root = get(s:vcs_viewer_saved, 'root', '')
    let prompt = 1

    if root == ''
      let response = eclim#util#PromptConfirm(
        \ "VcsWeb commands requires the following project settings\n" .
        \ "  org.eclim.project.vcs.web.viewer\n" .
        \ "  org.eclim.project.vcs.web.url\n\n" .
        \ "Would you like to enter these values?", g:EclimInfoHighlight)
      if response != 1
        return
      endif
    else
      let response = eclim#util#PromptConfirm(
        \ "Using values\n" .
        \ "  viewer: " . type . "\n" .
        \ "     url: " . root . "\n" .
        \ "Continue using these values?", g:EclimInfoHighlight)
      let prompt = response != 1
    endif

    if prompt
      " TODO: maybe filter types by the vcs
      let types = sort(keys(s:vcs_viewers))
      let response = eclim#util#PromptList(
        \ 'Choose the appropriate web viewer', types, g:EclimInfoHighlight)
      if response < 0
        return
      endif

      let type = types[response]
      let root = s:vcs_viewers[type]
      let vars = split(substitute(root, '.\{-}\(\${\w\+}\).\{-}\|.*', '\1 ', 'g'))
      exec "echohl " . g:EclimInfoHighlight
      try
        for var in vars
          redraw
          echo "Building url: " . root . "\n"
          let varname = substitute(var, '\${\|}', '', 'g')
          let response = input("Please enter the " . varname . ": ")
          if response == ''
            return
          endif
          let root = substitute(root, var, response, '')
        endfor
      finally
        echohl None
      endtry

      let s:vcs_viewer_saved = {'type': type, 'root': root}

      if eclim#project#util#IsCurrentFileInProject(0)
        let response = eclim#util#PromptConfirm(
          \ "  org.eclim.project.vcs.web.viewer=" . type . "\n" .
          \ "  org.eclim.project.vcs.web.url=" . root . "\n\n" .
          \ "Would you like to persist these values?", g:EclimInfoHighlight)
        if response > 0
          call eclim#project#util#SetProjectSetting(
            \ 'org.eclim.project.vcs.web.viewer', type)
          call eclim#project#util#SetProjectSetting(
            \ 'org.eclim.project.vcs.web.url', root)
        endif
      endif

    endif
  endif

  if root =~ '/$'
    let root = root[:-2]
  elseif type(root) == 0 && root == 0
    return
  endif

  let file = expand('%:p')
  let dir = fnamemodify(file, ':h')
  let cwd = getcwd()
  exec 'lcd ' . escape(dir, ' ')
  try
    let GetVcsWebPath = eclim#vcs#util#GetVcsFunction('GetVcsWebPath')
    if type(GetVcsWebPath) != 2
      return
    endif
    let path = GetVcsWebPath()
  finally
    exec 'lcd ' . escape(cwd, ' ')
  endtry

  if path == ''
    call eclim#util#EchoError('Current file is not under a supported version control.')
    return
  endif

  let GetUrl = eclim#vcs#web#GetVcsWebFunction(type, a:url_func)
  if type(GetUrl) != 2
    return
  endif

  let url = GetUrl(root, path, a:000)
  if url == '0'
    return
  endif

  call eclim#web#OpenUrl(url, 1)
endfunction " }}}

" VcsWebLog(revision) {{{
" View the vcs web log.
function eclim#vcs#web#VcsWebLog(revision)
  let revision = a:revision != '' ? a:revision : eclim#vcs#util#GetRevision()
  call eclim#vcs#web#VcsWeb('GetLogUrl', revision)
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
