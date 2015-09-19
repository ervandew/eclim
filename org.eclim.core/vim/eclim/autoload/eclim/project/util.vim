" Author:  Eric Van Dewoestine
"
" Description: {{{
"
" License:
"
" Copyright (C) 2005 - 2015  Eric Van Dewoestine
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
  let g:EclimTodoSearchExtensions = [
      \ 'css',
      \ 'html',
      \ 'java',
      \ 'js',
      \ 'jsp',
      \ 'php',
      \ 'py',
      \ 'rb',
      \ 'sql',
      \ 'xml',
    \ ]
endif

if !exists('g:EclimProjectStatusLine')
  let g:EclimProjectStatusLine = '${name}'
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
let s:command_build = '-command project_build -p "<project>"'
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

function! eclim#project#util#ClearProjectsCache() " {{{
  " Flush the cached list of projects.
  let s:workspace_projects = {}
endfunction " }}}

function! eclim#project#util#ProjectCD(scope) " {{{
  " Change the current working directory to the current project root.
  let dir = eclim#project#util#GetCurrentProjectRoot()
  if a:scope == 0
    exec 'cd ' . escape(dir, ' ')
  elseif a:scope == 1
    exec 'lcd ' . escape(dir, ' ')
  endif
endfunction " }}}

function! eclim#project#util#ProjectCreate(args) " {{{
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

  " execute any pre-project creation hooks
  let hook_result = s:ProjectNatureHooks(natureIds, 'ProjectCreatePre', [folder])
  if type(hook_result) == g:NUMBER_TYPE && !hook_result
    return
  elseif type(hook_result) == g:STRING_TYPE && len(hook_result)
    let command .= ' -a ' . hook_result
  endif

  let result = eclim#Execute(command, {'dir': folder})
  if result != '0'
    call eclim#util#Echo(result)
    call eclim#project#util#ClearProjectsCache()
  endif

  " execute any post-project creation hooks
  call s:ProjectNatureHooks(natureIds, 'ProjectCreatePost', [folder])
endfunction " }}}

function! s:ProjectNatureHooks(natureIds, hookName, args) " {{{
  let results = ''
  for nature in a:natureIds
    if nature == 'none'
      continue
    endif

    exec 'runtime autoload/eclim/' . nature . '/project.vim'
    try
      let l:Hook = function('eclim#' . nature . '#project#' . a:hookName)
      let result = call(l:Hook, a:args)
      if type(result) == g:NUMBER_TYPE && !result
        return result
      endif
      if type(result) == g:STRING_TYPE
        if len(results)
          let results .= ' '
        endif
        let results .= result
      endif
    catch /E\(117\|700\):.*/
      " ignore
    endtry
  endfor

  if len(results)
    return results
  endif

  return 1
endfunction " }}}

function! eclim#project#util#ProjectImportDiscover(arg) " {{{
  " Recursively searches the given directory for any project files
  " and imports them.
  let projects = split(globpath(a:arg, '**/.project'), '\n')
  if (len(projects) == 0)
    call eclim#util#Echo("No projects found")
    return
  endif

  for project in projects
    call eclim#project#util#ProjectImport(fnamemodify(project, ':h'))
  endfor

  call eclim#util#Echo("Imported " . len(projects) . " projects.")
endfunction " }}}

function! eclim#project#util#ProjectImport(arg) " {{{
  let folder = fnamemodify(expand(a:arg), ':p')
  let folder = substitute(folder, '\', '/', 'g')
  if has('win32unix')
    let folder = eclim#cygwin#WindowsPath(folder)
  endif
  let command = substitute(s:command_import, '<folder>', folder, '')

  let naturesDict = {}
  for [key, value] in items(eclim#project#util#GetNatureAliasesDict())
    let naturesDict[value[-1]] = key
  endfor

  let natureIds = []
  let dotproject = folder . '/' . '.project'
  if filereadable(dotproject)
    for line in readfile(dotproject)
      if line =~ '^\s*<nature>'
        let id = substitute(line, '.*\<nature>\(.*\)</nature>.*', '\1', '')
        if has_key(naturesDict, id)
          call add(natureIds, naturesDict[id])
        endif
      endif
    endfor
    if !s:ProjectNatureHooks(natureIds, 'ProjectImportPre', [folder])
      return
    endif
  endif

  let result = eclim#Execute(command, {'dir': folder})
  if result != '0'
    let project = eclim#project#util#GetProject(folder)
    if !len(natureIds)
      let natureIds = eclim#project#util#GetProjectNatureAliases(
        \ get(project, 'name', ''))
    endif
    call s:ProjectNatureHooks(natureIds, 'ProjectImportPost', [project])
    call eclim#util#Echo(result)
    call eclim#project#util#ClearProjectsCache()
  endif
endfunction " }}}

function! eclim#project#util#ProjectDelete(name) " {{{
  let command = substitute(s:command_delete, '<project>', a:name, '')
  let result = eclim#Execute(command, {'project': a:name})
  if result != '0'
    call eclim#util#Echo(result)
    call eclim#project#util#ClearProjectsCache()
  endif
endfunction " }}}

function! eclim#project#util#ProjectRename(args) " {{{
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
      \ g:EclimHighlightInfo)
  endif

  if response == 1
    let command = s:command_rename
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<name>', name, '')
    call s:ProjectMove(project, name, command)
  endif
