" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/common/maximize.html
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

" Global Variables {{{
  if !exists('g:MaximizeMinWinHeight')
    let g:MaximizeMinWinHeight = 0
  endif
  if !exists('g:MaximizeMinWinWidth')
    let g:MaximizeMinWinWidth = 0
  endif

  let g:MaximizedMode = ''
" }}}

" MaximizeWindow(full) {{{
function! eclim#display#maximize#MaximizeWindow(full)
  " disable any minimize settings
  call eclim#display#maximize#ResetMinimized()

  " get the window that is maximized
  let maximized = s:GetMaximizedWindow()
  if maximized
    call s:DisableMaximizeAutoCommands()
    call eclim#display#maximize#RestoreWindows(maximized)
  endif

  let maximized_mode = a:full ? 'full' : 'fixed'
  if g:MaximizedMode != maximized_mode
    let g:MaximizedMode = maximized_mode
    let last = winnr('$')
    let index = 1
    while index <= last
      call s:InitWindowDimensions(index)
      let index += 1
    endwhile
    exec 'set winminwidth=' . g:MaximizeMinWinWidth
    exec 'set winminheight=' . g:MaximizeMinWinHeight
    call eclim#display#maximize#MaximizeUpdate(a:full)
  else
    let g:MaximizedMode = ''
  endif
endfunction " }}}

" MinimizeWindow([winnr, ...]) {{{
function! eclim#display#maximize#MinimizeWindow(...)
  let curwinnum = winnr()

  exec 'set winminheight=' . g:MaximizeMinWinHeight
  exec 'set winminwidth=' . g:MaximizeMinWinWidth

  call s:DisableMaximizeAutoCommands()
  call s:DisableMinimizeAutoCommands()

  " first turn off maximized if enabled
  let maximized = s:GetMaximizedWindow()
  if maximized
    call eclim#display#maximize#RestoreWindows(maximized)
  endif

  let args = []
  if len(a:000) == 0
    let args = [winnr()]
  else
    let args = a:000[:]
  endif

  " initialize window dimensions
  let last = winnr('$')
  let index = 1
  while index <= last
    call s:InitWindowDimensions(index)
    let index += 1
  endwhile

  " loop through and mark the buffers
  let num_minimized = 0
  for winnum in args
    if winnum < 0 || winnum > last
      continue
    endif

    let val = getwinvar(winnum, 'minimized')
    let minimized = type(val) == 0 ? !val : 1
    if minimized
      let num_minimized += 1
    else
      call s:RestoreWinVar(winnum, '&winfixheight')
      call s:RestoreWinVar(winnum, '&winfixwidth')
    endif
    call setwinvar(winnum, 'minimized', minimized)
  endfor

  " check for existing minimized windows
  if num_minimized == 0
    let index = 1
    while index <= last
      if getwinvar(index, 'minimized') == 1
        let num_minimized += 1
      endif
      let index = index + 1
    endwhile
  endif

  noautocmd call s:Reminimize()
  if num_minimized > 0
    call s:EnableMinimizeAutoCommands()
  endif
endfunction " }}}

" MaximizeUpdate(full) {{{
function! eclim#display#maximize#MaximizeUpdate(full)
  call s:InitWindowDimensions(winnr())
  call s:DisableMaximizeAutoCommands()

  let w:maximized = 1
  winc |
  winc _

  if !a:full
    call s:RestoreFixedWindows()
  else
    let winnr = winnr()
    if getwinvar(winnr, '&winfixheight')
      exec winnr . 'resize ' . getwinvar(winnr, 'winheight')
    endif
    if getwinvar(winnr, '&winfixwidth')
      exec 'vertical ' . winnr . 'resize ' . getwinvar(winnr, 'winwidth')
    endif
  endif
  call s:EnableMaximizeAutoCommands(a:full)
endfunction " }}}

" ResetMinimized() {{{
function! eclim#display#maximize#ResetMinimized()
  call s:DisableMinimizeAutoCommands()
  let winend = winnr('$')
  let winnum = 1
  let num_minimized = 0
  while winnum <= winend
    if getwinvar(winnum, 'minimized') == 1
      let num_minimized += 1
      call setwinvar(winnum, 'minimized', 0)
      call s:RestoreWinVar(winnum, '&winfixheight')
      call s:RestoreWinVar(winnum, '&winfixwidth')
    endif
    let winnum = winnum + 1
  endwhile
  if num_minimized > 0
    call s:RestoreFixedWindows()
  endif
