" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/common/maximize.html
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
  if !exists('g:MaximizeExcludes')
    let g:MaximizeExcludes =
      \ '\(ProjectTree_*\|__Tag_List__\|-MiniBufExplorer-\|^\[.*\]$\)'
  endif
  if !exists('g:MaximizeMinWinHeight')
    let g:MaximizeMinWinHeight = 0
  endif
  if !exists('g:MaximizeMinWinWidth')
    let g:MaximizeMinWinWidth = 0
  endif
  if !exists('g:MaximizeQuickfixHeight')
    let g:MaximizeQuickfixHeight = 10
  endif
" }}}

" MaximizeWindow() {{{
function! eclim#display#maximize#MaximizeWindow ()
  " disable any minimize settings
  call eclim#display#maximize#ResetMinimized()

  " get the window that is maximized
  let maximized = s:GetMaximizedWindow()

  if maximized
    call s:DisableMaximizeAutoCommands()
    call eclim#display#maximize#RestoreWindows()
  else
    exec "set winminwidth=" . g:MaximizeMinWinWidth
    exec "set winminheight=" . g:MaximizeMinWinHeight
    call s:MaximizeNewWindow()
  endif
  unlet maximized
endfunction " }}}

" MinimizeWindow() {{{
function! eclim#display#maximize#MinimizeWindow (...)
  let winnum = winnr()

  exec "set winminheight=" . g:MaximizeMinWinHeight
  exec "set winminwidth=" . g:MaximizeMinWinWidth
  call s:DisableMaximizeAutoCommands()

  " first turn off maximized if enabled
  let maximized = s:GetMaximizedWindow()
  if maximized
    call eclim#display#maximize#RestoreWindows()
  endif

  let args = []
  if len(a:000) == 0
    let args = [bufnr('%')]
  else
    let args = a:000[:]
    call map(args, 's:BufferNumber(v:val)')
  endif

  " first loop through and mark the buffers
  for buffer in args
    let window = bufwinnr(buffer)
    if window != -1
      call setbufvar(buffer, "minimized", 1)
    endif
  endfor

  " second loop sweep through and resize
  for buffer in args
    let window = bufwinnr(buffer)
    if window != -1
      if s:IsVerticalSplit(window) && s:IsColumnMinimized(window)
        exec "vertical " . window . "resize 0"
        call setwinvar(window, "&winfixwidth", 1)
      " if s:IsRowMinimized(window)
      else
        exec window . "resize 0"
        call setwinvar(window, "&winfixheight", 1)
      endif
    endif
  endfor

  " ensure we end up in the window we started in
  exec winnum . 'winc w'

  winc =

  call s:RestoreFixedWindows()
  call s:EnableMinimizeAutoCommands()
endfunction " }}}

" MaximizeNewWindow() {{{
function! s:MaximizeNewWindow ()
  if expand('%') !~ g:MaximizeExcludes
    call s:DisableMaximizeAutoCommands()
    call s:GetMaximizedWindow()
    let b:maximized = 1
    call s:ResizeWindows()
    call s:EnableMaximizeAutoCommands()
  endif
endfunction " }}}

" GetMaximizedWindow() {{{
function! s:GetMaximizedWindow ()
  let result = 0

  let bufend = bufnr('$')
  let bufnum = 1
  while bufnum <= bufend
    if bufexists(bufnum)
      let max = getbufvar(bufnum, "maximized")
      " reset the maximized/minimized vars.
      call setbufvar(bufnum, "maximized", 0)

      if max && bufwinnr(bufnum) != -1
        let result = bufwinnr(bufnum)
      endif
    endif
    let bufnum = bufnum + 1
  endwhile
  unlet bufend bufnum

  return result
endfunction " }}}

" ResetMinimized() {{{
function! eclim#display#maximize#ResetMinimized ()
  call s:DisableMinimizeAutoCommands()
  let bufend = bufnr('$')
  let bufnum = 1
  while bufnum <= bufend
    if bufwinnr(bufnum) != -1
      call setbufvar(bufnum, "minimized", 0)
      call setwinvar(bufwinnr(bufnum), "&winfixheight", 0)
      call setwinvar(bufwinnr(bufnum), "&winfixwidth", 0)
    endif
    let bufnum = bufnum + 1
  endwhile
  unlet bufend bufnum
endfunction " }}}

