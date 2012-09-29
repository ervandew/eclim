" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2005 - 2012  Eric Van Dewoestine
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

function! eclim#client#nailgun#Execute(port, command, ...) " {{{
  let exec = a:0 ? a:1 : 0

  if !exec
    if !exists('g:EclimNailgunClient')
      call s:DetermineClient()
    endif

    if g:EclimNailgunClient == 'python'
      return eclim#client#python#nailgun#Execute(a:port, a:command)
    endif
  endif

  let eclim = eclim#client#nailgun#GetEclimCommand()
  if string(eclim) == '0'
    return [1, g:EclimErrorReason]
  endif

  let command = a:command
  if exec
    let command = escape(command, '%#')
  endif

  " on windows/cygwin where cmd.exe is used, we need to escape any '^'
  " characters in the command args.
  if has('win32') || has('win64') || has('win32unix')
    let command = substitute(command, '\^', '^^', 'g')
  endif

  let eclim .= ' --nailgun-port ' . a:port . ' ' . command
  if exec
    let eclim = '!' . eclim
  endif

  let result = eclim#util#System(eclim, exec, exec)
  return [v:shell_error, result]
endfunction " }}}

function! eclim#client#nailgun#GetEclimCommand() " {{{
  " Gets the command to exexute eclim.
  if !exists('g:EclimPath')
    let g:EclimPath = g:EclimEclipseHome . '/eclim'

    if has('win32') || has('win64') || has('win32unix')
      let g:EclimPath = g:EclimPath . (has('win95') ? '.bat' : '.cmd')
    endif

    if !filereadable(g:EclimPath)
      let g:EclimErrorReason = 'Could not locate file: ' . g:EclimPath
      unlet g:EclimPath
      return
    endif

    if has('win32unix')
      " in cygwin, we must use 'cmd /c' to prevent issues with eclim script +
      " some arg containing spaces causing a failure to invoke the script.
      let g:EclimPath = 'cmd /c "' . eclim#cygwin#WindowsPath(g:EclimPath, 1) . '"'
    else
      let g:EclimPath = '"' . g:EclimPath . '"'
    endif
  endif
  return g:EclimPath
endfunction " }}}

function! eclim#client#nailgun#GetNgCommand() " {{{
  " Gets path to the ng executable.
  if !exists('g:EclimNgPath')
    let g:EclimNgPath = substitute(g:EclimHome, '\', '/', 'g') .  '/bin/ng'

    " on windows, ng.exe is at the eclipse root
    if has('win32') || has('win64') || has('win32unix')
      let g:EclimNgPath = g:EclimEclipseHome . '/ng.exe'
      if !has('win32unix')
        let g:EclimNgPath = substitute(g:EclimNgPath, '/', '\', 'g')
      endif
    endif

    if !filereadable(g:EclimNgPath)
      let g:EclimErrorReason = 'Could not locate file: ' . g:EclimNgPath
      return
    endif

    let g:EclimNgPath = '"' . g:EclimNgPath . '"'
  endif
  return g:EclimNgPath
endfunction " }}}

function! eclim#client#nailgun#GetNgPort(...) " {{{
  " Gets port that the nailgun server is configured to run on.
  " Optional args:
  "   workspace

  let port = 9091
  let eclimrc = eclim#UserHome() . '/.eclimrc'
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

  let instances = eclim#UserHome() . '/.eclim/.eclimd_instances'
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
    if path == ''
      let path = getcwd() . '/'
    endif
    let path = substitute(path, '\', '/', 'g')

    " when we are in a temp window, use the initiating filename
    if &buftype != '' && exists('b:filename')
      let path = b:filename
    endif

    " project inside of a workspace dir
    for workspace in keys(workspaces)
      if path =~ '^' . workspace
        return workspaces[workspace]
      endif
    endfor

    " project outside of a workspace dir
    let project = eclim#project#util#GetProject(path)
    if len(project) > 0
      return get(workspaces, project.workspace, default)
    endif
  endif

  return port
endfunction " }}}

function! s:DetermineClient() " {{{
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
