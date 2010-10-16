" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/common/vcs.html
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

if !exists('g:eclim_vcs_util_loaded')
  let g:eclim_vcs_util_loaded = 1
else
  finish
endif

" Script Variables {{{
  let s:types = {'git': '.git', 'hg': '.hg'}
" }}}

" GetVcsType() {{{
function eclim#vcs#util#GetVcsType()
  let cwd = escape(getcwd(), ' ')
  let result_dir = ''
  let result_vcs = ''
  for [type, dir] in items(s:types)
    let vcsdir = finddir(dir, cwd . ';')
    if vcsdir != ''
      let vcsdir = fnamemodify(vcsdir, ':p')
      if result_dir == '' || len(vcsdir) > len(result_dir)
        let result_dir = vcsdir
        let result_vcs = type
      endif
    endif
  endfor

  if result_dir != ''
      exec 'runtime autoload/eclim/vcs/impl/' . result_vcs . '.vim'
  endif
  return result_vcs
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

" GetPreviousRevision(path, [revision]) {{{
" Gets the previous revision of the supplied path.
function eclim#vcs#util#GetPreviousRevision(path, ...)
  let cwd = eclim#vcs#util#LcdRoot()
  try
    let GetPreviousRevision =
      \ eclim#vcs#util#GetVcsFunction('GetPreviousRevision')
    if type(GetPreviousRevision) != 2
      return
    endif
    if len(a:000) > 0
      let revision = GetPreviousRevision(a:path, a:000[0])
    else
      let revision = GetPreviousRevision(a:path)
    endif
  finally
    exec 'lcd ' . cwd
  endtry

  return revision
endfunction " }}}

" GetRevision(path) {{{
" Gets the current revision of the current or supplied file.
function eclim#vcs#util#GetRevision(path)
  let cwd = eclim#vcs#util#LcdRoot()
  try
    let GetRevision = eclim#vcs#util#GetVcsFunction('GetRevision')
    if type(GetRevision) != 2
      return
    endif
    let revision = GetRevision(a:path)
  finally
    exec 'lcd ' . cwd
  endtry
  return revision
endfunction " }}}

" GetRevisions(path) {{{
" Gets a list of revision numbers for the supplied path.
function eclim#vcs#util#GetRevisions(path)
  let revisions = []

  let cwd = eclim#vcs#util#LcdRoot()
  try
    let GetRevisions = eclim#vcs#util#GetVcsFunction('GetRevisions')
    if type(GetRevisions) == 2
      let revisions = GetRevisions(a:path)
    endif
  finally
    exec 'lcd ' . cwd
  endtry

  return revisions
endfunction " }}}

" GetModifiedFiles() {{{
" Gets a list of modified files, including untracked files that are not
" ignored.
function eclim#vcs#util#GetModifiedFiles()
  let files = []

  let cwd = eclim#vcs#util#LcdRoot()
  try
    let GetModifiedFiles = eclim#vcs#util#GetVcsFunction('GetModifiedFiles')
    if type(GetModifiedFiles) == 2
      let files = GetModifiedFiles()
    endif
  finally
    exec 'lcd ' . cwd
  endtry

  return files
endfunction " }}}

" GetRelativePath(path) {{{
" Converts the supplied absolute path into a repos relative path.
function eclim#vcs#util#GetRelativePath(path)
  let root = eclim#vcs#util#GetRoot(a:path)
  let path = substitute(a:path, '\', '/', 'g')
  let path = substitute(path, '^' . root, '', '')
  let path = substitute(path, '^/', '', '')
  return path
endfunction " }}}

" GetRoot([path]) {{{
" Gets the absolute path to the repository root on the local file system.
function eclim#vcs#util#GetRoot(...)
  if exists('b:vcs_props') && has_key(b:vcs_props, 'root_dir')
    return b:vcs_props.root_dir
  endif

  let root = ''

  let cwd = getcwd()
  let path = len(a:000) > 0 && a:000[0] != '' ? a:000[0] : expand('%:p')
  if !isdirectory(path)
    let path = fnamemodify(path, ':h')
  endif

  exec 'lcd ' . escape(path, ' ')
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

" GetInfo(dir) {{{
" Gets some displayable info for the specified vcs directory (branch info, etc.)
function eclim#vcs#util#GetInfo(dir)
  let info = ''

  let cwd = getcwd()
  let dir = a:dir == '' ? expand('%:p:h') : a:dir
  exec 'lcd ' . escape(dir, ' ')
  try
    let GetInfo = eclim#vcs#util#GetVcsFunction('GetInfo')
    if type(GetInfo) == 2
      let info = GetInfo()
    endif
  catch /E117/
    " function not found
  finally
    exec 'lcd ' . escape(cwd, ' ')
  endtry

  return info
endfunction " }}}

" LcdRoot([path]) {{{
" lcd to the vcs root and return the previous working directory.
function eclim#vcs#util#LcdRoot(...)
  let cwd = getcwd()
  let path = len(a:000) > 0 ? a:000[0] : expand('%:p')
  let root = eclim#vcs#util#GetRoot(path)
  exec 'lcd ' . escape(root, ' ')
  return escape(cwd, ' ')
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
