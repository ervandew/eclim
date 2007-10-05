" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Initially based on vimscript 1506
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

if exists('g:eclim_largefile') || exists('#LargeFile')
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
function! s:InitSettings ()
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
function! s:ApplySettings ()
  set undolevels=-1
  if !exists('b:largefile_notified')
    let b:largefile_notified = 1
    call eclim#util#Echo('Note: Large file settings applied.')"
  endif
endfunction " }}}

" s:RevertSettings() {{{
function! s:RevertSettings ()
  let &undolevels=b:save_undo
  let &eventignore=b:save_events
endfunction " }}}

" vim:ft=vim:fdm=marker
