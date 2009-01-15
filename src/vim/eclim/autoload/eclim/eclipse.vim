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
    \ eclim#GetEclimHome() . '/../../configuration/.settings/org.eclipse.ui.ide.prefs'
" }}}

" GetWorkspaceDir() {{{
" Gets the path to the workspace.  Ensures the path uses cross platform '/'
" separators and includes a trailing '/'.  If the workspace could not be
" determined, the empty string is returned.
function! eclim#eclipse#GetWorkspaceDir()
  if !exists('g:EclimWorkspace')
    let result = ''

    if result == ''
      let result = eclim#ExecuteEclim(s:command_workspace_dir)
      if result == '0'
        let result = ''
      endif
    endif

    " fall back to file based discovery
    if result == '' && filereadable(s:ide_prefs)
      let lines = readfile(s:ide_prefs)
      call filter(lines, 'v:val =~ "^\s*RECENT_WORKSPACES\s*="')
      if len(lines) == 1
        let result = substitute(lines[0], '.\{-}=\s*\(.\{-}\)\(\s*,\|$\)', '\1', '')
        " unescape the escaped dir name in windows
        exec 'let result = "' . result . '"'
      endif
    endif

    " failed to get the workspace.
    if result == ''
      return result
    endif

    " ensure value uses unix slashes and ends in a slash
    let result = substitute(result, '\', '/', 'g')
    if result !~ '/$'
      let result .= '/'
    endif

    let g:EclimWorkspace = result
  endif
  return g:EclimWorkspace
endfunction " }}}

" vim:ft=vim:fdm=marker