endfunction " }}}

function! eclim#project#util#ProjectMove(args) " {{{
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
      \ g:EclimHighlightInfo)
  endif

  if response == 1
    let command = s:command_move
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<dir>', dir, '')
    call s:ProjectMove(project, project, command)
  endif
endfunction " }}}

function! s:ProjectMove(oldname, newname, command) " {{{
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

    let result = eclim#Execute(a:command, {'project': a:oldname})
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

function! eclim#project#util#ProjectRefreshAll() " {{{
  call eclim#project#util#ClearProjectsCache()
  let projects = eclim#project#util#GetProjectNames()
  for project in projects
    call eclim#project#util#ProjectRefresh(project, 0)
  endfor
  call eclim#util#Echo('Done.')
endfunction " }}}

function! eclim#project#util#ProjectRefresh(args, ...) " {{{
  " Optional args:
  "   clear_cache: Clear the in memory project cache first

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
    call eclim#util#Echo(eclim#Execute(command, {'project': project}))
  endfor

  if len(projects) > 1
    call eclim#util#Echo('Done.')
  endif
endfunction " }}}

function! eclim#project#util#ProjectBuild(...) " {{{
  " Option args:
  "   project: The name of the project to build (use the current project
  "            otherwise)
  let project = a:0 > 0 ? a:1 : ''

  if project == ''
    if !eclim#project#util#IsCurrentFileInProject()
      return
    endif
    let project = eclim#project#util#GetCurrentProjectName()
  endif

  call eclim#util#Echo("Building project '" . project . "'...")
  let command = substitute(s:command_build, '<project>', project, '')
  let result = eclim#Execute(command, {'project': project})
  call eclim#project#problems#ProblemsUpdate('build')
  call eclim#util#Echo(result)
endfunction " }}}

function! eclim#project#util#ProjectInfo(project) " {{{
  let project = a:project
  if project == ''
    let project = eclim#project#util#GetCurrentProjectName()
  endif
  if project == ''
    call eclim#project#util#UnableToDetermineProject()
    return
  endif

  let command = substitute(s:command_project_info, '<project>', project, '')
  let result = eclim#Execute(command, {'project': project})
  if type(result) == g:DICT_TYPE
    let output =
        \ 'Name:      ' . result.name . "\n" .
        \ 'Path:      ' . result.path . "\n" .
        \ 'Workspace: ' . result.workspace . "\n" .
        \ 'Open:      ' . (result.open ? 'true' : 'false')
    if has_key(result, 'natures')
      let output .= "\n" . 'Natures:   ' . join(result.natures, ', ')
    endif
    if has_key(result, 'depends')
      let output .= "\n" . 'Depends On: ' . join(result.depends, ', ')
    endif
    if has_key(result, 'referenced')
      let output .= "\n" . 'Referenced By: ' . join(result.referenced, ', ')
    endif
    call eclim#util#Echo(output)
  elseif type(result) == g:STRING_TYPE
    call eclim#util#Echo(result)
  endif
endfunction " }}}

function! eclim#project#util#ProjectStatusLine() " {{{
  " Includes status information for the current file to VIM status

  " don't ever display errors since this is called from the user's status
  " line.
  silent! let project = eclim#project#util#GetProject(expand('%:p'))
  if !empty(project)
    let status = g:EclimProjectStatusLine
    while status =~ '\${\w\+}'
      let m = matchstr(status, '\${\w\+}')
      let key = substitute(m, '^\${\(\w\+\)}', '\1', '')
      let val = ''
      if has_key(project, key)
        let type = type(project[key])
        if type == 1
          let val = project[key]
        elseif type == 3
          let val = join(project[key], ',')
        else
          let val = string(project[key])
        endif
      endif
      let status = substitute(status, m, val, 'g')
    endwhile
    return status
  endif
  return ''
endfunction " }}}

