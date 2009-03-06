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
  if !exists("g:EclimCommand")
    let g:EclimCommand = 'eclim'
  endif
  if !exists("g:EclimShowErrors")
    let g:EclimShowErrors = 1
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

  let s:eclimd_running = 1
  let s:eclimd_available_file = g:EclimHome . '/.available'
" }}}

" ExecuteEclim(command) {{{
" Executes the supplied eclim command.
function! eclim#ExecuteEclim(command)
  if exists('g:EclimDisabled')
    return
  endif

  " eclimd appears to be down, so exit early if in an autocmd
  if !s:eclimd_running && expand('<amatch>') != ''
    " check for file created by eclimd to signal that it is back up.
    if !filereadable(s:eclimd_available_file)
      return
    endif
  endif

  let s:eclimd_running = 1

  let command = a:command

  " encode special characters
  " http://www.cs.net/lucid/ascii.htm
  let command = substitute(command, '*', '%2A', 'g')
  let command = substitute(command, '\$', '%24', 'g')

  " execute the command.
  let [retcode, result] = eclim#client#nailgun#Execute(command)
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
        let s:eclimd_running = 0

        " if we are not in an autocmd, alert the user that eclimd is not
        " running.
        if expand('<amatch>') == ''
          call eclim#util#EchoWarning("unable to connect to eclimd - " . error)
        endif
      else
        let error = error . "\n" . 'while executing command: ' . command
        call eclim#util#EchoError(error)
      endif
    endif
    return
  endif

  return result
endfunction " }}}

" GetEclimHome() {{{
" Gets the directory of the main eclim eclipse plugin.
function! eclim#GetEclimHome()
  if !exists('g:EclimHome')
    if !exists('$ECLIM_ECLIPSE_HOME')
      let g:EclimErrorReason = 'ECLIM_ECLIPSE_HOME must be set.'
      return
    endif

    let g:EclimHome = eclim#util#Glob('$ECLIM_ECLIPSE_HOME/plugins/org.eclim_*')
    if g:EclimHome == ''
      let g:EclimErrorReason =
        \ "eclim plugin not found in eclipse plugins directory at " .
        \ "ECLIM_ECLIPSE_HOME = '" .  expand('$ECLIM_ECLIPSE_HOME') . "'"
      return
    elseif g:EclimHome =~ "\n"
      let g:EclimErrorReason =
        \ "multiple versions of eclim plugin found in eclipse plugins directory at " .
        \ "ECLIM_ECLIPSE_HOME = '" .  expand('$ECLIM_ECLIPSE_HOME') . "'"
      return
    endif
  endif
  return g:EclimHome
endfunction " }}}

" Disable() {{{
" Temporarily disables communication with eclimd.
function! eclim#Disable()
  if !exists('g:EclimDisabled')
    let g:EclimDisabled = 1

    " if taglisttoo enabled, disable its communication w/ eclimd
    if exists('g:Tlist_Ctags_Cmd_Orig')
      let g:EclimTlistCtagsCmdSaved = g:Tlist_Ctags_Cmd
      let g:Tlist_Ctags_Cmd = g:Tlist_Ctags_Cmd_Orig
    endif
  endif
endfunction " }}}

" Enable() {{{
" Re-enables communication with eclimd.
function! eclim#Enable()
  if exists('g:EclimDisabled')
    unlet g:EclimDisabled
    if exists('g:EclimTlistCtagsCmdSaved')
      let g:Tlist_Ctags_Cmd = g:EclimTlistCtagsCmdSaved
      unlet g:EclimTlistCtagsCmdSaved
    endif
  endif
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

" PingEclim(echo) {{{
" Pings the eclimd server.
" If echo is non 0, then the result is echoed to the user.
function! eclim#PingEclim(echo)
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
function! s:SaveSettings()
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
function! eclim#Settings()
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
function! eclim#ShutdownEclim()
  call eclim#ExecuteEclim(s:command_shutdown)
endfunction " }}}

" CommandCompleteScriptRevision(argLead, cmdLine, cursorPos) {{{
" Custom command completion for vim script names and revision numbers.
function! eclim#CommandCompleteScriptRevision(argLead, cmdLine, cursorPos)
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
