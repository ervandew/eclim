" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/php/validate.html
"
" License:
"
" Copyright (c) 2005 - 2006
"
" Licensed under the Apache License, Version 2.0 (the "License");
" you may not use this file except in compliance with the License.
" You may obtain a copy of the License at
"
"      http://www.apache.org/licenses/LICENSE-2.0
"
" Unless required by applicable law or agreed to in writing, software
" distributed under the License is distributed on an "AS IS" BASIS,
" WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
" See the License for the specific language governing permissions and
" limitations under the License.
"
" }}}

" Script Variables {{{
  let s:update_command =
    \ '-filter vim -command php_src_update -p "<project>" -f "<file>"'
  let s:html_validate_command =
    \ '-filter vim -command html_validate -p "<project>" -f "<file>"'
" }}}

" UpdateSrcFile(validate) {{{
" Updates the src file on the server w/ the changes made to the current file.
function! eclim#php#util#UpdateSrcFile (validate)
  let project = eclim#project#GetCurrentProjectName()
  if project != ""
    let file = eclim#project#GetProjectRelativeFilePath(expand("%:p"))
    let command = s:update_command
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')
    if (g:EclimPhpValidate || a:validate) && !eclim#util#WillWrittenBufferClose()
      let command = command . " -v"
    endif
    let result = eclim#ExecuteEclim(command)

    " html validate
    let project = eclim#project#GetCurrentProjectName()
    let file = eclim#project#GetProjectRelativeFilePath(expand("%:p"))
    let command = s:html_validate_command
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')
    let result .= "\n" . eclim#ExecuteEclim(command)

    if (g:EclimPhpValidate || a:validate) && !eclim#util#WillWrittenBufferClose()
      if result =~ '|'
        let errors = eclim#util#ParseLocationEntries(split(result, '\n'))
        call eclim#util#SetLocationList(errors)
      else
        call eclim#util#SetLocationList([], 'r')
      endif
    endif
  endif
endfunction " }}}

" CommandCompleteProject(argLead, cmdLine, cursorPos) {{{
" Custom command completion for project names.
function! eclim#php#util#CommandCompleteProject (argLead, cmdLine, cursorPos)
  return eclim#project#CommandCompleteProjectByNature(
    \ a:argLead, a:cmdLine, a:cursorPos, 'php')
endfunction " }}}

" vim:ft=vim:fdm=marker
