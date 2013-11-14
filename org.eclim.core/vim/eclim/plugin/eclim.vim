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
" Copyright (C) 2005 - 2013  Eric Van Dewoestine
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
let g:NUMBER_TYPE = 0
let g:STRING_TYPE = 1
let g:FUNCREF_TYPE = 2
let g:LIST_TYPE = 3
let g:DICT_TYPE = 4
let g:FLOAT_TYPE = 5

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

if !exists("g:EclimBuffersTabTracking")
  let g:EclimBuffersTabTracking = 1
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

if !exists("g:EclimCompletionMethod")
  let g:EclimCompletionMethod = 'completefunc'
endif

if !exists("g:EclimLocationListHeight")
  let g:EclimLocationListHeight = 10
endif

if !exists("g:EclimMakeLCD")
  let g:EclimMakeLCD = 1
endif

if !exists("g:EclimMakeQfFilter")
  let g:EclimMakeQfFilter = 1
endif

if !exists("g:EclimMenus")
  let g:EclimMenus = 1
endif

if !exists("g:EclimTemplatesDisabled")
  " Disabled for now.
  let g:EclimTemplatesDisabled = 1
endif

if !exists('g:EclimLargeFileEnabled')
  let g:EclimLargeFileEnabled = 0
endif
if !exists('g:EclimLargeFileSize')
  let g:EclimLargeFileSize = 5
endif
" }}}

" Command Declarations {{{
if !exists(":PingEclim")
  command -nargs=? -complete=customlist,eclim#client#nailgun#CommandCompleteWorkspaces
    \ PingEclim :call eclim#PingEclim(1, '<args>')
endif
if !exists(":ShutdownEclim")
  command ShutdownEclim :call eclim#ShutdownEclim()
endif
if !exists(":EclimSettings")
  command -nargs=? -complete=customlist,eclim#client#nailgun#CommandCompleteWorkspaces
    \ EclimSettings :call eclim#Settings('<args>')
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

if !exists(":RefactorUndo")
  command RefactorUndo :call eclim#lang#UndoRedo('undo', 0)
  command RefactorRedo :call eclim#lang#UndoRedo('redo', 0)
  command RefactorUndoPeek :call eclim#lang#UndoRedo('undo', 1)
  command RefactorRedoPeek :call eclim#lang#UndoRedo('redo', 1)
endif

if !exists(":Buffers")
  command -bang Buffers :call eclim#common#buffers#Buffers('<bang>')
  command -bang BuffersToggle :call eclim#common#buffers#BuffersToggle('<bang>')
endif

if !exists(":Only")
  command Only :call eclim#common#buffers#Only()
endif

if !exists(":DiffLastSaved")
  command DiffLastSaved :call eclim#common#util#DiffLastSaved()
endif

if !exists(":SwapWords")
  command SwapWords :call eclim#common#util#SwapWords()
endif
if !exists(":SwapTypedArguments")
  command SwapTypedArguments :call eclim#common#util#SwapTypedArguments()
endif
if !exists(":LocateFile")
  command -nargs=? LocateFile :call eclim#common#locate#LocateFile('', '<args>')
  command -nargs=? LocateBuffer
    \ :call eclim#common#locate#LocateFile('', '<args>', 'buffers')
endif

if !exists(":QuickFixClear")
  command QuickFixClear :call setqflist([]) | call eclim#display#signs#Update()
endif
if !exists(":LocationListClear")
  command LocationListClear :call setloclist(0, []) | call eclim#display#signs#Update()
endif

if !exists(":Tcd")
  command -nargs=1 -complete=dir Tcd :call eclim#common#util#Tcd('<args>')
endif

if !exists(":History")
  command History call eclim#common#history#History()
  command -bang HistoryClear call eclim#common#history#HistoryClear('<bang>')
endif

if has('signs')
  if !exists(":Sign")
    command Sign :call eclim#display#signs#Toggle('user', line('.'))
  endif
  if !exists(":Signs")
    command Signs :call eclim#display#signs#ViewSigns('user')
  endif
  if !exists(":SignClearUser")
    command SignClearUser :call eclim#display#signs#UnplaceAll(
      \ eclim#display#signs#GetExisting('user'))
  endif
  if !exists(":SignClearAll")
    command SignClearAll :call eclim#display#signs#UnplaceAll(
      \ eclim#display#signs#GetExisting())
  endif
endif

if !exists(":OpenUrl")
  command -bang -range -nargs=? OpenUrl
    \ :call eclim#web#OpenUrl('<args>', '<bang>', <line1>, <line2>)
endif

if !exists(":Make")
  command -bang -nargs=* Make :call eclim#util#Make('<bang>', '<args>')
endif
" }}}

" Auto Commands{{{
augroup eclim_archive_read
  autocmd!
  if exists('#archive_read')
    autocmd! archive_read
  endif
  autocmd BufReadCmd
    \ jar:/*,jar:\*,jar:file:/*,jar:file:\*,
    \tar:/*,tar:\*,tar:file:/*,tar:file:\*,
    \tbz2:/*,tgz:\*,tbz2:file:/*,tbz2:file:\*,
    \tgz:/*,tgz:\*,tgz:file:/*,tgz:file:\*,
    \zip:/*,zip:\*,zip:file:/*,zip:file:\*
    \ call eclim#common#util#ReadFile()
augroup END

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
    autocmd WinEnter,BufWinEnter * call eclim#display#signs#Update()
    if has('gui_running')
      " delayed to keep the :make output on the screen for gvim
      autocmd QuickFixCmdPost * call eclim#util#DelayedCommand(
        \ 'call eclim#display#signs#QuickFixCmdPost()')
    else
      autocmd QuickFixCmdPost * call eclim#display#signs#QuickFixCmdPost()
    endif
  augroup END
endif

if g:EclimBuffersTabTracking && exists('*gettabvar')
  call eclim#common#buffers#TabInit()
  augroup eclim_buffer_tab_tracking
    autocmd!
    autocmd BufWinEnter,BufWinLeave * call eclim#common#buffers#TabLastOpenIn()
    autocmd TabEnter * call eclim#common#buffers#TabEnter()
    autocmd TabLeave * call eclim#common#buffers#TabLeave()
  augroup END
endif

if has('gui_running') && g:EclimMenus
  augroup eclim_menus
    autocmd BufNewFile,BufReadPost,WinEnter * call eclim#display#menu#Generate()
    autocmd VimEnter * if expand('<amatch>')=='' | call eclim#display#menu#Generate() | endif
  augroup END
endif

if !g:EclimTemplatesDisabled
  augroup eclim_template
    autocmd!
    autocmd BufNewFile * call eclim#common#template#Template()
  augroup END
endif

if !exists('#LargeFile') && g:EclimLargeFileEnabled
  augroup eclim_largefile
    autocmd!
    autocmd BufReadPre * call eclim#common#largefile#InitSettings()
  augroup END
endif
" }}}

" vim:ft=vim:fdm=marker