endfunction " }}}

" RestoreWindows(maximized) {{{
function! eclim#display#maximize#RestoreWindows(maximized)
  " reset the maximized var.
  if a:maximized
    call setwinvar(a:maximized, 'maximized', 0)
  endif

  winc _
  winc =

  call s:RestoreFixedWindows()
endfunction " }}}

" NavigateWindows(cmd) {{{
" Used navigate windows by skipping minimized windows.
function! eclim#display#maximize#NavigateWindows(wincmd)
  " edge case for the command line window
  if &ft == 'vim' && bufname('%') == '[Command Line]'
    quit
    return
  endif

  let start = winnr()
  let lastwindow = start

  exec a:wincmd
  while exists('w:minimized') && w:minimized && winnr() != lastwindow
    let lastwindow = winnr()
    let lastfile = expand('%')
    exec a:wincmd
  endwhile

  if exists('w:minimized') && w:minimized && winnr() != start
    exec start . 'wincmd w'
  endif
endfunction " }}}

" s:InitWindowDimensions(winnr) {{{
function! s:InitWindowDimensions(winnr)
  if getwinvar(a:winnr, 'winheight') == ''
    "echom 'win: ' . a:winnr . ' height: ' . winheight(a:winnr)
    call setwinvar(a:winnr, 'winheight', winheight(a:winnr))
  endif
  if getwinvar(a:winnr, 'winwidth') == ''
    "echom 'win: ' . a:winnr . ' width:  ' . winwidth(a:winnr)
    call setwinvar(a:winnr, 'winwidth', winwidth(a:winnr))
  endif
endfunction " }}}

" s:DisableMaximizeAutoCommands() {{{
function! s:DisableMaximizeAutoCommands()
  augroup maximize
    autocmd!
  augroup END
endfunction " }}}

" s:EnableMaximizeAutoCommands(full) {{{
function! s:EnableMaximizeAutoCommands(full)
  call s:DisableMaximizeAutoCommands()
  call s:DisableMinimizeAutoCommands()
  augroup maximize
    autocmd!
    exec 'autocmd BufWinEnter,WinEnter * nested ' .
      \ 'call eclim#display#maximize#MaximizeUpdate(' . a:full . ')'
    exec 'autocmd VimResized,BufDelete * nested ' .
      \ 'call s:MaximizeRefresh(' . a:full . ')'
    exec 'autocmd BufReadPost quickfix ' .
      \ 'call s:MaximizeRefresh(' . a:full . ')'
    exec 'autocmd BufUnload * call s:CloseFixedWindow(' . a:full . ')'
  augroup END
endfunction " }}}

" s:DisableMinimizeAutoCommands() {{{
function! s:DisableMinimizeAutoCommands()
  augroup minimize
    autocmd!
  augroup END
endfunction " }}}

" s:EnableMinimizeAutoCommands() {{{
function! s:EnableMinimizeAutoCommands()
  call s:DisableMaximizeAutoCommands()
  augroup minimize
    autocmd!
    autocmd BufWinEnter,WinEnter * nested noautocmd call s:Reminimize()
  augroup END
endfunction " }}}

" s:GetMaximizedWindow() {{{
function! s:GetMaximizedWindow()
  let winend = winnr('$')
  let winnum = 1
  while winnum <= winend
    let max = getwinvar(winnum, 'maximized')
    if max
      return winnum
    endif
    let winnum = winnum + 1
  endwhile

  return 0
endfunction " }}}

" s:MaximizeRefresh(full) {{{
function! s:MaximizeRefresh(full)
  call s:InitWindowDimensions(winnr())
  let maximized = s:GetMaximizedWindow()
  if maximized
    let curwin = winnr()
    try
      noautocmd exec maximized . 'winc w'
      call eclim#display#maximize#MaximizeUpdate(a:full)
    finally
      exec curwin . 'winc w'
    endtry
  endif
endfunction " }}}

" s:CloseFixedWindow(full) {{{
function! s:CloseFixedWindow(full)
  if expand('<afile>') == '' || &buftype != ''
    let maximized = s:GetMaximizedWindow()
    if maximized
      call eclim#util#DelayedCommand(
        \ 'call eclim#display#maximize#MaximizeUpdate(' . a:full . ')')
    endif
  endif
