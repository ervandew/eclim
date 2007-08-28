" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/bean.html
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

" Global Variables {{{
if !exists("g:EclimJavaBeanInsertIndexed")
  let g:EclimJavaBeanInsertIndexed = 1
endif
" }}}

" Script Variables {{{
let s:command_properties =
  \ '-command java_bean_properties -p "<project>" -f "<file>" ' .
  \ '-o <offset> -t <type> -r <properties> <indexed>'

let s:no_properties =
  \ 'Unable to find property at current cursor position: ' .
  \ 'Not on a field declaration or possible java syntax error.'
" }}}

" GetterSetter(first, last, type) {{{
function! eclim#java#bean#GetterSetter (first, last, type)
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  call eclim#java#util#SilentUpdate()

  let project = eclim#project#util#GetCurrentProjectName()
  let properties = eclim#java#util#GetSelectedFields(a:first, a:last)

  if len(properties) == 0
    call eclim#util#EchoError (s:no_properties)
    return
  endif

  let indexed = g:EclimJavaBeanInsertIndexed ? '-i' : ''

  let command = s:command_properties
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', eclim#java#util#GetFilename(), '')
  let command = substitute(command, '<offset>', eclim#util#GetCharacterOffset(), '')
  let command = substitute(command, '<type>', a:type, '')
  let command = substitute(command, '<properties>', join(properties, ','), '')
  let command = substitute(command, '<indexed>', indexed, '')

  let result = eclim#ExecuteEclim(command)
  if result != "0"
    call eclim#util#RefreshFile()
    silent retab
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
