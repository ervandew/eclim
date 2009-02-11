" Author:  Eric Van Dewoestine
"
" Description: {{{
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
  if !exists("g:EclimNailgunKeepAlive")
    " keepAlive flag - can be re-defined in the user ~/.vimrc .
    " Read once, on client initialization. Subsequent changes of
    " this flag in run-time has no effect.
    let g:EclimNailgunKeepAlive = 0
  endif
" }}}

" Execute(command) {{{
" Function which invokes nailgun.
function eclim#client#nailgun#Execute(command)
  if !exists('g:EclimNailgunClient')
    call s:DetermineClient()
  endif

  if g:EclimNailgunClient == 'python'
    return eclim#client#python#nailgun#Execute(a:command)
  endif

  let command = eclim#client#nailgun#GetEclimCommand()
  if string(command) == '0'
    return [1, g:EclimErrorReason]
  endif

  let command = command . ' ' . a:command

  " for windows, need to add a trailing quote to complete the command.
  if command =~ '^"[a-zA-Z]:'
    let command = command . '"'
  endif

  let result = eclim#util#System(command)
  return [v:shell_error, result]
endfunction " }}}

" GetEclimCommand() {{{
" Gets the command to exexute eclim.
function! eclim#client#nailgun#GetEclimCommand()
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

" s:DetermineClient() {{{
function s:DetermineClient()
  if has('python')
    let g:EclimNailgunClient = 'python'
  else
    let g:EclimNailgunClient = 'external'
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
