" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Defines some quickfix specific funtionality.
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

" Autocommands {{{
augroup eclim_qf
  autocmd QuickFixCmdPost *make* call <SID>Show('', 'qf')
  autocmd QuickFixCmdPost grep*,vimgrep* call <SID>Show('i', 'qf')
  autocmd QuickFixCmdPost lgrep*,lvimgrep* call <SID>Show('i', 'loc')

  autocmd BufWinEnter * call eclim#signs#Update()
augroup END
" }}}

" Show(type,list) {{{
" Set the type on each entry in the specified list and mark any matches in the
" current file.
function! s:Show (type, list)
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

  call eclim#signs#Update()

  redraw!
endfunction " }}}

" vim:ft=vim:fdm=marker
