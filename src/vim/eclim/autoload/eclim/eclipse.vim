" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Utility functions for eclipse.
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

" Script Variables {{{
  let s:command_workspace_dir = '-command workspace_dir'
  let s:ide_prefs =
    \ g:EclimHome . '/../../configuration/.settings/org.eclipse.ui.ide.prefs'
" }}}

" GetWorkspaceDir() {{{
" Gets the path to the workspace.  Ensures the path uses cross platform '/'
" separators and includes a trailing '/'.  If the workspace could not be
" determined, the empty string is returned.
function! eclim#eclipse#GetWorkspaceDir ()
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
