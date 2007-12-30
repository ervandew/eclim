" Author:  Eric Van Dewoestine
" Version: $Revision$
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
" Copyright (c) 2005 - 2008
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

  if !exists("g:EclimSystemWorkaround")
    let g:EclimSystemWorkaround = 0
  endif

  if !exists("g:EclimHome")
    " set via installer
    "${vim.eclim.home}"
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
" }}}

" ExecuteEclim(args) {{{
" Executes eclim using the supplied argument string.
function! eclim#ExecuteEclim (args)
  if exists('g:EclimDisabled')
    return
  endif

  let args = a:args

  " encode special characters
  " http://www.cs.net/lucid/ascii.htm
  let args = substitute(args, '*', '%2A', 'g')
  let args = substitute(args, '\$', '%24', 'g')
  let command = eclim#GetEclimCommand()
  if string(command) == '0'
    "let g:EclimDisabled = 1
    if expand('<amatch>') == '' && exists('g:EclimErrorReason')
      call eclim#util#EchoError(g:EclimErrorReason)
    endif
    return 0
  endif
  let command = command . ' ' . args

  " for windows, need to add a trailing quote to complete the command.
  if command =~ '^"[a-zA-Z]:'
    let command = command . '"'
  endif

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
    let result = eclim#util#System(command)
    let result = substitute(result, '\(.*\)\n$', '\1', '')
  endif

  call eclim#util#Echo(' ')

  " check for errors
  let error = ''
  if result =~ 'Exception.*\s\+\<at\> '
    let error = substitute(result, '\(.\{-}\)\n.*', '\1', '')
  elseif v:shell_error
    let error = result
  endif

  if v:shell_error || error != ''
    if g:EclimShowErrors
      if error =~ s:connect
        " eclimd is not running and we appear to not be in an autocmd
        if expand('<amatch>') == ''
          call eclim#util#EchoWarning("unable to connect to eclimd - " . error)
        endif
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
  silent eclim#util#Exec(command)
  let result = join(readfile(tempfile), "\n")

  call delete(tempfile)
  redraw!

  return result
endfunction " }}}

" GetEclimCommand() {{{
" Gets the command to exexute eclim.
function! eclim#GetEclimCommand ()
  if !exists('g:EclimPath')
    let eclim_home = eclim#GetEclimHome()
    if eclim_home == '' || string(eclim_home) == '0'
      return
    endif

    let g:EclimPath = substitute(eclim_home, '\', '/', 'g') .
      \ '/bin/' . g:EclimCommand

    if has("win32") || has("win64")
      let g:EclimPath = g:EclimPath . (has('win95') ? '.bat' : '.cmd')
    elseif has("win32unix")
      let g:EclimPath = system('cygpath "' . g:EclimPath . '"')
      let g:EclimPath = substitute(g:EclimPath, '\n.*', '', '')
    endif

    if !filereadable(g:EclimPath)
      let g:EclimErrorReason = 'Could not locate file: ' . g:EclimPath
      return
    endif

    " on windows, the command must be executed on the drive where eclipse is
    " installed.
    if has("win32") || has("win64")
      let g:EclimPath =
        \ '"' . substitute(g:EclimPath, '^\([a-zA-Z]:\).*', '\1', '') .
        \ ' && "' . g:EclimPath . '"'
    else
      let g:EclimPath = '"' . g:EclimPath . '"'
    endif
  endif
  return g:EclimPath
endfunction " }}}

" GetEclimHome() {{{
" Gets the directory of the main eclim eclipse plugin.
function! eclim#GetEclimHome ()
  if !exists('g:EclimHome')
    if !exists('$ECLIPSE_HOME')
      let g:EclimErrorReason = 'ECLIPSE_HOME must be set.'
      return
    endif

    let g:EclimHome = eclim#util#Glob('$ECLIPSE_HOME/plugins/org.eclim_*')
    if g:EclimHome == ''
      let g:EclimErrorReason =
        \ "eclim plugin not found in eclipse plugins directory at " .
        \ "ECLIPSE_HOME = '" .  expand('$ECLIPSE_HOME') . "'"
      return
    elseif g:EclimHome =~ "\n"
      let g:EclimErrorReason =
        \ "multiple versions of eclim plugin found in eclipse plugins directory at " .
        \ "ECLIPSE_HOME = '" .  expand('$ECLIPSE_HOME') . "'"
      return
    endif
  endif
  return g:EclimHome
endfunction " }}}

" PatchEclim(file, revision) {{{
" Patches an eclim vim script file.
function! eclim#PatchEclim (file, revision)
  let command = s:command_patch_file
  let command = substitute(command, '<file>', a:file, '')
  let command = substitute(command, '<revision>', a:revision, '')
  let command = substitute(command, '<basedir>', EclimBaseDir(), '')

  let result = eclim#ExecuteEclim(command)
  if result != '0'
    call eclim#util#Echo(result)
  endif
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
    let tempfile = substitute(tempname(), '\', '/', 'g')
    silent exec 'write! ' . escape(tempfile, ' ')

    let command = substitute(s:command_settings_update, '<settings>', tempfile, '')
    let result = eclim#ExecuteEclim(command)
    call eclim#util#Echo(result)

    setlocal nomodified
  "endif
endfunction " }}}

" Settings() {{{
" Opens a window that can be used to edit the global settings.
function! eclim#Settings ()
  if eclim#util#TempWindowCommand(s:command_settings, "Eclim_Global_Settings")
    setlocal buftype=acwrite
    setlocal filetype=jproperties
    setlocal noreadonly
    setlocal modifiable
    setlocal foldmethod=marker
    setlocal foldmarker={,}

    augroup eclim_settings
      autocmd! BufWriteCmd <buffer>
      autocmd BufWriteCmd <buffer> call <SID>SaveSettings()
    augroup END
  endif
endfunction " }}}

" ShutdownEclim() {{{
" Shuts down the eclimd server.
function! eclim#ShutdownEclim ()
  call eclim#ExecuteEclim(s:command_shutdown)
endfunction " }}}

" CommandCompleteScriptRevision(argLead, cmdLine, cursorPos) {{{
" Custom command completion for vim script names and revision numbers.
function! eclim#CommandCompleteScriptRevision (argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseArgs(cmdLine)
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
