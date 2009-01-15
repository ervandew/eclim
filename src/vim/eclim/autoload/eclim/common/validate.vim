" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Shared functionality.
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

" Script Variables {{{
  let s:validate_command = '-command <type>_validate -p "<project>" -f "<file>"'
" }}}

" Validate(type, on_save) {{{
" Validates the current file.
function! eclim#common#validate#Validate(type, on_save)
  if eclim#util#WillWrittenBufferClose()
    return
  endif

  if !eclim#project#util#IsCurrentFileInProject(!a:on_save)
    return
  endif

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath(expand("%:p"))
  let command = s:validate_command
  let command = substitute(command, '<type>', a:type, '')
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')

  let result = eclim#ExecuteEclim(command)
  if result =~ '|'
    let errors = eclim#util#ParseLocationEntries(split(result, '\n'))
    call eclim#util#SetLocationList(errors)
  else
    call eclim#util#ClearLocationList()
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
