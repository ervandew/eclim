" Author:  Eric Van Dewoestine
"
" Description: {{{
"
" License:
"
" Copyright (C) 2005 - 2014  Eric Van Dewoestine
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
if !exists('g:EclimBuffersDeleteOnTabClose')
  let g:EclimBuffersDeleteOnTabClose = 0
endif
if !exists('g:EclimOnlyExclude')
  let g:EclimOnlyExclude = '^NONE$'
endif
if !exists('g:EclimOnlyExcludeFixed')
  let g:EclimOnlyExcludeFixed = 1
endif
" }}}

" ScriptVariables {{{
  let s:eclim_tab_id = 0
" }}}

function! eclim#common#buffers#Buffers(bang) " {{{
  " Like, :buffers, but opens a temporary buffer.

  let options = {'maxfilelength': 0}
  let buffers = eclim#common#buffers#GetBuffers(options)

  if g:EclimBuffersSort != ''
    call sort(buffers, 'eclim#common#buffers#BufferCompare')
  endif

  let lines = []
  let buflist = []
  let filelength = options['maxfilelength']
  let tabid = exists('*gettabvar') ? s:GetTabId() : 0
  let tabbuffers = tabpagebuflist()
  for buffer in buffers
    let eclim_tab_id = getbufvar(buffer.bufnr, 'eclim_tab_id')
    if a:bang != '' || eclim_tab_id == '' || eclim_tab_id == tabid
      " for buffers w/ out a tab id, don't show them in the list if they
      " are active, but aren't open on the current tab.
      if a:bang == '' && buffer.status =~ 'a' && index(tabbuffers, buffer.bufnr) == -1
        continue
      endif

      call add(lines, s:BufferEntryToLine(buffer, filelength))
      call add(buflist, buffer)
    endif
  endfor

  call eclim#util#TempWindow('[buffers]', lines)

  setlocal modifiable noreadonly
  call append(line('$'), ['', '" use ? to view help'])
  setlocal nomodifiable readonly

  let b:eclim_buffers = buflist

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
  nnoremap <silent> <buffer> V :call <SID>BufferOpen('vsplit')<cr>
  nnoremap <silent> <buffer> T :call <SID>BufferOpen('tablast \| tabnew')<cr>
  nnoremap <silent> <buffer> D :call <SID>BufferDelete()<cr>
  nnoremap <silent> <buffer> R :Buffers<cr>

  " assign to buffer var to get around weird vim issue passing list containing
  " a string w/ a '<' in it on execution of mapping.
  let b:buffers_help = [
      \ '<cr> - open buffer with default action',
      \ 'E - open with :edit',
      \ 'S - open in a new split window',
      \ 'V - open in a new vertically split window',
      \ 'T - open in a new tab',
      \ 'D - delete the buffer',
      \ 'R - refresh the buffer list',
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

function! eclim#common#buffers#BuffersToggle(bang) " {{{
  let name = eclim#util#EscapeBufferName('[buffers]')
  if bufwinnr(name) == -1
    call eclim#common#buffers#Buffers(a:bang)
  else
    exec "bdelete " . bufnr(name)
  endif
endfunction " }}}

function! eclim#common#buffers#BufferCompare(buffer1, buffer2) " {{{
  exec 'let attr1 = a:buffer1.' . g:EclimBuffersSort
  exec 'let attr2 = a:buffer2.' . g:EclimBuffersSort
  let compare = attr1 == attr2 ? 0 : attr1 > attr2 ? 1 : -1
  if g:EclimBuffersSortDirection == 'desc'
    let compare = 0 - compare
  endif
  return compare
endfunction " }}}

function! eclim#common#buffers#Only() " {{{
  let curwin = winnr()
  let winnum = 1
  while winnum <= winnr('$')
    let fixed = g:EclimOnlyExcludeFixed && (
      \ getwinvar(winnum, '&winfixheight') == 1 ||
      \ getwinvar(winnum, '&winfixwidth') == 1)
    let excluded = bufname(winbufnr(winnum)) =~ g:EclimOnlyExclude
    if winnum != curwin && !fixed && !excluded
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

function! eclim#common#buffers#GetBuffers(...) " {{{
  let options = a:0 ? a:1 : {}

  redir => list
  silent buffers
  redir END

  let buffers = []
  let maxfilelength = 0
  for entry in split(list, '\n')
    let buffer = {}
    let buffer.status = substitute(entry, '\s*[0-9]\+\s\+\(.\{-}\)\s\+".*', '\1', '')
    let buffer.path = substitute(entry, '.\{-}"\(.\{-}\)".*', '\1', '')
    let buffer.path = fnamemodify(buffer.path, ':p')
    let buffer.file = fnamemodify(buffer.path, ':p:t')
    let buffer.dir = fnamemodify(buffer.path, ':p:h')
    let buffer.bufnr = str2nr(substitute(entry, '\s*\([0-9]\+\).*', '\1', ''))
    let buffer.lnum = str2nr(substitute(entry, '.*"\s\+\w\+\s\+\(\d\+\)', '\1', ''))
    call add(buffers, buffer)

    if len(buffer.file) > maxfilelength
      let maxfilelength = len(buffer.file)
    endif
  endfor

  if has_key(options, 'maxfilelength')
    let options['maxfilelength'] = maxfilelength
  endif

  return buffers
endfunction " }}}

function! eclim#common#buffers#TabInit() " {{{
  let tabnr = 1
  while tabnr <= tabpagenr('$')
    let tab_id = gettabvar(tabnr, 'eclim_tab_id')
    if tab_id == ''
      let s:eclim_tab_id += 1
      call settabvar(tabnr, 'eclim_tab_id', s:eclim_tab_id)
      for bufnr in tabpagebuflist(tabnr)
        let btab_id = getbufvar(bufnr, 'eclim_tab_id')
        if btab_id == ''
          call setbufvar(bufnr, 'eclim_tab_id', s:eclim_tab_id)
        endif
      endfor
    endif
    let tabnr += 1
  endwhile
endfunction " }}}

function! eclim#common#buffers#TabEnter() " {{{
  if !s:GetTabId()
    call s:SetTabId()
  endif

  if g:EclimBuffersDeleteOnTabClose
    if exists('s:tab_count') && s:tab_count > tabpagenr('$')
      " delete any buffers associated with the closed tab
      let buffers = eclim#common#buffers#GetBuffers()
      for buffer in buffers
        let eclim_tab_id = getbufvar(buffer.bufnr, 'eclim_tab_id')
        " don't delete active buffers, just in case the tab has the wrong
        " eclim_tab_id
        if eclim_tab_id == s:tab_prev && buffer.status !~ 'a'
          try
            exec 'bdelete ' . buffer.bufnr
          catch /E89/
            " ignore since it happens when using bd! on the last buffer for
            " another tab.
          endtry
        endif
      endfor
    endif
  endif
endfunction " }}}

function! eclim#common#buffers#TabLeave() " {{{
  let s:tab_prev = s:GetTabId()
  let s:tab_count = tabpagenr('$')
endfunction " }}}

function! eclim#common#buffers#TabLastOpenIn() " {{{
  if !buflisted('%')
    silent! unlet b:eclim_tab_id
  endif

  if !s:GetTabId()
    call s:SetTabId()
  endif

  let tabnr = 1
  let other_tab = 0
  let bufnr = bufnr('%')
  while tabnr <= tabpagenr('$')
    if tabnr != tabpagenr() &&
     \ eclim#util#ListContains(tabpagebuflist(tabnr), bufnr)
      let other_tab = tabnr
      break
    endif
    let tabnr += 1
  endwhile

  if !exists('b:eclim_tab_id') || !other_tab
    let b:eclim_tab_id = s:GetTabId()
  endif
endfunction " }}}

function! eclim#common#buffers#OpenNextHiddenTabBuffer(current) " {{{
  let allbuffers = eclim#common#buffers#GetBuffers()

  " build list of buffers open in other tabs to exclude
  let tabbuffers = []
  let lasttab = tabpagenr('$')
  let index = 1
  while index <= lasttab
    if index != tabpagenr()
      for bnum in tabpagebuflist(index)
        call add(tabbuffers, bnum)
      endfor
    endif
    let index += 1
  endwhile

  " build list of buffers not open in any window, and last seen on the
  " current tab.
  let hiddenbuffers = []
  for buffer in allbuffers
    let bnum = buffer.bufnr
    if bnum != a:current && index(tabbuffers, bnum) == -1 && bufwinnr(bnum) == -1
      let eclim_tab_id = getbufvar(bnum, 'eclim_tab_id')
      if eclim_tab_id != '' && eclim_tab_id != t:eclim_tab_id
        continue
      endif

      if bnum < a:current
        call insert(hiddenbuffers, bnum)
      else
        call add(hiddenbuffers, bnum)
      endif
    endif
  endfor

  " we found a hidden buffer, so open it
  if len(hiddenbuffers) > 0
    exec 'buffer ' . hiddenbuffers[0]
    doautocmd BufEnter
    doautocmd BufWinEnter
    doautocmd BufReadPost
    return hiddenbuffers[0]
  endif
  return 0
endfunction " }}}

function! s:BufferDelete() " {{{
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

  let winnr = winnr()
  " make sure the autocmds are executed in the following order
  noautocmd exec 'bd ' . buffer.bufnr
  doautocmd BufDelete
  doautocmd BufEnter
  exec winnr . 'winc w'
endfunction " }}}

function! s:BufferEntryToLine(buffer, filelength) " {{{
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

function! s:BufferOpen(cmd) " {{{
  let line = line('.')
  if line > len(b:eclim_buffers)
    return
  endif

  let file = bufname(b:eclim_buffers[line - 1].bufnr)
  let winnr = b:winnr
  close

  " prevent opening the buffer in a split of a vertical tool window (project
  " tree, taglist, etc.)
  if exists('g:VerticalToolBuffers') && has_key(g:VerticalToolBuffers, winbufnr(winnr))
    let winnr = 1
    while has_key(g:VerticalToolBuffers, winbufnr(winnr))
      let winnr += 1
      if winnr > winnr('$')
        let winnr -= 1
        break
      endif
    endwhile
  endif

  exec winnr . 'winc w'
  call eclim#util#GoToBufferWindowOrOpen(file, a:cmd)
endfunction " }}}

function! s:GetTabId(...) " {{{
  let tabnr = a:0 ? a:1 : tabpagenr()
  " using gettabvar over t:eclim_tab_id because while autocmds are executing,
  " the tabpagenr() may return the correct tab number, but accessing
  " t:eclim_tab_id may return the value from the previously focused tab.
  return gettabvar(tabnr, 'eclim_tab_id')
endfunction " }}}

function! s:SetTabId(...) " {{{
  let tabnr = a:0 ? a:1 : tabpagenr()
  let s:eclim_tab_id += 1
  " using settabvar for reason explained in s:GetTabId()
  call settabvar(tabnr, 'eclim_tab_id', s:eclim_tab_id)
endfunction " }}}

" vim:ft=vim:fdm=marker
