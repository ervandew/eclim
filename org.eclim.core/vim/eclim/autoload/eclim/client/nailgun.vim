" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2005 - 2020  Eric Van Dewoestine
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
  " Optional args:
  "   dir

  if !eclim#EclimAvailable()
    return
  endif

  let instances = eclim#client#nailgun#GetEclimdInstances()
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
      \ workspaces, g:EclimHighlightInfo)

    " user cancelled, error, etc.
    if response < 0
      return
    endif

    return instances[workspaces[response]]
  endif

  call eclim#util#Echo('No eclimd instances found running.')
endfunction " }}}

function! eclim#client#nailgun#GetEclimdInstances() " {{{
  " Returns a dict with eclimd instances.
  let instances = {}
  if eclim#EclimAvailable()
    let dotinstances = eclim#UserHome() . '/.eclim/.eclimd_instances'
    let lines = readfile(dotinstances)
    for line in lines
      if line !~ '^{'
        continue
      endif
      let values = eval(line)
      let instances[values.workspace] = values
    endfor
  endif
  return instances
endfunction " }}}

function! eclim#client#nailgun#Execute(instance, command, ...) " {{{
  let exec = a:0 ? a:1 : 0

  if !exec
    if g:EclimNailgunClient == 'python' && has('python3')
      return eclim#client#python#nailgun#Execute(a:instance.port, a:command)
    endif
  endif

  let [retcode, result] = eclim#client#nailgun#GetEclimCommand(a:instance.home)
  if retcode != 0
    return [retcode, result]
  endif

  let command = a:command
  if exec
    let command = escape(command, '%#')
  endif

  let eclim = result . ' --nailgun-server localhost --nailgun-port ' . a:instance.port . ' ' . command
  if exec
    let eclim = '!' . eclim
  endif

  let result = eclim#util#System(eclim, exec, exec)
  return [v:shell_error, result]
endfunction " }}}

function! eclim#client#nailgun#GetEclimCommand(home) " {{{
  " Gets the command to exexute eclim.
  let command = a:home . 'bin/eclim'
  if !filereadable(command)
    return [1, 'Could not locate file: ' . command]
  endif
  return [0, '"' . command . '"']
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
