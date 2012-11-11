" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Utility functions for working with vim windows.
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

function! eclim#display#window#VerticalToolWindowOpen(name, weight, ...) " {{{
  " Handles opening windows in the vertical tool window on the left (taglist,
  " project tree, etc.)

  let taglist_window = -1
  if exists('g:TagList_title')
    let taglist_window = bufwinnr(eclim#util#EscapeBufferName(g:TagList_title))
    let taglist_position = 'left'
    if exists('g:Tlist_Use_Horiz_Window') && g:Tlist_Use_Horiz_Window
      let taglist_position = 'horizontal'
    elseif exists('g:TaglistTooPosition')
      let taglist_position = g:TaglistTooPosition
    elseif exists('g:Tlist_Use_Right_Window') && g:Tlist_Use_Right_Window
      let taglist_position = 'right'
    endif
  endif
  if taglist_window == -1 && exists(':TagbarOpen')
    let taglist_window = bufwinnr('__Tagbar__')
    let taglist_position = 'right'
    if exists('g:tagbar_left') && g:tagbar_left
      let taglist_position = 'left'
    endif
  endif
  if taglist_window != -1
    " don't consider horizontal taglist, or taglist configured to display
    " opposite the tool windows as a tool window member.
    if taglist_position != g:VerticalToolWindowSide
      let taglist_window = -1
    endif
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

  doautocmd BufWinEnter
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
   \ (!exists('g:Tlist_Use_Horiz_Window') || !g:Tlist_Use_Horiz_Window)
    augroup eclim_vertical_tool_windows_move_taglist
      autocmd!
      exec 'autocmd BufWinEnter ' . eclim#util#EscapeBufferName(g:TagList_title) .
        \ ' call s:MoveRelativeTo()'
    augroup END
  endif
  if exists(':TagbarOpen')
    augroup eclim_vertical_tool_windows_move_tagbar
      autocmd!
      autocmd BufWinEnter __Tagbar__ call s:MoveRelativeTo()
    augroup END
  endif
  augroup eclim_vertical_tool_windows_buffer
    exec 'autocmd BufWinLeave <buffer> ' .
      \ 'silent! call remove(g:VerticalToolBuffers, ' . bufnum . ') | ' .
      \ 'autocmd! eclim_vertical_tool_windows_buffer * <buffer=' . bufnum . '>'
  augroup END
endfunction " }}}

function! eclim#display#window#VerticalToolWindowRestore() " {{{
  " Used to restore the tool windows to their proper width if some action
  " altered them.

  for toolbuf in keys(g:VerticalToolBuffers)
    exec 'let toolbuf = ' . toolbuf
    if bufwinnr(toolbuf) != -1
      exec 'vertical ' . bufwinnr(toolbuf) . 'resize ' . g:VerticalToolWindowWidth
    endif
  endfor
endfunction " }}}

function! eclim#display#window#GetWindowOptions(winnum) " {{{
  " Gets a dictionary containing all the localy set options for the specified
  " window.

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
  for wopt in split(list, '\(\n\|\s\s\+\)')[1:]
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

function! eclim#display#window#SetWindowOptions(winnum, options) " {{{
  " Given a dictionary of options, sets each as local options for the specified
  " window.

  let curwin = winnr()
  try
    exec a:winnum . 'winc w'
    for key in keys(a:options)
      if key =~ '^no'
        silent! exec 'setlocal ' . key
      else
        silent! exec 'setlocal ' . key . '=' . escape(a:options[key], ' ')
      endif
    endfor
  finally
    exec curwin . 'winc w'
  endtry
endfunction " }}}

function! s:CloseIfLastWindow() " {{{
  if histget(':', -1) !~ '^bd'
    let close = 1
    for bufnr in tabpagebuflist()
      if has_key(g:VerticalToolBuffers, bufnr)
        continue
      endif
      if exists('g:TagList_title') && bufname(bufnr) == g:TagList_title
        continue
      endif
      if exists('g:BufExplorer_title') && bufname(bufnr) == '[BufExplorer]'
        let close = 0
        break
      endif

      let buftype = getbufvar(bufnr, '&buftype')
      if buftype != '' && buftype != 'help'
        continue
      endif

      let close = 0
      break
    endfor

    if close
      if tabpagenr('$') > 1
        tabclose
      else
        quitall
      endif
    endif
  endif
endfunction " }}}

function! s:MoveRelativeTo() " {{{
  " get the buffer that the taglist was opened from
  let curwin = winnr()
  let list_buffer = bufnr('%')
  winc p
  let orig_buffer = bufnr('%')
  exec curwin . 'winc p'

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

  " some window juggling so that winc p from taglist goes back to the original
  " buffer
  exec bufwinnr(orig_buffer) . 'winc w'
  exec bufwinnr(list_buffer) . 'winc w'
endfunction " }}}

function! s:PreventCloseOnBufferDelete() " {{{
  let index = 1
  let numtoolwindows = 0
  let numtempwindows = 0
  let tempbuffersbot = []
  while index <= winnr('$')
    let buf = winbufnr(index)
    let bufname = bufname(buf)
    if index(keys(g:VerticalToolBuffers), string(buf)) != -1
      let numtoolwindows += 1
    elseif getwinvar(index, '&winfixheight') || getwinvar(index, '&winfixwidth')
      let numtempwindows += 1
      if getwinvar(index, '&winfixheight')
        call add(tempbuffersbot, buf)
      endif
    endif
    let index += 1
  endwhile

  if winnr('$') == (numtoolwindows + numtempwindows)
    let toolbuf = bufnr('%')
    if g:VerticalToolWindowSide == 'right'
      vertical topleft new
    else
      vertical botright new
    endif
    setlocal noreadonly modifiable
    let curbuf = bufnr('%')
    let removed = str2nr(expand('<abuf>'))
    let next = eclim#common#buffers#OpenNextHiddenTabBuffer(removed)
    if next != 0
      let curbuf = next
    endif

    " resize windows
    exec bufwinnr(toolbuf) . 'winc w'
    exec 'vertical resize ' . g:VerticalToolWindowWidth

    " fix the position of the temp windows
    for buf in tempbuffersbot
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

    exec bufwinnr(curbuf) . 'winc w'
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
