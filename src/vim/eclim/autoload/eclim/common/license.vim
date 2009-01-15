" Author:  Eric Van Dewoestine
"
" Description: {{{
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
  let s:year = exists('*strftime') ? strftime('%Y') : '2009'
" }}}

" GetLicense() {{{
" Retrieves the file containing the license text.
function! eclim#common#license#GetLicense()
  let file = eclim#project#util#GetProjectSetting('org.eclim.project.copyright')
  if type(file) == 0
    return
  elseif file == ''
    call eclim#util#EchoWarning(
      \ "Project setting 'org.eclim.project.copyright' has not been supplied.")
    return
  endif

  let file = eclim#project#util#GetCurrentProjectRoot() . '/' . file
  if !filereadable(file)
    return
  endif
  return file
endfunction " }}}

" License(pre, post, mid) {{{
" Retrieves the license configured license and applies the specified prefix
" and postfix as the lines before and after the license and uses 'mid' as the
" prefix for every line.
" Returns the license as a list of strings.
function! eclim#common#license#License(pre, post, mid)
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

  let author = eclim#project#util#GetProjectSetting('org.eclim.user.name')
  if type(author) != 0 && author != ''
    call map(contents, "substitute(v:val, '${author}', author, 'g')")
  endif

  let email = eclim#project#util#GetProjectSetting('org.eclim.user.email')
  if type(email) != 0 && email != ''
    call map(contents, "substitute(v:val, '${email}', email, 'g')")
  endif
  call map(contents, "substitute(v:val, '\\s\\+$', '', '')")

  return contents
endfunction " }}}

" vim:ft=vim:fdm=marker
