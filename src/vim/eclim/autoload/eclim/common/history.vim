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
  let file = eclim#java#util#GetFilename()
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
  let file = eclim#java#util#GetFilename()
  let command = s:command_list
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let result = eclim#ExecuteEclim(command)
  if result == "0"
    return
  endif

  let history = eval(result)
  let lines = [file]
  let revisions = [0]
  for rev in history
    call add(lines, g:EclimIndent . rev.datetime . ' (' . rev.delta . ')')
    call add(revisions, rev.timestamp)
  endfor
  call add(lines, '')
  call add(lines, 'v: view  d: diff  r: revert  c: clear')
  call eclim#util#TempWindow('[History]', lines)

  let b:history_revisions = revisions

  noremap <buffer> <silent> v :call <SID>View()<cr>
  noremap <buffer> <silent> d :call <SID>Diff()<cr>
  noremap <buffer> <silent> r :call <SID>Revert()<cr>
  noremap <buffer> <silent> c :call <SID>Clear(1)<cr>
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
  let revision = b:history_revisions[line('.') - 1]
  if eclim#util#GoToBufferWindow(current)
    let filetype = &ft
    let project = eclim#project#util#GetCurrentProjectName()
    let file = eclim#java#util#GetFilename()
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

    let saved = @"
    let @" = result
    silent 1,$delete _
    silent put "
    silent 1,1delete _
    let @" = saved

    exec 'setlocal filetype=' . filetype
    setlocal nomodified
    setlocal readonly
    setlocal nomodifiable
    setlocal noswapfile
    setlocal nobuflisted
    setlocal buftype=nofile
    setlocal bufhidden=delete
    doautocmd BufReadPost

    return 1
  endif
endfunction " }}}

" s:Diff() {{{
" Diff the contents of the revision under the cursor against the current
" contents.
function s:Diff()
  let current = b:filename
  let orien = g:EclimHistoryDiffOrientation == 'horizontal' ? '' : 'vertical '
  if s:View(orien . 'split')
    diffthis
    augroup history_diff
      autocmd! BufUnload <buffer>
      call eclim#util#GoToBufferWindowRegister(current)
      autocmd BufUnload <buffer> diffoff
    augroup END

    call eclim#util#GoToBufferWindow(current)
    diffthis
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
    let file = eclim#java#util#GetFilename()
    let command = s:command_revision
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')
    let command = substitute(command, '<revision>', revision, '')
    let result = eclim#ExecuteEclim(command)
    if result == "0"
      return
    endif

    let saved = @"
    let @" = result
    silent 1,$delete _
    silent put "
    silent 1,1delete _
    let @" = saved
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

" vim:ft=vim:fdm=marker
