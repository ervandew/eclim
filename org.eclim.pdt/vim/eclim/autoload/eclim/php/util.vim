" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/php/validate.html
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
  let s:update_command = '-command php_src_update -p "<project>" -f "<file>"'
  let s:html_validate_command = '-command html_validate -p "<project>" -f "<file>"'
" }}}

" IsPhpCode(lnum) {{{
" Determines if the code under the cursor is php code (in a php block).
function! eclim#php#util#IsPhpCode(lnum)
  " FIXME: may get confused if either of these occur in a comment.
  "        can fix with searchpos and checking syntax name on result.
  let phpstart = search('<?\(php\|=\)\?', 'bcnW')
  let phpend = search('?>', 'bnW', line('w0'))
  return phpstart > 0 && phpstart <= a:lnum && (phpend == 0 || phpend < phpstart)
endfunction " }}}

" UpdateSrcFile(validate) {{{
" Updates the src file on the server w/ the changes made to the current file.
function! eclim#php#util#UpdateSrcFile(validate)
  let project = eclim#project#util#GetCurrentProjectName()
  if project != ""
    let file = eclim#project#util#GetProjectRelativeFilePath()
    let command = s:update_command
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')
    if (g:EclimPhpValidate || a:validate) && !eclim#util#WillWrittenBufferClose()
      let command = command . ' -v'
      if eclim#project#problems#IsProblemsList()
        let command = command . ' -b'
      endif
    endif
    let result = eclim#ExecuteEclim(command)
    if type(result) != g:LIST_TYPE
      return
    endif

    if (g:EclimPhpHtmlValidate || a:validate) && !eclim#util#WillWrittenBufferClose()
      " html validate
      let command = s:html_validate_command
      let command = substitute(command, '<project>', project, '')
      let command = substitute(command, '<file>', file, '')
      let result_html = eclim#ExecuteEclim(command)
      if type(result_html) == g:LIST_TYPE
        let result += result_html
      endif
    endif

    if type(result) == g:LIST_TYPE && len(result) > 0
      let errors = eclim#util#ParseLocationEntries(
        \ result, g:EclimValidateSortResults)
      call eclim#util#SetLocationList(errors)
    else
      call eclim#util#ClearLocationList()
    endif

    call eclim#project#problems#ProblemsUpdate('save')
  elseif a:validate
    call eclim#project#util#IsCurrentFileInProject()
  endif
endfunction " }}}

" CommandCompleteProject(argLead, cmdLine, cursorPos) {{{
" Custom command completion for project names.
function! eclim#php#util#CommandCompleteProject(argLead, cmdLine, cursorPos)
  return eclim#project#util#CommandCompleteProjectByNature(
    \ a:argLead, a:cmdLine, a:cursorPos, 'php')
endfunction " }}}

" vim:ft=vim:fdm=marker