function! eclim#project#util#ProjectOpen(name) " {{{
  let name = a:name
  if name == ''
    if !eclim#project#util#IsCurrentFileInProject()
      return
    endif
    let name = eclim#project#util#GetCurrentProjectName()
  endif

  let command = substitute(s:command_open, '<project>', name, '')
  let result = eclim#Execute(command, {'project': name})
  if result != '0'
    call eclim#util#Echo(result)
    call eclim#project#util#ClearProjectsCache()
  endif
endfunction " }}}

function! eclim#project#util#ProjectClose(name) " {{{
  let name = a:name
  if name == ''
    if !eclim#project#util#IsCurrentFileInProject()
      return
    endif
    let name = eclim#project#util#GetCurrentProjectName()
  endif

  let command = substitute(s:command_close, '<project>', name, '')
  let result = eclim#Execute(command, {'project': name})
  if result != '0'
    call eclim#util#Echo(result)
  endif
endfunction " }}}

function! eclim#project#util#ProjectList(workspace) " {{{
  let projects = eclim#Execute(s:command_project_list, {'workspace': a:workspace})
  if len(projects) == 0
    call eclim#util#Echo("No projects.")
  endif
  if type(projects) != g:LIST_TYPE
    return
  endif

  let pad = 0
  for project in projects
    let pad = len(project.name) > pad ? len(project.name) : pad
  endfor

  let output = []
  for project in projects
    call add(output,
      \ eclim#util#Pad(project.name, pad) . ' - ' .
      \ (project.open ? ' open ' : 'closed') . ' - ' .
      \ project.path)
  endfor

  call eclim#util#Echo(join(output, "\n"))
endfunction " }}}

function! eclim#project#util#ProjectNatures(project) " {{{
  " Prints nature info of one or all projects.

  if !eclim#EclimAvailable()
    return
  endif

  let command = s:command_natures
  if a:project != ''
    let command .= ' -p "' . a:project . '"'
    let projects = eclim#Execute(command, {'project': a:project})
    if type(projects) != g:LIST_TYPE
      return
    endif
  else
    let projects = []
    let instances = eclim#client#nailgun#GetEclimdInstances()
    for workspace in keys(instances)
      let results = eclim#Execute(command, {'instance': instances[workspace]})
      if type(results) != g:LIST_TYPE
        continue
      endif
      let projects += results
    endfor
  endif

  if len(projects) == 0
    call eclim#util#Echo("No projects.")
  endif

  let pad = 0
  for project in projects
    let pad = len(project.name) > pad ? len(project.name) : pad
  endfor

  let output = []
  for project in projects
    call add(output,
      \ eclim#util#Pad(project.name, pad) . ' - ' . join(project.natures, ', '))
  endfor
  call eclim#util#Echo(join(output, "\n"))
endfunction " }}}

function! eclim#project#util#ProjectNatureModify(command, args) " {{{
  " Modifies one or more natures for the specified project.

  let args = eclim#util#ParseCmdLine(a:args)

  let project = args[0]
  if eclim#project#util#GetProjectRoot(project) == ''
    call eclim#util#EchoError('Project not found: ' . project)
    return
  endif

  let natures = args[1:]
  if len(natures) == 0
    call eclim#util#EchoError('Please supply at least one nature alias.')
    return
  else
    let aliases = eclim#project#util#GetNatureAliasesDict()
    let invalid = []
    for nature in natures
      if !has_key(aliases, nature)
        call add(invalid, nature)
      endif
    endfor
    if len(invalid) > 0
      call eclim#util#EchoError(
        \ 'One or more unrecognized nature aliases: ' . join(invalid, ','))
      return
    endif
  endif

  let command = a:command == 'add' ? s:command_nature_add : s:command_nature_remove
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<natures>', join(natures, ','), '')

  if a:command == 'add'
    let hook_result = s:ProjectNatureHooks(natures, 'ProjectNatureAddPre', [project])
    if type(hook_result) == g:NUMBER_TYPE && !hook_result
      return
    elseif type(hook_result) == g:STRING_TYPE && len(hook_result)
      let command .= ' -a ' . hook_result
    endif
  endif

  let result = eclim#Execute(command, {'project': project})
  if result != '0'
    if a:command == 'add'
      call s:ProjectNatureHooks(natures, 'ProjectNatureAddPost', [project])
    endif
    call eclim#util#Echo(result)
  endif
endfunction " }}}

function! eclim#project#util#ProjectRun(...) " {{{
  " Option args:
  "   config: The name of the configuration to run for the current project
  
  if !eclim#EclimAvailable()
    return
  endif

  let config = a:0 > 0 ? a:1 : ''
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif
  let project = eclim#project#util#GetCurrentProjectName()

  let command = s:command_project_run
  if config != ''
    let command = s:command_project_run_config
  endif

  call eclim#util#Echo("Running project '" . project . "'...")
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<config>', config, '')
  let result = eclim#Execute(command, {'project': project})
  call eclim#util#Echo(result)