" ResizeWindows() {{{
function! s:ResizeWindows ()
  winc |
  winc _

  let curwindow = winnr()
  if &ft == 'qf'
    let quickfixwindow = curwindow
  endif

  winc w
  while curwindow != winnr()
    let window = winnr()
    let buffername = expand('%')
    let quickfix = (&ft == 'qf')
    if quickfix
      let quickfixwindow = window
    endif
    winc w
    if !quickfix && buffername !~ g:MaximizeExcludes
      exec "vertical " . window . "resize 0"
      exec window . "resize 0"
    endif
  endwhile

  resize +100
  vertical resize +100
  if exists("quickfixwindow")
    exec quickfixwindow . "resize " . g:MaximizeQuickfixHeight
    exec "vertical " . quickfixwindow . "resize"
    winc |
  endif
  unlet curwindow

  call s:RestoreFixedWindows()
endfunction " }}}

" DisableMaximizeAutoCommands() {{{
function! s:DisableMaximizeAutoCommands ()
  augroup maximize
    autocmd!
  augroup END
endfunction " }}}

" EnableMaximizeAutoCommands() {{{
function! s:EnableMaximizeAutoCommands ()
  call s:DisableMaximizeAutoCommands()
  call s:DisableMinimizeAutoCommands()
  augroup maximize
    autocmd!
    autocmd BufReadPost quickfix call s:FixQuickfix(1)
    autocmd BufUnload * call s:CloseQuickfix()
    autocmd BufWinEnter,WinEnter * call s:MaximizeNewWindow()
  augroup END
endfunction " }}}

" DisableMinimizeAutoCommands() {{{
function! s:DisableMinimizeAutoCommands ()
  augroup minimize
    autocmd!
  augroup END
endfunction " }}}

" EnableMinimizeAutoCommands() {{{
function! s:EnableMinimizeAutoCommands ()
  call s:DisableMaximizeAutoCommands()
  augroup minimize
    autocmd!
    autocmd BufReadPost quickfix call s:FixQuickfix(0)
    autocmd BufWinEnter,WinEnter * call s:Reminimize()
  augroup END
endfunction " }}}

" FixQuickfix(maximize) {{{
function s:FixQuickfix (maximize)
  exec "resize " . g:MaximizeQuickfixHeight
  set winfixheight

  let curwindow = winnr()

  if a:maximize
    "return to previous window to restore it's maximized
    winc p
    call s:MaximizeNewWindow()

    exec curwindow . "winc w"
  endif
endfunction " }}}

" CloseQuickfix() {{{
function s:CloseQuickfix ()
  if expand('<afile>') == ""
    "echom "Closing nofile (maybe quickfix)"
    " can't figure out how to re-maximize if cclose is called from a maximized
    " window, so just resetting.
    call s:GetMaximizedWindow()
  endif
endfunction " }}}

" Reminimize() {{{
" Invoked when changing windows to ensure that any minimized windows are
" returned to their minimized state.
function s:Reminimize ()
  call s:DisableMinimizeAutoCommands()
  let bufend = bufnr('$')
  let bufnum = 1
  while bufnum <= bufend
    if bufwinnr(bufnum) != -1 && expand('%') !~ g:MaximizeExcludes
      let window = bufwinnr(bufnum)
      let minimized = getbufvar(bufnum, "minimized")
      if minimized
        if s:IsVerticalSplit(window) && s:IsColumnMinimized(window)
          exec "vertical " . window . "resize 0"
          call setwinvar(window, "&winfixwidth", 1)
        " if s:IsRowMinimized(window)
        else
          exec window . "resize 0"
          call setwinvar(window, "&winfixheight", 1)
        endif
      endif
    endif
    let bufnum = bufnum + 1
  endwhile
  unlet bufend bufnum
  call s:RestoreFixedWindows()
  call s:EnableMinimizeAutoCommands()
endfunction " }}}

" RestoreWindows() {{{
function! eclim#display#maximize#RestoreWindows ()
  winc _
  winc =

  call s:RestoreFixedWindows()

  let curwinnr = winnr()
  winc w
  while winnr() != curwinnr
    if &ft == 'qf'
      exec "resize " . g:MaximizeQuickfixHeight
    endif
    winc w
  endwhile
endfunction " }}}

