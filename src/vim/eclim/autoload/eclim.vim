" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
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

" Global Variables {{{
  if !exists("g:EclimCommand")
    let g:EclimCommand = 'eclim'
  endif
  if !exists("g:EclimShowErrors")
    let g:EclimShowErrors = 1
  endif
  let g:EclimdRunning = 1

  if !exists("g:EclimSystemWorkaround")
    let g:EclimSystemWorkaround = 0
  endif
" }}}

" Script Variables {{{
  let s:command_ping = '-command ping'
  let s:command_settings = '-command settings -filter vim'
  let s:command_settings_update = '-command settings_update -s "<settings>"'
  let s:command_shutdown = "-command shutdown"
  let s:connect= '^connect: .*$'

  " list of commands that may fail using system() call, so using a temp file
  " instead.
  let s:exec_commands = ['java_complete']
" }}}

" ExecuteEclim(args) {{{
" Executes eclim using the supplied argument string.
function! eclim#ExecuteEclim (args)
  if !g:EclimdRunning
    return 0
  endif

  let args = a:args

  " encode special characters
  " http://www.cs.net/lucid/ascii.htm
  let args = substitute(args, '*', '%2A', 'g')
  let args = substitute(args, '\$', '%24', 'g')
  let command = eclim#GetEclimCommand() . ' ' . args
  if string(command) == '0'
    return 0
  endif
  let command = command . ' ' . args

  call eclim#util#EchoDebug("eclim: executing (Ctrl-C to cancel)...")
  call eclim#util#EchoTrace("command: " . command)

  " determine whether to use system call or exec with a temp file
  let use_exec = 0
  if g:EclimSystemWorkaround
    for cmd in s:exec_commands
      if command =~ '-command\s\+' . cmd
        let use_exec = 1
        break
      endif
    endfor
  endif

  " execute the command.
  if use_exec
    let result = eclim#ExecuteTempFile(command)
  else
    let result = system(command)
    let result = substitute(result, '\(.*\)\n$', '\1', '')
  endif

  call eclim#util#Echo("")

  " check for errors
  let error = ''
  if v:shell_error && result =~ 'Exception.*\s\+\<at\> '
    let error = substitute(result, '\(.\{-}\)\n.*', '\1', '')
  elseif v:shell_error
    let error = result
  endif

  if v:shell_error
    if g:EclimShowErrors
      if error =~ s:connect
        call eclim#util#EchoWarning("unable to connect to eclimd - " . error)
        let g:EclimdRunning = 0
      else
        let error = error . "\n" . 'while executing command: ' . command
        call eclim#util#EchoError(error)
      endif
    endif
    return 0
  endif

  return result
endfunction " }}}

" ExecuteTempFile(command) {{{
" Exectue the supplied command piping results to a temp file.
function! eclim#ExecuteTempFile (command)
  let tempfile = tempname()

  let command = '!' . a:command . ' > ' . tempfile . ' 2>&1'
  silent exec command
  let result = join(readfile(tempfile), "\n")

  call delete(tempfile)
  redraw!

  return result
endfunction " }}}

" GetEclimCommand() {{{
" Gets the command to exexute eclim.
function! eclim#GetEclimCommand ()
  if !exists('g:EclimPath')
    if !exists('$ECLIPSE_HOME')
      call eclim#util#EchoError('ECLIPSE_HOME must be set.')
      return
    endif
    let g:EclimHome = glob(expand('$ECLIPSE_HOME') . '/plugins/org.eclim_*')
    if g:EclimHome == ''
      call eclim#util#EchoError(
        \ "eclim plugin not found in eclipse plugins directory for " .
        \ "ECLIPSE_HOME = '" .  expand('$ECLIPSE_HOME') . "'")
      return
    endif
    let g:EclimPath = substitute(g:EclimHome, '\', '/', 'g') . '/bin/' . g:EclimCommand
  endif
  return g:EclimPath
endfunction " }}}

" PingEclim(echo) {{{
" Pings the eclimd server.
" If echo is non 0, then the result is echoed to the user.
function! eclim#PingEclim (echo)
  if a:echo
    let result = eclim#ExecuteEclim(s:command_ping)
    if result != '0'
      call eclim#util#Echo(result)
    endif
  else
    let savedErr = g:EclimShowErrors
    let savedLog = g:EclimLogLevel
    let g:EclimShowErrors = 0
    let g:EclimLogLevel = 0

    let result = eclim#ExecuteEclim(s:command_ping)

    let g:EclimShowErrors = savedErr
    let g:EclimLogLevel = savedLog

    return result != '0'
  endif
endfunction " }}}

" SaveSettings() {{{
function! s:SaveSettings ()
  " don't check modified since undo seems to not set the modified flag
  "if &modified
    let settings = getline(1, line('$'))
    let result = ""
    for setting in settings
      if setting !~ '^\s*\($\|#\)'
        if result != ""
          let result = result . "|"
        endif
        let result = result . setting
      endif
    endfor

    let command = substitute(s:command_settings_update, '<settings>', result, '')
    let result = eclim#ExecuteEclim(command)
    call eclim#util#Echo(result)

    setlocal nomodified
  "endif
endfunction " }}}

" Settings() {{{
" Opens a window that can be used to edit the global settings.
function! eclim#Settings ()
  call eclim#util#TempWindowCommand(s:command_settings, "Eclim_Global_Settings")

  setlocal buftype=acwrite
  setlocal filetype=jproperties
  setlocal noreadonly
  setlocal modifiable

  augroup eclim_settings
    autocmd!
    autocmd BufWriteCmd <buffer> call <SID>SaveSettings()
  augroup END
endfunction " }}}

" ShutdownEclim() {{{
" Shuts down the eclimd server.
function! eclim#ShutdownEclim ()
  call eclim#ExecuteEclim(s:command_shutdown)
endfunction " }}}

" vim:ft=vim:fdm=marker
