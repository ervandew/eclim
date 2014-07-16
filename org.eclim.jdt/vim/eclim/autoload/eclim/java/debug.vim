" Author:  Kannan Rajah
"
" Description: {{{
"   see http://eclim.org/vim/java/debug.html
"
" License:
"
" Copyright (C) 2005 - 2014  Eric Van Dewoestine
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
let s:command_debug =
  \ '-command java_debug -a "<action>" -n "<target_name>" -c "<connection>"'
let s:command_breakpoint =
  \ '-command java_breakpoint -p "<project>" -f "<file>" -l "<line_num>"'
" }}}

function! eclim#java#debug#Debug(action, connection) " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let file = eclim#lang#SilentUpdate()

  let command = s:command_debug
  let command = substitute(command, '<action>', a:action, '')
  let command = substitute(command, '<target_name>', file, '')
  let command = substitute(command, '<connection>', a:connection, '')

  let result = eclim#Execute(command)
endfunction " }}}

function! eclim#java#debug#Breakpoint() " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#lang#SilentUpdate()
  let line_num = line('.')

  let command = s:command_breakpoint
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<line_num>', line_num, '')

  let result = eclim#Execute(command)
endfunction " }}}

" vim:ft=vim:fdm=marker
