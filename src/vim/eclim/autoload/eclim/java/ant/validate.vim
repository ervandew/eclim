" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/ant/validate.html
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
  let s:validate_command =
    \ '-filter vim -command ant_validate -p "<project>" -f "<file>"'
" }}}

" Validate() {{{
" Validates the current ant file.
function! eclim#java#ant#validate#Validate ()
  if !g:EclimAntValidate || eclim#util#WillWrittenBufferClose()
    return
  endif

  " ensure xml validate is OFF.
  augroup eclim_xml
    autocmd! BufWritePost <buffer>
  augroup END

  let project = eclim#project#GetCurrentProjectName()
  " as of now a valid project name is not necessary, but may be later.
  if project == ''
    let project = 'none'
  endif
  if project != ""
    let file = escape(expand("%:p"), '\')
    let command = s:validate_command
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')

    let result = eclim#ExecuteEclim(command)
    if result =~ '|'
      let errors = eclim#util#ParseLocationEntries(split(result, '\n'))
      call eclim#util#SetLocationList(errors)
    else
      call eclim#util#SetLocationList([], 'r')
    endif
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
