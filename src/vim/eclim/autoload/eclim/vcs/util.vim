" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/common/vcs.html
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

" GetVcsFunction() {{{
" Gets a reference to the proper vcs function.
" Ex. let GetRevision = eclim#vcs#util#GetVcsFunction(expand('%:p:h'), 'GetRevision')
function eclim#vcs#util#GetVcsFunction (dir, func_name)
  let type = ''
  if isdirectory(a:dir . '/CVS')
    runtime autoload/eclim/vcs/cvs.vim
    let type = 'cvs'
  elseif isdirectory(a:dir . '/.svn')
    runtime autoload/eclim/vcs/svn.vim
    let type = 'svn'
  else
    runtime autoload/eclim/vcs/hg.vim
    let dir = finddir('.hg', a:dir . ';')
    if dir != ''
      let type = 'hg'
    endif
  endif

  if type == ''
    return ''
  endif
  return function('eclim#vcs#' . type . '#' . a:func_name)
endfunction " }}}

" GetFilePath(dir, file) {{{
" Gets the repository root relative path of the specified file, or the current
" file if the empty string supplied.
" Ex. /trunk/src/vim/eclim/autoload/eclim/eclipse.vim
function eclim#vcs#util#GetFilePath (file)
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
  return eclim#vcs#util#GetPath(dir, file)
endfunction " }}}

" GetPath(dir, file) {{{
" Gets the repository root relative path of the specified file in the supplied
" dir.
" Ex. /trunk/src/vim/eclim/autoload/eclim/eclipse.vim
function eclim#vcs#util#GetPath (dir, file)
  let path = ''
  let cmd = winrestcmd()

  let cwd = getcwd()
  exec 'lcd ' . a:dir
  try
    let GetPath = eclim#vcs#util#GetVcsFunction(a:dir, 'GetPath')
    if type(GetPath) == 2
      let path = GetPath(a:dir, a:file)
    endif
  finally
    silent exec cmd
    exec 'lcd ' . cwd
  endtry

  return path
endfunction " }}}

" GetPreviousRevision() {{{
" Gets the previous revision of the current file.
function eclim#vcs#util#GetPreviousRevision ()
  let revision = '0'

  let cwd = getcwd()
  let dir = expand('%:p:h')
  exec 'lcd ' . dir
  try
    let GetPreviousRevision =
      \ eclim#vcs#util#GetVcsFunction(dir, 'GetPreviousRevision')
    if type(GetPreviousRevision) == 2
      let revision = GetPreviousRevision()
    endif
  finally
    exec 'lcd ' . cwd
  endtry

  return revision
endfunction " }}}

" GetRevision([url]) {{{
" Gets the current revision of the current or supplied file.
function eclim#vcs#util#GetRevision (...)
  let revision = '0'

  let file = len(a:000) > 0 ? a:000[0] : expand('%:t')
  let dir = expand('%:p:h')
  if len(a:000) == 0
    let cwd = getcwd()
    exec 'lcd ' . dir
  endif
  try
    let GetRevision = eclim#vcs#util#GetVcsFunction(dir, 'GetRevision')
    if type(GetRevision) == 2
      let revision = GetRevision(file)
    endif
  finally
    if len(a:000) == 0
      exec 'lcd ' . cwd
    endif
  endtry

  return revision
endfunction " }}}

" GetRevisions() {{{
" Gets a list of revision numbers for the current file.
function eclim#vcs#util#GetRevisions ()
  let revisions = []

  let cwd = getcwd()
  let dir = expand('%:p:h')
  exec 'lcd ' . dir
  try
    let GetRevisions = eclim#vcs#util#GetVcsFunction(dir, 'GetRevisions')
    if type(GetRevisions) == 2
      let revisions = GetRevisions()
    endif
  finally
    exec 'lcd ' . cwd
  endtry

  return revisions
endfunction " }}}

" GetReposUrl(dir) {{{
" Gets the repository root url for the repository backing the supplied dir.
" Ex. http://svn.eclim.sf.net/
function eclim#vcs#util#GetReposUrl (dir)
  let repos = ''
  let cmd = winrestcmd()

  try
    let GetReposUrl = eclim#vcs#util#GetVcsFunction(a:dir, 'GetReposUrl')
    if type(GetReposUrl) == 2
      let repos = GetReposUrl(a:dir)
    endif
  finally
    silent exec cmd
  endtry

  return repos
endfunction " }}}

" GetUrl(dir, file) {{{
" Gets the repository url for the specified file in the supplied dir.
" Ex. http://svn.eclim.sf.net/trunk/src/vim/eclim/autoload/eclim/eclipse.vim
function eclim#vcs#util#GetUrl (dir, file)
  let url = '0'

  let file = len(a:000) > 0 ? a:000[0] : expand('%:t')
  let dir = expand('%:p:h')
  if len(a:000) == 0
    let cwd = getcwd()
    exec 'lcd ' . dir
  endif
  try
    let GetUrl = eclim#vcs#util#GetVcsFunction(a:dir, 'GetUrl')
    if type(GetUrl) == 2
      let url = GetUrl(a:dir, a:file)
    endif
  finally
    if len(a:000) == 0
      exec 'lcd ' . cwd
    endif
  endtry

  return url
endfunction " }}}

" Info() {{{
" Retrieves and echos info on the current file.
function eclim#vcs#util#Info ()
  let cwd = getcwd()
  let dir = expand('%:p:h')
  exec 'lcd ' . dir
  try
    let Info = eclim#vcs#util#GetVcsFunction(dir, 'Info')
    if type(Info) == 2
      call Info()
    endif
  finally
    exec 'lcd ' . cwd
  endtry
endfunction " }}}

" Vcs(cmd, args) {{{
" Executes the supplied vcs command with the supplied args.
function eclim#vcs#util#Vcs (cmd, args)
  if !executable(a:cmd)
    call eclim#util#EchoError(a:cmd . ' executable not found in your path.')
    return
  endif

  let result = eclim#util#System(a:cmd . ' ' . a:args)
  if v:shell_error
    call eclim#util#EchoError(
      \ "Error executing command: " . a:cmd . " " . a:args . "\n" . result)
    return
  endif

  return result
endfunction " }}}

" IsCacheValid(metadata) {{{
" Function used to validate cached values on get from the cache.
function eclim#vcs#util#IsCacheValid (metadata)
  let revision = eclim#vcs#util#GetRevision(a:metadata.url)
  return revision == a:metadata.revision
endfunction " }}}

" CommandCompleteRevision(argLead, cmdLine, cursorPos) {{{
" Custom command completion for revision numbers out of viewvc.
function! eclim#vcs#util#CommandCompleteRevision (argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseArgs(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]

  let revisions = eclim#vcs#util#GetRevisions()
  call filter(revisions, 'v:val =~ "^' . argLead . '"')
  return revisions
endfunction " }}}

" vim:ft=vim:fdm=marker
