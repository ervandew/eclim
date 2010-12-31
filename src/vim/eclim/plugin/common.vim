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
if !exists("g:EclimTemplatesDisabled")
  " Disabled for now.
  let g:EclimTemplatesDisabled = 1
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
  silent! autocmd! archive_read
  autocmd BufReadCmd
    \ jar:/*,jar:\*,jar:file:/*,jar:file:\*,
    \tar:/*,tar:\*,tar:file:/*,tar:file:\*,
    \tbz2:/*,tgz:\*,tbz2:file:/*,tbz2:file:\*,
    \tgz:/*,tgz:\*,tgz:file:/*,tgz:file:\*,
    \zip:/*,zip:\*,zip:file:/*,zip:file:\*
    \ call eclim#common#util#ReadFile()
augroup END
" }}}

" Command Declarations {{{
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
" }}}

" vim:ft=vim:fdm=marker
