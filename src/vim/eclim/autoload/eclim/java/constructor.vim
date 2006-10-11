" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/constructor.html
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
let s:command_properties =
  \ '-filter vim -command java_constructor -p "<project>" -f "<file>" ' .
  \ '-o <offset>'
" }}}

" Constructor(first, last) {{{
function! eclim#java#constructor#Constructor (first, last)
  if !eclim#project#IsCurrentFileInProject()
    return
  endif

  call eclim#java#util#SilentUpdate()

  let project = eclim#project#GetCurrentProjectName()
  let properties = a:last == 1 ? [] :
    \ eclim#java#util#GetSelectedFields(a:first, a:last)

  let command = s:command_properties
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', eclim#java#util#GetFilename(), '')
  let command = substitute(command, '<offset>', eclim#util#GetCharacterOffset(), '')
  if len(properties) > 0
    let command = command . ' -r ' . join(properties, ',')
  endif

  let result = eclim#ExecuteEclim(command)
  if result != "0"
    call eclim#util#RefreshFile()
    silent retab
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
