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

" Global Variables {{{
  if !exists("g:EclimShowErrors")
    let g:EclimShowErrors = 1
  endif
" }}}

" Script Variables {{{
  let s:command_patch_file =
    \ '-command patch_file -f <file> -r <revision> -b <basedir>'
  let s:command_patch_revisions = '-command patch_revisions -f <file>'
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

" ExecuteEclim(command, [port]) {{{
" Executes the supplied eclim command.
function! eclim#ExecuteEclim(command, ...)
  if exists('g:EclimDisabled')
    return
  endif

  " eclimd appears to be down, so exit early if in an autocmd
  if !g:eclimd_running && expand('<amatch>') != ''
    " check for file created by eclimd to signal that it is running.
    if !filereadable(expand('~/.eclim/.eclimd_instances'))
      return
    endif
  endif

  let g:eclimd_running = 1

  let command = a:command

  " encode special characters
  " http://www.cs.net/lucid/ascii.htm
  let command = substitute(command, '*', '%2A', 'g')
  let command = substitute(command, '\$', '%24', 'g')

  " execute the command.
  let port = len(a:000) > 0 ? a:000[0] : eclim#client#nailgun#GetNgPort()
  let [retcode, result] = eclim#client#nailgun#Execute(port, command)
  let result = substitute(result, '\n$', '', '')

  " not sure this is the best place to handle this, but when using the python
  " client, the result has a trailing ctrl-m on windows.
  if has('win32') || has('win64')
    let result = substitute(result, "\<c-m>$", '', '')
  endif

  call eclim#util#Echo(' ')

  " check for errors
  let error = ''
  if result =~ '^[^\n]*Exception:\?[^\n]*\n\s\+\<at\> ' ||
   \ result =~ '^[^\n]*ResourceException(.\{-})\[[0-9]\+\]:[^\n]*\n\s\+\<at\> '
    let error = substitute(result, '\(.\{-}\)\n.*', '\1', '')
  elseif retcode
    let error = result
  endif

  if retcode || error != ''
    if g:EclimShowErrors
      if error =~ s:connect
        " eclimd is not running, disable further eclimd calls
        let g:eclimd_running = 0

        " if we are not in an autocmd, alert the user that eclimd is not
        " running.
        if expand('<amatch>') == ''
          call eclim#util#EchoWarning(
            \ "unable to connect to eclimd (port: " . port . ") - " . error)
        endif
      else
        let error = error . "\n" .
          \ 'while executing command (port: ' . port . '): ' . command
        call eclim#util#EchoError(error)
      endif
    endif
    return
  endif

  return result
endfunction " }}}

" Disable() {{{
" Temporarily disables communication with eclimd.
function! eclim#Disable()
  if !exists('g:EclimDisabled')
    let g:EclimDisabled = 1
  endif
endfunction " }}}

" Enable() {{{
" Re-enables communication with eclimd.
function! eclim#Enable()
  if exists('g:EclimDisabled')
    unlet g:EclimDisabled
  endif
endfunction " }}}

" EclimAvailable() {{{
function! eclim#EclimAvailable()
  return filereadable(expand('~/.eclim/.eclimd_instances'))
endfunction " }}}

" PatchEclim(file, revision) {{{
" Patches an eclim vim script file.
function! eclim#PatchEclim(file, revision)
  let command = s:command_patch_file
  let command = substitute(command, '<file>', a:file, '')
  let command = substitute(command, '<revision>', a:revision, '')
  let command = substitute(command, '<basedir>', EclimBaseDir(), '')

  let result = eclim#ExecuteEclim(command)
  if result != '0'
    call eclim#util#Echo(result)
  endif
endfunction " }}}