" RestoreFixedWindows() {{{
function! s:RestoreFixedWindows ()
  "fixes TList pane that ends up getting resized
  if exists("g:TagList_title")
    if bufwinnr(g:TagList_title) != -1
      exec "vertical " . bufwinnr(g:TagList_title) .
        \ "resize " . g:Tlist_WinWidth
    endif
  endif
  if exists("g:EclimProjectTreeTitle")
    let winnr = bufwinnr(g:EclimProjectTreeTitle . '_*')
    if winnr != -1
      exec "vertical " . winnr . "resize " . g:EclimProjectTreeWidth
    endif
  endif
endfunction " }}}

" BufferNumber() {{{
" Convert a string buffer # to an int buffer #
function! s:BufferNumber (number)
  exec "return winbufnr(bufwinnr(" . a:number . "))"
endfunction " }}}

" IsVerticalSplit(window) {{{
" Determines if the current window is vertically split.
function! s:IsVerticalSplit (window)
  let origwinnr = winnr()

  exec a:window . 'winc w'
  let curwinnr = winnr()

  " check to the right
  winc l
  if winnr() != curwinnr && expand('%') !~ g:MaximizeExcludes
    return 1
  endif

  exec a:window . 'winc w'

  " check to the left
  winc h
  if winnr() != curwinnr && expand('%') !~ g:MaximizeExcludes
    return 1
  endif

  exec origwinnr . 'winc w'
  return 0
endfunction " }}}

" IsRowMinimized(window) {{{
" Determines all windows on a row are minimized.
function! s:IsRowMinimized (window)
  let origwinnr = winnr()
  exec a:window . 'winc w'
  let curwinnr = winnr()

  " check windows to the right
  let lastwinnr = winnr()
  winc l
  while winnr() != lastwinnr
    let buffer = bufnr('%')
    let lastwinnr = winnr()
    if winheight(lastwinnr) == winheight(curwinnr) &&
        \ getbufvar(buffer, 'minimized') == '' &&
        \ expand('%') !~ g:MaximizeExcludes
      exec origwinnr . 'winc w'
      return 0
    endif
    winc l
  endwhile

  exec curwinnr . 'winc w'

  " check windows to the left
  let lastwinnr = winnr()
  winc h
  while winnr() != lastwinnr
    let buffer = bufnr('%')
    let lastwinnr = winnr()
    if winheight(lastwinnr) == winheight(curwinnr) &&
        \ getbufvar(buffer, 'minimized') == '' &&
        \ expand('%') !~ g:MaximizeExcludes
      exec origwinnr . 'winc w'
      return 0
    endif
    winc h
  endwhile

  exec origwinnr . 'winc w'
  return 1
endfunction " }}}

" IsColumnMinimized(window) {{{
" Determines all windows on in column are minimized.
function! s:IsColumnMinimized (window)
  let origwinnr = winnr()
  exec a:window . 'winc w'
  let curwinnr = winnr()

  " check windows above
  let lastwinnr = winnr()
  winc k
  while winnr() != lastwinnr
    let buffer = bufnr('%')
    let lastwinnr = winnr()
    if winwidth(lastwinnr) == winwidth(curwinnr) &&
        \ getbufvar(buffer, 'minimized') == '' &&
        \ expand('%') !~ g:MaximizeExcludes
      exec origwinnr . 'winc w'
      return 0
    endif
    winc k
  endwhile

  exec curwinnr . 'winc w'

  " check windows below
  let lastwinnr = winnr()
  winc j
  while winnr() != lastwinnr
    let buffer = bufnr('%')
    let lastwinnr = winnr()
    if winwidth(lastwinnr) == winwidth(curwinnr) &&
        \ getbufvar(buffer, 'minimized') == '' &&
        \ expand('%') !~ g:MaximizeExcludes
      exec origwinnr . 'winc w'
      return 0
    endif
    winc j
  endwhile

  exec origwinnr . 'winc w'
  return 1
endfunction " }}}

" NavigateWindows(cmd) {{{
" Used navigate windows by skipping minimized windows.
function! eclim#display#maximize#NavigateWindows (wincmd)
  let start = winnr()
  let lastwindow = start

  exec a:wincmd
  while exists('b:minimized') && b:minimized && winnr() != lastwindow
    let lastwindow = winnr()
    exec a:wincmd
  endwhile

  if exists('b:minimized') && b:minimized
    exec start . 'wincmd w'
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
