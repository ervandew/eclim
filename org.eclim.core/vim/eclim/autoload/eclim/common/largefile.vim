" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Initially based on vimscript 1506
"
" License:
"
" Copyright (C) 2005 - 2012  Eric Van Dewoestine
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

" Script Settings {{{
let s:file_size = g:EclimLargeFileSize * 1024 * 1024
let s:events = ['BufRead', 'CursorHold', 'FileType']
" }}}

function! eclim#common#largefile#InitSettings() " {{{
  let file = expand("<afile>")
  let size = getfsize(file)
  if size >= s:file_size || size == -2
    if !exists('b:save_events')
      let b:save_events = &eventignore
      call s:ApplySettings()
      setlocal noswapfile nowrap bufhidden=unload
      autocmd eclim_largefile BufEnter,BufWinEnter <buffer> call <SID>ApplySettings()
      autocmd eclim_largefile BufLeave,BufWinLeave <buffer> call <SID>RevertSettings()
    endif
  endif
endfunction " }}}

function! s:ApplySettings() " {{{
  let &eventignore=join(s:events, ',')
  if !exists('b:largefile_notified')
    let b:largefile_notified = 1
    call eclim#util#Echo('Note: Large file settings applied.')
  endif
endfunction " }}}

function! s:RevertSettings() " {{{
  if exists('b:save_events')
    let &eventignore=b:save_events
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
