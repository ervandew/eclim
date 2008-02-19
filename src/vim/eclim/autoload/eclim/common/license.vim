" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"
" License:
"
" Copyright (c) 2005 - 2008
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
if !exists("g:Author")
  let g:Author = ''
endif
" }}}

" Script Variables {{{
  let s:year = exists('*strftime') ? strftime('%Y') : '2008'
" }}}

" GetLicense () {{{
" Retrieves the file containing the license text.
function! eclim#common#license#GetLicense ()
  let file = eclim#project#util#GetProjectSetting('org.eclim.project.copyright')
  if file == '0'
    return
  endif
  if file == ''
    call eclim#util#EchoWarning(
      \ "Project setting 'org.eclim.project.copyright' has not been supplied.")
    return
  elseif type(file) == 0 && file == 0
    return
  endif

  let file = eclim#project#util#GetCurrentProjectRoot() . '/' . file
  if !filereadable(file)
    return
  endif
  return file
endfunction " }}}

" License (name, pre, post, mid) {{{
" Retrieves the license given the supplied name and applies the specified
" prefix and postfix as the lines before and after the license and uses 'mid'
" as the prefix for every line.
" Returns the license as a list of strings.
function! eclim#common#license#License (pre, post, mid)
  let file = eclim#common#license#GetLicense()
  if type(file) == 0 && file == 0
    return ''
  endif

  let contents = readfile(file)
  if a:mid != ''
    call map(contents, 'a:mid . v:val')
  endif

  if a:pre != ''
    call insert(contents, a:pre)
  endif

  if a:post != ''
    call add(contents, a:post)
  endif

  call map(contents, "substitute(v:val, '${year}', s:year, 'g')")
  call map(contents, "substitute(v:val, '${author}', g:Author, 'g')")
  call map(contents, "substitute(v:val, '\\s\\+$', '', '')")

  return contents
endfunction " }}}

" vim:ft=vim:fdm=marker
