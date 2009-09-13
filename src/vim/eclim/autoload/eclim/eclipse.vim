" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Utility functions for eclipse.
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

" Script Variables {{{
  let s:command_workspace_dir = '-command workspace_dir'
  let s:ide_prefs =
    \ g:EclimEclipseHome . '/configuration/.settings/org.eclipse.ui.ide.prefs'
" }}}

" GetWorkspaceDir() {{{
" Gets the path to the workspace.  Ensures the path uses cross platform '/'
" separators and includes a trailing '/'.  If the workspace could not be
" determined, the empty string is returned.
function! eclim#eclipse#GetWorkspaceDir()
  silent let result = eclim#ExecuteEclim(s:command_workspace_dir)
  if result == '0'
    let result = ''
  endif

  if result == ''
    let workspaces = eclim#eclipse#GetAllWorkspaceDirs()
    if len(workspaces) > 0
      let result = workspaces[0]
      if len(workspaces) > 1
        " more than one recent workspace, check if the curent file is is one
        " of those.
        let path = expand('%:p')
        for r in results[1:]
          if path =~ '^' . r . '\>'
            let result = r
            break
          endif
        endfor
      endif
    endif

    " failed to get the workspace.
    if result == ''
      return result
    endif
  endif

  if result != ''
    " ensure value uses unix slashes and ends in a slash
    let result = substitute(result, '\', '/', 'g')
    if result !~ '/$'
      let result .= '/'
    endif

    let g:EclimWorkspace = result
  endif

  return g:EclimWorkspace
endfunction " }}}

" GetAllWorkspaceDirs() {{{
function! eclim#eclipse#GetAllWorkspaceDirs()
  let results = []

  let instances = expand('~/.eclim/.eclimd_instances')
  if filereadable(instances)
    let results = readfile(instances)
    call map(results, 'substitute(v:val, "\\(.*\\):.*", "\\1", "")')
  endif

  if len(results) == 0 && filereadable(s:ide_prefs)
    let results = readfile(s:ide_prefs)
    call filter(results, 'v:val =~ "^\s*RECENT_WORKSPACES\s*="')
    call map(results,
      \ 'substitute(v:val, ".\\{-}=\\s*\\(.\\{-}\\)\\(\\s*,\\|$\\)", "\\1", "")')
    " unescape the escaped dir name in windows
    exec 'let results = ["' . results[0] . '"]'

    if results[0] =~ "\n"
      let results = split(results[0], "\n")
    endif
  endif

  " ensure each value uses unix slashes and ends in a slash
  let results = map(results, 'substitute(v:val, "\\\\", "/", "g")')
  let results = map(results, 'v:val . (v:val !~ "/$" ? "/" : "")')

  " only return workspaces that exist.
  let results = filter(results, 'isdirectory(v:val)')

  return results
endfunction " }}}

" vim:ft=vim:fdm=marker
