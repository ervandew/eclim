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

if v:version < 700 | finish | endif

" Global Variables {{{
  if !exists("g:EclimShowCurrentError")
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

if g:EclimMakeLCD
  augroup eclim_make_lcd
    autocmd!
    autocmd QuickFixCmdPre make
      \ let w:quickfix_dir = getcwd() | exec "lcd " . expand('%:p:h')
    autocmd QuickFixCmdPost make
      \ exec "lcd " . w:quickfix_dir
  augroup END
endif
" }}}

" vim:ft=vim:fdm=marker
