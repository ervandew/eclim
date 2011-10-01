" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Utility functions for eclipse.
"
" License:
"
" Copyright (C) 2005 - 2011  Eric Van Dewoestine
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
  let s:ide_prefs =
    \ g:EclimEclipseHome . '/configuration/.settings/org.eclipse.ui.ide.prefs'
" }}}

" GetAllWorkspaceDirs() {{{
function! eclim#eclipse#GetAllWorkspaceDirs()
  let results = []

  let instances = eclim#UserHome() . '/.eclim/.eclimd_instances'
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

    if has('win32unix')
      call map(results, 'eclim#cygwin#CygwinPath(v:val, 1)')
    endif
  endif

  " ensure each value uses unix slashes and ends in a slash
  let results = map(results, 'substitute(v:val, "\\\\", "/", "g")')
  let results = map(results, 'v:val . (v:val !~ "/$" ? "/" : "")')

  " only return workspaces that exist.
  let results = filter(results, 'isdirectory(v:val)')

  call sort(results)

  return results
endfunction " }}}

" ChooseWorkspace([dir]) {{{
" Function which prompts the user to pick the target workspace and returns
" their choice or if only one workspace is active simply return it without
" prompting the user.  If the optional 'dir' argument is supplied and that dir
" is a subdirectory of one of the workspaces, then that workspace will be
" returned.
function! eclim#eclipse#ChooseWorkspace(...)
  let workspaces = eclim#eclipse#GetAllWorkspaceDirs()
  if len(workspaces) == 1
    return workspaces[0]
  endif

  if len(workspaces) > 1
    if a:0 > 0
      for workspace in workspaces
        if a:1 =~ '^' . substitute(workspace, '\(/\|\\\)$', '', '') . '\>'
          return workspace
        endif
      endfor
    else
      let project = eclim#project#util#GetCurrentProjectName()
      if project != ''
        let workspace = eclim#project#util#GetProjectWorkspace(project)
        if workspace != ''
          return workspace
        endif
      endif
    endif

    let response = eclim#util#PromptList(
      \ 'Muliple workspaces found, please choose the target workspace',
      \ workspaces, g:EclimInfoHighlight)

    " user cancelled, error, etc.
    if response < 0
      return
    endif

    return workspaces[response]
  endif

  call eclim#util#Echo('Unable to determine your eclipse workspace.')
endfunction " }}}

" CommandCompleteWorkspaces(argLead, cmdLine, cursorPos) {{{
" Custom command completion for available workspaces.
function! eclim#eclipse#CommandCompleteWorkspaces(argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]

  let workspaces = eclim#eclipse#GetAllWorkspaceDirs()
  if cmdLine !~ '[^\\]\s$'
    call filter(workspaces, 'v:val =~ "^' . argLead . '"')
  endif

  return workspaces
endfunction " }}}

" vim:ft=vim:fdm=marker
