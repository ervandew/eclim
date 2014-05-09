" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Plugin that integrates vim with the eclipse plugin eclim (ECLipse
"   IMproved).
"
"   This plugin contains shared functions that can be used regardless of the
"   current file type being edited.
"
" License:
"
" Copyright (C) 2005 - 2014  Eric Van Dewoestine
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
  let s:echo_connection_errors = 1
" }}}

" Script Variables {{{
  let s:command_ping = '-command ping'
  let s:command_settings = '-command settings'
  let s:command_settings_update = '-command settings_update -s "<settings>"'
  let s:command_shutdown = "-command shutdown"
  let s:command_jobs = '-command jobs'
  let s:connect= '^connect: .*$'

  let s:vim_settings = {}
  let s:vim_settings_defaults = {}
  let s:vim_settings_types = {}
  let s:vim_settings_validators = {}
" }}}

function! eclim#Execute(command, ...) " {{{
  " Optional args:
  "   options {
  "     One of the following to determine the eclimd instance to use, honored in
  "     the order shown here:
  "       instance: dictionary representing an eclimd instance.
  "       project: project name
  "       workspace: workspace path
  "       dir: directory path to use as the current dir
  "     exec: 1 to execute the command using execute instead of system.
  "     raw: 1 to get the result without evaluating as json
  "   }

  if exists('g:EclimDisabled')
    " if we are not in an autocmd or the autocmd is for an acwrite buffer,
    " alert the user that eclimd is disabled.
    if expand('<abuf>') == '' || &buftype == 'acwrite'
      call eclim#util#EchoWarning(
        \ "eclim is currently disabled. use :EclimEnable to enable it.")
    endif
    return
  endif

  if !eclim#EclimAvailable()
    return
  endif

  let command = '-editor vim ' . a:command

  " encode special characters
  " http://www.w3schools.com/TAGS/ref_urlencode.asp
  let command = substitute(command, '\*', '%2A', 'g')
  let command = substitute(command, '\$', '%24', 'g')
  let command = substitute(command, '<', '%3C', 'g')
  let command = substitute(command, '>', '%3E', 'g')

  " determine the eclimd instance to use
  let options = a:0 ? a:1 : {}
  let instance = get(options, 'instance', {})
  if len(instance) == 0
    let project = get(options, 'project', '')
    if project != ''
      let workspace = eclim#project#util#GetProjectWorkspace(project)
      if type(workspace) == g:LIST_TYPE
        let workspaces = workspace
        unlet workspace
        let response = eclim#util#PromptList(
          \ 'Muliple workspaces found, please choose the target workspace',
          \ workspaces, g:EclimHighlightInfo)

        " user cancelled, error, etc.
        if response < 0
          return
        endif

        let workspace = workspaces[response]
      endif
    else
      let workspace = ''
    endif

    if workspace == ''
      let workspace = get(options, 'workspace', '')
    endif

    let dir = workspace != '' ? workspace : get(options, 'dir', '')
    let chosen = eclim#client#nailgun#ChooseEclimdInstance(dir)
    if type(chosen) != g:DICT_TYPE
      return
    endif
    let instance = chosen
  endif

  let exec = get(options, 'exec', 0)
  let [retcode, result] = eclim#client#nailgun#Execute(instance, command, exec)
  let result = substitute(result, '\n$', '', '')

  " not sure this is the best place to handle this, but when using the python
  " client, the result has a trailing ctrl-m on windows.  also account for
  " running under cygwin vim.
  if has('win32') || has('win64') || has('win32unix')
    let result = substitute(result, "\<c-m>$", '', '')
  endif

  " an echo during startup causes an annoying issue with vim.
  "call eclim#util#Echo(' ')

  " check for errors
  let error = ''
  if result =~ '^[^\n]*Exception:\?[^\n]*\n\s\+\<at\> ' ||
   \ result =~ '^[^\n]*ResourceException(.\{-})\[[0-9]\+\]:[^\n]*\n\s\+\<at\> '
    if g:EclimLogLevel != 'trace'
      let error = substitute(result, '\(.\{-}\)\n.*', '\1', '')
    else
      let error = result
    endif
  elseif retcode
    let error = result
  endif

  if retcode || error != ''
    if s:echo_connection_errors
      if error =~ s:connect
        " if we are not in an autocmd or the autocmd is for an acwrite buffer,
        " alert the user that eclimd is not running.
        if expand('<abuf>') == '' || &buftype == 'acwrite'
          call eclim#util#EchoWarning(
            \ "unable to connect to eclimd (port: " . instance.port . ") - " . error)
        endif
      else
        let error = error . "\n" .
          \ 'while executing command (port: ' . instance.port . '): ' . command
        " if we are not in an autocmd or in a autocmd for an acwrite buffer,
        " echo the error, otherwise just log it.
        if expand('<abuf>') == '' || &buftype == 'acwrite'
          call eclim#util#EchoError(error)
        else
          call eclim#util#EchoDebug(error)
        endif
      endif
    endif
    return
  endif

  let raw = get(options, 'raw', 0)
  return result != '' && !raw ? eval(result) : result
