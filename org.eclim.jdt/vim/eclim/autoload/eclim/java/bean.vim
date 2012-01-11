" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/bean.html
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

" Global Variables {{{
if !exists("g:EclimJavaBeanInsertIndexed")
  let g:EclimJavaBeanInsertIndexed = 1
endif
" }}}

" Script Variables {{{
let s:command_properties =
  \ '-command java_bean_properties -p "<project>" -f "<file>" ' .
  \ '-o <offset> -e <encoding> -t <type> -r <properties> <indexed>'

let s:no_properties =
  \ 'Unable to find property at current cursor position: ' .
  \ 'Not on a field declaration or possible java syntax error.'
" }}}

" GetterSetter(first, last, type) {{{
function! eclim#java#bean#GetterSetter(first, last, type)
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  call eclim#lang#SilentUpdate()

  let properties = eclim#java#util#GetSelectedFields(a:first, a:last)

  if len(properties) == 0
    call eclim#util#EchoError(s:no_properties)
    return
  endif

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()
  let indexed = g:EclimJavaBeanInsertIndexed ? '-i' : ''

  let command = s:command_properties
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<offset>', eclim#util#GetOffset(), '')
  let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')
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
