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
" Copyright (C) 2005 - 2011  Eric Van Dewoestine
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

if has("signs")
  if !exists("g:EclimSignLevel")
    let g:EclimSignLevel = 5
  endif
else
  let g:EclimSignLevel = 0
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

if !exists("g:EclimShowCurrentError")
  let g:EclimShowCurrentError = 1
endif

if !exists("g:EclimShowCurrentErrorBalloon")
  let g:EclimShowCurrentErrorBalloon = 1
endif

if !exists("g:EclimValidateSortResults")
  let g:EclimValidateSortResults = 'occurrence'
endif

if !exists("g:EclimDefaultFileOpenAction")
  let g:EclimDefaultFileOpenAction = 'split'
endif

if !exists("g:EclimMakeLCD")
  let g:EclimMakeLCD = 1
endif

if !exists("g:EclimMakeQfFilter")
  let g:EclimMakeQfFilter = 1
endif

if !exists("g:EclimHome")
  " set at build/install time.
  "${vim.eclim.home}"
  if has('win32unix')
    let g:EclimHome = eclim#cygwin#CygwinPath(g:EclimHome)
  endif
endif
if !exists("g:EclimEclipseHome")
  " set at build/install time.
  "${vim.eclipse.home}"
  if has('win32unix')
    let g:EclimEclipseHome = eclim#cygwin#CygwinPath(g:EclimEclipseHome)
  endif
endif

if !exists("g:EclimMenus")
  let g:EclimMenus = 1
endif
" }}}

" Command Declarations {{{
if !exists(":PingEclim")
  command -nargs=? -complete=customlist,eclim#eclipse#CommandCompleteWorkspaces
    \ PingEclim :call eclim#PingEclim(1, '<args>')
endif
if !exists(":ShutdownEclim")
  command ShutdownEclim :call eclim#ShutdownEclim()
endif
if !exists(":EclimSettings")
  command -nargs=? -complete=customlist,eclim#eclipse#CommandCompleteWorkspaces
    \ EclimSettings :call eclim#Settings('<args>')
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
if !exists(':EclimHelp')
  command -nargs=? -complete=customlist,eclim#help#CommandCompleteTag
    \ EclimHelp :call eclim#help#Help('<args>', 0)
endif
if !exists(':EclimHelpGrep')
  command -nargs=+ EclimHelpGrep :call eclim#help#HelpGrep(<q-args>)
endif
" }}}

" Auto Commands{{{

if g:EclimShowCurrentError
  " forcing load of util, otherwise a bug in vim is sometimes triggered when
  " searching for a pattern where the pattern is echoed twice.  Reproducable
  " by opening a new vim and searching for 't' (/t<cr>).
  runtime eclim/autoload/eclim/util.vim

  augroup eclim_show_error
    autocmd!
    autocmd CursorMoved * call eclim#util#ShowCurrentError()
  augroup END
endif

if g:EclimShowCurrentErrorBalloon && has('balloon_eval')
  set ballooneval
  set balloonexpr=eclim#util#Balloon(eclim#util#GetLineError(line('.')))
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

if g:EclimSignLevel
  augroup eclim_qf
    autocmd QuickFixCmdPost *make* call eclim#display#signs#Show('', 'qf', 1)
    autocmd QuickFixCmdPost grep*,vimgrep* call eclim#display#signs#Show('i', 'qf', 1)
    autocmd QuickFixCmdPost lgrep*,lvimgrep* call eclim#display#signs#Show('i', 'loc', 1)
    autocmd WinEnter,BufWinEnter * call eclim#display#signs#Update()
  augroup END
endif

if has('netbeans_intg')
  augroup eclim_vimplugin
    " autocommands used to work around the fact that the "unmodified" event in
    " vim's netbean support is commentted out for some reason.
    autocmd BufWritePost * call eclim#vimplugin#BufferWritten()
    autocmd CursorHold,CursorHoldI * call eclim#vimplugin#BufferModified()
    autocmd BufWinLeave * call eclim#vimplugin#BufferClosed()
    autocmd BufEnter * call eclim#vimplugin#BufferEnter()
  augroup END
endif

if has('gui_running') && g:EclimMenus
  augroup eclim_menus
    autocmd BufNewFile,BufReadPost,WinEnter * call eclim#display#menu#Generate()
    autocmd VimEnter * if expand('<amatch>')=='' | call eclim#display#menu#Generate() | endif
  augroup END
endif
" }}}

" vim:ft=vim:fdm=marker
