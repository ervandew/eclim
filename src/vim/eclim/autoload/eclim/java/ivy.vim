" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/classpath.html
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
  let s:update_command = '-command project_update -p "<project>" -b "<build>"'
" }}}

" SetRepo(path) {{{
" Sets the location of the ivy repository.
function! eclim#java#ivy#SetRepo (path)
  call eclim#java#classpath#VariableCreate('IVY_REPO', a:path)
endfunction " }}}

" UpdateClasspath() {{{
" Updates the classpath on the server w/ the changes made to the current file.
function! eclim#java#ivy#UpdateClasspath ()
  if !eclim#project#IsCurrentFileInProject()
    return
  endif

  " validate the xml first
  if eclim#xml#Validate(expand('%:p'), 0)
    return
  endif

  let name = eclim#project#GetCurrentProjectName()
  let command = s:update_command
  let command = substitute(command, '<project>', name, '')
  let command = substitute(command, '<build>', escape(expand('%:p'), '\'), '')
  let result = eclim#ExecuteEclim(command)

  if result =~ '|'
    call eclim#util#SetLocationList
      \ (eclim#util#ParseLocationEntries(split(result, '\n')), 'r')
    call eclim#util#EchoError
      \ ("Operation contained errors.  See location list for details (:lopen).")
  else
    call eclim#util#SetLocationList([], 'r')
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
