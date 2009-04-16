" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/c/project.html
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

" Script Varables {{{
  let s:configs_command = '-command c_project_configs -p "<project>"'
  let s:src_command =
    \ '-command c_project_src -p "<project>" -a <action> -d "<dir>"'
  let s:include_command =
    \ '-command c_project_include -p "<project>" -a <action> -d "<dir>" -l <lang>'
" }}}

" Configs([project]) {{{
" Open a buffer with current project configs info.
function! eclim#c#project#Configs(...)
  if len(a:000) > 0 && a:000[0] != ''
    let project = a:000[0]
  else
    let project = eclim#project#util#GetCurrentProjectName()
  endif

  if project == ''
    " force printing of project error message
    call eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let command = s:configs_command
  let command = substitute(command, '<project>', project, '')
  let results = split(eclim#ExecuteEclim(command), '\n')
  if len(results) == 1 && results[0] == '0'
    return
  endif

  call eclim#util#TempWindow('[' . project . ' configs]', results)
  let b:project = project
  call s:Syntax()

  nnoremap <silent> <buffer> <cr> :call <SID>FollowLink()<cr>
  nnoremap <silent> <buffer> D :call <SID>Delete()<cr>
endfunction " }}}

" s:Syntax() {{{
function s:Syntax()
  syntax match CProjectConfigLabel /^\s*[A-Z]\w\+:/
  syntax match CProjectConfigSubLabel /^\s*[a-z]\w\+:/
  syntax match CProjectConfigLink /|\S.\{-}\S|/
  hi link CProjectConfigLabel Statement
  hi link CProjectConfigSubLabel Identifier
  hi link CProjectConfigLink Label
endfunction " }}}

" s:FollowLink() {{{
function s:FollowLink()
  let line = getline('.')
  let link = substitute(
    \ getline('.'), '.*|\(.\{-}\%' . col('.') . 'c.\{-}\)|.*', '\1', '')
  if link == line
    return
  endif

  if line =~ '^\s*Sources:'
    call s:AddSource()
  elseif line =~ '^\s*Includes:'
    call s:AddInclude()
  elseif line =~ '^\s*Symbols:'
    echom 'add symbol'
    "call s:AddSymbol()
  endif
endfunction " }}}

" s:AddSource() {{{
function s:AddSource()
  let project_root = eclim#project#util#GetProjectRoot(b:project)
  let complete = 'customlist,eclim#project#util#CommandCompleteProjectRelative'
  let dir = input('dir: ', '', complete)
  while dir != '' && !isdirectory(project_root . '/' . dir)
    call eclim#util#Echo('Directory "' . dir . '" not found in the project.')
    let dir = input('dir: ', dir, complete)
  endwhile

  if dir == ''
    return
  endif

  let excludes = input('excludes (comma separated patterns): ')

  let command = s:src_command
  let command = substitute(command, '<project>', b:project, '')
  let command = substitute(command, '<action>', 'add', '')
  let command = substitute(command, '<dir>', dir, '')
  if excludes != ''
    let command .= ' -e "' . excludes . '"'
  endif

  let result = eclim#ExecuteEclim(command)
  if result != '0'
    call eclim#c#project#Configs()
    call eclim#util#Echo(result)
  endif
endfunction " }}}

" s:AddInclude() {{{
function s:AddInclude()
  let project_root = eclim#project#util#GetProjectRoot(b:project)
  let complete = 'customlist,eclim#project#util#CommandCompleteAbsoluteOrProjectRelative'
  let dir = input('dir: ', '', complete)

  if dir == ''
    return
  endif

  " get the lang
  let lang_line = getline(search('^\s\+Tool:', 'bnW'))
  if lang_line =~ 'assembl\c'
    let lang = 'assembly'
  else
    let lang = 'c'
  endif

  let command = s:include_command
  let command = substitute(command, '<project>', b:project, '')
  let command = substitute(command, '<action>', 'add', '')
  let command = substitute(command, '<dir>', dir, '')
  let command = substitute(command, '<lang>', lang, '')

  let result = eclim#ExecuteEclim(command)
  if result != '0'
    call eclim#c#project#Configs()
    call eclim#util#Echo(result)
  endif
endfunction " }}}

" s:Delete() {{{
function s:Delete()
  let reload = 0
  let message = ''
  let pos = getpos('.')
  let line = getline('.')

  " source entry excludes
  if line =~ '^\s*excludes:\s\+'
    let line = getline(line('.') - 1)
  endif

  " source entry dir
  if line =~ '^\s*dir:\s\+'
    let reload = 1
    let dir = substitute(line, '^\s*dir:\s\+\(.*\)', '\1', '')
    let command = s:src_command
    let command = substitute(command, '<project>', b:project, '')
    let command = substitute(command, '<action>', 'delete', '')
    let command = substitute(command, '<dir>', dir, '')

    let result = eclim#ExecuteEclim(command)
    if result != '0'
      let message = result
    endif

  " include path entry
  elseif line =~ '^\s*path:\s\+'
    let reload = 1
    let dir = substitute(line, '^\s*path:\s\+\(.*\)', '\1', '')
    let dir = substitute(dir, '\(^"\|"$\)', '', 'g')
    let dir = substitute(dir, '^\$', '', '')
    let dir = substitute(dir, '\(^{\|}$\)', '', 'g')
    let dir = substitute(dir, '^workspace_loc:', '', '')

    " get the lang
    let lang_line = getline(search('^\s\+Tool:', 'bnW'))
    if lang_line =~ 'assembl\c'
      let lang = 'assembly'
    else
      let lang = 'c'
    endif

    let command = s:include_command
    let command = substitute(command, '<project>', b:project, '')
    let command = substitute(command, '<action>', 'delete', '')
    let command = substitute(command, '<dir>', dir, '')
    let command = substitute(command, '<lang>', lang, '')

    let result = eclim#ExecuteEclim(command)
    if result != '0'
      let message = result
    endif
  endif

  if reload
    call eclim#c#project#Configs()
    call setpos('.', pos)
    if message != ''
      call eclim#util#Echo(message)
    endif
  endif
endfunction " }}}

" CommandCompleteProject(argLead, cmdLine, cursorPos) {{{
" Custom command completion for project names.
function! eclim#c#project#CommandCompleteProject(argLead, cmdLine, cursorPos)
  let c_projects = eclim#project#util#CommandCompleteProjectByNature(
    \ a:argLead, a:cmdLine, a:cursorPos, 'c')
  let cpp_projects = eclim#project#util#CommandCompleteProjectByNature(
    \ a:argLead, a:cmdLine, a:cursorPos, 'cpp')
  let projects = c_projects + cpp_projects
  call sort(projects)
  return projects
endfunction " }}}

" vim:ft=vim:fdm=marker
