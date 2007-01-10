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
    let g:EclimShowCurrentError = 1
  endif

  if !exists("g:EclimMakeLCD")
    let g:EclimMakeLCD = 1
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
" }}}

" Auto Commands{{{
if g:EclimShowCurrentError
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
      \ if g:EclimMakeLCD | exec "lcd " . w:quickfix_dir | endif
  augroup END
endif
" }}}

" QuickFixLocalChangeDirectory() {{{
function! s:QuickFixLocalChangeDirectory ()
  let w:quickfix_dir = getcwd()

  let dir = eclim#project#GetCurrentProjectRoot()
  if dir == ''
    let dir = substitute(expand('%:p:h'), '\', '/', 'g')
  endif
  exec 'lcd ' . dir
endfunction " }}}

" vim:ft=vim:fdm=marker
