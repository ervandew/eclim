" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
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
  let s:command_create = '-command project_create -f "<folder>"'
  let s:command_create_natures = ' -n <natures>'
  let s:command_create_depends = ' -d <depends>'
  let s:command_delete = '-command project_delete -n "<project>"'
  let s:command_refresh = '-command project_refresh -n "<project>"'
  let s:command_projects = '-command project_info -filter vim'
  let s:command_project_info = s:command_projects . ' -n "<project>"'
  let s:command_project_setting = s:command_project_info . ' -s <setting>'
  let s:command_update = '-command project_update -n "<project>" -s "<settings>"'
  let s:command_open = '-command project_open -n "<project>"'
  let s:command_close = '-command project_close -n "<project>"'
  let s:command_nature_aliases = '-command project_nature_aliases'
" }}}

" ProjectCD(scope) {{{
" Change the current working directory to the current project root.
function! eclim#project#ProjectCD (scope)
  let dir = eclim#project#GetCurrentProjectRoot()
  if a:scope == 0
    exec 'cd ' . dir
  elseif a:scope == 1
    exec 'lcd ' . dir
  endif
endfunction " }}}

" ProjectCreate(args) {{{
" Creates a project at the supplied folder
function! eclim#project#ProjectCreate (args)
  let args = eclim#util#ParseArgs(a:args)

  let folder = fnamemodify(expand(args[0]), ':p')
  let folder = substitute(folder, '\', '/', 'g')
  let command = substitute(s:command_create, '<folder>', folder, '')

  let natures = substitute(a:args, '.* -n\s\+\(.\{-}\)\(\s\+-d\>.*\|$\)', '\1', '')
  if natures != a:args
    let natures = substitute(natures, '\s\+', ',', 'g')
    let command .= substitute(s:command_create_natures, '<natures>', natures, '')
  endif

  let depends = substitute(a:args, '.* -d\s\+\(.\{-}\)\(\s\+-n\>.*\|$\)', '\1', '')
  if depends != a:args
    let depends = substitute(depends, '\s\+', ',', 'g')
    let command .= substitute(s:command_create_depends, '<depends>', depends, '')
  endif

  " BEGIN: backwards compatability
  let index = index(args, '-p')
  if index != -1
    if index + 1 >= len(args)
      call eclim#util#EchoError('No argument for "-p" supplied.')
      return
    endif
    let command .= ' ' . args[index] . ' "' . args[index + 1] . '"'
    call remove(args, index, index + 1)
  endif

  let index = index(args, '-n')
  if index != -1
    if index + 1 >= len(args)
      call eclim#util#EchoError('No argument for "-n" supplied.')
      return
    endif
    if command !~ '-n'
      let command .= ' ' . args[index] . ' ' . args[index + 1]
    endif
    call remove(args, index, index + 1)
  else
    let command .= ' -n java'
  endif

  if command !~ '-d '
    if len(args) > 1
      let command .= ' -d ' . join(args[1:], ',')
    endif
  endif
  " END: backwards compatability

  let result = eclim#ExecuteEclim(command)
  if result != '0'
    call eclim#util#Echo(result)
  endif
endfunction " }}}

" ProjectDelete(name) {{{
" Deletes a project with the supplied name.
function! eclim#project#ProjectDelete (name)
  let command = substitute(s:command_delete, '<project>', a:name, '')
  let result = eclim#ExecuteEclim(command)
  if result != '0'
    call eclim#util#Echo(result)
  endif
endfunction " }}}

" ProjectRefreshAll() {{{
" Refresh all projects.
function! eclim#project#ProjectRefreshAll ()
  let projects = eclim#project#GetProjectNames()
  for project in projects
    call eclim#project#ProjectRefresh(project)
  endfor
  call eclim#util#Echo('Done.')
endfunction " }}}

" ProjectRefresh(args) {{{
" Refresh the requested projects.
function! eclim#project#ProjectRefresh (args)
  if a:args != ''
    let projects = eclim#util#ParseArgs(a:args)
  else
    if !eclim#project#IsCurrentFileInProject()
      return
    endif
    let project = eclim#project#GetCurrentProjectName()
    let projects = [project]
  endif
  for project in projects
    call eclim#util#Echo("Updating project '" . project . "'...")
    let command = substitute(s:command_refresh, '<project>', project, '')
    call eclim#util#Echo(eclim#ExecuteEclim(command))
  endfor

  if len(projects) > 1
    call eclim#util#Echo('Done.')
  endif
endfunction " }}}

" ProjectOpen(name) {{{
" Open the requested project.
function! eclim#project#ProjectOpen (name)
  let command = substitute(s:command_open, '<project>', a:name, '')
  let result = eclim#ExecuteEclim(command)
  if result != '0'
    call eclim#util#Echo(result)
  endif
endfunction " }}}

" ProjectClose(name) {{{
" Close the requested project.
function! eclim#project#ProjectClose (name)
  let command = substitute(s:command_close, '<project>', a:name, '')
  let result = eclim#ExecuteEclim(command)
  if result != '0'
    call eclim#util#Echo(result)
  endif
endfunction " }}}

" ProjectList() {{{
" Lists all the projects currently available in eclim.
function! eclim#project#ProjectList ()
  let projects = split(eclim#ExecuteEclim(s:command_projects), '\n')
  if len(projects) == 0
    call eclim#util#Echo("No projects.")
  endif
  if len(projects) == 1 && projects[0] == '0'
    return
  endif
  exec "echohl " . g:EclimInfoHighlight
  redraw
  for project in projects
    echom project
  endfor
 echohl None
endfunction " }}}

" ProjectSettings(project) {{{
" Opens a window that can be used to edit a project's settings.
function! eclim#project#ProjectSettings (project)
  let project = a:project
  if project == ''
    let project = eclim#project#GetCurrentProjectName()
  endif
  if project == ''
    call eclim#util#EchoError("Unable to determine project. " .
      \ "Please specify a project name or " .
      \ "execute from a valid project directory.")
    return
  endif

  let command = substitute(s:command_project_info, '<project>', project, '')
  if eclim#util#TempWindowCommand(command, project . "_settings")
    exec "lcd " . eclim#project#GetProjectRoot(project)
    setlocal buftype=acwrite
    setlocal filetype=jproperties
    setlocal noreadonly
    setlocal modifiable
    setlocal foldmethod=marker
    setlocal foldmarker={,}

    let b:project = project
    augroup project_settings
      autocmd! BufWriteCmd <buffer>
      autocmd BufWriteCmd <buffer> call <SID>SaveSettings()
    augroup END
  endif
endfunction " }}}

" ProjectGrep(command, pattern, ...) {{{
" Executes the supplied vim grep command with the specified pattern against
" one or more file patterns.
function! eclim#project#ProjectGrep (command, args)
  if !eclim#project#IsCurrentFileInProject()
    return
  endif

  let bufnum = bufnr('%')
  let project_dir = eclim#project#GetCurrentProjectRoot()
  let cwd = getcwd()
"  let save_opt = &eventignore
"  set eventignore=all
  try
    silent exec 'lcd ' . project_dir
    silent! exec a:command . ' ' . a:args
  finally
"    let &eventignore = save_opt
    silent exec 'lcd ' . cwd
    " force quickfix / location list signs to update.
    call eclim#signs#Update()
  endtry
"  if bufnum != bufnr('%')
    " force autocommands to execute if grep jumped to a file.
"    edit
"  endif
  if a:command =~ '^l'
    let numresults = len(getloclist(0))
  else
    let numresults = len(getqflist())
  endif

  if numresults == 0
    call eclim#util#EchoInfo('No results found.')
  endif
endfunction " }}}

" SaveSettings() {{{
function! s:SaveSettings ()
  " don't check modified since undo seems to not set the modified flag
  "if &modified
    let tempfile = substitute(tempname(), '\', '/', 'g')
    silent exec 'write! ' . escape(tempfile, ' ')
    let command = s:command_update
    let command = substitute(command, '<project>', b:project, '')
    let command = substitute(command, '<settings>', tempfile, '')

    let result = eclim#ExecuteEclim(command)
    if result =~ '|'
      call eclim#util#EchoError
        \ ("Operation contained errors.  See quickfix for details.")
      call eclim#util#SetLocationList
        \ (eclim#util#ParseLocationEntries(split(result, '\n')))
    else
      call eclim#util#SetLocationList([], 'r')
      call eclim#util#Echo(result)
    endif

    setlocal nomodified
  "endif
endfunction " }}}

" GetCurrentProjectFile() {{{
" Gets the path to the project file for the project that the current file is in.
function! eclim#project#GetCurrentProjectFile ()
  let dir = fnamemodify(expand('%:p'), ':h')
  let dir = substitute(escape(dir, ' '), '\', '/', 'g')

  let projectFile = eclim#util#Findfile('.project', dir . ';')
  while 1
    if filereadable(projectFile)
      return substitute(fnamemodify(projectFile, ':p'), '\', '/', 'g')
    endif
    if projectFile == '' && dir != getcwd()
      let dir = getcwd()
    else
      break
    endif
  endwhile
  return ''
endfunction " }}}

" GetCurrentProjectName() {{{
" Gets the project name that the current file is in.
function! eclim#project#GetCurrentProjectName ()
  let projectName = ''
  let projectFile = eclim#project#GetCurrentProjectFile()
  if projectFile != ''
    let cmd = winrestcmd()

    silent exec 'sview ' . escape(projectFile, ' ')
    setlocal noswapfile
    setlocal bufhidden=delete

    call cursor(1,1)
    let line = search('<name\s*>', 'wnc')
    if line != 0
      let projectName = substitute(getline(line), '.\{-}>\(.*\)<.*', '\1', '')
    endif
    silent close

    silent exec cmd

    " can potentially screw up display, like when used durring startup
    " (project/tree.vim), it causes display for :Ant, :make commands to be all
    " screwed up.
    "redraw
  endif

  return projectName
endfunction " }}}

" GetCurrentProjectRoot() {{{
" Gets the project root dir for the project that the current file is in.
function! eclim#project#GetCurrentProjectRoot ()
  return fnamemodify(eclim#project#GetCurrentProjectFile(), ':h')
endfunction " }}}

" GetProjectRelativeFilePath (file) {{{
" Gets the project relative path for the given file.
function! eclim#project#GetProjectRelativeFilePath (file)
  let file = substitute(a:file, '\', '/', 'g')
  let result = substitute(file, eclim#project#GetCurrentProjectRoot(), '', '')
  if result =~ '^/'
    let result = result[1:]
  endif
  return result
endfunction " }}}

" GetProjectDirs() {{{
" Gets list of all project root directories.
function! eclim#project#GetProjectDirs ()
  let projects = split(eclim#ExecuteEclim(s:command_projects), '\n')
  if len(projects) == 1 && projects[0] == '0'
    return []
  endif

  call map(projects,
    \ "substitute(v:val, '.\\{-}\\s\\+-\\s.\\{-}\\s\\+-\\s\\(.*\\)', '\\1', '')")

  return projects
endfunction " }}}

" GetProjectNames() {{{
" Gets list of all project names.
function! eclim#project#GetProjectNames ()
  let projects = split(eclim#ExecuteEclim(s:command_projects), '\n')
  if len(projects) == 1 && projects[0] == '0'
    return []
  endif

  call map(projects, "substitute(v:val, '\\(.\\{-}\\)\\s\\+-\\s\\+.*', '\\1', '')")

  return projects
endfunction " }}}

" GetProjectNatureAliases() {{{
" Gets list of all project nature aliases.
function! eclim#project#GetProjectNatureAliases ()
  let aliases = split(eclim#ExecuteEclim(s:command_nature_aliases), '\n')
  if len(aliases) == 1 && aliases[0] == '0'
    return []
  endif

  return aliases
endfunction " }}}

" GetProjectRoot(project) {{{
" Gets the project root dir for the supplied project name.
function! eclim#project#GetProjectRoot (project)
  let projects = split(eclim#ExecuteEclim(s:command_projects), '\n')
  for project in projects
    if project =~ '^' . a:project . ' '
      return substitute(project, '.\{-}\s\+-\s\+.\{-}\s\+-\s\+\(.*\)', '\1', '')
    endif
  endfor

  return ""
endfunction " }}}

" GetProjectSetting(setting) {{{
function! eclim#project#GetProjectSetting (setting)
  let project = eclim#project#GetCurrentProjectName()
  if project != ""
    let command = s:command_project_setting
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<setting>', a:setting, '')

    let result = split(eclim#ExecuteEclim(command), '\n')
    call filter(result, 'v:val !~ "^\\s*#"')

    if len(result) == 0
      call eclim#util#EchoWarning("Setting '" . a:setting . "' does not exist.")
      return ""
    endif

    return substitute(result[0], '.\{-}=\(.*\)', '\1', '')
  endif
  return ""
endfunction " }}}

" IsCurrentFileInProject(...) {{{
" Determines if the current file is in a project directory.
" Accepts an optional arg that determines if a message is displayed to the
" user if the file is not in a project (defaults to 1, to display the
" message).
function! eclim#project#IsCurrentFileInProject (...)
  if eclim#project#GetCurrentProjectName() == ''
    if a:0 == 0 || a:1
      call eclim#util#EchoError('Unable to determine project. ' .
        \ 'Check that the current file is in a valid project.')
    endif
    return 0
  endif
  return 1
endfunction " }}}

" CommandCompleteProject(argLead, cmdLine, cursorPos) {{{
" Custom command completion for project names.
function! eclim#project#CommandCompleteProject (argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')

  let projects = eclim#project#GetProjectNames()
  if cmdLine !~ '[^\\]\s$'
    call filter(projects, 'v:val =~ "^' . argLead . '"')
  endif

  return projects
endfunction " }}}

" CommandCompleteProjectCreate(argLead, cmdLine, cursorPos) {{{
" Custom command completion for ProjectCreate
function! eclim#project#CommandCompleteProjectCreate (argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseArgs(cmdLine)
  let argLead = len(args) > 1 ? args[len(args) - 1] : ""

  " complete dirs for first arg
  if cmdLine =~ '^' . args[0] . '\s\+' . escape(argLead, '~.\') . '$'
    return eclim#util#CommandCompleteDir(argLead, a:cmdLine, a:cursorPos)
  endif

  " complete options
  if cmdLine =~ '^' . args[0] . '\s\+' . escape(argLead, '~.\') . '\s\+$'
    return s:CommandCompleteProjectCreateOptions(argLead, a:cmdLine, a:cursorPos)
  endif
  if argLead =~ '-$'
    return s:CommandCompleteProjectCreateOptions(argLead, a:cmdLine, a:cursorPos)
  endif

  if cmdLine =~ '\s-n\s.*$' && stridx(cmdLine, ' -d') < stridx(cmdLine, ' -n')
    let aliases = eclim#project#GetProjectNatureAliases()
    if cmdLine !~ '[^\\]\s$'
      call filter(aliases, 'v:val =~ "^' . argLead . '"')
    endif

    return aliases
  endif

  " for remaining args, complete project name.
  return eclim#project#CommandCompleteProject(argLead, a:cmdLine, a:cursorPos)
endfunction " }}}

" CommandCompleteProjectCreateOptions(argLead, cmdLine, cursorPos) {{{
" Custom command completion for ProjectCreate options.
function! s:CommandCompleteProjectCreateOptions (argLead, cmdLine, cursorPos)
  let options = ['-n', '-d', '-p']
  if a:cmdLine =~ '\s-n\>'
    call remove(options, 0)
  endif
  if a:cmdLine =~ '\s-d\>'
    call remove(options, 1)
  endif
  return options
endfunction " }}}

" CommandCompleteProjectRelative(argLead, cmdLine, cursorPos) {{{
" Custom command completion for project relative files and directories.
function! eclim#project#CommandCompleteProjectRelative (argLead, cmdLine, cursorPos)
  let dir = eclim#project#GetCurrentProjectRoot()

  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseArgs(cmdLine)
  let argLead = len(args) > 1 ? args[len(args) - 1] : ""

  let results = split(eclim#util#Glob(dir . '/' . argLead . '*', 1), '\n')
  call map(results, "substitute(v:val, '\\', '/', 'g')")
  call map(results, 'isdirectory(v:val) ? v:val . "/" : v:val')
  call map(results, 'substitute(v:val, dir, "", "")')
  call map(results, 'substitute(v:val, "^\\(/\\|\\\\\\)", "", "g")')
  call map(results, "substitute(v:val, ' ', '\\\\ ', 'g')")

  return eclim#util#ParseCommandCompletionResults(argLead, results)
endfunction " }}}

" vim:ft=vim:fdm=marker
