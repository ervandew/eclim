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
" Copyright (C) 2005 - 2012  Eric Van Dewoestine
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
  if !exists("g:EclimShowErrors")
    let g:EclimShowErrors = 1
  endif
" }}}

" Script Variables {{{
  let s:command_ping = '-command ping'
  let s:command_settings = '-command settings'
  let s:command_settings_update = '-command settings_update -s "<settings>"'
  let s:command_shutdown = "-command shutdown"
  let s:connect= '^connect: .*$'

  " list of commands that may fail using system() call, so using a temp file
  " instead.
  let s:exec_commands = ['java_complete']

  let g:eclimd_running = 1
" }}}

function! eclim#ExecuteEclim(command, ...) " {{{
  " Optional args:
  "   port: the nailgun port to use.
  "   options {
  "     exec: 1 to execute the command using execute instead of system.
  "     raw: 1 to get the result without evaluating as json
  "   }

  if exists('g:EclimDisabled')
    return
  endif

  " eclimd appears to be down, so exit early if in an autocmd
  if !g:eclimd_running && expand('<abuf>') != ''
    " check for file created by eclimd to signal that it is running.
    if !eclim#EclimAvailable()
      return
    endif
  endif

  let g:eclimd_running = 1

  let command = '-editor vim ' . a:command

  " encode special characters
  " http://www.w3schools.com/TAGS/ref_urlencode.asp
  let command = substitute(command, '\*', '%2A', 'g')
  let command = substitute(command, '\$', '%24', 'g')
  let command = substitute(command, '<', '%3C', 'g')
  let command = substitute(command, '>', '%3E', 'g')

  " execute the command.
  let port = len(a:000) > 0 ? a:000[0] : eclim#client#nailgun#GetNgPort()
  let options = len(a:000) > 1 ? a:000[1] : {}
  let exec = get(options, 'exec', 0)
  let [retcode, result] = eclim#client#nailgun#Execute(port, command, exec)
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
    if g:EclimLogLevel < 10
      let error = substitute(result, '\(.\{-}\)\n.*', '\1', '')
    else
      let error = result
    endif
  elseif retcode
    let error = result
  endif

  if retcode || error != ''
    if g:EclimShowErrors
      if error =~ s:connect
        " eclimd is not running, disable further eclimd calls
        let g:eclimd_running = 0

        " if we are not in an autocmd or the autocmd is for an acwrite buffer,
        " alert the user that eclimd is not running.
        if expand('<abuf>') == '' || &buftype == 'acwrite'
          call eclim#util#EchoWarning(
            \ "unable to connect to eclimd (port: " . port . ") - " . error)
        endif
      else
        let error = error . "\n" .
          \ 'while executing command (port: ' . port . '): ' . command
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

function! eclim#EclimAvailable() " {{{
  let instances = eclim#UserHome() . '/.eclim/.eclimd_instances'
  return filereadable(instances)
endfunction " }}}

function! eclim#PingEclim(echo, ...) " {{{
  " If echo is non 0, then the result is echoed to the user.
  " Optional args:
  "   workspace

  let workspace_found = 1
  if len(a:000) > 0 && a:1 != ''
    let workspace = substitute(a:1, '\', '/', 'g')
    let workspace .= workspace !~ '/$' ? '/' : ''
    if !eclim#util#ListContains(eclim#eclipse#GetAllWorkspaceDirs(), workspace)
      let workspace_found = 0
    endif
    let port = eclim#client#nailgun#GetNgPort(workspace)
  else
    let workspace = eclim#eclipse#ChooseWorkspace()
    let port = eclim#client#nailgun#GetNgPort(workspace)
  endif

  if a:echo
    if !workspace_found
      call eclim#util#Echo('eclimd instance for workspace not found: ' . workspace)
      return
    endif

    let result = eclim#ExecuteEclim(s:command_ping, port)
    if type(result) == g:DICT_TYPE
      call eclim#util#Echo(
        \ 'eclim   ' . result.eclim . "\n" .
        \ 'eclipse ' . result.eclipse)
    endif
  else
    if !workspace_found
      return
    endif

    let savedErr = g:EclimShowErrors
    let savedLog = g:EclimLogLevel
    let g:EclimShowErrors = 0
    let g:EclimLogLevel = 0

    let result = eclim#ExecuteEclim(s:command_ping, port)

    let g:EclimShowErrors = savedErr
    let g:EclimLogLevel = savedLog

    return type(result) == g:DICT_TYPE
  endif
endfunction " }}}

function! eclim#ParseSettingErrors(errors) " {{{
  let errors = []
  for error in a:errors
    let setting = substitute(error, '^\(.\{-}\): .*', '\1', '')
    let message = substitute(error, '^.\{-}: \(.*\)', '\1', '')
    let line = search('^\s*' . setting . '\s*=', 'cnw')
    call add(errors, {
        \ 'bufnr': bufnr('%'),
        \ 'lnum': line > 0 ? line : 1,
        \ 'text': message,
        \ 'type': 'e'
      \ })
  endfor
  return errors
endfunction " }}}

function! eclim#SaveSettings(command, project, ...) " {{{
  " Optional args:
  "   port

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

    if len(a:000) > 0
      let port = a:000[0]
      let result = eclim#ExecuteEclim(command, port)
    else
      let result = eclim#ExecuteEclim(command)
    endif

    if type(result) == g:LIST_TYPE
      call eclim#util#EchoError
        \ ("Operation contained errors.  See location list for details.")
      call eclim#util#SetLocationList(eclim#ParseSettingErrors(result))
    else
      call eclim#util#ClearLocationList()
      call eclim#util#Echo(result)
    endif

    setlocal nomodified
  "endif
endfunction " }}}

function! eclim#Settings(workspace) " {{{
  let workspace = a:workspace
  if workspace == ''
    let workspace = eclim#eclipse#ChooseWorkspace()
    if workspace == '0'
      return
    endif
  endif

  let port = eclim#client#nailgun#GetNgPort(workspace)

  let settings = eclim#ExecuteEclim(s:command_settings, port)
  if type(settings) != g:LIST_TYPE
    return
  endif

  let content = ['# Global settings for workspace: ' . workspace, '']
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

  call eclim#util#TempWindow("Eclim_Global_Settings", content)
  setlocal buftype=acwrite
  setlocal filetype=jproperties
  setlocal noreadonly
  setlocal modifiable
  setlocal foldmethod=marker
  setlocal foldmarker={,}

  augroup eclim_settings
    autocmd! BufWriteCmd <buffer>
    exec 'autocmd BufWriteCmd <buffer> ' .
      \ 'call eclim#SaveSettings(s:command_settings_update, "", ' . port . ')'
  augroup END
endfunction " }}}

function! eclim#ShutdownEclim() " {{{
  let workspace = eclim#eclipse#ChooseWorkspace()
  if workspace != '0'
    let port = eclim#client#nailgun#GetNgPort()
    call eclim#ExecuteEclim(s:command_shutdown, port)
  endif
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

" vim:ft=vim:fdm=marker
