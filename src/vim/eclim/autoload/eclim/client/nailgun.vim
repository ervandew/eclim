" Author:  Eric Van Dewoestine
"
" Description: {{{
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

" Global Variables {{{
  if !exists("g:EclimNailgunKeepAlive")
    " keepAlive flag - can be re-defined in the user ~/.vimrc .
    " Read once, on client initialization. Subsequent changes of
    " this flag in run-time has no effect.
    let g:EclimNailgunKeepAlive = 0
  endif
" }}}

" Execute(port, command) {{{
" Function which invokes nailgun.
function! eclim#client#nailgun#Execute(port, command)
  if !exists('g:EclimNailgunClient')
    call s:DetermineClient()
  endif

  if g:EclimNailgunClient == 'python'
    return eclim#client#python#nailgun#Execute(a:port, a:command)
  endif

  let command = eclim#client#nailgun#GetEclimCommand()
  if string(command) == '0'
    return [1, g:EclimErrorReason]
  endif

  let command .= ' -Dnailgun.server.port=' . a:port . ' ' . a:command

  " for windows, need to add a trailing quote to complete the command.
  if command =~ '^"[a-zA-Z]:'
    let command = command . '"'
  endif

  let result = eclim#util#System(command)
  return [v:shell_error, result]
endfunction " }}}

" GetEclimCommand() {{{
" Gets the command to exexute eclim.
function! eclim#client#nailgun#GetEclimCommand()
  if !exists('g:EclimPath')
    let g:EclimPath = g:EclimEclipseHome . '/eclim'

    if has("win32") || has("win64")
      let g:EclimPath = g:EclimPath . (has('win95') ? '.bat' : '.cmd')
    elseif has("win32unix")
      let g:EclimPath = system('cygpath "' . g:EclimPath . '"')
      let g:EclimPath = substitute(g:EclimPath, '\n.*', '', '')
    endif

    if !filereadable(g:EclimPath)
      let g:EclimErrorReason = 'Could not locate file: ' . g:EclimPath
      unlet g:EclimPath
      return
    endif

    " on windows, the command must be executed on the drive where eclipse is
    " installed.
    if has("win32") || has("win64")
      let g:EclimPath =
        \ '"' . substitute(g:EclimPath, '^\([a-zA-Z]:\).*', '\1', '') .
        \ ' && "' . g:EclimPath . '"'
    else
      let g:EclimPath = '"' . g:EclimPath . '"'
    endif
  endif
  return g:EclimPath
endfunction " }}}

" GetNgCommand() {{{
" Gets path to the ng executable.
function! eclim#client#nailgun#GetNgCommand()
  if !exists('g:EclimNgPath')
    let g:EclimNgPath = substitute(g:EclimHome, '\', '/', 'g') .  '/bin/ng'

    if has("win32") || has("win64")
      let g:EclimNgPath = g:EclimNgPath . '.exe'
      let g:EclimNgPath = substitute(g:EclimNgPath, '/', '\', 'g')
    elseif has("win32unix")
      let g:EclimNgPath = system('cygpath "' . g:EclimNgPath . '"')
      let g:EclimNgPath = substitute(g:EclimNgPath, '\n.*', '', '')
    endif

    if !filereadable(g:EclimNgPath)
      let g:EclimErrorReason = 'Could not locate file: ' . g:EclimNgPath
      return
    endif

    " on windows, the command must be executed on the drive where eclipse is
    " installed.
    "if has("win32") || has("win64")
    "  let g:EclimNgPath =
    "    \ '"' . substitute(g:EclimNgPath, '^\([a-zA-Z]:\).*', '\1', '') .
    "    \ ' && "' . g:EclimNgPath . '"'
    "else
      let g:EclimNgPath = '"' . g:EclimNgPath . '"'
    "endif
  endif
  return g:EclimNgPath
endfunction " }}}

" GetNgPort([workspace]) {{{
" Gets port that the nailgun server is configured to run on.
function! eclim#client#nailgun#GetNgPort(...)
  let port = 9091
  let eclimrc = expand('~/.eclimrc')
  if filereadable(eclimrc)
    let lines = filter(
      \ readfile(eclimrc),
      \ 'v:val =~ "^\\s*nailgun\.server\.port\\s*="')
    if len(lines) > 0
      exec 'let port = ' .
        \ substitute(lines[0], '^\s*.\{-}=\s*\(\d\+\).*', '\1', '')
    endif
  endif
  let default = port

  let instances = expand('~/.eclim/.eclimd_instances')
  if filereadable(instances)
    let workspaces = {}
    let entries = readfile(instances)
    for entry in entries
      let workspace = substitute(entry, '\(.*\):.*', '\1', '')
      let workspace = substitute(workspace, '\', '/', 'g')
      let workspace .= workspace !~ '/$' ? '/' : ''
      exec 'let port = ' . substitute(entry, '.*:\(\d\+\).*', '\1', '')
      let workspaces[workspace] = port
    endfor

    " a specific workspace was supplied
    if len(a:000) > 0
      let workspace = a:000[0]
      let workspace = substitute(workspace, '\', '/', 'g')
      let workspace .= workspace !~ '/$' ? '/' : ''
      return get(workspaces, workspace, default)
    endif

    let path = expand('%:p')

    " project inside of a workspace dir
    for workspace in keys(workspaces)
      if path =~ '^' . workspace
        return workspaces[workspace]
      endif
    endfor

    " project outside of a workspace dir
    for workspace in keys(workspaces)
      let project = eclim#project#util#GetProject(path)
      if len(project) > 0
        return get(workspaces, project.workspace, default)
      endif
    endfor
  endif

  return port
endfunction " }}}

" s:DetermineClient() {{{
function! s:DetermineClient()
  " at least one ubuntu user had serious performance issues using the python
  " client, so we are only going to default to python on windows machines
  " where there is an actual potential benefit to using it.
  if has('python') && (has('win32') || has('win64'))
    let g:EclimNailgunClient = 'python'
  else
    let g:EclimNailgunClient = 'external'
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