" PingEclim(echo, [workspace]) {{{
" Pings the eclimd server.
" If echo is non 0, then the result is echoed to the user.
function! eclim#PingEclim(echo, ...)
  let workspace_found = 1
  if len(a:000) > 0 && a:1 != ''
    let workspace = substitute(a:1, '\', '/', 'g')
    let workspace .= workspace !~ '/$' ? '/' : ''
    if !eclim#util#ListContains(eclim#eclipse#GetAllWorkspaceDirs(), workspace)
      let workspace_found = 0
    endif
    let port = eclim#client#nailgun#GetNgPort(workspace)
  else
    let workspace = eclim#eclipse#GetWorkspaceDir()
    let port = eclim#client#nailgun#GetNgPort()
  endif

  if a:echo
    if !workspace_found
      call eclim#util#Echo('eclimd instance for workspace not found: ' . workspace)
      return
    endif

    let result = eclim#ExecuteEclim(s:command_ping, port)
    if result != '0'
      call eclim#util#Echo(result)
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

    return result != '0'
  endif
endfunction " }}}

" ParseSettingErrors() {{{
function! eclim#ParseSettingErrors(errors)
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

" SaveSettings(command, project, [port]) {{{
function! eclim#SaveSettings(command, project, ...)
  " don't check modified since undo seems to not set the modified flag
  "if &modified
    let tempfile = substitute(tempname(), '\', '/', 'g')
    silent exec 'write! ' . escape(tempfile, ' ')

    let command = a:command
    let command = substitute(command, '<project>', a:project, '')
    let command = substitute(command, '<settings>', tempfile, '')

    if len(a:000) > 0
      let port = a:000[0]
      let result = eclim#ExecuteEclim(command, port)
    else
      let result = eclim#ExecuteEclim(command)
    endif

    if result =~ ':'
      call eclim#util#EchoError
        \ ("Operation contained errors.  See location list for details.")
      call eclim#util#SetLocationList
        \ (eclim#ParseSettingErrors(split(result, '\n')))
    else
      call eclim#util#ClearLocationList()
      call eclim#util#Echo(result)
    endif

    setlocal nomodified
  "endif
endfunction " }}}

" Settings(workspace) {{{
" Opens a window that can be used to edit the global settings.
function! eclim#Settings(workspace)
  let workspace = a:workspace
  if workspace == ''
    let workspace = eclim#eclipse#ChooseWorkspace()
    if workspace == '0'
      return
    endif
  endif

  let port = eclim#client#nailgun#GetNgPort(workspace)

  if eclim#util#TempWindowCommand(
   \ s:command_settings, "Eclim_Global_Settings", port)
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
  endif
endfunction " }}}

" ShutdownEclim() {{{
" Shuts down the eclimd server.
function! eclim#ShutdownEclim()
  let workspace = eclim#eclipse#ChooseWorkspace()
  if workspace != '0'
    let port = eclim#client#nailgun#GetNgPort()
    call eclim#ExecuteEclim(s:command_shutdown, port)
  endif
endfunction " }}}

" CommandCompleteScriptRevision(argLead, cmdLine, cursorPos) {{{
" Custom command completion for vim script names and revision numbers.
function! eclim#CommandCompleteScriptRevision(argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]

  " complete script name for first arg.
  if cmdLine =~ '^' . args[0] . '\s*' . escape(argLead, '.\') . '$'
    let dir = EclimBaseDir()
    let results = split(eclim#util#Glob(dir . '/' . argLead . '*'), '\n')
    call map(results, "substitute(v:val, '\\', '/', 'g')")
    call map(results, 'isdirectory(v:val) ? v:val . "/" : v:val')
    call map(results, 'substitute(v:val, dir, "", "")')
    call map(results, 'substitute(v:val, "^/", "", "")')

    return results
  endif

  " for remaining args, complete revision numbers
  let file = substitute(cmdLine, '^' . args[0] . '\s*\(.\{-}\)\s.*', '\1', '')
  let command = s:command_patch_revisions
  let command = substitute(command, '<file>', file, '')

  "let argLead = len(args) > 2 ? args[len(args) - 1] : ""
  let result = eclim#ExecuteEclim(command)
  if result != '0'
    let results =  split(result, '\n')
    call filter(results, 'v:val =~ "^' . argLead . '"')
    return results
  endif
  return []
endfunction " }}}

" vim:ft=vim:fdm=marker
