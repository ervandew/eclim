" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/xml/format.html
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

" Script Variables {{{
  let s:command_format =
    \ '-command xml_format -f "<file>" -w <width> -i <indent> -m <ff>'
" }}}

function! eclim#xml#format#Format() " {{{
  call eclim#util#ExecWithoutAutocmds('update')
  let file = substitute(expand('%:p'), '\', '/', 'g')
  if has('win32unix')
    let file = eclim#cygwin#WindowsPath(file)
  endif

  let command = s:command_format
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<width>', &textwidth, '')
  let command = substitute(command, '<indent>', &shiftwidth, '')
  let command = substitute(command, '<ff>', &ff, '')

  let result = eclim#Execute(command)
  if result != '0'
    silent! 1,$delete _
    silent put =result
    silent! 1,1delete _
  endif
endfunction " }}}

function! s:SelectOuterTag(count) " {{{
  let pos = getpos('.')

  exec 'silent! normal! v' . a:count . 'atv'
  call setpos('.', pos)

  return s:VisualSelectionMap()
endfunction " }}}

function! s:SelectInnerTag() " {{{
  silent! normal! vit
  normal! v
  call cursor(line("'<"), col("'<"))

  return s:VisualSelectionMap()
endfunction " }}}

function! s:VisualSelectionMap() " {{{
  let lstart = line("'<")
  let cstart = col("'<")
  let lend = line("'>")
  let cend = col("'>")

  if cstart > len(getline(lstart))
    let lstart += 1
    let cstart = 1
  endif

  if strpart(getline(lend), 0, cend) =~ '^\s*$'
    let lend -= 1
    let cend = len(getline(lend))
  endif

  return {'lstart': lstart, 'cstart': cstart, 'lend': lend, 'cend': cend}
endfunction " }}}

function! s:InsertCr(line, col) " {{{
  call cursor(a:line, a:col)
  exec "normal! i\<cr>\<esc>"
endfunction " }}}

function! s:GetRootLine() " {{{
  let pos = getpos('.')

  let line = 1
  call cursor(1, 1)
  while getline('.') !~ '<\w'
    let line = line('.') + 1
    if line > line('$')
      break
    endif
    call cursor(line, 1)
  endwhile

  call setpos('.', pos)
  return line
endfunction " }}}

" vim:ft=vim:fdm=marker