endfunction " }}}

function! eclim#project#util#ProjectRunList() " {{{

  if !eclim#EclimAvailable()
    return
  endif

  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif
  let project = eclim#project#util#GetCurrentProjectName()

  let command = s:command_project_run_list

  call eclim#util#Echo("Fetching launch configs for project '" . project . "'...")
  let command = substitute(command, '<project>', project, '')
  let result = eclim#Execute(command, {'project': project})
  if type(result) != g:LIST_TYPE
    call eclim#util#Echo(result)
    return
  endif

  if len(result) == 0
    call eclim#util#Echo("No launch configs for project '" . project . ".")
    return
  endif

  let pad = 0
  for config in result
    let pad = len(config.name) > pad ? len(config.name) : pad
  endfor

  let output = []
  for config in result
    call add(output,
      \ eclim#util#Pad(config.name, pad) . ' - ' . config.type)
  endfor
  call eclim#util#Echo(join(output, "\n"))
endfunction " }}}

function! eclim#project#util#ProjectSettings(project) " {{{
  " Opens a window that can be used to edit a project's settings.

  let project = a:project
  if project == ''
    let project = eclim#project#util#GetCurrentProjectName()
  endif
  if project == ''
    call eclim#project#util#UnableToDetermineProject()
    return
  endif

  let command = substitute(s:command_project_settings, '<project>', project, '')
  let settings = eclim#Execute(command, {'project': project})
  if type(settings) != g:LIST_TYPE
    return
  endif

  let content = ['# Settings for project: ' . project, '']
  let path = ''
  for setting in settings
    if setting.path != path
      if path != ''
        let content += ['# }', '']
      endif
      let path = setting.path
      call add(content, '# ' . path . ' {')
    endif
    let description = split(setting.description, '\n')
    let content += map(description, "'\t# ' . v:val")
    call add(content, "\t" . setting.name . '=' . setting.value)
  endfor
  if path != ''
    call add(content, '# }')
  endif

  call eclim#util#TempWindow(project . "_settings", content)
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
endfunction " }}}

function! eclim#project#util#ProjectUpdate() " {{{
  " Executes a project update which may also validate nature specific resource
  " file.

  let name = eclim#project#util#GetCurrentProjectName()
  if name == ''
    call eclim#util#EchoError('Unable to determine the project.')
    return
  endif

  let command = substitute(s:command_project_update, '<project>', name, '')

  let result = eclim#Execute(command)
  if type(result) == g:LIST_TYPE && len(result) > 0
    let errors = eclim#util#ParseLocationEntries(
      \ result, g:EclimValidateSortResults)
    call eclim#util#SetLocationList(errors)
  else
    call eclim#util#ClearLocationList()
    call eclim#util#Echo(result)
  endif
endfunction " }}}

function! eclim#project#util#ProjectGrep(command, args) " {{{
  " Executes the supplied vim grep command with the specified pattern against
  " one or more file patterns.

  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let project = eclim#project#util#GetProject(expand('%:p'))
  let tail = substitute(a:args, '\(.\).\{-}\1\s\(.*\)', '\2', '')
  let pattern = substitute(a:args, '\(.*\)\s\+\M' . tail . '\m$', '\1', '')
  let cmd = a:command
  let acd = &autochdir
  set noautochdir
  try
    if pattern != a:args && tail != a:args && tail != ''
      let files = eclim#util#ParseArgs(tail)
      let paths = ''
      for file in files
        if paths != ''
          let paths .= ' '
        endif
        let paths .= escape(project.path, ' ') . '/' . file
      endfor
      let links = get(project, 'links', {})
      if len(links)
        for link in values(links)
          for file in files
            let paths .= ' ' . escape(link, ' ') . '/' . file
          endfor
        endfor
      endif
      silent exec a:command . ' ' . pattern . ' ' . paths
    else
      " let vim generate the proper error
      silent exec a:command . ' ' . a:args
    endif
  catch /E480/
    " no results found
  catch /.*/
    call eclim#util#EchoError(v:exception)
    return
  finally
    let &autochdir = acd
    " force quickfix / location list signs to update.
    call eclim#display#signs#Update()
  endtry

  let numresults = len(a:command =~ '^l' ? getloclist(0) : getqflist())
  if numresults == 0
    call eclim#util#EchoInfo('No results found.')
  endif
endfunction " }}}

