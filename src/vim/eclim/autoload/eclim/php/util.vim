" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/php/validate.html
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
  let s:update_command = '-command php_src_update -p "<project>" -f "<file>"'
  let s:html_validate_command = '-command html_validate -p "<project>" -f "<file>"'
" }}}

" UpdateSrcFile(validate) {{{
" Updates the src file on the server w/ the changes made to the current file.
function! eclim#php#util#UpdateSrcFile(validate)
  let project = eclim#project#util#GetCurrentProjectName()
  if project != ""
    let file = eclim#project#util#GetProjectRelativeFilePath(expand("%:p"))
    let command = s:update_command
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')
    if (g:EclimPhpValidate || a:validate) && !eclim#util#WillWrittenBufferClose()
      let command = command . " -v"
    endif
    let result = eclim#ExecuteEclim(command)

    if (g:EclimPhpValidate || a:validate) && !eclim#util#WillWrittenBufferClose()
      " html validate
      let command = s:html_validate_command
      let command = substitute(command, '<project>', project, '')
      let command = substitute(command, '<file>', file, '')
      let result .= "\n" . eclim#ExecuteEclim(command)

      if result =~ '|'
        let errors = eclim#util#ParseLocationEntries(split(result, '\n'))
        call eclim#util#SetLocationList(errors)
      else
        call eclim#util#ClearLocationList()
      endif
    endif
  endif
endfunction " }}}

" CommandCompleteProject(argLead, cmdLine, cursorPos) {{{
" Custom command completion for project names.
function! eclim#php#util#CommandCompleteProject(argLead, cmdLine, cursorPos)
  return eclim#project#util#CommandCompleteProjectByNature(
    \ a:argLead, a:cmdLine, a:cursorPos, 'php')
endfunction " }}}

" vim:ft=vim:fdm=marker
