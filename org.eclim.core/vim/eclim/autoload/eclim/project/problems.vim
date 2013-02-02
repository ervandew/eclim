" Author:  Eric Van Dewoestine
"
" Description: {{{
"
" License:
"
" Copyright (C) 2005 - 2013  Eric Van Dewoestine
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

" Global variables {{{
  if !exists('g:EclimProblemsQuickFixOpen')
    let g:EclimProblemsQuickFixOpen = 'botright copen'
  endif
" }}}

" Script variables {{{
  let s:problems_command = '-command problems -p "<project>"'
" }}}

function! eclim#project#problems#Problems(project, open, bang) " {{{
  let project = a:project
  if project == ''
    let project = eclim#project#util#GetCurrentProjectName()
  endif
  if project == ''
    call eclim#project#util#UnableToDetermineProject()
    return
  endif

  let command = s:problems_command
  let command = substitute(command, '<project>', project, '')
  if a:bang != ""
    let command .= ' -e'
  endif
  let result = eclim#Execute(command)
  let errors = []
  if type(result) == g:LIST_TYPE && len(result) > 0
    let errors = eclim#util#ParseLocationEntries(
      \ result, g:EclimValidateSortResults)
  endif

  let action = eclim#project#problems#IsProblemsList() ? 'r' : ' '
  call eclim#util#SetQuickfixList(errors, action)

  " generate a 'signature' to distinguish the problems list from other qf
  " lists.
  let s:eclim_problems_sig = s:QuickfixSignature()
  let s:eclim_problems_bang = a:bang

  if a:open
    exec g:EclimProblemsQuickFixOpen
  endif
endfunction " }}}

function! eclim#project#problems#ProblemsUpdate(action) " {{{
  if a:action == 'save' && !g:EclimProjectProblemsUpdateOnSave
    return
  endif

  if a:action == 'build' && !g:EclimProjectProblemsUpdateOnBuild
    return
  endif

  if !eclim#project#problems#IsProblemsList()
    return
  endif

  " preserve the cursor position in the quickfix window
  let qf_winnr = 0
  let index = 1
  while index <= winnr('$')
    if getbufvar(winbufnr(index), '&ft') == 'qf'
      let cur = winnr()
      let qf_winnr = index
      exec qf_winnr . 'winc w'
      let pos = getpos('.')
      exec cur . 'winc w'
      break
    endif
    let index += 1
  endwhile

  let bang = exists('s:eclim_problems_bang') ? s:eclim_problems_bang : ''
  call eclim#project#problems#Problems('', 0, bang)

  " restore the cursor position
  if qf_winnr
    let cur = winnr()
    exec qf_winnr . 'winc w'
    call setpos('.', pos)
    redraw
    exec cur . 'winc w'
  endif
endfunction " }}}

function! eclim#project#problems#IsProblemsList() " {{{
  " if available, compare the problems signature against the signature of
  " the current list to see if we are now on the problems list, probably via
  " :colder or :cnewer.
  if exists('s:eclim_problems_sig')
    return s:QuickfixSignature() == s:eclim_problems_sig
  endif
  if exists('s:eclim_problems_bang')
    unlet s:eclim_problems_bang
  endif
  return 0
endfunction " }}}

function! s:QuickfixSignature() " {{{
  let qflist = getqflist()
  let len = len(qflist)
  return {
      \ 'len': len,
      \ 'first': len > 0 ? (qflist[0]['bufnr'] . ':' . qflist[0]['text']) : '',
      \ 'last': len > 0 ? (qflist[-1]['bufnr'] . ':' . qflist[-1]['text']) : ''
    \ }
endfunction " }}}

" vim:ft=vim:fdm=marker
