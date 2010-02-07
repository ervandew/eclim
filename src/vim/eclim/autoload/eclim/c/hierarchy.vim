" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/c/hierarchy.html
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
if !exists('g:EclimCHierarchyDefaultAction')
  let g:EclimCHierarchyDefaultAction = g:EclimDefaultFileOpenAction
endif
" }}}

" Script Varables {{{
  let s:call_hierarchy =
    \ '-command c_callhierarchy -p "<project>" -f "<file>" ' .
    \ '-o <offset> -l <length> -e <encoding>'
" }}}

" CallHierarchy() {{{
function! eclim#c#hierarchy#CallHierarchy()
  if !eclim#project#util#IsCurrentFileInProject(1)
    return
  endif

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()
  let position = eclim#util#GetCurrentElementPosition()
  let offset = substitute(position, '\(.*\);\(.*\)', '\1', '')
  let length = substitute(position, '\(.*\);\(.*\)', '\2', '')
  let command = s:call_hierarchy
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<offset>', offset, '')
  let command = substitute(command, '<length>', length, '')
  let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')

  let results = split(eclim#ExecuteEclim(command), '\n')
  if len(results) == 1 && results[0] == '0'
    return
  endif

  if len(results) == 0
    call eclim#util#Echo('No results found.')
    return
  endif

  let lines = []
  let info = []
  for result in results
    if result =~ '.*|.*|.*'
      let file = substitute(result, '\s*\(.\{-}\)|.*', '\1', '')
      let line = substitute(result, '.\{-}|\(\d\+\) col.*', '\1', '')
      let col = substitute(result, '.\{-}|\d\+ col \(\d\+\)|.*', '\1', '')
      let name = substitute(result, '\(\s*\).\{-}|\d\+ col \d\+|\(.*\)', '\1\2', '')
      call add(info, {'file': file, 'line': line, 'col': col})
      call add(lines, name)
    else
      call add(info, {'file': '', 'line': -1, 'col': -1})
      call add(lines, result)
    endif
  endfor

  call eclim#util#TempWindow('[Call Hierarchy]', lines)
  set ft=c

  setlocal modifiable noreadonly
  call append(line('$'), ['', '" use ? to view help'])
  setlocal nomodifiable readonly
  syntax match Comment /^".*/

  let b:hierarchy_info = info

  nnoremap <buffer> <silent> <cr>
    \ :call <SID>Open(g:EclimCHierarchyDefaultAction)<cr>
  nnoremap <buffer> <silent> E :call <SID>Open('edit')<cr>
  nnoremap <buffer> <silent> S :call <SID>Open('split')<cr>
  nnoremap <buffer> <silent> T :call <SID>Open("tablast \| tabnew")<cr>

  " assign to buffer var to get around weird vim issue passing list containing
  " a string w/ a '<' in it on execution of mapping.
  let b:hierarchy_help = [
      \ '<cr> - open file with default action',
      \ 'E - open with :edit',
      \ 'S - open in a new split window',
      \ 'T - open in a new tab',
    \ ]
  nnoremap <buffer> <silent> ?
    \ :call eclim#help#BufferHelp(b:hierarchy_help, 'vertical', 40)<cr>
endfunction " }}}

" s:Open(action) {{{
function! s:Open(action)
  let line = line('.')
  if line > len(b:hierarchy_info)
    return
  endif

  let info = b:hierarchy_info[line - 1]
  if info.file != ''
    " go to the buffer that initiated the hierarchy
    exec b:winnr . 'winc w'

    let action = a:action
    call eclim#util#GoToBufferWindowOrOpen(info.file, action)
    call cursor(info.line, info.col)

   " force any previous messge from else below to be cleared
    echo ''
  else
    call eclim#util#Echo('No associated file was found.')
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
