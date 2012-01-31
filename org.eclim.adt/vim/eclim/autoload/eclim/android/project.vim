" Author:  Eric Van Dewoestine
"
" Description: {{{
"
" License:
"
" Copyright (C) 2012  Eric Van Dewoestine
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
let s:command_list_targets = '-command android_list_targets'
" }}}

function! eclim#android#project#ProjectCreatePre(folder) " {{{
  return s:InitAndroid(a:folder)
endfunction " }}}

function! eclim#android#project#ProjectNatureAddPre(project) " {{{
  return s:InitAndroid(eclim#project#util#GetProjectRoot(a:project))
endfunction " }}}

function! eclim#android#project#GetTargets(folder) " {{{
  let workspace = eclim#eclipse#ChooseWorkspace(a:folder)
  let port = eclim#client#nailgun#GetNgPort(workspace)
  let results = eclim#ExecuteEclim(s:command_list_targets, port)
  if type(results) != g:LIST_TYPE
    if type(results) == g:STRING_TYPE
      call eclim#util#EchoError(results)
    endif
    return
  endif
  return results
endfunction " }}}

function! s:InitAndroid(folder) " {{{
  let args = ''

  let targets = eclim#android#project#GetTargets(a:folder)
  if type(targets) != g:LIST_TYPE
    return
  endif

  if len(targets) == 0
    let message = "No android platform targets found."
    let sdk = eclim#util#GetSetting('com.android.ide.eclipse.adt.sdk')
    if type(sdk) == g:STRING_TYPE && sdk != ''
      let sdk = substitute(sdk, '\\', '/', 'g')
      if sdk !~ '[/\\]$'
        let sdk .= '/'
      endif
      let managers = [
          \ sdk . 'SDK Manager.exe',
          \ sdk . 'tools/android',
        \ ]
      let manager = ''
      for path in managers
        if filereadable(path)
          let manager = path
          break
        endif
      endfor

      if manager != ''
        let message .=
          \ "\nYou can use the SDK Manager to install target packages:" .
          \ "\n    " . manager .
          \ "\nThen you can reload the android sdk by running the vim command:" .
          \ "\n    :AndroidReload"
      endif
    endif
    call eclim#util#EchoError(message)
    return 0
  endif

  if len(targets) == 1
    let target = targets[0].hash
  else
    let answer = eclim#util#PromptList(
      \ "Please choose the target android platform for this project",
      \ map(copy(targets), 'v:val.name'))
    if answer == -1
      return 0
    endif

    let target = targets[answer].hash
    redraw
  endif
  let args = '--target ' . target

  let manifest = a:folder . '/AndroidManifest.xml'
  if !filereadable(manifest)
    " choose a package name
    let package = eclim#util#Prompt(
      \ "Please specify a package name",
      \ function('eclim#android#project#ValidatePackage'))
    if package == ''
      call eclim#util#EchoWarning('Project create canceled.')
      return 0
    endif
    let args .= ' --package ' . package

    " choose an app name
    let name = eclim#util#Prompt("Please specify a name for this application")
    if name == ''
      call eclim#util#EchoWarning('Project create canceled.')
      return 0
    endif
    let args .= ' --application "' . escape(name, '"') . '"'

    " is this a library?
    let library = eclim#util#PromptConfirm('Is this a library project?')
    if library == -1
      call eclim#util#EchoWarning('Project create canceled.')
      return 0
    endif
    if library
      let args .= ' --library'
    else
      " create an activity?
      let create = eclim#util#PromptConfirm('Create an activity?')
      if create == -1
        call eclim#util#EchoWarning('Project create canceled.')
        return 0
      endif
      if create
        " choose an app name
        redraw
        let default = substitute(name, '\W', '', 'g') . 'Activity'
        let activity = eclim#util#Prompt(
          \ ["Please specify an activity name", default],
          \ function('eclim#android#project#ValidateActivity'))
        if activity == ''
          call eclim#util#EchoWarning('Project create canceled.')
          return 0
        endif
        let args .= ' --activity ' . activity
      endif
    endif
  endif

  return args
endfunction " }}}

function! eclim#android#project#ValidateActivity(activity) " {{{
  if a:activity !~? '^[a-z]\w*$'
    return "Activity name must be a valid java identifier."
  endif
  return 1
endfunction " }}}

function! eclim#android#project#ValidatePackage(package) " {{{
  if a:package !~? '^[a-z][a-z0-9_]*\(.[a-z][a-z0-9_]*\)*$'
    return "Must be a valid package name with no trailing dots."
  endif
  return 1
endfunction " }}}

" vim:ft=vim:fdm=marker
