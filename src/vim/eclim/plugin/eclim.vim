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
if !exists("g:EclimLogLevel")
  let g:EclimLogLevel = 4
endif

if !exists("g:EclimSignLevel")
  if has("signs")
    let g:EclimSignLevel = 5
  else
    let g:EclimSignLevel = 0
  endif
endif

if !exists("g:EclimShowCurrentError")
  let g:EclimShowCurrentError = 1
endif

if !exists("g:EclimShowCurrentErrorBalloon")
  let g:EclimShowCurrentErrorBalloon = 1
endif

if !exists("g:EclimMakeLCD")
  let g:EclimMakeLCD = 1
endif

if !exists("g:EclimMakeQfFilter")
  let g:EclimMakeQfFilter = 1
endif

if !exists("g:EclimIndent")
  if !&expandtab
    let g:EclimIndent = "\t"
  else
    let g:EclimIndent = ""
    let index = 0
    while index < &shiftwidth
      let g:EclimIndent = g:EclimIndent . " "
      let index = index + 1
    endwhile
  endif
endif

if !exists("g:EclimSeparator")
  let g:EclimSeparator = '/'
  if has("win32") || has("win64")
    let g:EclimSeparator = '\'
  endif
endif
let g:EclimQuote = "['\"]"

if !exists("g:EclimTempDir")
  let g:EclimTempDir = expand('$TMP')
  if g:EclimTempDir == '$TMP'
    let g:EclimTempDir = expand('$TEMP')
  endif
  if g:EclimTempDir == '$TEMP' && has('unix')
    let g:EclimTempDir = '/tmp'
  endif
  " FIXME: mac?

  let g:EclimTempDir = substitute(g:EclimTempDir, '\', '/', 'g')
endif

if !exists("g:EclimTraceHighlight")
  let g:EclimTraceHighlight = "Normal"
endif
if !exists("g:EclimDebugHighlight")
  let g:EclimDebugHighlight = "Normal"
endif
if !exists("g:EclimInfoHighlight")
  let g:EclimInfoHighlight = "Statement"
endif
if !exists("g:EclimWarningHighlight")
  let g:EclimWarningHighlight = "WarningMsg"
endif
if !exists("g:EclimErrorHighlight")
  let g:EclimErrorHighlight = "Error"
endif
if !exists("g:EclimFatalHighlight")
  let g:EclimFatalHighlight = "Error"
endif

if !exists("g:EclimEchoErrorHighlight")
  let g:EclimEchoErrorHighlight = "Error"
endif
" }}}

" Command Declarations {{{
if !exists(":PingEclim")
  command PingEclim :call eclim#PingEclim(1)
endif
if !exists(":ShutdownEclim")
  command ShutdownEclim :call eclim#ShutdownEclim()
endif
if !exists(":EclimSettings")
  command -nargs=0 EclimSettings :call eclim#Settings()
endif
if !exists(":PatchEclim")
  command -nargs=+ -complete=customlist,eclim#CommandCompleteScriptRevision
    \ PatchEclim :call eclim#PatchEclim(<f-args>)
endif
if !exists(":EclimDisable")
  command EclimDisable :call eclim#Disable()
endif
if !exists(":EclimEnable")
  command EclimEnable :call eclim#Enable()
endif
" }}}

" Auto Commands{{{

if g:EclimShowCurrentError && has('signs')
  augroup eclim_show_error
    autocmd!
    autocmd CursorHold * call eclim#util#ShowCurrentError()
  augroup END
endif

if g:EclimShowCurrentErrorBalloon && has('balloon_eval')
  set ballooneval
  set balloonexpr=eclim#util#Balloon(eclim#util#GetLineError(line('.')))
endif

if g:EclimMakeLCD
  augroup eclim_make_lcd
    autocmd!
    autocmd QuickFixCmdPre make
      \ if g:EclimMakeLCD | call <SID>QuickFixLocalChangeDirectory() | endif
    autocmd QuickFixCmdPost make
      \ if g:EclimMakeLCD && exists('w:quickfix_dir') |
      \   exec "lcd " . w:quickfix_dir |
      \ endif
  augroup END
endif

if g:EclimMakeQfFilter
  augroup eclim_qf_filter
    autocmd!
    autocmd QuickFixCmdPost make
      \ if exists('b:EclimQuickfixFilter') |
      \   call eclim#util#SetQuickfixList(getqflist(), 'r') |
      \ endif
  augroup END
endif
" }}}

" QuickFixLocalChangeDirectory() {{{
function! s:QuickFixLocalChangeDirectory()
  if g:EclimMakeLCD
    let w:quickfix_dir = getcwd()

    let dir = eclim#project#util#GetCurrentProjectRoot()
    if dir == ''
      let dir = substitute(expand('%:p:h'), '\', '/', 'g')
    endif
    exec 'lcd ' . dir
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
