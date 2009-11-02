" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/classpath.html
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
  let s:update_command = '-command project_update -p "<project>" -b "<build>"'
" }}}

" SetRepo(path) {{{
" Sets the location of the ivy repository.
function! eclim#java#ivy#SetRepo(path)
  call eclim#java#classpath#VariableCreate('IVY_REPO', a:path)
endfunction " }}}

" UpdateClasspath() {{{
" Updates the classpath on the server w/ the changes made to the current file.
function! eclim#java#ivy#UpdateClasspath()
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  " validate the xml first
  if eclim#xml#validate#Validate(expand('%:p'), 0)
    return
  endif

  let name = eclim#project#util#GetCurrentProjectName()
  let command = s:update_command
  let command = substitute(command, '<project>', name, '')
  let command = substitute(command, '<build>', escape(expand('%:p'), '\'), '')
  let result = eclim#ExecuteEclim(command)

  if result =~ '|'
    let errors = eclim#util#ParseLocationEntries(
      \ split(result, '\n'), g:EclimValidateSortResults)
    call eclim#util#SetLocationList(errors, 'r')
    call eclim#util#EchoError(
      \ "Operation contained errors.  See location list for details (:lopen).")
  else
    call eclim#util#ClearLocationList()
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
