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

if !exists('g:eclim_vcs_util_loaded')
  let g:eclim_vcs_util_loaded = 1
else
  finish
endif

" GetVcsType() {{{
function eclim#vcs#util#GetVcsType()
  let type = ''
  if isdirectory('CVS')
    runtime autoload/eclim/vcs/impl/cvs.vim
    let type = 'cvs'
  elseif isdirectory('.svn') ||
       \ (exists('b:vcs_props') && has_key(b:vcs_props, 'svn_root_url'))
    runtime autoload/eclim/vcs/impl/svn.vim
    let type = 'svn'
  else
    let cwd = escape(getcwd(), ' ')
    let hgdir = finddir('.hg', cwd . ';')
    if hgdir != ''
      let hgdir = fnamemodify(hgdir, ':p')
    endif
    let gitdir = finddir('.git', cwd . ';')
    if gitdir != ''
      let gitdir = fnamemodify(gitdir, ':p')
    endif
    if hgdir != '' || gitdir != ''
      if len(hgdir) > len(gitdir)
        runtime autoload/eclim/vcs/impl/hg.vim
        let type = 'hg'
      else
        runtime autoload/eclim/vcs/impl/git.vim
        let type = 'git'
      endif
    endif
  endif
  return type
endfunction " }}}

" GetVcsFunction(func_name) {{{
" Gets a reference to the proper vcs function.
" Ex. let GetRevision = eclim#vcs#util#GetVcsFunction('GetRevision')
function eclim#vcs#util#GetVcsFunction(func_name)
  let type = eclim#vcs#util#GetVcsType()
  if type == ''
    return
  endif

  try
    return function('eclim#vcs#impl#' . type . '#' . a:func_name)
  catch /E700:.*/
    call eclim#util#EchoError('This function is not supported by "' . type . '".')
    return
  endtry
endfunction " }}}

" GetFilePath(dir, file) {{{
" Gets the repository root relative path of the specified file, or the current
" file if the empty string supplied.
" Ex. /trunk/src/vim/eclim/autoload/eclim/eclipse.vim
function eclim#vcs#util#GetFilePath(file)
  let project_root = eclim#project#util#GetCurrentProjectRoot()
  let file = a:file
  let dir = file
  if file == ''
    let file = expand('%:t')
    let dir = expand('%:p:h')
  elseif !isdirectory(project_root . '/' . file)
    let dir = fnamemodify(project_root . '/' . file, ':p:h')
    let file = fnamemodify(project_root . '/' . file, ':t')
  else
    let dir = fnamemodify(project_root . '/' . file, ':p')
    let file = ''
  endif
  return eclim#vcs#util#GetRelativePath(dir, file)
endfunction " }}}

" GetRelativePath(dir, file) {{{
" Gets the repository root relative path of the specified file in the supplied
" dir.
" Ex. /src/vim/eclim/autoload/eclim/eclipse.vim
function eclim#vcs#util#GetRelativePath(dir, file)
  let path = ''

  let cwd = getcwd()
  exec 'lcd ' . escape(a:dir, ' ')
  try
    let GetRelativePath = eclim#vcs#util#GetVcsFunction('GetRelativePath')
    if type(GetRelativePath) == 2
      let path = GetRelativePath(a:dir, a:file)
    endif
  finally
    exec 'lcd ' . escape(cwd, ' ')
  endtry

  return path
endfunction " }}}

" GetPreviousRevision([file, revision]) {{{
" Gets the previous revision of the current file.
function eclim#vcs#util#GetPreviousRevision(...)
  let cwd = getcwd()
  let dir = len(a:000) > 0 ? fnamemodify(a:000[0], ':p:h') : expand('%:p:h')
  if isdirectory(dir)
    exec 'lcd ' . escape(dir, ' ')
  endif
  try
    let GetPreviousRevision =
      \ eclim#vcs#util#GetVcsFunction('GetPreviousRevision')
    if type(GetPreviousRevision) != 2
      return
    endif
    if len(a:000) > 0
      let revision = GetPreviousRevision(a:000[0], len(a:000) > 1 ? a:000[1] : '')
    else
      let revision = GetPreviousRevision()
    endif
  finally
    exec 'lcd ' . escape(cwd, ' ')
  endtry

  return revision