function! eclim#project#util#ProjectTab(project) " {{{
  " Opens a new tab with the project tree and tab relative working directory for
  " the specified project.

  let project = a:project
  let names = eclim#project#util#GetProjectNames()
  if index(names, project) == -1
    let is_project = 0
    let dir = fnamemodify(project, ':p')
    if !isdirectory(dir)
      if eclim#EclimAvailable(0)
        call eclim#util#EchoError("No project '" . project . "' found.")
      endif
      return
    endif
    let project = fnamemodify(substitute(dir, '/$', '', ''), ':t')
  else
    let is_project = 1
    let dir = eclim#project#util#GetProjectRoot(project)
  endif

  if exists('t:eclim_project') ||
   \ winnr('$') > 1 || expand('%') != '' ||
   \ &modified || line('$') != 1 || getline(1) != ''
    tablast | tabnew
  endif

  let t:eclim_project = project
  call eclim#common#util#Tcd(dir)
  if g:EclimProjectTabTreeAutoOpen
    if is_project
      call eclim#project#tree#ProjectTree(project)
    else
      call eclim#project#tree#ProjectTree(dir)
    endif
  else
    call eclim#util#Echo('ProjectTab ' . project . ' cwd: ' . dir)
  endif
endfunction " }}}

function! eclim#project#util#Todo() " {{{
  " Show the todo tags of the curent file in the location list.

  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let path = expand('%:p')
  silent! exec 'lvimgrep /' . g:EclimTodoSearchPattern . '/gj ' . path
  if !empty(getloclist(0))
    exec 'lopen ' . g:EclimLocationListHeight
  else
    call eclim#util#Echo('No Results found')
  endif
endfunction " }}}

function! eclim#project#util#ProjectTodo() " {{{
  " Show the todo tags of the whole project in the location list.
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  if len(g:EclimTodoSearchExtensions) == 0
  endif

  let project = eclim#project#util#GetProject(expand('%:p'))
  let paths = ''
  for ext in g:EclimTodoSearchExtensions
    if paths != ''
      let paths .= ' '
    endif
    let paths .= escape(project.path, ' ') . '/**/*' . ext
  endfor
  let links = get(project, 'links', {})
  if len(links)
    for link in values(links)
      for ext in g:EclimTodoSearchExtensions
        let paths .= ' ' . escape(link, ' ') . '/**/*' . ext
      endfor
    endfor
  endif

  silent! exec 'lvimgrep /' . g:EclimTodoSearchPattern . '/gj ' . paths

  if !empty(getloclist(0))
    exec 'lopen ' . g:EclimLocationListHeight
  else
    call eclim#util#Echo('No Results found')
  endif
endfunction " }}}

function! s:SaveSettings() " {{{
  call eclim#SaveSettings(s:command_update, b:project)
endfunction " }}}

function! eclim#project#util#GetCurrentProjectName() " {{{
  " Gets the project name that the current file is in.
  let project = eclim#project#util#GetProject(expand('%:p'))
  return len(project) > 0 ? project.name : ''
endfunction " }}}

function! eclim#project#util#GetCurrentProjectRoot(...) " {{{
  " Gets the project root dir for the project that the current or supplied
  " file is in.
  let path = len(a:000) > 0 ? a:000[0] : expand('%:p')
  let project = eclim#project#util#GetProject(path)
  return len(project) > 0 ? project.path : ''
endfunction " }}}

function! eclim#project#util#GetProjectWorkspace(name) " {{{
  " Gets the workspace that a project belongs to.

  " ensure s:workspace_projects is initialized
  call eclim#project#util#GetProjects()

  " loop through each workspace since the same project name could be used in
  " more than one workspace.
  let workspaces = []
  for [workspace, projects] in items(s:workspace_projects)
    for p in projects
      if p.name == a:name
        call add(workspaces, workspace)
        break
      endif
    endfor
  endfor

  if len(workspaces) > 1
    return workspaces
  endif
  return len(workspaces) ? workspaces[0] : ''
endfunction " }}}