endfunction " }}}

" s:RestoreFixedWindows() {{{
function! s:RestoreFixedWindows()
  let last = winnr('$')
  let index = last
  while index >= 1
    let minimized = getwinvar(index, 'minimized')
    if getwinvar(index, '&winfixheight') && minimized != 1
      "echom index . 'resize ' . getwinvar(index, 'winheight')
      exec index . 'resize ' . getwinvar(index, 'winheight')
    endif
    if getwinvar(index, '&winfixwidth') && minimized != 1
      "echom 'vertical ' . index . 'resize ' . getwinvar(index, 'winwidth')
      exec 'vertical ' . index . 'resize ' . getwinvar(index, 'winwidth')
    endif
    let index -= 1
  endwhile
endfunction " }}}

" s:Reminimize() {{{
" Invoked when changing windows to ensure that any minimized windows are
" returned to their minimized state.
function! s:Reminimize()
  call s:InitWindowDimensions(winnr())
  let curwinnum = winnr()
  let winend = winnr('$')
  let winnum = 1
  let commands = []
  while winnum <= winend
    let minimized = getwinvar(winnum, 'minimized')
    if minimized
      let row_minimized = s:RowMinimized(winnum)
      let column_minimized = s:ColumnMinimized(winnum)

      "echom 'winnr = ' . winnum
      "echom '  row minimized    = ' . row_minimized
      "echom '  column minimized = ' . column_minimized
      "echom '  in row           = ' . s:IsInRow(winnum)
      "echom '  in column        = ' . s:IsInColumn(winnum)

      if row_minimized
        call add(commands, winnum . "resize 0")
        call s:SaveWinVar(winnum, '&winfixheight')
        call setwinvar(winnum, "&winfixheight", 1)

      elseif column_minimized
        call add(commands, "vertical " . winnum . "resize 0")
        call s:SaveWinVar(winnum, '&winfixwidth')
        call setwinvar(winnum, "&winfixwidth", 1)

      elseif s:IsInRow(winnum)
        call add(commands, "vertical " . winnum . "resize 0")
        call s:SaveWinVar(winnum, '&winfixwidth')
        call setwinvar(winnum, "&winfixwidth", 1)

      elseif s:IsInColumn(winnum)
        call add(commands, winnum . "resize 0")
        call s:SaveWinVar(winnum, '&winfixheight')
        call setwinvar(winnum, "&winfixheight", 1)

      else
        call add(commands, winnum . "resize 0")
        call add(commands, "vertical " . winnum . "resize 0")
        call s:SaveWinVar(winnum, '&winfixheight')
        call s:SaveWinVar(winnum, '&winfixwidth')
        call setwinvar(winnum, "&winfixheight", 1)
        call setwinvar(winnum, "&winfixwidth", 1)
      endif
    endif
    let winnum = winnum + 1
  endwhile

  " ensure we end up in the window we started in
  exec curwinnum . 'winc w'

  " run all the resizing commands
  for cmd in commands
    echom cmd
    exec cmd
  endfor

  winc =
  call s:RestoreFixedWindows()
endfunction " }}}

" s:IsInRow(window) {{{
" Determines if the supplied window is in a row of equally sized windows.
function! s:IsInRow(window)
  let origwinnr = winnr()
  exec a:window . 'winc w'

  " check windows to the right
  let curwinnr = winnr()
  winc l
  while winnr() != curwinnr
    let curwinnr = winnr()
    if winheight(curwinnr) == winheight(a:window)
      exec origwinnr . 'winc w'
      return 1
    endif
    winc l
  endwhile

  exec a:window . 'winc w'

  " check windows to the left
  let curwinnr = winnr()
  winc h
  while winnr() != curwinnr
    let curwinnr = winnr()
    if winheight(curwinnr) == winheight(a:window)
      exec origwinnr . 'winc w'
      return 1
    endif
    winc h
  endwhile

  exec origwinnr . 'winc w'
  return 0
endfunction " }}}

