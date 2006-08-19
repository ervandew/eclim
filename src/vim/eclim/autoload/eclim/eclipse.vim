" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   Utility functions for eclipse.
"
" License:
"
" Copyright (c) 2005 - 2006
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
" }}}

" GetWorkspaceDir() {{{
" Gets the path tot the workspace.  Ensures the path uses cross platform '/'
" separators and includes a trailing '/'.  If eclim is not running the empty
" string is returned.
function! eclim#eclipse#GetWorkspaceDir ()
  let result = eclim#ExecuteEclim(s:command_workspace_dir)

  if result == '0'
    return ''
  endif

  let result = substitute(result, '\', '/', 'g')
  if result !~ '/$'
    let result .= '/'
  endif
  return result
endfunction " }}}

" vim:ft=vim:fdm=marker
