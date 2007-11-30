" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
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
if !exists("g:EclimLicenseDir")
  let g:EclimLicenseDir = g:EclimBaseDir . '/license'
endif
if !exists("g:Author")
  let g:Author = ''
endif
" }}}

" Script Variables {{{
  let s:year = exists('*strftime') ? strftime('%Y') : '2007'
" }}}

" License (name, pre, post, mid) {{{
" Retrieves the license given the supplied name and applies the specified
" prefix and postfix as the lines before and after the license and uses 'mid'
" as the prefix for every line.
" Returns the license as a list of strings.
function! eclim#common#license#License (name, pre, post, mid)
  let file = g:EclimLicenseDir . '/' . a:name . '.license'
  if !filereadable(file)
    return ''
  endif

  let contents = readfile(file)
  call map(contents, 'a:mid . v:val')

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

" LicenseForFile (name, pre, post, mid) {{{
" Retrieves the license that should be used for the given file.
" Determines the license by taking the file's path and checking if any of the
" licenses have a name matching one of the directories in that path.
function! eclim#common#license#LicenseForFile (file, pre, post, mid)
  let path = fnamemodify(a:file, ':p:h')
  let licenses = split(globpath(g:EclimLicenseDir, '*.license'), '\n')
  for license in licenses
    let name = fnamemodify(license, ':t:r')
    if path =~ name
      return eclim#common#license#License(name, a:pre, a:post, a:mid)
    endif
  endfor
  return ''
endfunction " }}}

" vim:ft=vim:fdm=marker