endfunction " }}}

function! eclim#Disable() " {{{
  if !exists('g:EclimDisabled')
    let g:EclimDisabled = 1
  endif
endfunction " }}}

function! eclim#Enable() " {{{
  if exists('g:EclimDisabled')
    unlet g:EclimDisabled
  endif
endfunction " }}}

function! eclim#EclimAvailable(...) " {{{
  " Optional args:
  "   echo: Whether or not to echo an error if eclim is not available
  "         (default: 1)
  let instances = eclim#UserHome() . '/.eclim/.eclimd_instances'
  let available = filereadable(instances)
  let echo = a:0 ? a:1 : 1
  if echo && !available && expand('<abuf>') == ''
    call eclim#util#EchoError(printf(
      \ 'No eclimd instances found running (eclimd created file not found %s)',
      \ eclim#UserHome() . '/.eclim/.eclimd_instances'))
  endif
  return available
endfunction " }}}

function! eclim#PingEclim(echo, ...) " {{{
  " If echo is non 0, then the result is echoed to the user.
  " Optional args:
  "   workspace

  let workspace = a:0 ? a:1 : ''
  if a:echo
    let result = eclim#Execute(s:command_ping, {'workspace': workspace})
    if type(result) == g:DICT_TYPE
      call eclim#util#Echo(
        \ 'eclim   ' . result.eclim . "\n" .
        \ 'eclipse ' . result.eclipse)
    endif
  else
    let savedErr = s:echo_connection_errors
    let savedLog = g:EclimLogLevel
    let s:echo_connection_errors = 0
    let g:EclimLogLevel = 'off'

    let result = eclim#Execute(s:command_ping, {'workspace': workspace})

    let s:echo_connection_errors = savedErr
    let g:EclimLogLevel = savedLog

    return type(result) == g:DICT_TYPE
  endif
endfunction " }}}

function! eclim#ParseSettingErrors(errors) " {{{
  let errors = []
  for error in a:errors
    let message = error.message
    let setting = substitute(message, '^\(.\{-}\): .*', '\1', '')
    let message = substitute(message, '^.\{-}: \(.*\)', '\1', '')
    if error.line == 1 && setting != error.message
      let line = search('^\s*' . setting . '\s*=', 'cnw')
      let error.line = line > 0 ? line : 1
    endif
    call add(errors, {
        \ 'bufnr': bufnr('%'),
        \ 'lnum': error.line,
        \ 'text': message,
        \ 'type': error.warning == 1 ? 'w' : 'e',
      \ })
  endfor
  return errors
endfunction " }}}

