" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Defines some quickfix specific funtionality.
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

" Autocommands {{{
augroup eclim_qf
  autocmd QuickFixCmdPost *make* call <SID>Show('', 'qf')
  autocmd QuickFixCmdPost grep*,vimgrep* call <SID>Show('i', 'qf')
  autocmd QuickFixCmdPost lgrep*,lvimgrep* call <SID>Show('i', 'loc')

  autocmd BufWinEnter * call eclim#display#signs#Update()
augroup END
" }}}

" Show(type,list) {{{
" Set the type on each entry in the specified list and mark any matches in the
" current file.
function! s:Show(type, list)
  if a:type != ''
    if a:list == 'qf'
      let list = getqflist()
    else
      let list = getloclist(0)
    endif

    let newentries = []
    for entry in list
      let newentry = {
          \ 'filename': bufname(entry.bufnr),
          \ 'lnum': entry.lnum,
          \ 'col': entry.col,
          \ 'text': entry.text,
          \ 'type': a:type
        \ }
      call add(newentries, newentry)
    endfor

    if a:list == 'qf'
      call setqflist(newentries, 'r')
    else
      call setloclist(0, newentries, 'r')
    endif
  endif

  call eclim#display#signs#Update()

  redraw!
endfunction " }}}

" vim:ft=vim:fdm=marker