endfunction " }}}

" GetRevision([file]) {{{
" Gets the current revision of the current or supplied file.
function eclim#vcs#util#GetRevision(...)
  let path = len(a:000) > 0 ? a:000[0] : expand('%')
  let cwd = getcwd()
  if filereadable(path)
    let file = fnamemodify(path, ':t')
    let dir = fnamemodify(path, ':h')
    exec 'lcd ' . escape(dir, ' ')
  else
    let file = path
  endif
  try
    let GetRevision = eclim#vcs#util#GetVcsFunction('GetRevision')
    if type(GetRevision) != 2
      return
    endif
    let revision = GetRevision(file)
  finally
    exec 'lcd ' . escape(cwd, ' ')
  endtry
  return revision
endfunction " }}}

" GetRevisions() {{{
" Gets a list of revision numbers for the current file.
function eclim#vcs#util#GetRevisions()
  let revisions = []

  let cwd = getcwd()
  let dir = expand('%:p:h')
  exec 'lcd ' . escape(dir, ' ')
  try
    let GetRevisions = eclim#vcs#util#GetVcsFunction('GetRevisions')
    if type(GetRevisions) == 2
      let revisions = GetRevisions()
    endif
  finally
    exec 'lcd ' . escape(cwd, ' ')
  endtry

  return revisions
endfunction " }}}

" GetModifiedFiles() {{{
" Gets a list of modified files, including untracked files that are not
" ignored.
function eclim#vcs#util#GetModifiedFiles()
  let files = []

  let cwd = getcwd()
  let dir = eclim#vcs#util#GetRoot('')
  exec 'lcd ' . escape(dir, ' ')
  try
    let GetModifiedFiles = eclim#vcs#util#GetVcsFunction('GetModifiedFiles')
    if type(GetModifiedFiles) == 2
      let files = GetModifiedFiles()
    endif
  finally
    exec 'lcd ' . escape(cwd, ' ')
  endtry

  return files
endfunction " }}}

" GetRoot(dir) {{{
" Gets the absolute path to the repository root on the local file system.
function eclim#vcs#util#GetRoot(dir)
  let root = ''

  let cwd = getcwd()
  let dir = a:dir == '' ? expand('%:p:h') : a:dir
  exec 'lcd ' . escape(dir, ' ')
  try
    let GetRoot = eclim#vcs#util#GetVcsFunction('GetRoot')
    if type(GetRoot) == 2
      let root = GetRoot()
    endif
  finally
    exec 'lcd ' . escape(cwd, ' ')
  endtry

  return root
endfunction " }}}

" Vcs(cmd, args) {{{
" Executes the supplied vcs command with the supplied args.
function eclim#vcs#util#Vcs(cmd, args)
  if !executable(a:cmd)
    call eclim#util#EchoError(a:cmd . ' executable not found in your path.')
    return
  endif

  let result = eclim#util#System(a:cmd . ' ' . a:args)
  if v:shell_error
    call eclim#util#EchoError(
      \ "Error executing command: " . a:cmd . " " . a:args . "\n" . result)
    throw 'vcs error'
  endif

  return result
endfunction " }}}

" IsCacheValid(metadata) {{{
" Function used to validate cached values on get from the cache.
function eclim#vcs#util#IsCacheValid(metadata)
  let revision = eclim#vcs#util#GetRevision(a:metadata.path)
  return revision == a:metadata.revision
endfunction " }}}

" CommandCompleteRevision(argLead, cmdLine, cursorPos) {{{
" Custom command completion for revision numbers out of viewvc.
function! eclim#vcs#util#CommandCompleteRevision(argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]

  let revisions = eclim#vcs#util#GetRevisions()
  call filter(revisions, 'v:val =~ "^' . argLead . '"')
  return revisions
endfunction " }}}

" vim:ft=vim:fdm=marker
