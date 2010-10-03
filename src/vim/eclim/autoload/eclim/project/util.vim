" Author:  Eric Van Dewoestine
"
" Description: {{{
"
" License:
"
" Copyright (C) 2005 - 2010  Eric Van Dewoestine
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
if !exists('g:EclimTodoSearchPattern')
  let g:EclimTodoSearchPattern = '\(\<fixme\>\|\<todo\>\)\c'
endif

if !exists('g:EclimTodoSearchExtensions')
  let g:EclimTodoSearchExtensions = ['java', 'py', 'php', 'jsp', 'xml', 'html']
endif
" }}}

" Script Variables {{{
let s:command_create = '-command project_create -f "<folder>"'
let s:command_create_name = ' -p "<name>"'
let s:command_create_natures = ' -n <natures>'
let s:command_create_depends = ' -d <depends>'
let s:command_import = '-command project_import -f "<folder>"'
let s:command_delete = '-command project_delete -p "<project>"'
let s:command_rename = '-command project_rename -p "<project>" -n "<name>"'
let s:command_move = '-command project_move -p "<project>" -d "<dir>"'
let s:command_refresh = '-command project_refresh -p "<project>"'
let s:command_refresh_file =
  \ '-command project_refresh_file -p "<project>" -f "<file>"'
let s:command_projects = '-command projects'
let s:command_project_list = '-command project_list'
let s:command_project_by_resource = '-command project_by_resource -f "<file>"'
let s:command_project_info = '-command project_info -p "<project>"'
let s:command_project_settings = '-command project_settings -p "<project>"'
let s:command_project_setting = '-command project_setting -p "<project>" -s <setting>'
let s:command_project_update = '-command project_update -p "<project>"'
let s:command_update = '-command project_update -p "<project>" -s "<settings>"'
let s:command_open = '-command project_open -p "<project>"'
let s:command_close = '-command project_close -p "<project>"'
let s:command_nature_aliases = '-command project_nature_aliases'
let s:command_natures = '-command project_natures'
let s:command_nature_add =
  \ '-command project_nature_add -p "<project>" -n "<natures>"'
let s:command_nature_remove =
  \ '-command project_nature_remove -p "<project>" -n "<natures>"'

let s:workspace_projects = {}
" }}}

" ClearProjectsCache() {{{
" Flush the cached list of projects.
function! eclim#project#util#ClearProjectsCache()
  let s:workspace_projects = {}
endfunction " }}}

" ProjectCD(scope) {{{
" Change the current working directory to the current project root.
function! eclim#project#util#ProjectCD(scope)
  let dir = eclim#project#util#GetCurrentProjectRoot()
  if a:scope == 0
    exec 'cd ' . escape(dir, ' ')
  elseif a:scope == 1
    exec 'lcd ' . escape(dir, ' ')
  endif
endfunction " }}}

