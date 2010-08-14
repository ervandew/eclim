" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Utility functions for working with vim windows.
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

" GlobalVariables {{{
let g:VerticalToolBuffers = {}

if !exists('g:VerticalToolWindowSide')
  let g:VerticalToolWindowSide = 'left'
endif

if g:VerticalToolWindowSide == 'right'
  let g:VerticalToolWindowPosition = 'botright vertical'
else
  let g:VerticalToolWindowPosition = 'topleft vertical'
endif

if !exists('g:VerticalToolWindowWidth')
  let g:VerticalToolWindowWidth = 30
endif
" }}}

" VerticalToolWindowOpen(name, weight, [tablocal]) {{{
" Handles opening windows in the vertical tool window on the left (taglist,
" project tree, etc.)
function! eclim#display#window#VerticalToolWindowOpen(name, weight, ...)
  let taglist_window = exists('g:TagList_title') ? bufwinnr(g:TagList_title) : -1
  if exists('g:Tlist_Use_Horiz_Window') && g:Tlist_Use_Horiz_Window
    let taglist_window = -1
  endif

  let relative_window = 0
  let relative_window_loc = 'below'
  if taglist_window != -1 || len(g:VerticalToolBuffers) > 0
    if taglist_window != -1
      let relative_window = taglist_window
    endif
    for toolbuf in keys(g:VerticalToolBuffers)
      exec 'let toolbuf = ' . toolbuf
      if bufwinnr(toolbuf) != -1
        if relative_window == 0
          let relative_window = bufwinnr(toolbuf)
          if getbufvar(toolbuf, 'weight') > a:weight
            let relative_window_loc = 'below'
          else
            let relative_window_loc = 'above'
          endif
        elseif getbufvar(toolbuf, 'weight') > a:weight
          let relative_window = bufwinnr(toolbuf)
          let relative_window_loc = 'below'
        endif
      endif
    endfor
  endif

  if relative_window != 0
    let wincmd = relative_window . 'winc w | ' . relative_window_loc . ' '
  else
    let wincmd = g:VerticalToolWindowPosition . ' ' . g:VerticalToolWindowWidth
  endif

  let escaped = substitute(
    \ a:name, '\(.\{-}\)\[\(.\{-}\)\]\(.\{-}\)', '\1[[]\2[]]\3', 'g')
  if a:0 && a:1
    let bufnum = -1
    for bnr in tabpagebuflist()
      if bufname(bnr) == a:name
        let bufnum = bnr
        break
      endif
    endfor
  else
    let bufnum = bufnr(escaped)
  endif
  let name = bufnum == -1 ? a:name : '+buffer' . bufnum
  silent call eclim#util#ExecWithoutAutocmds(wincmd . ' split ' . name)

  setlocal winfixwidth
  setlocal nonumber

  let b:weight = a:weight
  let bufnum = bufnr('%')
  let g:VerticalToolBuffers[bufnum] = a:name
  augroup eclim_vertical_tool_windows
    autocmd!
    autocmd BufDelete * call s:PreventCloseOnBufferDelete()
    autocmd BufEnter * nested call s:CloseIfLastWindow()
  augroup END
  if exists('g:TagList_title') &&
   \ !exists('g:TagListToo') &&
   \ (!exists('g:Tlist_Use_Horiz_Window') || !g:Tlist_Use_Horiz_Window)
    augroup eclim_vertical_tool_windows_move
      autocmd!
    augroup END
    exec 'autocmd BufWinEnter ' . g:TagList_title .
      \ ' call s:MoveRelativeTo(g:TagList_title)'
  endif
  augroup eclim_vertical_tool_windows_buffer
    exec 'autocmd BufWinLeave <buffer> ' .
      \ 'silent! call remove(g:VerticalToolBuffers, ' . bufnum . ') | ' .
      \ 'autocmd! eclim_vertical_tool_windows_buffer * <buffer=' . bufnum . '>'
  augroup END
endfunction " }}}

" VerticalToolWindowRestore() {{{
" Used to restore the tool windows to their proper width if some action
" altered them.
function! eclim#display#window#VerticalToolWindowRestore()
  for toolbuf in keys(g:VerticalToolBuffers)
    exec 'let toolbuf = ' . toolbuf
    if bufwinnr(toolbuf) != -1
      exec 'vertical ' . bufwinnr(toolbuf) . 'resize ' . g:VerticalToolWindowWidth
    endif
  endfor
endfunction " }}}

" GetWindowOptions(winnum) {{{
" Gets a dictionary containing all the localy set options for the specified
" window.
function! eclim#display#window#GetWindowOptions(winnum)
  let curwin = winnr()
  try
    exec a:winnum . 'winc w'
    redir => list
    silent exec 'setlocal'
    redir END
  finally
    exec curwin . 'winc w'
  endtry

  let list = substitute(list, '---.\{-}---', '', '')
  let winopts = {}
  for wopt in split(list, '\_s\+')[1:]
    if wopt =~ '^[a-z]'
      if wopt =~ '='
        let key = substitute(wopt, '\(.\{-}\)=.*', '\1', '')
        let value = substitute(wopt, '.\{-}=\(.*\)', '\1', '')
        let winopts[key] = value
      else
        let winopts[wopt] = ''
      endif
    endif
  endfor
  return winopts
