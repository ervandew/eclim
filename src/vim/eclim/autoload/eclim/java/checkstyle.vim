" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Plugin to support running checkstyle on a java source file.
"
" License:
"
" Copyright (C) 2005 - 2010  Eric Van Dewoestine
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
  let s:checkstyle_command = '-command java_checkstyle -p "<project>" -f "<file>"'
" }}}

" Checkstyle() {{{
" Executes checkstyle on the current java source file.
function! eclim#java#checkstyle#Checkstyle()
  let project = eclim#project#util#GetCurrentProjectName()
  if project != ""
    let config =
      \ eclim#project#util#GetProjectSetting('org.eclim.java.checkstyle.config')
    if type(config) == 0
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
    if result =~ '|'
      let errors = eclim#util#ParseLocationEntries(
        \ split(result, '\n'), g:EclimValidateSortResults)
      for error in errors
        let error["text"] = "[checkstyle] " . error.text
      endfor
      call eclim#util#SetLocationList(errors)
    else
      call eclim#util#ClearLocationList('checkstyle')
    endif
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
