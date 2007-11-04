" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
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
if !exists('g:EclimBuffersSort')
  let g:EclimBuffersSort = 'path'
endif
if !exists('g:EclimBuffersSortDirection')
  let g:EclimBuffersSortDirection = 'asc'
endif
if !exists('g:EclimBuffersDefaultAction')
  let g:EclimBuffersDefaultAction = 'split'
endif
" }}}

" Buffers() {{{
" Like, :buffers, but opens a temporary buffer.
function! eclim#common#buffers#Buffers ()
  redir => list
  silent exec 'buffers'
  redir END

  let buffers = []
  let current = ''
  for entry in split(list, '\n')
    let buffer = {}
    let buffer.status = substitute(entry, '\s*[0-9]\+\s\+\(.\{-}\)\s\+".*', '\1', '')
    let buffer.path = substitute(entry, '.\{-}"\(.\{-}\)".*', '\1', '')
    exec 'let buffer.bufnr = ' . substitute(entry, '\s*\([0-9]\+\).*', '\1', '')
    exec 'let buffer.lnum = ' . substitute(entry, '.*"\s\+line\s\+\([0-9]\+\).*', '\1', '')

    if buffer.status =~ '%'
      let current = buffer.path
    endif

    call add(buffers, buffer)
  endfor

  if g:EclimBuffersSort != ''
    call sort(buffers, 'eclim#common#buffers#BufferCompare')
  endif

  let lines = []
  for buffer in buffers
    call add(lines, s:BufferEntryToLine(buffer))
  endfor

  call eclim#util#TempWindow('[buffers]', lines)
  let b:eclim_buffers = buffers

  " syntax
  set ft=eclim_buffers
  hi link BufferActive Special
  hi link BufferHidden Comment
  syntax match BufferActive /+\?active\s\+\(\[RO\]\)\?/
  syntax match BufferHidden /+\?hidden\s\+\(\[RO\]\)\?/

  " mappings
  nnoremap <silent> <buffer> <cr> :call <SID>BufferOpen(g:EclimBuffersDefaultAction)<cr>
  nnoremap <silent> <buffer> E :call <SID>BufferOpen('edit')<cr>
  nnoremap <silent> <buffer> S :call <SID>BufferOpen('split')<cr>
  nnoremap <silent> <buffer> T :call <SID>BufferOpen('tabnew')<cr>
  nnoremap <silent> <buffer> D :call <SID>BufferDelete()<cr>

  "augroup eclim_buffers
  "  autocmd!
  "  autocmd BufAdd,BufWinEnter,BufDelete,BufWinLeave *
  "    \ call eclim#common#buffers#BuffersUpdate()
  "  autocmd BufUnload <buffer> autocmd! eclim_buffers
  "augroup END
endfunction " }}}

" BufferCompare(buffer1, buffer2) {{{
function! eclim#common#buffers#BufferCompare (buffer1, buffer2)
  exec 'let attr1 = a:buffer1.' . g:EclimBuffersSort
  exec 'let attr2 = a:buffer2.' . g:EclimBuffersSort
  let compare = attr1 == attr2 ? 0 : attr1 > attr2 ? 1 : -1
  if g:EclimBuffersSortDirection == 'desc'
    let compare = 0 - compare
  endif
  return compare
endfunction " }}}

" s:BufferDelete() {{{
function! s:BufferDelete ()
  let line = line('.')
  let index = line - 1
  exec 'bd ' . b:eclim_buffers[index].bufnr
  let save = @"
  setlocal modifiable
  setlocal noreadonly
  exec line . ',' . line . 'delete'
  setlocal nomodifiable
  setlocal readonly
  let @" = save
  call remove(b:eclim_buffers, index)
endfunction " }}}

" s:BufferEntryToLine(buffer) {{{
function! s:BufferEntryToLine (buffer)
  let line = ''
  let line .= a:buffer.status =~ '+' ? '+' : ' '
  let line .= a:buffer.status =~ 'a' ? 'active' : 'hidden'
  let line .= a:buffer.status =~ '[-=]' ? ' [RO] ' : '      '
  let line .= a:buffer.path
  return line
endfunction " }}}

" s:BufferOpen(cmd) {{{
function! s:BufferOpen (cmd)
  let file = bufname(b:eclim_buffers[line('.') - 1].bufnr)
  let winnr = b:winnr
  close
  exec winnr . 'winc w'
  call eclim#util#GoToBufferWindowOrOpen(file, a:cmd)
endfunction " }}}

" vim:ft=vim:fdm=marker