" ProjectCreate(args) {{{
" Creates a project at the supplied folder
function! eclim#project#util#ProjectCreate(args)
  let args = eclim#util#ParseCmdLine(a:args)

  let folder = fnamemodify(expand(args[0]), ':p')
  let folder = substitute(folder, '\', '/', 'g')
  if has('win32unix')
    let folder = eclim#cygwin#WindowsPath(folder)
  endif
  let command = substitute(s:command_create, '<folder>', folder, '')

  let name = substitute(a:args, '.* -p\s\+\(.\{-}\)\(\s\+-\(d\|n\)\>.*\|$\)', '\1', '')
  if name != a:args
    let command .= substitute(s:command_create_name, '<name>', name, '')
  endif

  let natureIds = []
  let natures = substitute(a:args, '.* -n\s\+\(.\{-}\)\(\s\+-\(d\|p\)\>.*\|$\)', '\1', '')
  if natures != a:args
    let natures = substitute(natures, '\s\+', ',', 'g')
    let natureIds = split(natures, ',')
    let command .= substitute(s:command_create_natures, '<natures>', natures, '')
  endif

  let depends = substitute(a:args, '.* -d\s\+\(.\{-}\)\(\s\+-\(n\|p\)\>.*\|$\)', '\1', '')
  if depends != a:args
    let depends = substitute(depends, '\s\+', ',', 'g')
    let command .= substitute(s:command_create_depends, '<depends>', depends, '')
  endif

  let workspace = eclim#eclipse#ChooseWorkspace(folder)
  if workspace == '0'
    return
  endif

  " execute any pre-project creation hooks
  for nature in natureIds
    exec 'runtime autoload/eclim/' . nature . '/project.vim'
    try
      let ProjectPre = function('eclim#' . nature . '#project#ProjectCreatePre')
      if !ProjectPre(folder)
        return
      endif
    catch /E\(117\|700\):.*/
      " ignore
    endtry
  endfor

  let port = eclim#client#nailgun#GetNgPort(workspace)
  let result = eclim#ExecuteEclim(command, port)
  if result != '0'
    call eclim#util#Echo(result)
    call eclim#project#util#ClearProjectsCache()
  endif

  " execute any post-project creation hooks
  for nature in natureIds
    try
      let ProjectPost = function('eclim#' . nature . '#project#ProjectCreatePost')
      call ProjectPost(folder)
    catch /E\(117\|700\):.*/
      " ignore
    endtry
  endfor
endfunction " }}}

" ProjectImport(arg) {{{
" Import a project from the supplied folder
function! eclim#project#util#ProjectImport(arg)
  let folder = fnamemodify(expand(a:arg), ':p')
  let folder = substitute(folder, '\', '/', 'g')
  if has('win32unix')
    let folder = eclim#cygwin#WindowsPath(folder)
  endif
  let command = substitute(s:command_import, '<folder>', folder, '')

  let workspace = eclim#eclipse#ChooseWorkspace(folder)
  if workspace == '0'
    return
  endif
  let port = eclim#client#nailgun#GetNgPort(workspace)

  let result = eclim#ExecuteEclim(command, port)
  if result != '0'
    call eclim#util#Echo(result)
    call eclim#project#util#ClearProjectsCache()
  endif
endfunction " }}}

" ProjectDelete(name) {{{
" Deletes a project with the supplied name.
function! eclim#project#util#ProjectDelete(name)
  let command = substitute(s:command_delete, '<project>', a:name, '')
  let port = eclim#project#util#GetProjectPort(a:name)
  let result = eclim#ExecuteEclim(command, port)
  if result != '0'
    call eclim#util#Echo(result)
    call eclim#project#util#ClearProjectsCache()
  endif
endfunction " }}}

" ProjectRename(args) {{{
" Renames a project.
function! eclim#project#util#ProjectRename(args)
  let args = eclim#util#ParseCmdLine(a:args)
  if len(args) == 1
    if !eclim#project#util#IsCurrentFileInProject()
      return
    endif
    let project = eclim#project#util#GetCurrentProjectName()
    let name = args[0]
  else
    let project = args[0]
    let name = args[1]
  endif

  if exists('g:EclimProjectRenamePrompt') && !g:EclimProjectRenamePrompt
    let response = 1
  else
    let response = eclim#util#PromptConfirm(
      \ printf("Rename project '%s' to '%s'", project, name),
      \ g:EclimInfoHighlight)
  endif

  if response == 1
    let command = s:command_rename
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<name>', name, '')
    call s:ProjectMove(project, name, command)
  endif
endfunction " }}}

" ProjectMove(args) {{{
" Moves a project.
function! eclim#project#util#ProjectMove(args)
  let args = eclim#util#ParseCmdLine(a:args)
  if len(args) == 1
    if !eclim#project#util#IsCurrentFileInProject()
      return
    endif
    let project = eclim#project#util#GetCurrentProjectName()
    let dir = args[0]
  else
    let project = args[0]
    let dir = args[1]
  endif
  let dir = expand(dir)
  let dir = substitute(fnamemodify(dir, ':p'), '\', '/', 'g')
  if has('win32unix')
    let dir = eclim#cygwin#WindowsPath(dir)
  endif

  if exists('g:EclimProjectMovePrompt') && !g:EclimProjectMovePrompt
    let response = 1
  else
    let response = eclim#util#PromptConfirm(
      \ printf("Move project '%s' to '%s'", project, dir),
      \ g:EclimInfoHighlight)
  endif

  if response == 1
    let command = s:command_move
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<dir>', dir, '')
    call s:ProjectMove(project, project, command)
  endif
endfunction " }}}

" s:ProjectMove(oldname, newname, command) {{{
function! s:ProjectMove(oldname, newname, command)
  let cwd = substitute(getcwd(), '\', '/', 'g')
  let cwd_return = 1
  let oldpath = eclim#project#util#GetProjectRoot(a:oldname)

  let curwin = winnr()
  try
    " cd to home to avoid folder renaming issues on windows.
    cd ~

    " turn off swap files temporarily to avoid issues with folder renaming.
    let bufend = bufnr('$')
    let bufnum = 1
    while bufnum <= bufend
      if bufexists(bufnum)
        call setbufvar(bufnum, 'save_swapfile', getbufvar(bufnum, '&swapfile'))
        call setbufvar(bufnum, '&swapfile', 0)
      endif
      let bufnum = bufnum + 1
    endwhile

    " write all changes before moving
    wall

    let port = eclim#project#util#GetProjectPort(a:oldname)
    let result = eclim#ExecuteEclim(a:command, port)
    if result == "0"
      return
    endif
    call eclim#project#util#ClearProjectsCache()
    let newpath = eclim#project#util#GetProjectRoot(a:newname)
    if cwd =~ '^' . oldpath
      exec 'cd ' . substitute(cwd, oldpath, newpath, '')
      let cwd_return = 0
    endif

    " reload files affected by the project renaming
    let bufnum = 1
    while bufnum <= bufend
      if buflisted(bufnum)
        let path = substitute(fnamemodify(bufname(bufnum), ':p'), '\', '/', 'g')
        if path =~ '^' . oldpath
          let path = substitute(path, oldpath, newpath, '')
          if filereadable(path)
            let winnr = bufwinnr(bufnum)
            if winnr != -1
              exec winnr . 'winc w'
              exec 'edit ' . eclim#util#Simplify(path)
            endif
            exec 'bdelete ' . bufnum
          endif
        endif
      endif
      let bufnum = bufnum + 1
    endwhile

  finally
    exec curwin 'winc w'
    if cwd_return
      exec 'cd ' . escape(cwd, ' ')
    endif

    " re-enable swap files
    let bufnum = 1
    while bufnum <= bufend
      if bufexists(bufnum)
        let save_swapfile = getbufvar(bufnum, 'save_swapfile')
        if save_swapfile != ''
          call setbufvar(bufnum, '&swapfile', save_swapfile)
        endif
      endif
      let bufnum = bufnum + 1
    endwhile
  endtry

  call eclim#util#Echo(result)
endfunction " }}}

" ProjectRefreshAll() {{{
" Refresh all projects.
function! eclim#project#util#ProjectRefreshAll()
  call eclim#project#util#ClearProjectsCache()
  let projects = eclim#project#util#GetProjectNames()
  for project in projects
    call eclim#project#util#ProjectRefresh(project, 0)
  endfor
  call eclim#util#Echo('Done.')
endfunction " }}}

" ProjectRefresh(args, [clear_cache]) {{{
" Refresh the requested projects.
function! eclim#project#util#ProjectRefresh(args, ...)
  if a:0 == 0 || a:000[0] == 1
    call eclim#project#util#ClearProjectsCache()
  endif

  if a:args != ''
    let projects = eclim#util#ParseCmdLine(a:args)
  else
    if !eclim#project#util#IsCurrentFileInProject()
      return
    endif
    let project = eclim#project#util#GetCurrentProjectName()
    let projects = [project]
  endif

  for project in projects
    call eclim#util#Echo("Updating project '" . project . "'...")
    let command = substitute(s:command_refresh, '<project>', project, '')
    let port = eclim#project#util#GetProjectPort(project)
    call eclim#util#Echo(eclim#ExecuteEclim(command, port))
  endfor

  if len(projects) > 1
    call eclim#util#Echo('Done.')
  endif
endfunction " }}}

" ProjectInfo(project) {{{
" Echos info for the current or supplied project.
function! eclim#project#util#ProjectInfo(project)
  let project = a:project
  if project == ''
    let project = eclim#project#util#GetCurrentProjectName()
  endif
  if project == ''
    call eclim#project#util#UnableToDetermineProject()
    return
  endif

  let command = substitute(s:command_project_info, '<project>', project, '')
  let port = eclim#project#util#GetProjectPort(project)
  let result = eclim#ExecuteEclim(command, port)
  if result != '0'
    call eclim#util#Echo(result)
  endif
endfunction " }}}

" ProjectOpen(name) {{{
" Open the requested project.
function! eclim#project#util#ProjectOpen(name)
  let name = a:name
  if name == ''
    if !eclim#project#util#IsCurrentFileInProject()
      return
    endif
    let name = eclim#project#util#GetCurrentProjectName()
  endif

  let command = substitute(s:command_open, '<project>', name, '')
  let port = eclim#project#util#GetProjectPort(name)
  let result = eclim#ExecuteEclim(command, port)
  if result != '0'
    call eclim#util#Echo(result)
  endif
endfunction " }}}

" ProjectClose(name) {{{
" Close the requested project.
function! eclim#project#util#ProjectClose(name)
  let name = a:name
  if name == ''
    if !eclim#project#util#IsCurrentFileInProject()
      return
    endif
    let name = eclim#project#util#GetCurrentProjectName()
  endif

  let command = substitute(s:command_close, '<project>', name, '')
  let port = eclim#project#util#GetProjectPort(name)
  let result = eclim#ExecuteEclim(command, port)
  if result != '0'
    call eclim#util#Echo(result)
  endif
endfunction " }}}

" ProjectList(workspace) {{{
" Lists all the projects that exist in the supplied workspace.
function! eclim#project#util#ProjectList(workspace)
  let workspace = a:workspace
  if workspace == ''
    let workspace = eclim#eclipse#ChooseWorkspace()
    if workspace == '0'
      return
    endif
  endif

  let port = eclim#client#nailgun#GetNgPort(workspace)
  let projects = split(eclim#ExecuteEclim(s:command_project_list, port), '\n')
  if len(projects) == 0
    call eclim#util#Echo("No projects.")
  endif
  if len(projects) == 1 && projects[0] == '0'
    return
  endif
  call eclim#util#Echo(join(projects, "\n"))
endfunction " }}}

" ProjectNatures(project) {{{
" Prints nature info one or all projects.
function! eclim#project#util#ProjectNatures(project)
  let command = s:command_natures
  if a:project != ''
    let command .= ' -p "' . a:project . '"'
    let port = eclim#project#util#GetProjectPort(a:project)
    let projects = split(eclim#ExecuteEclim(command, port), '\n')
  else
    let projects = []
    for workspace in eclim#eclipse#GetAllWorkspaceDirs()
      let port = eclim#client#nailgun#GetNgPort(workspace)
      let results = split(eclim#ExecuteEclim(command, port), '\n')
      if len(results) == 1 && results[0] == '0'
        continue
      endif
      let projects += results
    endfor
  endif

  if len(projects) == 0
    call eclim#util#Echo("No projects.")
  endif
  if len(projects) == 1 && projects[0] == '0'
    return
  endif

  call eclim#util#Echo(join(projects, "\n"))
endfunction " }}}

" ProjectNatureModify(command, args) {{{
" Modifies one or more natures for the specified project.
function! eclim#project#util#ProjectNatureModify(command, args)
  let args = eclim#util#ParseCmdLine(a:args)

  let project = args[0]
  let natures = join(args[1:], ',')
  let command = a:command == 'add' ? s:command_nature_add : s:command_nature_remove
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<natures>', natures, '')

  let port = eclim#project#util#GetProjectPort(project)
  let result = eclim#ExecuteEclim(command, port)
  if result != '0'
    call eclim#util#Echo(result)
  endif
endfunction " }}}

" ProjectSettings(project) {{{
" Opens a window that can be used to edit a project's settings.
function! eclim#project#util#ProjectSettings(project)
  let project = a:project
  if project == ''
    let project = eclim#project#util#GetCurrentProjectName()
  endif
  if project == ''
    call eclim#project#util#UnableToDetermineProject()
    return
  endif

  let command = substitute(s:command_project_settings, '<project>', project, '')
  let port = eclim#project#util#GetProjectPort(project)
  if eclim#util#TempWindowCommand(command, project . "_settings", port)
    exec "lcd " . escape(eclim#project#util#GetProjectRoot(project), ' ')
    setlocal buftype=acwrite
    setlocal filetype=jproperties
    setlocal noreadonly
    setlocal modifiable
    setlocal foldmethod=marker
    setlocal foldmarker={,}
    setlocal foldlevel=0

    let b:project = project
    augroup project_settings
      autocmd! BufWriteCmd <buffer>
      autocmd BufWriteCmd <buffer> call <SID>SaveSettings()
    augroup END
  endif
endfunction " }}}

" ProjectUpdate() {{{
" Executes a project update which may also validate nature specific resource
" file.
function! eclim#project#util#ProjectUpdate()
  let name = eclim#project#util#GetCurrentProjectName()
  if name == ''
    call eclim#util#EchoError('Unable to determine the project.')
    return
  endif

  let command = substitute(s:command_project_update, '<project>', name, '')

  let result = eclim#ExecuteEclim(command)
  if result =~ '|'
    let errors = eclim#util#ParseLocationEntries(
      \ split(result, '\n'), g:EclimValidateSortResults)
    call eclim#util#SetLocationList(errors)
  else
    call eclim#util#ClearLocationList()
    call eclim#util#Echo(result)
  endif
endfunction " }}}

" ProjectGrep(command, args) {{{
" Executes the supplied vim grep command with the specified pattern against
" one or more file patterns.
function! eclim#project#util#ProjectGrep(command, args)
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let bufnum = bufnr('%')
  let project_dir = eclim#project#util#GetCurrentProjectRoot()
  let cwd = getcwd()
"  let save_opt = &eventignore
"  set eventignore=all
  try
    silent exec 'lcd ' . escape(project_dir, ' ')
    silent! exec a:command . ' ' . a:args
  finally
"    let &eventignore = save_opt
    silent exec 'lcd ' . escape(cwd, ' ')
    " force quickfix / location list signs to update.
    call eclim#display#signs#Update()
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

" ProjectTab(project) {{{
" Opens a new tab with the project tree and tab relative working directory for
" the specified project.
function! eclim#project#util#ProjectTab(project)
  let names = eclim#project#util#GetProjectNames()
  if index(names, a:project) == -1
    call eclim#util#EchoError("No project '" . a:project . "' found.")
    return
  endif
  if winnr('$') > 1 || expand('%') != '' ||
   \ &modified || line('$') != 1 || getline(1) != ''
    tablast | tabnew
  endif
  call eclim#common#util#Tcd(eclim#project#util#GetProjectRoot(a:project))
  call eclim#project#tree#ProjectTree(a:project)
endfunction " }}}

" TreeTab(title, dir) {{{
" Like ProjectTab, but opens for an arbitrary directory.
function! eclim#project#util#TreeTab(title, dir)
  let dir = fnamemodify(a:dir, ':p')
  let dir = substitute(dir, '/$', '', '')
  if !isdirectory(dir)
    call eclim#util#EchoError('Directory does not exist: ' . dir)
    return
  endif

  if winnr('$') > 1 || expand('%') != '' ||
   \ &modified || line('$') != 1 || getline(1) != ''
    tablast | tabnew
    if dir == expand('~')
      tabmove 0
    endif
  endif
  call eclim#common#util#Tcd(dir)
  call eclim#project#tree#ProjectTreeOpen([dir], [dir], a:title)
endfunction " }}}

" Todo() {{{
" Show the todo tags of the curent file in the location list.
function! eclim#project#util#Todo()
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let path = expand('%:p')
  silent! exec 'lvimgrep /' . g:EclimTodoSearchPattern . '/gj ' . path
  if !empty(getloclist(0))
    lopen
  else
    call eclim#util#Echo('No Results found')
  endif
endfunction " }}}

" ProjectTodo() {{{
" Show the todo tags of the whole project in the location list.
function! eclim#project#util#ProjectTodo()
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let path = eclim#project#util#GetCurrentProjectRoot()
  if len(g:EclimTodoSearchExtensions) > 0
    let paths = map(copy(g:EclimTodoSearchExtensions), 'path . "/**/*." . v:val')

    silent! exec 'lvimgrep /' . g:EclimTodoSearchPattern . '/gj ' . paths[0]
    for path in paths[1:]
      silent! exec 'lvimgrepadd /' . g:EclimTodoSearchPattern . '/gj ' . path
    endfor

    if !empty(getloclist(0))
      lopen
    else
      call eclim#util#Echo('No Results found')
    endif
  endif
endfunction " }}}

" SaveSettings() {{{
function! s:SaveSettings()
  call eclim#SaveSettings(s:command_update, b:project)
endfunction " }}}

" GetCurrentProjectName() {{{
" Gets the project name that the current file is in.
function! eclim#project#util#GetCurrentProjectName()
  let project = eclim#project#util#GetProject(expand('%:p'))
  return len(project) > 0 ? project.name : ''
endfunction " }}}

" GetCurrentProjectRoot() {{{
" Gets the project root dir for the project that the current file is in.
function! eclim#project#util#GetCurrentProjectRoot()
  let project = eclim#project#util#GetProject(expand('%:p'))
  return len(project) > 0 ? project.path : ''
endfunction " }}}

" GetProjectWorkspace(name) {{{
" Gets the workspace that a project belongs to.
function! eclim#project#util#GetProjectWorkspace(name)
  let project = {}
  for p in eclim#project#util#GetProjects()
    if p.name == a:name
      let project = p
      break
    endif
  endfor
  return get(project, 'workspace', '')
endfunction " }}}

" GetProjectRelativeFilePath([file]) {{{
" Gets the project relative path for the current or supplied file.
function! eclim#project#util#GetProjectRelativeFilePath(...)
  let file = a:0 == 0 ? expand('%:p') : a:1
  let project = eclim#project#util#GetProject(file)

  let file = substitute(fnamemodify(file, ':p'), '\', '/', 'g')
  let pattern = '\(/\|$\)'
  if has('win32') || has('win64')
    let pattern .= '\c'
  endif
  let result = substitute(file, get(project, 'path', '') . pattern, '', '')

  " handle file in linked folder
  if result == file
    for name in keys(project.links)
      if file =~ '^' . project.links[name] . pattern
        let result = substitute(file, project.links[name], name, '')
      endif
    endfor
  endif

  if result != file && result =~ '^/'
    let result = result[1:]
  endif
  return result
endfunction " }}}

" GetProjects() {{{
" Returns a list of project dictionaries containing the following properties:
"   workspace: The path of the workspace the project belongs to.
"   name: The name of the project.
"   path: The root path of the project.
"   links: List of linked paths.
function! eclim#project#util#GetProjects()
  let workspaces = eclim#eclipse#GetAllWorkspaceDirs()
  if len(s:workspace_projects) != len(workspaces)
    for workspace in workspaces
      let result = eclim#ExecuteEclim(
        \ s:command_projects, eclim#client#nailgun#GetNgPort(workspace))
      if result == '0'
        continue
      endif
      let results = split(result, "\n")
      let projects = []
      for line in results
        let name = substitute(line, '\(.\{-}\):.*', '\1', '')
        let paths = split(substitute(line, '.\{-}:\(.*\)', '\1', ''), ',')
        if has('win32unix')
          let paths[0] = eclim#cygwin#CygwinPath(paths[0])
        endif

        let links = {}
        for p in paths[1:]
          let linkname = substitute(p, '\(.\{-}\):.*', '\1', '')
          let linkpath = substitute(p, '.\{-}:\(.*\)', '\1', '')
          if has('win32unix')
            let linkpath = eclim#cygwin#CygwinPath(linkpath)
          endif
          let links[linkname] = linkpath
        endfor
        call add(projects, {
            \ 'workspace': workspace,
            \ 'name': name,
            \ 'path': paths[0],
            \ 'links': links,
          \ })
      endfor
      let s:workspace_projects[workspace] = projects
    endfor
  endif

  let all = []
  for projects in values(s:workspace_projects)
    let all += copy(projects)
  endfor
  return all
endfunction " }}}

" GetProject(file) {{{
function! eclim#project#util#GetProject(file)
  let path = substitute(fnamemodify(a:file, ':p'), '\', '/', 'g')
  let dir = fnamemodify(path, ':h')
  let pattern = '\(/\|$\)'
  if has('win32') || has('win64')
    let pattern .= '\c'
  endif

  let projects = eclim#project#util#GetProjects()

  " sort projects depth wise by path to properly support nested projects.
  call sort(projects, 's:ProjectSortPathDepth')

  for project in projects
    if dir =~ '^' . project.path . pattern
      return project
    endif

    " check linked folders
    for name in keys(project.links)
      if dir =~ '^' . project.links[name] . pattern
        return project
      endif
    endfor
  endfor
  return {}
endfunction " }}}

" s:ProjectSortPathDepth(p1, p2) {{{
function! s:ProjectSortPathDepth(p1, p2)
  return len(a:p2.path) - len(a:p1.path)
endfunction " }}}

" GetProjectDirs() {{{
" Gets list of all project root directories.
function! eclim#project#util#GetProjectDirs()
  return map(eclim#project#util#GetProjects(), 'v:val.path')
endfunction " }}}

" GetProjectNames(...) {{{
" Gets list of all project names, with optional filter by the supplied nature
" alias.
function! eclim#project#util#GetProjectNames(...)
  " filter by nature
  if a:0 > 0 && a:1 != ''
    let projects = []
    let command = s:command_project_list . ' -n ' . a:1
    for workspace in eclim#eclipse#GetAllWorkspaceDirs()
      let port = eclim#client#nailgun#GetNgPort(workspace)
      let results = split(eclim#ExecuteEclim(command, port), '\n')
      if len(results) == 1 && results[0] == '0'
        continue
      endif
      let projects += results
    endfor

    call map(projects, "substitute(v:val, '\\(.\\{-}\\)\\s\\+-\\s\\+.*', '\\1', '')")

    return projects
  endif
  let names = map(eclim#project#util#GetProjects(), 'v:val.name')
  call sort(names)
  return names
endfunction " }}}

" GetProjectNatureAliases(...) {{{
" Gets list of all project nature aliases or a list of aliases associated with
" a project if the project name is supplied.
function! eclim#project#util#GetProjectNatureAliases(...)
  if a:0 > 0 && a:1 != ''
    let command = s:command_natures . ' -p "' . a:1 . '"'
    let result = eclim#ExecuteEclim(command)
    if result == '0'
      return []
    endif
    let aliases = split(substitute(result, '.\{-}\s-\s', '', ''), ' ')
    return aliases
  endif

  let aliases = split(eclim#ExecuteEclim(s:command_nature_aliases), '\n')
  if len(aliases) == 1 && aliases[0] == '0'
    return []
  endif

  return aliases
endfunction " }}}

" GetProjectPort(name) {{{
" Gets the nailgun port where the eclimd instance is running for the workspace
" that the project belongs to.
function! eclim#project#util#GetProjectPort(name)
  let workspace = eclim#project#util#GetProjectWorkspace(a:name)
  return eclim#client#nailgun#GetNgPort(workspace)
endfunction " }}}

" GetProjectRoot(name) {{{
" Gets the project root dir for the supplied project name.
function! eclim#project#util#GetProjectRoot(name)
  let project = {}
  for p in eclim#project#util#GetProjects()
    if p.name == a:name
      let project = p
      break
    endif
  endfor
  return get(project, 'path', '')
endfunction " }}}

" GetProjectSetting(setting) {{{
" Gets a project setting from eclim.  Returns '' if the setting does not
" exist, 0 if not in a project or an error occurs communicating with the
" server.
function! eclim#project#util#GetProjectSetting(setting)
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let project = eclim#project#util#GetCurrentProjectName()
  let command = s:command_project_setting
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<setting>', a:setting, '')

  let result = eclim#ExecuteEclim(command)
  if result == '0'
    return result
  endif

  if result == ''
    call eclim#util#EchoWarning("Setting '" . a:setting . "' does not exist.")
  endif
  return result
endfunction " }}}

" SetProjectSetting(setting, value) {{{
" Sets a project setting.
function! eclim#project#util#SetProjectSetting(setting, value)
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let project = eclim#project#util#GetCurrentProjectName()
  let command = s:command_project_setting
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<setting>', a:setting, '')
  let command .= ' -v "' . a:value . '"'

  call eclim#ExecuteEclim(command)
endfunction " }}}

" IsCurrentFileInProject(...) {{{
" Determines if the current file is in a project directory.
" Accepts an optional arg that determines if a message is displayed to the
" user if the file is not in a project(defaults to 1, to display the
" message).
function! eclim#project#util#IsCurrentFileInProject(...)
  if eclim#project#util#GetCurrentProjectName() == ''
    if (a:0 == 0 || a:1) && g:eclimd_running
      call eclim#util#EchoError('Unable to determine the project. ' .
        \ 'Check that the current file is in a valid project.')
    endif
    return 0
  endif
  return 1
endfunction " }}}

" RefreshFileBootstrap() {{{
" Boostraps a post write autocommand for updating files, which forces a
" refresh by the eclim project. The command should only be called as part of
" the a BufWritePre autocmd.
function! eclim#project#util#RefreshFileBootstrap()
  if eclim#project#util#GetCurrentProjectName() != '' && &modified
    augroup eclim_refresh_files_bootstrap
      autocmd!
      autocmd BufWritePost <buffer> call eclim#project#util#RefreshFile()
    augroup END
  endif
endfunction " }}}

" RefreshFile() {{{
" Refreshes the current files in eclipse.
function! eclim#project#util#RefreshFile()
  augroup eclim_refresh_files_bootstrap
    autocmd! BufWritePost <buffer>
  augroup END
  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()
  let command = s:command_refresh_file
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  call eclim#ExecuteEclim(command)
endfunction " }}}

" UnableToDetermineProject() {{{
function! eclim#project#util#UnableToDetermineProject()
  if g:eclimd_running
    call eclim#util#EchoError("Unable to determine the project. " .
      \ "Please specify a project name or " .
      \ "execute from a valid project directory.")
  endif
endfunction " }}}

" CommandCompleteProject(argLead, cmdLine, cursorPos) {{{
" Custom command completion for project names.
function! eclim#project#util#CommandCompleteProject(argLead, cmdLine, cursorPos)
  return eclim#project#util#CommandCompleteProjectByNature(
    \ a:argLead, a:cmdLine, a:cursorPos, '')
endfunction " }}}

" CommandCompleteProjectContainsThis(argLead, cmdLine, cursorPos) {{{
" Custom command completion for project names, filtering by those that contain
" a file with the same path as the current file, excluding the current
" project.
function! eclim#project#util#CommandCompleteProjectContainsThis(
  \ argLead, cmdLine, cursorPos)
  let names = eclim#project#util#CommandCompleteProject(
    \ a:argLead, a:cmdLine, a:cursorPos)

  let path = eclim#project#util#GetProjectRelativeFilePath()
  let project = eclim#project#util#GetCurrentProjectName()
  let projects = eclim#project#util#GetProjects()
  call filter(names, 'v:val != project && filereadable(eclim#project#util#GetProjectRoot(v:val) . "/" . path)')
  return names
endfunction " }}}

" CommandCompleteProjectByNature(argLead, cmdLine, cursorPos, nature) {{{
" Custom command completion for project names.
function! eclim#project#util#CommandCompleteProjectByNature(
    \ argLead, cmdLine, cursorPos, nature)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')

  let projects = eclim#project#util#GetProjectNames(a:nature)
  if cmdLine !~ '[^\\]\s$'
    let argLead = escape(escape(argLead, '~'), '~')
    call filter(projects, 'v:val =~ "^' . argLead . '"')
  endif

  return projects
endfunction " }}}

" CommandCompleteProjectCreate(argLead, cmdLine, cursorPos) {{{
" Custom command completion for ProjectCreate
function! eclim#project#util#CommandCompleteProjectCreate(argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]

  " complete dirs for first arg
  if cmdLine =~ '^' . args[0] . '\s\+' . escape(argLead, '~.\') . '$'
    return eclim#util#CommandCompleteDir(a:argLead, a:cmdLine, a:cursorPos)
  endif

  " complete nature aliases
  if cmdLine =~ '-n\s\+[^-]*$'
    let aliases = eclim#project#util#GetProjectNatureAliases()

    " if one alias already supplied complete options as well
    if cmdLine !~ '-n\s\+$' && argLead == ''
      let aliases =
        \ s:CommandCompleteProjectCreateOptions(argLead, a:cmdLine, a:cursorPos) +
        \ aliases
    endif

    if cmdLine !~ '[^\\]\s$'
      call filter(aliases, 'v:val =~ "^' . escape(escape(argLead, '~.\'), '\') . '"')
    endif

    return aliases
  endif

  " complete project dependencies
  if cmdLine =~ '-d\s\+[^-]*$'
    " if one dependency already supplied complete options as well
    if cmdLine !~ '-d\s\+$' && argLead == ''
      let options =
        \ s:CommandCompleteProjectCreateOptions(argLead, a:cmdLine, a:cursorPos)
      return options +
        \ eclim#project#util#CommandCompleteProject(argLead, a:cmdLine, a:cursorPos)
    endif
    return eclim#project#util#CommandCompleteProject(argLead, a:cmdLine, a:cursorPos)
  endif

  return s:CommandCompleteProjectCreateOptions(argLead, a:cmdLine, a:cursorPos)
endfunction " }}}

" CommandCompleteProjectCreateOptions(argLead, cmdLine, cursorPos) {{{
" Custom command completion for ProjectCreate options.
function! s:CommandCompleteProjectCreateOptions(argLead, cmdLine, cursorPos)
  let options = ['-n', '-d', '-p']
  if a:cmdLine =~ '\s-n\>'
    call remove(options, index(options, '-n'))
  endif
  if a:cmdLine =~ '\s-d\>'
    call remove(options, index(options, '-d'))
  endif
  if a:cmdLine =~ '\s-p\>'
    call remove(options, index(options, '-p'))
  endif
  return options
endfunction " }}}

" CommandCompleteProjectMove(argLead, cmdLine, cursorPos) {{{
" Custom command completion for ProjectMove
function! eclim#project#util#CommandCompleteProjectMove(argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]

  " complete dirs for second arg if first arg is a project name
  if len(args) > 1 && eclim#project#util#GetProjectRoot(args[1]) != '' &&
   \ cmdLine =~ '^' . args[0] . '\s\+' . args[1] . '\s\+' . escape(argLead, '~.\') . '$'
    return eclim#util#CommandCompleteDir(a:argLead, a:cmdLine, a:cursorPos)
  endif

  " attempt complete project and dir for first arg
  if cmdLine =~ '^' . args[0] . '\s\+' . escape(argLead, '~.\') . '$'
    let projects = []
    let dirs = eclim#util#CommandCompleteDir(a:argLead, a:cmdLine, a:cursorPos)
    if argLead !~ '[~]'
      let projects = eclim#project#util#CommandCompleteProject(
            \ argLead, a:cmdLine, a:cursorPos)
    endif
    return projects + dirs
  endif
  return []
endfunction " }}}

" CommandCompleteProjectRelative(argLead, cmdLine, cursorPos) {{{
" Custom command completion for project relative files and directories.
function! eclim#project#util#CommandCompleteProjectRelative(
    \ argLead, cmdLine, cursorPos)
  let dir = eclim#project#util#GetCurrentProjectRoot()
  if dir == '' && exists('b:project')
    let dir = eclim#project#util#GetProjectRoot(b:project)
  endif

  if dir == ''
    return []
  endif

  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : (len(args) > 0 ? args[len(args) - 1] : '')

  let results = split(eclim#util#Glob(dir . '/' . argLead . '*', 1), '\n')
  call map(results, "substitute(v:val, '\\', '/', 'g')")
  call map(results, 'isdirectory(v:val) ? v:val . "/" : v:val')
  call map(results, 'substitute(v:val, dir, "", "")')
  call map(results, 'substitute(v:val, "^\\(/\\|\\\\\\)", "", "g")')
  call map(results, "substitute(v:val, ' ', '\\\\ ', 'g')")

  return eclim#util#ParseCommandCompletionResults(argLead, results)
endfunction " }}}

" CommandCompleteAbsoluteOrProjectRelative(argLead, cmdLine, cursorPos) {{{
" Custom command completion for project relative files and directories.
function! eclim#project#util#CommandCompleteAbsoluteOrProjectRelative(
    \ argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)
  if len(args) > 0
    let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]
    if argLead =~ '^\(/\|[a-zA-Z]:\)'
      return eclim#util#CommandCompleteDir(a:argLead, a:cmdLine, a:cursorPos)
    endif
  endif
  return eclim#project#util#CommandCompleteProjectRelative(
    \ a:argLead, a:cmdLine, a:cursorPos)
endfunction " }}}

" CommandCompleteProjectNatureAdd(argLead, cmdLine, cursorPos) {{{
" Custom command completion for project names and natures.
function! eclim#project#util#CommandCompleteProjectNatureAdd(
    \ argLead, cmdLine, cursorPos)
  return s:CommandCompleteProjectNatureModify(
    \ a:argLead, a:cmdLine, a:cursorPos, function("s:AddAliases"))
endfunction
function! s:AddAliases(allAliases, projectAliases)
  let aliases = a:allAliases
  call filter(aliases, 'index(a:projectAliases, v:val) == -1')
  return aliases
endfunction " }}}

" CommandCompleteProjectNatureRemove(argLead, cmdLine, cursorPos) {{{
" Custom command completion for project names and natures.
function! eclim#project#util#CommandCompleteProjectNatureRemove(
    \ argLead, cmdLine, cursorPos)
  return s:CommandCompleteProjectNatureModify(
    \ a:argLead, a:cmdLine, a:cursorPos, function("s:RemoveAliases"))
endfunction
function! s:RemoveAliases(allAliases, projectAliases)
  return a:projectAliases
endfunction " }}}

" CommandCompleteProjectNatureModify(argLead, cmdLine, cursorPos) {{{
" Custom command completion for project names and natures.
function! s:CommandCompleteProjectNatureModify(
    \ argLead, cmdLine, cursorPos, aliasesFunc)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]

  " complete dirs for first arg
  if cmdLine =~ '^' . args[0] . '\s\+' . escape(argLead, '~.\') . '$'
    return eclim#project#util#CommandCompleteProject(argLead, a:cmdLine, a:cursorPos)
  endif

  let allAliases = eclim#project#util#GetProjectNatureAliases()
  call filter(allAliases, 'v:val != "none"')

  let projectAliases = eclim#project#util#GetProjectNatureAliases(args[1])
  let aliases = a:aliasesFunc(allAliases, projectAliases)
  if cmdLine !~ '[^\\]\s$'
    call filter(aliases, 'v:val =~ "^' . argLead . '"')
  endif

  call filter(aliases, 'index(args[2:], v:val) == -1')

  return aliases
endfunction " }}}

" vim:ft=vim:fdm=marker
