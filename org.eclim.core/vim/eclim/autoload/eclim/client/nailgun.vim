" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2005 - 2013  Eric Van Dewoestine
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

function! eclim#client#nailgun#ChooseEclimdInstance(...) " {{{
  " Function which prompts the user to pick the target workspace and returns
  " their choice or if only one workspace is active simply return it without
  " prompting the user.  If the optional 'dir' argument is supplied and that dir
  " is a subdirectory of one of the workspaces, then that workspace will be
  " returned.
  " Option args:
  "   dir

  let instances = eclim#client#nailgun#GetEclimdInstances()
  if type(instances) == g:NUMBER_TYPE
    call eclim#util#Echo(printf(
      \ 'No eclimd instances found running (eclimd created file not found %s)',
      \ eclim#UserHome() . '/.eclim/.eclimd_instances'))
    return
  endif

  if len(instances) == 1
    return instances[keys(instances)[0]]
  endif

  if len(instances) > 1
    let path = a:0 && a:1 != '' ? a:1 : expand('%:p')
    if path == ''
      let path = getcwd() . '/'
    endif
    let path = substitute(path, '\', '/', 'g')

    " when we are in a temp window, use the initiating filename
    if &buftype != '' && exists('b:filename')
      let path = b:filename
    endif

    " project inside of a workspace dir
    for workspace in keys(instances)
      if path =~ '^' . workspace
        return instances[workspace]
      endif
    endfor

    " project outside of a workspace dir
    let project = eclim#project#util#GetProject(path)
    if len(project) > 0
      return get(instances, project.workspace, 0)
    endif

    let workspaces = keys(instances)
    let response = eclim#util#PromptList(
      \ 'Muliple workspaces found, please choose the target workspace',
      \ workspaces, g:EclimInfoHighlight)

    " user cancelled, error, etc.
    if response < 0
      return
    endif

    return instances[workspaces[response]]
  endif

  call eclim#util#Echo('No eclimd instances found running.')
endfunction " }}}

function! eclim#client#nailgun#GetEclimdInstances() " {{{
  let instances = {}
  let dotinstances = eclim#UserHome() . '/.eclim/.eclimd_instances'
  if filereadable(dotinstances)
    let lines = readfile(dotinstances)
    for line in lines
      if line !~ '^{'
        continue
      endif
      let values = eval(line)
      let instances[values.workspace] = values
    endfor
    return instances
  endif
endfunction " }}}

function! eclim#client#nailgun#Execute(instance, command, ...) " {{{
  let exec = a:0 ? a:1 : 0

  if !exec
    if !exists('g:EclimNailgunClient')
      call s:DetermineClient()
    endif

    if g:EclimNailgunClient == 'python' && has('python')
      return eclim#client#python#nailgun#Execute(a:instance.port, a:command)
    endif
  endif

  let eclim = eclim#client#nailgun#GetEclimCommand(a:instance.home)
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

  let eclim .= ' --nailgun-port ' . a:instance.port . ' ' . command
  if exec
    let eclim = '!' . eclim
  endif

  let result = eclim#util#System(eclim, exec, exec)
  return [v:shell_error, result]
endfunction " }}}

function! eclim#client#nailgun#GetEclimCommand(home) " {{{
  " Gets the command to exexute eclim.
  let command = a:home . 'bin/eclim'

  if has('win32') || has('win64') || has('win32unix')
    let command = command . (has('win95') ? '.bat' : '.cmd')
  endif

  if !filereadable(command)
    let g:EclimErrorReason = 'Could not locate file: ' . command
    return
  endif

  if has('win32unix')
    " in cygwin, we must use 'cmd /c' to prevent issues with eclim script +
    " some arg containing spaces causing a failure to invoke the script.
    return 'cmd /c "' . eclim#cygwin#WindowsPath(command, 1) . '"'
  endif
  return '"' . command . '"'
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

function! eclim#client#nailgun#CommandCompleteWorkspaces(argLead, cmdLine, cursorPos) " {{{
  " Custom command completion for available workspaces.

  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]

  let instances = eclim#client#nailgun#GetEclimdInstances()
  let workspaces = sort(keys(instances))
  if cmdLine !~ '[^\\]\s$'
    call filter(workspaces, 'v:val =~ "^' . argLead . '"')
  endif

  return workspaces
endfunction " }}}

" vim:ft=vim:fdm=marker