endfunction " }}}

" SetWindowOptions(winnum, options) {{{
" Given a dictionary of options, sets each as local options for the specified
" window.
function! eclim#display#window#SetWindowOptions(winnum, options)
  let curwin = winnr()
  try
    exec a:winnum . 'winc w'
    for key in keys(a:options)
      if key =~ '^no'
        silent! exec 'setlocal ' . key
      else
        silent! exec 'setlocal ' . key . '=' . a:options[key]
      endif
    endfor
  finally
    exec curwin . 'winc w'
  endtry
endfunction " }}}

" s:CloseIfLastWindow() {{{
function! s:CloseIfLastWindow()
  if histget(':', -1) !~ '^bd'
    let numtoolwindows = 0
    for toolbuf in keys(g:VerticalToolBuffers)
      exec 'let toolbuf = ' . toolbuf
      if bufwinnr(toolbuf) != -1
        let numtoolwindows += 1
      endif
    endfor
    if winnr('$') == numtoolwindows
      if tabpagenr('$') > 1
        tabclose
      else
        quitall
      endif
    endif
  endif
endfunction " }}}

" s:MoveRelativeTo(name) {{{
function! s:MoveRelativeTo(name)
  for toolbuf in keys(g:VerticalToolBuffers)
    exec 'let toolbuf = ' . toolbuf
    if bufwinnr(toolbuf) != -1
      call setwinvar(bufwinnr(toolbuf), 'marked_for_removal', 1)
      let winoptions = eclim#display#window#GetWindowOptions(bufwinnr(toolbuf))
      call remove(winoptions, 'filetype')
      call remove(winoptions, 'syntax')
      call eclim#display#window#VerticalToolWindowOpen(
        \ g:VerticalToolBuffers[toolbuf], getbufvar(toolbuf, 'weight'))
      call eclim#display#window#SetWindowOptions(winnr(), winoptions)
    endif
  endfor

  let winnum = 1
  while winnum <= winnr('$')
    if getwinvar(winnum, 'marked_for_removal') == 1
      exec winnum . 'winc w'
      close
    else
      let winnum += 1
    endif
  endwhile
  call eclim#display#window#VerticalToolWindowRestore()
endfunction " }}}

" s:PreventCloseOnBufferDelete() {{{
function! s:PreventCloseOnBufferDelete()
  let numtoolwindows = 0
  for toolbuf in keys(g:VerticalToolBuffers)
    exec 'let toolbuf = ' . toolbuf
    if bufwinnr(toolbuf) != -1
      let numtoolwindows += 1
    endif
  endfor

  let index = 1
  let numtempwindows = 0
  let tempbuffers = []
  while index <= winnr('$')
    let buf = winbufnr(index)
    if buf != -1 && getbufvar(buf, 'eclim_temp_window') != ''
      call add(tempbuffers, buf)
    endif
    let index += 1
  endwhile

  if winnr('$') == (numtoolwindows + len(tempbuffers))
    let toolbuf = bufnr('%')
    if g:VerticalToolWindowSide == 'right'
      vertical topleft new
    else
      vertical botright new
    endif
    setlocal noreadonly modifiable
    let winnum = winnr()
    exec 'let bufnr = ' . expand('<abuf>')

    redir => list
    silent exec 'buffers'
    redir END

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

    " build list of buffers not open in any window
    let buffers = []
    for entry in split(list, '\n')
      exec 'let bnum = ' . substitute(entry, '\s*\([0-9]\+\).*', '\1', '')
      if bnum != bufnr && index(tabbuffers, bnum) == -1 && bufwinnr(bnum) == -1
        if bnum < bufnr
          call insert(buffers, bnum)
        else
          call add(buffers, bnum)
        endif
      endif
    endfor

    " we found a hidden buffer, so open it
    if len(buffers) > 0
      exec 'buffer ' . buffers[0]
      doautocmd BufEnter
      doautocmd BufWinEnter
      doautocmd BufReadPost
    endif

    exec bufwinnr(toolbuf) . 'winc w'
    exec 'vertical resize ' . g:VerticalToolWindowWidth

    " fix the position of the temp windows
    if len(tempbuffers) > 0
      for buf in tempbuffers
        " open the buffer in the temp window position
        botright 10new
        exec 'buffer ' . buf
        setlocal winfixheight

        " close the old window
        let winnr = winnr()
        let index = 1
        while index <= winnr('$')
          if winbufnr(index) == buf && index != winnr
            exec index . 'winc w'
            close
            winc p
            break
          endif
          let index += 1
        endwhile
      endfor
    endif

    exec winnum . 'winc w'
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