function! eclim#SaveSettings(command, project) " {{{
  " don't check modified since undo seems to not set the modified flag
  "if &modified
    let tempfile = substitute(tempname(), '\', '/', 'g')

    " get all lines, filtering out comments and blank lines
    let lines = filter(getline(1, line('$')), 'v:val !~ "^\\s*\\(#\\|$\\)"')

    " convert lines into a settings dict
    let index = 0
    let settings = {}
    let pattern = '^\s*\([[:alnum:]_.-]\+\)\s*=\s*\(.*\)'
    while index < len(lines)
      if lines[index] =~ pattern
        let name = substitute(lines[index], pattern, '\1', '')
        let value = substitute(lines[index], pattern, '\2', '')
        while value =~ '\\$'
          let index += 1
          let value = substitute(value, '\\$', '', '')
          let value .= substitute(lines[index], '^\s*', '', '')
        endwhile
        let settings[name] = value
      endif
      let index += 1
    endwhile
    call writefile([string(settings)], tempfile)

    if has('win32unix')
      let tempfile = eclim#cygwin#WindowsPath(tempfile)
    endif

    let command = a:command
    let command = substitute(command, '<project>', a:project, '')
    let command = substitute(command, '<settings>', tempfile, '')

    if exists('b:eclimd_instance')
      let result = eclim#Execute(command, {'instance': b:eclimd_instance})
    else
      let result = eclim#Execute(command)
    endif

    if type(result) == g:LIST_TYPE
      call eclim#util#EchoError
        \ ("Operation contained errors.  See location list for details.")
      let errors = eclim#ParseSettingErrors(result)
      call eclim#util#SetLocationList(errors)
    else
      call eclim#util#ClearLocationList()
      call eclim#util#Echo(result)
    endif

    setlocal nomodified
  "endif
endfunction " }}}

function! eclim#Settings(workspace) " {{{
  let instance = eclim#client#nailgun#ChooseEclimdInstance(a:workspace)
  if type(instance) != g:DICT_TYPE
    return
  endif

  let settings = eclim#Execute(s:command_settings, {'instance': instance})
  if type(settings) != g:LIST_TYPE
    return
  endif

  let content = ['# Global settings for workspace: ' . instance.workspace, '']
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

  call eclim#util#TempWindow("Workspace_Settings", content)
  setlocal buftype=acwrite
  setlocal filetype=jproperties
  setlocal noreadonly
  setlocal modifiable
  setlocal foldmethod=marker
  setlocal foldmarker={,}
  let b:eclimd_instance = instance

  augroup eclim_settings
    autocmd! BufWriteCmd <buffer>
    exec 'autocmd BufWriteCmd <buffer> ' .
      \ 'call eclim#SaveSettings(s:command_settings_update, "")'
  augroup END
endfunction " }}}

function! eclim#ShutdownEclim() " {{{
  call eclim#Execute(s:command_shutdown)
endfunction " }}}

function! eclim#LoadVimSettings() " {{{
  let settings_file = eclim#UserHome() . '/.eclim/.eclim_settings'
  if filereadable(settings_file)
    let lines = readfile(settings_file)
    if len(lines) == 1 && lines[0] =~ '^{.*}$'
      let settings = eval(lines[0])
      for [key, value] in items(settings)
        let name = 'g:Eclim' . key
        if !exists(name)
          exec 'let ' . name . ' = ' . string(value)
        endif
        unlet value
      endfor
    endif
  endif

  " Handle legacy sign/log level values
  let legacy = {0: 'off', 1: 'error', 2: 'error', 3: 'warning', 4: 'info', 5: 'debug', 6: 'trace'}
  if exists('g:EclimLogLevel') && type(g:EclimLogLevel) == g:NUMBER_TYPE
    let g:EclimLogLevel = get(legacy, g:EclimLogLevel, 'info')
  endif
  if exists('g:EclimSignLevel') && type(g:EclimSignLevel) == g:NUMBER_TYPE
    let g:EclimSignLevel = get(legacy, g:EclimSignLevel, 'info')
  endif
endfunction " }}}

function! eclim#AddVimSetting(namespace, name, default, desc, ...) " {{{
  " Optional args:
  "   regex: regular expression used to validate the setting's value.
  if !has_key(s:vim_settings, a:namespace)
    let s:vim_settings[a:namespace] = {}
  endif
  let name = substitute(a:name, 'g:Eclim', '', '')
  let s:vim_settings[a:namespace][name] = {'desc': a:desc}
  let s:vim_settings_defaults[name] = a:default
  let s:vim_settings_types[name] = type(a:default)
  let regex = a:0 ? a:1 : ''
  if regex != ''
    let s:vim_settings_validators[name] = regex
    if exists(a:name)
      exec 'let value = ' . a:name
      if value !~ '^' . regex . '$'
        echo a:name . ' must match ' . regex
        exec 'unlet ' . a:name
      endif
    endif
  endif

  if !exists(a:name)
    exec 'let ' . a:name . ' = ' . string(a:default)
  endif
endfunction " }}}

function! eclim#VimSettings() " {{{
  let content = [
      \ "# Eclim's global vim settings",
      \ "# The settings here allow you to configure the vim side behavior of eclim.",
      \ "# You can use <cr> on a setting name to open the eclim docs for that setting.",
      \ "#",
      \ "# Note: If you have g:EclimXXX variables set in your .vimrc, those will take",
      \ "# precedence over any changes you make here.",
    \ ]
  for namespace in sort(keys(s:vim_settings))
    let content += ['', '# ' . namespace . ' {{{']
    for name in sort(keys(s:vim_settings[namespace]))
      let setting = s:vim_settings[namespace][name]
      let desc = split(setting.desc, '\n')
      let content += map(desc, "'\t# ' . v:val")
      exec 'let value = string(g:Eclim' . name . ')'
      call add(content, "\t" . name . '=' . value)
    endfor
    let content += ['# }}}']
  endfor

  call eclim#util#TempWindow("Vim_Settings", content)
  setlocal buftype=acwrite
  setlocal filetype=jproperties
  setlocal noreadonly
  setlocal modifiable
  setlocal foldmethod=marker
  setlocal foldmarker={{{,}}}
  nnoremap <cr> :call <SID>VimSettingHelp()<cr>

  augroup eclim_settings
    autocmd! BufWriteCmd <buffer>
    autocmd BufWriteCmd <buffer> call eclim#SaveVimSettings()
  augroup END
endfunction " }}}

function! s:VimSettingHelp() " {{{
  let pos = getpos('.')
  try
    call cursor(0, 1)
    normal! w
    let syntax =  synIDattr(synID(line('.'), col('.'), 1), 'name')
  finally
    call setpos('.', pos)
  endtry

  if syntax == 'jpropertiesIdentifier'
    let line = getline('.')
    let name = substitute(line, '^\s*\(\w\+\)=.*', '\1', '')
    if name != line
      exec 'EclimHelp g:Eclim' . name
    endif
  endif
endfunction " }}}

function! eclim#SaveVimSettings() " {{{
  " get all lines, filtering out comments and blank lines
  let lines = filter(getline(1, line('$')), 'v:val !~ "^\\s*\\(#\\|$\\)"')

  " convert lines into a settings dict
  let index = 0
  let settings = {}
  let pattern = '^\s*\([[:alnum:]_.-]\+\)\s*=\s*\(.*\)'
  let errors = []
  while index < len(lines)
    try
      if lines[index] =~ pattern
        let name = substitute(lines[index], pattern, '\1', '')
        if !has_key(s:vim_settings_types, name)
          continue
        endif

        let value = substitute(lines[index], pattern, '\2', '')
        while value =~ '\\$'
          let index += 1
          let value = substitute(value, '\\$', '', '')
          let value .= substitute(lines[index], '^\s*', '', '')
        endwhile
        let value = substitute(value, "\\(^['\"]\\|['\"]$\\)", '', 'g')

        if has_key(s:vim_settings_validators, name)
          let regex = s:vim_settings_validators[name]
          if value !~ '^' . regex . '$'
            let [line, col] = searchpos('^\s*' . name . '=', 'nw')
            call add(errors, {
                \ 'filename': expand('%'),
                \ 'message': name . ': must match ' . regex,
                \ 'line': line,
                \ 'column': col,
                \ 'type': 'error',
              \ })
            continue
          endif
        endif

        if s:vim_settings_types[name] != g:STRING_TYPE
          try
            let typed_value = eval(value)
            unlet value
            let value = typed_value
          catch /E121\|E115/
            let [line, col] = searchpos('^\s*' . name . '=', 'nw')
            call add(errors, {
                \ 'filename': expand('%'),
                \ 'message': name . ': ' . v:exception,
                \ 'line': line,
                \ 'column': col,
                \ 'type': 'error',
              \ })
            continue
          endtry
        endif

        let default = s:vim_settings_defaults[name]
        if value != default
          let settings[name] = value
        endif
      endif
    finally
      let index += 1
      unlet! value typed_value default
    endtry
  endwhile

  if len(errors)
    call eclim#util#SetLocationList(eclim#util#ParseLocationEntries(errors))
    call eclim#util#EchoError(
      \ len(errors) . ' error' . (len(errors) > 1 ? 's' : '') . ' found.')
    return
  endif

  call eclim#util#ClearLocationList()

  if !isdirectory(eclim#UserHome() . '/.eclim')
    call mkdir(eclim#UserHome() . '/.eclim')
  endif
  let settings_file = eclim#UserHome() . '/.eclim/.eclim_settings'
  if writefile([string(settings)], settings_file) == 0
    call eclim#util#Echo('Settings saved. You may need to restart vim for all changes to take affect.')
  else
    call eclim#util#Echo('Unable to write settings.')
  endif

  setlocal nomodified
endfunction " }}}

function! eclim#UserHome() " {{{
  let home = expand('$HOME')
  if has('win32unix')
    let home = eclim#cygwin#WindowsHome()
  elseif has('win32') || has('win64')
    let home = expand('$USERPROFILE')
  endif
  return substitute(home, '\', '/', 'g')
endfunction " }}}

function! eclim#WaitOnRunningJobs(timeout) " {{{
  " Args:
  "   timeout: max time to wait in milliseconds
  let running = 1
  let waited = 0
  while running && waited < a:timeout
    let jobs = eclim#Execute(s:command_jobs)
    if type(jobs) == g:LIST_TYPE
      let running = 0
      for job in jobs
        if job.status == 'running'
          call eclim#util#EchoDebug('Wait on job: ' . job.job)
          let running = 1
          let waited += 300
          sleep 300m
          break
        endif
      endfor
    endif
  endwhile
  if running
    call eclim#util#EchoDebug('Timeout exceeded waiting on jobs')
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
