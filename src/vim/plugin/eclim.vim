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
" Copyright (c) 2004 - 2005
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

if v:version < 700 | finish | endif

" Global Variables {{{
  if !exists("g:EclimShowCurrentError")
    let g:EclimShowCurrentError = 1
  endif

  if !exists("g:EclimLogLevel")
    let g:EclimLogLevel = 5
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
  if !exists("g:EclimCommand")
    let g:EclimCommand = 'eclim'
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

  let g:EclimQuickfixAvailable = 1
" }}}

" Command Declarations {{{
if !exists(":PingEclim")
  command PingEclim :call eclim#PingEclim(1)
endif
if !exists(":ShutdownEclim")
  command ShutdownEclim :call eclim#ShutdownEclim()
endif
if !exists(":Settings")
  command -nargs=0 Settings :call eclim#Settings()
endif
" }}}

" Auto Commands{{{
if g:EclimShowCurrentError
  augroup eclim_show_error
    autocmd!
    autocmd CursorHold * call eclim#util#ShowCurrentError()
  augroup END
endif

augroup eclim_quickfix_cmd
  autocmd!
  autocmd QuickFixCmdPost * call eclim#util#SetQuickfixAvailability()
  "autocmd QuickFixCmdPost make
  "  \ if exists("b:eclim_errors") |
  "  \   unlet b:eclim_errors |
  "  \   call eclim#util#ErrorsDisplayClear() |
  "  \ endif
augroup END
" }}}

" vim:ft=vim:fdm=marker
