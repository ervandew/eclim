" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Various commands that are useful in and out of eclim.
"
" License:
"
" Copyright (C) 2005 - 2010  Eric Van Dewoestine
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
if !exists('g:MaximizeStatusLine')
  let g:MaximizeStatusLine = '%<%f\ %M\ %h%r%=%-10.(%l,%c%V\ b=%n,w=%{winnr()}%)\ %P'
endif

if exists('g:MaximizeStatusLineEnabled') && g:MaximizeStatusLineEnabled
  exec "set statusline=" . g:MaximizeStatusLine
endif

if !exists("g:EclimTemplatesDisabled")
  " Disabled for now.
  let g:EclimTemplatesDisabled = 1
endif

if !exists('g:EclimArchiveViewerEnabled')
  let g:EclimArchiveViewerEnabled = 1
endif

if g:EclimArchiveViewerEnabled
  " disable tar.vim autocmds... tar.vim is now included w/ vim7
  let g:loaded_tarPlugin = 1
  silent! autocmd! tar

  " disable zipPlugin.vim autocmds... zipPlugin.vim is now included w/ vim7
  let g:loaded_zipPlugin = 1
  silent! autocmd! zip
endif
" }}}

" Auto Commands {{{
if !g:EclimTemplatesDisabled
  augroup eclim_template
    autocmd!
    autocmd BufNewFile * call eclim#common#template#Template()
  augroup END
endif

