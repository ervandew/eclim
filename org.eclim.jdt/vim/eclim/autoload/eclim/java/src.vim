" Author:  Eric Van Dewoestine
"
" License: " {{{
"
" Copyright (C) 2005 - 2012  Eric Van Dewoestine
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
  let s:format_command =
    \ '-command java_format -p "<project>" -f "<file>" ' .
    \ '-h <hoffset> -t <toffset> -e <encoding>'
  let s:checkstyle_command = '-command java_checkstyle -p "<project>" -f "<file>"'
" }}}

function! eclim#java#src#Format(first, last) " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  call eclim#lang#SilentUpdate()

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()

  let command = s:format_command
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let begin = eclim#util#GetOffset(a:first, 1)
  let end = eclim#util#GetOffset(a:last, 1) + len(getline(a:last)) - 1
  let command = substitute(command, '<hoffset>', begin, '')
  let command = substitute(command, '<toffset>', end, '')
  let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')

  let result = eclim#ExecuteEclim(command)
  if result != "0"
    call eclim#util#Reload({'retab': 1})
    write
  endif
endfunction " }}}

function! eclim#java#src#Checkstyle() " {{{
  let project = eclim#project#util#GetCurrentProjectName()
  if project != ""
    let config =
      \ eclim#project#util#GetProjectSetting('org.eclim.java.checkstyle.config')
    if type(config) == g:NUMBER_TYPE
      return
    endif

    if config == ''
      call eclim#util#EchoWarning(
        \ "Before invokeing checkstyle, you must first configure the " .
        \ "location of your\ncheckstyle config via the setting:  " .
        \ "'org.eclim.java.checkstyle.config'.")
      return
    endif

    let file = eclim#project#util#GetProjectRelativeFilePath()
    let command = s:checkstyle_command
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')

    let result = eclim#ExecuteEclim(command)
    if type(result) == g:LIST_TYPE && len(result) > 0
      let errors = eclim#util#ParseLocationEntries(
        \ result, g:EclimValidateSortResults)
      call eclim#util#SetLocationList(errors)
    else
      call eclim#util#ClearLocationList('checkstyle')
    endif
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
