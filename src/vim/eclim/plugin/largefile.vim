" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Initially based on vimscript 1506
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

if exists('g:eclim_largefile') || exists('#LargeFile') ||
    \ (exists('g:EclimLargeFileEnabled') && !g:EclimLargeFileEnabled)
  finish
endif
let g:eclim_largefile = 1

" Global Settings {{{
if !exists('g:EclimLargeFileSize')
  let g:EclimLargeFileSize = 5
endif
" }}}

" Script Settings {{{
let s:file_size = g:EclimLargeFileSize * 1024 * 1024
let s:events = [
    \ 'BufRead', 'BufWinEnter', 'BufWinLeave',
    \ 'CursorHold',
    \ 'FileType',
  \ ]
" }}}

" Autocommands {{{
augroup eclim_largefile
  autocmd!
  autocmd BufReadPre * call <SID>InitSettings()
augroup END
" }}}

" s:InitSettings() {{{
function! s:InitSettings()
  let file = expand("<afile>")
  let size = getfsize(file)
  if size >= s:file_size || size == -2
    let b:save_events = &eventignore
    let b:save_undo = &undolevels
    let &eventignore=join(s:events, ',')
    setlocal noswapfile nowrap bufhidden=unload
    autocmd eclim_largefile BufEnter <buffer> call <SID>ApplySettings()
    autocmd eclim_largefile BufLeave <buffer> call <SID>RevertSettings()
  endif
endfunction " }}}

" s:ApplySettings() {{{
function! s:ApplySettings()
  set undolevels=-1
  if !exists('b:largefile_notified')
    let b:largefile_notified = 1
    call eclim#util#Echo('Note: Large file settings applied.')"
  endif
endfunction " }}}

" s:RevertSettings() {{{
function! s:RevertSettings()
  let &undolevels=b:save_undo
  let &eventignore=b:save_events
endfunction " }}}

" vim:ft=vim:fdm=marker