" s:IsInColumn(window) {{{
" Determines is the supplied window is in a column of equally sized windows.
function! s:IsInColumn(window)
  let origwinnr = winnr()
  exec a:window . 'winc w'

  " check windows above
  let curwinnr = winnr()
  winc k
  while winnr() != curwinnr
    let curwinnr = winnr()
    if winwidth(curwinnr) == winwidth(a:window)
      exec origwinnr . 'winc w'
      return 1
    endif
    winc k
  endwhile

  exec a:window . 'winc w'

  " check windows below
  let curwinnr = winnr()
  winc j
  while winnr() != curwinnr
    let curwinnr = winnr()
    if winwidth(curwinnr) == winwidth(a:window)
      exec origwinnr . 'winc w'
      return 1
    endif
    winc j
  endwhile

  exec origwinnr . 'winc w'
  return 0
endfunction " }}}

" s:RowMinimized(window) {{{
" Determines if all windows on a row are minimized.
function! s:RowMinimized(window)
  let origwinnr = winnr()
  exec a:window . 'winc w'

  let windows = []

  " check windows to the right
  let right = 0
  let curwinnr = winnr()
  winc l
  while winnr() != curwinnr
    let right += 1
    let curwinnr = winnr()
    if winheight(curwinnr) == winheight(a:window)
      if getwinvar(curwinnr, 'minimized') == ''
        exec origwinnr . 'winc w'
        return 0
      else
        call add(windows, curwinnr)
      endif
    elseif winheight(curwinnr) > winheight(a:window)
      let right -= 1
    endif
    winc l
  endwhile

  exec a:window . 'winc w'

  " check windows to the left
  let left = 0
  let curwinnr = winnr()
  winc h
  while winnr() != curwinnr
    let left += 1
    let curwinnr = winnr()
    if winheight(curwinnr) == winheight(a:window)
      if getwinvar(curwinnr, 'minimized') == ''
        exec origwinnr . 'winc w'
        return 0
      else
        call add(windows, curwinnr)
      endif
    elseif winheight(curwinnr) >= winheight(a:window)
      let left -= 1
    endif
    winc h
  endwhile

  " if the window had none to the left or right, then it is in a row all by
  " itself.
  if !right && !left
    call add(windows, a:window)
  endif

  exec origwinnr . 'winc w'
  return len(windows) > 0
endfunction " }}}

" s:ColumnMinimized(window) {{{
" Determines all windows in column are minimized.
function! s:ColumnMinimized(window)
  let origwinnr = winnr()
  exec a:window . 'winc w'

  let windows = []

  " check windows above
  let above = 0
  let curwinnr = winnr()
  winc k
  while winnr() != curwinnr
    let above += 1
    let curwinnr = winnr()
    if winwidth(curwinnr) == winwidth(a:window)
      if getwinvar(curwinnr, 'minimized') == ''
        exec origwinnr . 'winc w'
        return 0
      else
        call add(windows, curwinnr)
      endif
    elseif winwidth(curwinnr) >= winwidth(a:window)
      let above -= 1
    endif
    winc k
  endwhile

  exec a:window . 'winc w'

  " check windows below
  let below = 0
  let curwinnr = winnr()
  winc j
  while winnr() != curwinnr
    let below += 1
    let curwinnr = winnr()
    if winwidth(curwinnr) == winwidth(a:window)
      if getwinvar(curwinnr, 'minimized') == ''
        exec origwinnr . 'winc w'
        return 0
      else
        call add(windows, curwinnr)
      endif
    elseif winwidth(curwinnr) >= winwidth(a:window)
      let below -= 1
    endif
    winc j
  endwhile

  " if the window had none above or below, then it is in a column all by
  " itself.
  if !above && !below
    call add(windows, a:window)
  endif

  exec origwinnr . 'winc w'
  return len(windows) > 0
endfunction " }}}

" s:SaveWinVar(winnr, var) {{{
function! s:SaveWinVar(winnr, var)
  let save = substitute(a:var, '^&', '', '') . '_save'
  if getwinvar(a:winnr, save) == ''
    call setwinvar(a:winnr, save, getwinvar(a:winnr, a:var))
  endif
endfunction " }}}

" s:RestoreWinVar(winnr, var) {{{
function! s:RestoreWinVar(winnr, var)
  let save = substitute(a:var, '^&', '', '') . '_save'
  if getwinvar(a:winnr, save) != ''
    call setwinvar(a:winnr, a:var, getwinvar(a:winnr, save))
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