function! eclim#project#util#GetProjectRelativeFilePath(...) " {{{
  " Gets the project relative path for the current or supplied file.
  " Optional args:
  "   file: get the relative path for this file instead of the current one.

  if exists('b:eclim_file')
    return b:eclim_file
  endif

  let file = a:0 == 0 ? expand('%:p') : a:1
  let project = eclim#project#util#GetProject(file)
  if !len(project)
    return ''
  endif

  let file = substitute(fnamemodify(file, ':p'), '\', '/', 'g')
  let pattern = '\(/\|$\)'
  if has('win32') || has('win64') || has('macunix')
    let pattern .= '\c'
  endif
  let result = substitute(file, get(project, 'path', '') . pattern, '', '')

  " handle file in linked folder
  if result == file
    for name in keys(get(project, 'links', {}))
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

function! eclim#project#util#GetProjects() " {{{
  " Returns a list of project dictionaries containing the following properties:
  "   workspace: The path of the workspace the project belongs to.
  "   name: The name of the project.
  "   path: The root path of the project.
  "   links: List of linked paths.
  let instances = eclim#client#nailgun#GetEclimdInstances()

  if keys(s:workspace_projects) != keys(instances)
    let s:workspace_projects = {}
    for workspace in keys(instances)
      let instance = instances[workspace]
      let results = eclim#Execute(
        \ s:command_projects, {'instance': instance})
      if type(results) != g:LIST_TYPE
        continue
      endif

      if has('win32unix')
        " gather paths to translate
        let winpaths = []
        for project in results
          call add(winpaths, project['path'])
          if has_key(project, 'links')
            for key in sort(keys(project['links']))
              call add(winpaths, project['links'][key])
            endfor
          endif
        endfor

        let cygpaths = eclim#cygwin#CygwinPath(winpaths)

        " update each project with the cygwin version of its paths
        let index = 0
        for project in results
          let project['path'] = cygpaths[index]
          let index += 1
          if has_key(project, 'links')
            for key in sort(keys(project['links']))
              let project['links'][key] = cygpaths[index]
              let index += 1
            endfor
          endif
        endfor
      endif

      for project in results
        let project['workspace'] = instance.workspace
      endfor

      let s:workspace_projects[instance.workspace] = results
      unlet results
    endfor
  endif

  let all = []
  for projects in values(s:workspace_projects)
    let all += copy(projects)
  endfor
  return all
endfunction " }}}

