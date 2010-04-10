" Author:  Eric Van Dewoestine
"
" Description: {{{
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

" Global Variables {{{
if !exists('g:EclimBuffersSort')
  let g:EclimBuffersSort = 'file'
endif
if !exists('g:EclimBuffersSortDirection')
  let g:EclimBuffersSortDirection = 'asc'
endif
if !exists('g:EclimBuffersDefaultAction')
  let g:EclimBuffersDefaultAction = g:EclimDefaultFileOpenAction
endif
if !exists('g:EclimOnlyExclude')
  let g:EclimOnlyExclude =
    \ '\(ProjectTree_*\|__Tag_List__\|-MiniBufExplorer-\|command-line\)'
endif
" }}}

" Buffers() {{{
" Like, :buffers, but opens a temporary buffer.
function! eclim#common#buffers#Buffers()
  redir => list
  silent exec 'buffers'
  redir END

  let buffers = []
  let filelength = 0
  for entry in split(list, '\n')
    let buffer = {}
    let buffer.status = substitute(entry, '\s*[0-9]\+\s\+\(.\{-}\)\s\+".*', '\1', '')
    let buffer.path = substitute(entry, '.\{-}"\(.\{-}\)".*', '\1', '')
    let buffer.path = fnamemodify(buffer.path, ':p')
    let buffer.file = fnamemodify(buffer.path, ':p:t')
    let buffer.dir = fnamemodify(buffer.path, ':p:h')
    exec 'let buffer.bufnr = ' . substitute(entry, '\s*\([0-9]\+\).*', '\1', '')
    exec 'let buffer.lnum = ' .
      \ substitute(entry, '.*"\s\+line\s\+\([0-9]\+\).*', '\1', '')
    call add(buffers, buffer)

    if len(buffer.file) > filelength
      let filelength = len(buffer.file)
    endif
  endfor

  if g:EclimBuffersSort != ''
    call sort(buffers, 'eclim#common#buffers#BufferCompare')
  endif

  let lines = []
  for buffer in buffers
    call add(lines, s:BufferEntryToLine(buffer, filelength))
  endfor

  call eclim#util#TempWindow('[buffers]', lines)

  setlocal modifiable noreadonly
  call append(line('$'), ['', '" use ? to view help'])
  setlocal nomodifiable readonly

  let b:eclim_buffers = buffers

  " syntax
  set ft=eclim_buffers
  hi link BufferActive Special
  hi link BufferHidden Comment
  syntax match BufferActive /+\?active\s\+\(\[RO\]\)\?/
  syntax match BufferHidden /+\?hidden\s\+\(\[RO\]\)\?/
  syntax match Comment /^".*/

  " mappings
  nnoremap <silent> <buffer> <cr> :call <SID>BufferOpen(g:EclimBuffersDefaultAction)<cr>
  nnoremap <silent> <buffer> E :call <SID>BufferOpen('edit')<cr>
  nnoremap <silent> <buffer> S :call <SID>BufferOpen('split')<cr>
  nnoremap <silent> <buffer> T :call <SID>BufferOpen('tablast \| tabnew')<cr>
  nnoremap <silent> <buffer> D :call <SID>BufferDelete()<cr>

  " assign to buffer var to get around weird vim issue passing list containing
  " a string w/ a '<' in it on execution of mapping.
  let b:buffers_help = [
      \ '<cr> - open buffer with default action',
      \ 'E - open with :edit',
      \ 'S - open in a new split window',
      \ 'T - open in a new tab',
      \ 'D - delete the buffer',
    \ ]
  nnoremap <buffer> <silent> ?
    \ :call eclim#help#BufferHelp(b:buffers_help, 'vertical', 40)<cr>

  "augroup eclim_buffers
  "  autocmd!
  "  autocmd BufAdd,BufWinEnter,BufDelete,BufWinLeave *
  "    \ call eclim#common#buffers#BuffersUpdate()
  "  autocmd BufUnload <buffer> autocmd! eclim_buffers
  "augroup END
endfunction " }}}

" BufferCompare(buffer1, buffer2) {{{
function! eclim#common#buffers#BufferCompare(buffer1, buffer2)
  exec 'let attr1 = a:buffer1.' . g:EclimBuffersSort
  exec 'let attr2 = a:buffer2.' . g:EclimBuffersSort
  let compare = attr1 == attr2 ? 0 : attr1 > attr2 ? 1 : -1
  if g:EclimBuffersSortDirection == 'desc'
    let compare = 0 - compare
  endif
  return compare
endfunction " }}}

" Only() {{{
function! eclim#common#buffers#Only()
  let curwin = winnr()
  let winnum = 1
  while winnum <= winnr('$')
    if winnum != curwin &&
     \ getwinvar(winnum, '&ft') != 'qf' &&
     \ bufname(winbufnr(winnum)) !~ g:EclimOnlyExclude
      if winnum < curwin
        let curwin -= 1
      endif
      exec winnum . 'winc w'
      close
      exec curwin . 'winc w'
      continue
    endif
    let winnum += 1
  endwhile
endfunction " }}}

" s:BufferDelete() {{{
function! s:BufferDelete()
  let line = line('.')
  if line > len(b:eclim_buffers)
    return
  endif

  let index = line - 1
  setlocal modifiable
  setlocal noreadonly
  exec line . ',' . line . 'delete _'
  setlocal nomodifiable
  setlocal readonly
  let buffer = b:eclim_buffers[index]
  call remove(b:eclim_buffers, index)
  exec 'bd ' . buffer.bufnr
endfunction " }}}

" s:BufferEntryToLine(buffer, filelength) {{{
function! s:BufferEntryToLine(buffer, filelength)
  let line = ''
  let line .= a:buffer.status =~ '+' ? '+' : ' '
  let line .= a:buffer.status =~ 'a' ? 'active' : 'hidden'
  let line .= a:buffer.status =~ '[-=]' ? ' [RO] ' : '      '
  let line .= a:buffer.file

  let pad = a:filelength - len(a:buffer.file) + 2
  while pad > 0
    let line .= ' '
    let pad -= 1
  endwhile

  let line .= a:buffer.dir
  return line
endfunction " }}}

" s:BufferOpen(cmd) {{{
function! s:BufferOpen(cmd)
  let line = line('.')
  if line > len(b:eclim_buffers)
    return
  endif

  let file = bufname(b:eclim_buffers[line - 1].bufnr)
  let winnr = b:winnr
  close
  exec winnr . 'winc w'
  call eclim#util#GoToBufferWindowOrOpen(file, a:cmd)
endfunction " }}}

" vim:ft=vim:fdm=marker
