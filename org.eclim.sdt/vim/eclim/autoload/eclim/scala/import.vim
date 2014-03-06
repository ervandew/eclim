" Author:  Fangmin Lv
" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2014  Eric Van Dewoestine
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

" Script Varables {{{
let s:command_import =
    \ '-command scala_import -p "<project>" -f "<file>" ' .
    \ '-o <offset> -e <encoding>'
" }}}

function! eclim#scala#import#Import(...) " {{{
  if !eclim#project#util#IsCurrentFileInProject(0)
    return
  endif

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()
  let offset = eclim#util#GetOffset()
  let encoding = eclim#util#GetEncoding()
  let type = a:0 ? a:1 : expand('<cword>')

  let command = s:command_import
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<offset>', offset, '')
  let command = substitute(command, '<encoding>', encoding, '')
  if a:0
    let command .= ' -t ' . a:1
  endif

  let result = eclim#Execute(command)

  if type(result) == g:STRING_TYPE
    call eclim#util#EchoError(result)
    return
  endif

  if type(result) == g:DICT_TYPE
    call eclim#util#Reload({'pos': [result.line, result.column]})
    call eclim#lang#UpdateSrcFile('scala', 1)
    if result.offset != offset
      call eclim#util#Echo('Imported ' . type)
    endif
    return
  endif

  if type(result) != g:LIST_TYPE
    return
  endif

  let choice = eclim#java#import#ImportPrompt(result)
  if choice != ''
    call eclim#scala#import#Import(choice)
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
