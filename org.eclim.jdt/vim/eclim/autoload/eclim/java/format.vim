" Author:  Anton Sharonov
" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/format.html
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

" Script Variables {{{
let s:command_properties =
  \ '-command java_format -p "<project>" -f "<file>" ' .
  \ '-b <boffset> -e <eoffset>'
" }}}

" eclim#java#format#Format(first, last, typeDummy) {{{
function! eclim#java#format#Format(first, last, typeDummy)
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  call eclim#lang#SilentUpdate()

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()

  let command = s:command_properties
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let begin = eclim#util#GetOffset(a:first, 1)
  let end = eclim#util#GetOffset(a:last, 1) + len(getline(a:last)) - 1
  let command = substitute(command, '<boffset>', begin, '')
  let command = substitute(command, '<eoffset>', end, '')

  let result = eclim#ExecuteEclim(command)
  if result != "0"
    call eclim#util#RefreshFile()
    silent retab
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
