" Author:  Eric Van Dewoestine
"
" Description: {{{
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

" Global Variables {{{
  if !exists('g:EclimHistoryDiffOrientation')
    let g:EclimHistoryDiffOrientation = 'vertical'
  endif
" }}}

" Script Variables {{{
let s:command_add = '-command history_add -p "<project>" -f "<file>"'
let s:command_list = '-command history_list -p "<project>" -f "<file>"'
let s:command_revision =
  \ '-command history_revision -p "<project>" -f "<file>" -r <revision>'
let s:command_clear = '-command history_clear -p "<project>" -f "<file>"'
" }}}

" AddHistory() {{{
" Adds the current state of the file to the eclipse local history (should be
" invoked prior to saving to disk).
function! eclim#common#history#AddHistory()
  if !filereadable(expand('%')) || !eclim#project#util#IsCurrentFileInProject(0)
    return
  endif

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()
  let command = s:command_add
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  call eclim#ExecuteEclim(command)
endfunction " }}}

" History() {{{
" Opens a temporary buffer with a list of local history revisions.
function! eclim#common#history#History()
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()
  let command = s:command_list
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let history = eclim#ExecuteEclim(command)
  if type(history) != g:LIST_TYPE
    return
  endif

  let lines = [file]
  let revisions = [0]
  let indent = eclim#util#GetIndent(1)
  for rev in history
    call add(lines, indent . rev.datetime . ' (' . rev.delta . ')')
    call add(revisions, rev.timestamp)
  endfor
  call add(lines, '')
  call eclim#util#TempWindow('[History]', lines)

  setlocal modifiable noreadonly
  if !g:EclimProjectKeepLocalHistory
    call append(line('$'),
      \ '" Note: local history is current disabled: ' .
      \ 'g:EclimProjectKeepLocalHistory = ' . g:EclimProjectKeepLocalHistory)
  endif
  call append(line('$'), '" use ? to view help')
  setlocal nomodifiable readonly
  syntax match Comment /^".*/

  let b:history_revisions = revisions
  call s:Syntax()

  command! -count=1 HistoryDiffNext call s:DiffNextPrev(1, <count>)
  command! -count=1 HistoryDiffPrev call s:DiffNextPrev(-1, <count>)
  augroup eclim_history_window
    autocmd! BufWinLeave <buffer>
    autocmd BufWinLeave <buffer>
      \ delcommand HistoryDiffNext |
      \ delcommand HistoryDiffPrev
  augroup END
  noremap <buffer> <silent> <cr> :call <SID>View()<cr>
  noremap <buffer> <silent> d :call <SID>Diff()<cr>
  noremap <buffer> <silent> r :call <SID>Revert()<cr>
  noremap <buffer> <silent> c :call <SID>Clear(1)<cr>

  " assign to buffer var to get around weird vim issue passing list containing
  " a string w/ a '<' in it on execution of mapping.
  let b:history_help = [
      \ '<cr> - view the entry',
      \ 'd - diff the file with the version under the cursor',
      \ 'r - revert the file to the version under the cursor',
      \ 'c - clear the history',
      \ ':HistoryDiffNext - diff the file with the next version in the history',
      \ ':HistoryDiffPrev - diff the file with the previous version in the history',
    \ ]
  nnoremap <buffer> <silent> ?
    \ :call eclim#help#BufferHelp(b:history_help, 'vertical', 50)<cr>
endfunction " }}}

" HistoryClear(bang) {{{
" Clear the history for the current file.
function! eclim#common#history#HistoryClear(bang)
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  call s:Clear(a:bang == '', expand('%:p'))
endfunction " }}}

" s:View([cmd]) {{{
" View the contents of the revision under the cursor.
function s:View(...)
  if line('.') == 1 || line('.') > len(b:history_revisions)
    return
  endif

  let current = b:filename
  let entry = line('.') - 1
  let revision = b:history_revisions[entry]
  if eclim#util#GoToBufferWindow(current)
    let filetype = &ft
    let project = eclim#project#util#GetCurrentProjectName()
    let file = eclim#project#util#GetProjectRelativeFilePath()
    let command = s:command_revision
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')
    let command = substitute(command, '<revision>', revision, '')
    let result = eclim#ExecuteEclim(command)
    if result == "0"
      return
    endif

    let cmd = len(a:000) > 0 ? a:000[0] : 'split'
    call eclim#util#GoToBufferWindowOrOpen(current . '_' . revision, cmd)

    setlocal modifiable
    setlocal noreadonly

    let temp = tempname()
    call writefile(split(result, '\n'), temp)
    try
      silent 1,$delete _
      silent read ++edit `=temp`
      silent 1,1delete _
    finally
      call delete(temp)
    endtry

    exec 'setlocal filetype=' . filetype
    setlocal nomodified
    setlocal readonly
    setlocal nomodifiable
    setlocal noswapfile
    setlocal nobuflisted
    setlocal buftype=nofile
    setlocal bufhidden=delete
    doautocmd BufReadPost

    call s:HighlightEntry(entry)

    return 1
  else
    call eclim#util#EchoWarning('Target file is no longer open.')
  endif
