" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/doc.html
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
let s:command_comment =
  \ '-command javadoc_comment -p "<project>" -f "<file>" -o <offset> -e <encoding>'
" }}}

" Comment() {{{
" Add / update the comments for the element under the cursor.
function! eclim#java#doc#Comment()
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  call eclim#java#util#SilentUpdate()

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#java#util#GetFilename()
  let offset = eclim#util#GetCurrentElementOffset()

  let command = s:command_comment
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<offset>', offset, '')
  let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')

  let result =  eclim#ExecuteEclim(command)

  if result != "0"
    call eclim#util#RefreshFile()
    silent retab
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