function! eclim#project#util#GetProject(path) " {{{
  " if a [No Name] buffer, use the current working directory.
  let path = a:path != '' ? a:path : getcwd()

  let path = substitute(fnamemodify(path, ':p'), '\', '/', 'g')
  let pattern = '\(/\|$\)'
  if has('win32') || has('win64') || has('macunix')
    let pattern .= '\c'
  endif

  let projects = eclim#project#util#GetProjects()

  " sort projects depth wise by path to properly support nested projects.
  call sort(projects, 's:ProjectSortPathDepth')

  for project in projects
    if path =~ '^' . project.path . pattern
      return project
    endif

    if has_key(project, 'link') && path =~ '^' . project.link . pattern
      return project
    endif

    " check linked folders
    for name in keys(get(project, 'links', {}))
      if path =~ '^' . project.links[name] . pattern
        return project
      endif
    endfor
  endfor

  " project not found by path, fallback to buffer local variable
  if exists('b:eclim_project')
    for project in projects
      if project.name == b:eclim_project
        return project
      endif
    endfor
  endif

  return {}
endfunction " }}}

function! s:ProjectSortPathDepth(p1, p2) " {{{
  return len(a:p2.path) - len(a:p1.path)
endfunction " }}}

function! eclim#project#util#GetProjectDirs() " {{{
  " Gets list of all project root directories.
  return map(eclim#project#util#GetProjects(), 'v:val.path')
endfunction " }}}

function! eclim#project#util#GetProjectNames(...) " {{{
  " Gets list of all project names, with optional filter by the supplied nature
  " alias.
  "   Option args:
  "     nature: The nature alias to filter projects by

  " filter by nature
  if a:0 > 0 && a:1 != ''
    let projects = []
    let command = s:command_project_list . ' -n ' . a:1
    let instances = eclim#client#nailgun#GetEclimdInstances()
    for workspace in keys(instances)
      let results = eclim#Execute(command, {'instance': instances[workspace]})
      if type(results) != g:LIST_TYPE
        continue
      endif
      let projects += results
    endfor

    let names = map(projects, "v:val.name")
  else
    let names = map(eclim#project#util#GetProjects(), 'v:val.name')
  endif

  return eclim#util#ListDedupe(sort(names))
endfunction " }}}

function! eclim#project#util#GetProjectNatureAliases(...) " {{{
  " Gets list of all project nature aliases or a list of aliases associated with
  " a project if the project name is supplied.
  " Optional args:
  "   project: the project to get natures aliases from.

  if a:0 > 0 && a:1 != ''
    let command = s:command_natures . ' -p "' . a:1 . '"'
    let result = eclim#Execute(command)
    if type(result) != g:LIST_TYPE || len(result) == 0
      return []
    endif
    return result[0]['natures']
  endif

  let aliases = eclim#Execute(s:command_nature_aliases)
  if type(aliases) != g:LIST_TYPE
    return []
  endif

  return aliases
endfunction " }}}

function! eclim#project#util#GetNatureAliasesDict() " {{{
  " Gets a dict of all natures aliases where the alias is the key and the nature
  " id is the value.

  let aliases = eclim#Execute(s:command_nature_aliases . ' -m')
  if type(aliases) != g:DICT_TYPE
    return {}
  endif

  return aliases
endfunction " }}}

function! eclim#project#util#GetProjectRoot(name) " {{{
  " Gets the project root dir for the supplied project name.

  let project = {}
  for p in eclim#project#util#GetProjects()
    if p.name == a:name
      let project = p
      break
    endif
  endfor
  return get(project, 'path', '')
endfunction " }}}

function! eclim#project#util#GetProjectSetting(setting) " {{{
  " Gets a project setting from eclim.  Returns '' if the setting does not
  " exist, 0 if not in a project or an error occurs communicating with the
  " server.

  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let project = eclim#project#util#GetCurrentProjectName()
  let command = s:command_project_setting
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<setting>', a:setting, '')

  let result = eclim#Execute(command)
  if result == '0'
    return result
  endif

  if result == ''
    call eclim#util#EchoWarning("Setting '" . a:setting . "' does not exist.")
  endif
  return result
endfunction " }}}

function! eclim#project#util#SetProjectSetting(setting, value) " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let project = eclim#project#util#GetCurrentProjectName()
  let command = s:command_project_setting
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<setting>', a:setting, '')
  let command .= ' -v "' . a:value . '"'

  call eclim#Execute(command)
endfunction " }}}

function! eclim#project#util#IsCurrentFileInProject(...) " {{{
  " Determines if the current file is in a project directory.
  " Accepts an optional arg that determines if a message is displayed to the
  " user if the file is not in a project(defaults to 1, to display the
  " message).
  " Optional args:
  "   echo_error (default: 1): when non-0, echo an error to the user if project
  "                            could not be determined.

  let echo = a:0 ? a:1 : 1
  if !echo
    silent let project = eclim#project#util#GetCurrentProjectName()
  else
    let project = eclim#project#util#GetCurrentProjectName()
  endif

  if project == ''
    " if eclimd isn't available, then that could be the reason the project
    " couldn't be determined, so don't hide that message with this one.
    if echo && eclim#EclimAvailable(0)
      call eclim#util#EchoError('Unable to determine the project. ' .
        \ 'Check that the current file is in a valid project.')
    endif
    return 0
  endif
  return 1
endfunction " }}}

function! eclim#project#util#RefreshFileBootstrap() " {{{
  " Boostraps a post write autocommand for updating files, which forces a
  " refresh by the eclim project. The command should only be called as part of
  " the a BufWritePre autocmd.
  if eclim#project#util#GetCurrentProjectName() != '' && &modified
    let refresh = !exists('b:EclimRefreshDisabled') || !b:EclimRefreshDisabled
    if refresh
      augroup eclim_refresh_files_bootstrap
        autocmd!
        autocmd BufWritePost <buffer> call eclim#project#util#RefreshFile()
      augroup END
    endif
  endif
endfunction " }}}

function! eclim#project#util#RefreshFile() " {{{
  " Refreshes the current files in eclipse.
  augroup eclim_refresh_files_bootstrap
    autocmd! BufWritePost <buffer>
  augroup END
  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()
  let command = s:command_refresh_file
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  call eclim#Execute(command)
endfunction " }}}

function! eclim#project#util#UnableToDetermineProject() " {{{
  " if eclimd isn't available, then that could be the reason the project
  " couldn't be determined, so don't hide that message with this one.
  if eclim#EclimAvailable(0)
    call eclim#util#EchoError("Unable to determine the project. " .
      \ "Please specify a project name or " .
      \ "execute from a valid project directory.")
  endif
endfunction " }}}

function! eclim#project#util#CommandCompleteProject(argLead, cmdLine, cursorPos) " {{{
  " Custom command completion for project names.
  return eclim#project#util#CommandCompleteProjectByNature(
    \ a:argLead, a:cmdLine, a:cursorPos, '')
endfunction " }}}

function! eclim#project#util#CommandCompleteProjectContainsThis(argLead, cmdLine, cursorPos) " {{{
  " Custom command completion for project names, filtering by those that contain
  " a file with the same path as the current file, excluding the current
  " project.

  let names = eclim#project#util#CommandCompleteProject(
    \ a:argLead, a:cmdLine, a:cursorPos)

  let path = eclim#project#util#GetProjectRelativeFilePath()
  let project = eclim#project#util#GetCurrentProjectName()
  let projects = eclim#project#util#GetProjects()
  call filter(names, 'v:val != project && filereadable(eclim#project#util#GetProjectRoot(v:val) . "/" . path)')
  return names
endfunction " }}}