endfunction " }}}

" s:Diff() {{{
" Diff the contents of the revision under the cursor against the current
" contents.
function s:Diff()
  let hist_buf = bufnr('%')
  let winend = winnr('$')
  let winnum = 1
  while winnum <= winend
    let bufnr = winbufnr(winnum)
    if getbufvar(bufnr, 'history_diff') != ''
      exec bufnr . 'bd'
      continue
    endif
    let winnum += 1
  endwhile
  call eclim#util#GoToBufferWindow(hist_buf)

  let current = b:filename
  let orien = g:EclimHistoryDiffOrientation == 'horizontal' ? '' : 'vertical'
  if s:View(orien . ' below split')
    let b:history_diff = 1
    diffthis
    augroup history_diff
      autocmd! BufWinLeave <buffer>
      call eclim#util#GoToBufferWindowRegister(current)
      autocmd BufWinLeave <buffer> diffoff
    augroup END

    call eclim#util#GoToBufferWindow(current)
    diffthis
  endif
endfunction " }}}

" s:DiffNextPrev(dir, count) {{{
function s:DiffNextPrev(dir, count)
  let winnr = winnr()
  if eclim#util#GoToBufferWindow('[History]')
    let num = v:count > 0 ? v:count : a:count
    let cur = exists('b:history_current_entry') ? b:history_current_entry : 0
    let index = cur + (a:dir * num)
    if index < 0 || index > len(b:history_revisions)
      call eclim#util#EchoError('Operation exceeds history stack range.')
      exec winnr . 'winc w'
      return
    endif
    call cursor(index + 1, 0)
    call s:Diff()
  endif
endfunction " }}}

" s:Revert() {{{
" Revert the file to the revision under the cursor.
function s:Revert()
  if line('.') == 1 || line('.') > len(b:history_revisions)
    return
  endif

  let current = b:filename
  let revision = b:history_revisions[line('.') - 1]
  if eclim#util#GoToBufferWindow(current)
    let project = eclim#project#util#GetCurrentProjectName()
    let file = eclim#project#util#GetProjectRelativeFilePath()
    let command = s:command_revision
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')
    let command = substitute(command, '<revision>', revision, '')
    let result = eclim#ExecuteEclim(command)
    if result == "0"
      return
    endif

    let ff = &ff
    let temp = tempname()
    call writefile(split(result, '\n'), temp)
    try
      silent 1,$delete _
      silent read ++edit `=temp`
      silent 1,1delete _
    finally
      call delete(temp)
    endtry

    if ff != &ff
      call eclim#util#EchoWarning(
        \ "Warning: the file format is being reverted from '" . ff . "' to '" .
        \ &ff . "'. Using vim's undo will not restore the previous format so " .
        \ "if you choose to undo the reverting of this file, you will need to " .
        \ "manually set the file format back to " . ff . " (set ff=" . ff . ").")
    endif
  endif
endfunction " }}}

" s:Clear(prompt, [filename]) {{{
" Clear the history.
function s:Clear(prompt, ...)
  let response = 1
  if a:prompt
    let response = eclim#util#PromptConfirm(
      \ 'Clear local history?', g:EclimInfoHighlight)
  endif

  if response == 1
    let filename = len(a:000) > 0 ? a:000[0] : b:filename
    let current = eclim#project#util#GetProjectRelativeFilePath(filename)
    let project = eclim#project#util#GetCurrentProjectName()
    let command = s:command_clear
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', current, '')
    let result = eclim#ExecuteEclim(command)
    if result == "0"
      return
    endif

    if filename != expand('%:p')
      quit
    endif
    call eclim#util#Echo(result)
  endif
endfunction " }}}

" s:Syntax() {{{
function! s:Syntax()
  set ft=eclim_history
  hi link HistoryFile Identifier
  hi link HistoryCurrentEntry Constant
  syntax match HistoryFile /.*\%1l.*/
  syntax match Comment /^".*/
endfunction " }}}

" s:HighlightEntry(index) {{{
function s:HighlightEntry(index)
  let winnr = winnr()
  if eclim#util#GoToBufferWindow('[History]')
    let b:history_current_entry = a:index
    try
      " forces reset of syntax
      call s:Syntax()
      exec 'syntax match HistoryCurrentEntry /.*\%' . (a:index + 1) . 'l.*/'
    finally
      exec winnr . 'winc w'
    endtry
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