augroup eclim_archive_read
  autocmd!
  autocmd BufReadCmd
    \ jar:/*,jar:\*,jar:file:/*,jar:file:\*,
    \tar:/*,tar:\*,tar:file:/*,tar:file:\*,
    \tbz2:/*,tgz:\*,tbz2:file:/*,tbz2:file:\*,
    \tgz:/*,tgz:\*,tgz:file:/*,tgz:file:\*,
    \zip:/*,zip:\*,zip:file:/*,zip:file:\*
    \ call eclim#common#archive#ReadFile()
augroup END

if g:EclimArchiveViewerEnabled
  augroup eclim_archive
    autocmd!
    autocmd BufReadCmd *.egg     call eclim#common#archive#List()
    autocmd BufReadCmd *.jar     call eclim#common#archive#List()
    autocmd BufReadCmd *.war     call eclim#common#archive#List()
    autocmd BufReadCmd *.ear     call eclim#common#archive#List()
    autocmd BufReadCmd *.zip     call eclim#common#archive#List()
    autocmd BufReadCmd *.tar     call eclim#common#archive#List()
    autocmd BufReadCmd *.tgz     call eclim#common#archive#List()
    autocmd BufReadCmd *.tar.gz  call eclim#common#archive#List()
    autocmd BufReadCmd *.tar.bz2 call eclim#common#archive#List()
  augroup END
endif
" }}}

" Command Declarations {{{
if !exists(":Split")
  command -nargs=+ -complete=file
    \ Split :call eclim#common#util#OpenFiles('split', '<args>')
endif
if !exists(":Tabnew")
  command -nargs=+ -complete=file
    \ Tabnew :call eclim#common#util#OpenFiles('tablast | tabnew', '<args>')
endif

if !exists(":EditRelative")
  command -nargs=1 -complete=customlist,eclim#common#util#CommandCompleteRelative
    \ EditRelative :call eclim#common#util#OpenRelative('edit', '<args>', 1)
endif
if !exists(":SplitRelative")
  command -nargs=+ -complete=customlist,eclim#common#util#CommandCompleteRelative
    \ SplitRelative :call eclim#common#util#OpenRelative('split', '<args>', 1)
endif
if !exists(":TabnewRelative")
  command -nargs=+ -complete=customlist,eclim#common#util#CommandCompleteRelative
    \ TabnewRelative :call eclim#common#util#OpenRelative('tablast | tabnew', '<args>')
endif
if !exists(":ReadRelative")
  command -nargs=1 -complete=customlist,eclim#common#util#CommandCompleteRelative
    \ ReadRelative :call eclim#common#util#OpenRelative('read', '<args>')
endif
if !exists(":ArgsRelative")
  command -nargs=+ -complete=customlist,eclim#common#util#CommandCompleteRelative
    \ ArgsRelative :call eclim#common#util#OpenRelative('args', '<args>')
endif
if !exists(":ArgAddRelative")
  command -nargs=+ -complete=customlist,eclim#common#util#CommandCompleteRelative
    \ ArgAddRelative :call eclim#common#util#OpenRelative('argadd', '<args>')
endif

if !exists(":Buffers")
  command Buffers :call eclim#common#buffers#Buffers()
  command BuffersToggle :call eclim#common#buffers#BuffersToggle()
endif

if !exists(":Only")
  command Only :call eclim#common#buffers#Only()
endif

if !exists(":DiffLastSaved")
  command DiffLastSaved :call eclim#common#util#DiffLastSaved()
endif

if !exists(":OtherWorkingCopyDiff")
  command -nargs=1
    \ -complete=customlist,eclim#project#util#CommandCompleteProjectContainsThis
    \ OtherWorkingCopyDiff :call eclim#common#util#OtherWorkingCopyDiff('<args>')
endif
if !exists(":OtherWorkingCopyEdit")
  command -nargs=1
    \ -complete=customlist,eclim#project#util#CommandCompleteProjectContainsThis
    \ OtherWorkingCopyEdit
    \ :call eclim#common#util#OtherWorkingCopy('<args>', 'edit')
endif
if !exists(":OtherWorkingCopySplit")
  command -nargs=1
    \ -complete=customlist,eclim#project#util#CommandCompleteProjectContainsThis
    \ OtherWorkingCopySplit
    \ :call eclim#common#util#OtherWorkingCopy('<args>', 'split')
endif
if !exists(":OtherWorkingCopyTabopen")
  command -nargs=1
    \ -complete=customlist,eclim#project#util#CommandCompleteProjectContainsThis
    \ OtherWorkingCopyTabopen
    \ :call eclim#common#util#OtherWorkingCopy('<args>', 'tablast | tabnew')
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

if !exists(":VimgrepRelative")
  command -nargs=+ -complete=customlist,eclim#common#util#CommandCompleteRelative
    \ VimgrepRelative :call eclim#common#util#GrepRelative('vimgrep', <q-args>)
endif
if !exists(":VimgrepAddRelative")
  command -nargs=+ -complete=customlist,eclim#common#util#CommandCompleteRelative
    \ VimgrepAddRelative :call eclim#common#util#GrepRelative('vimgrepadd', <q-args>)
endif
if !exists(":LvimgrepRelative")
  command -nargs=+ -complete=customlist,eclim#common#util#CommandCompleteRelative
    \ LvimgrepRelative :call eclim#common#util#GrepRelative('lvimgrep', <q-args>)
endif
if !exists(":LvimgrepAddRelative")
  command -nargs=+ -complete=customlist,eclim#common#util#CommandCompleteRelative
    \ LvimgrepAddRelative :call eclim#common#util#GrepRelative('lvimgrepadd', <q-args>)
endif

if !exists(":CdRelative")
  command -nargs=1 -complete=customlist,eclim#common#util#CommandCompleteRelativeDirs
    \ CdRelative :exec 'cd ' . expand('%:p:h') . '/<args>'
endif

if !exists(":LcdRelative")
  command -nargs=1 -complete=customlist,eclim#common#util#CommandCompleteRelativeDirs
    \ LcdRelative :exec 'lcd ' . expand('%:p:h') . '/<args>'
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

if !exists(":MaximizeWindow")
  command MaximizeWindow :call eclim#display#maximize#MaximizeWindow()
endif
if !exists(":MinimizeWindow")
  command -nargs=* MinimizeWindow :call eclim#display#maximize#MinimizeWindow(<f-args>)
endif
if !exists(":MinimizeRestore")
  command MinimizeRestore
      \ :call eclim#display#maximize#ResetMinimized() |
      \ call eclim#display#maximize#RestoreWindows(0)
endif

if !exists(":OpenUrl")
  command -bang -range -nargs=? OpenUrl
    \ :call eclim#web#OpenUrl('<args>', '<bang>', <line1>, <line2>)
endif
" }}}

" vim:ft=vim:fdm=marker