function! eclim#project#util#CommandCompleteProjectByNature(argLead, cmdLine, cursorPos, nature) " {{{
  " Custom command completion for project names limited by the supplied nature.

  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')

  let projects = eclim#project#util#GetProjectNames(a:nature)
  if cmdLine !~ '[^\\]\s$'
    let argLead = escape(escape(argLead, '~'), '~')
    " remove escape slashes
    let argLead = substitute(argLead, '\', '', 'g')
    call filter(projects, 'v:val =~ "^' . argLead . '"')
  endif

  call map(projects, 'escape(v:val, " ")')
  return projects
endfunction " }}}

function! eclim#project#util#CommandCompleteProjectCreate(argLead, cmdLine, cursorPos) " {{{
  " Custom command completion for ProjectCreate args.

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

function! s:CommandCompleteProjectCreateOptions(argLead, cmdLine, cursorPos) " {{{
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

function! eclim#project#util#CommandCompleteProjectMove(argLead, cmdLine, cursorPos) " {{{
  " Custom command completion for ProjectMove args.

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

function! eclim#project#util#CommandCompleteProjectRelative(argLead, cmdLine, cursorPos) " {{{
  " Custom command completion for project relative files and directories.

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

function! eclim#project#util#CommandCompleteProjectRelativeDir(argLead, cmdLine, cursorPos) " {{{
  " Custom command completion for project relative files and directories.

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
  call filter(results, 'isdirectory(v:val)')
  call map(results, 'v:val . "/"')
  call map(results, 'substitute(v:val, dir, "", "")')
  call map(results, 'substitute(v:val, "^\\(/\\|\\\\\\)", "", "g")')
  call map(results, "substitute(v:val, ' ', '\\\\ ', 'g')")

  return eclim#util#ParseCommandCompletionResults(argLead, results)
endfunction " }}}

function! eclim#project#util#CommandCompleteProjectOrDirectory(argLead, cmdLine, cursorPos) " {{{
  " Custom command completion for :ProjectTree/:ProjectTab to complete project names or
  " directories

  let projects = []
  if a:argLead !~ '[~/]'
    let projects = eclim#project#util#CommandCompleteProjectByNature(
      \ a:argLead, a:cmdLine, a:cursorPos, '')
  endif
  let dirs = eclim#util#CommandCompleteDir(a:argLead, a:cmdLine, a:cursorPos)
  return projects + dirs
endfunction " }}}

function! eclim#project#util#CommandCompleteAbsoluteOrProjectRelative(argLead, cmdLine, cursorPos) " {{{
  " Custom command completion for project relative files and directories.
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)
  if len(args) > 0
    let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]
    if argLead =~ '^\(/\|[a-zA-Z]:\)'
      return eclim#util#CommandCompleteFile(a:argLead, a:cmdLine, a:cursorPos)
    endif
  endif
  return eclim#project#util#CommandCompleteProjectRelative(
    \ a:argLead, a:cmdLine, a:cursorPos)
endfunction " }}}

function! eclim#project#util#CommandCompleteAbsoluteOrProjectRelativeDir(argLead, cmdLine, cursorPos) " {{{
  " Custom command completion for project relative files and directories.
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)
  if len(args) > 0
    let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]
    if argLead =~ '^\(/\|[a-zA-Z]:\)'
      return eclim#util#CommandCompleteDir(a:argLead, a:cmdLine, a:cursorPos)
    endif
  endif
  return eclim#project#util#CommandCompleteProjectRelativeDir(
    \ a:argLead, a:cmdLine, a:cursorPos)
endfunction " }}}

function! eclim#project#util#CommandCompleteProjectNatureAdd(argLead, cmdLine, cursorPos) " {{{
  " Custom command completion for project names and natures.
  return s:CommandCompleteProjectNatureModify(
    \ a:argLead, a:cmdLine, a:cursorPos, function("s:AddAliases"))
endfunction " }}}

function! s:AddAliases(allAliases, projectAliases) " {{{
  let aliases = a:allAliases
  call filter(aliases, 'index(a:projectAliases, v:val) == -1')
  return aliases
endfunction " }}}

function! eclim#project#util#CommandCompleteProjectNatureRemove(argLead, cmdLine, cursorPos) " {{{
  " Custom command completion for project names and natures.
  return s:CommandCompleteProjectNatureModify(
    \ a:argLead, a:cmdLine, a:cursorPos, function("s:RemoveAliases"))
endfunction " }}}

function! s:RemoveAliases(allAliases, projectAliases) " {{{
  return a:projectAliases
endfunction " }}}

function! s:CommandCompleteProjectNatureModify(argLead, cmdLine, cursorPos, aliasesFunc) " {{{
  " Custom command completion for project names and natures.
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
