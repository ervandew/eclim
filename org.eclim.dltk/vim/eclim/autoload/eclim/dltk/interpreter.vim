" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Functions to manage interpreters for a dltk based project.
"
" License:
"
" Copyright (C) 2005 - 2020  Eric Van Dewoestine
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
  let s:command_interpreters = '-command dltk_interpreters -l <nature>'
  let s:command_interpreter_addremove =
    \ '-command dltk_<action>_interpreter -l <nature> -p "<path>"'
" }}}

function eclim#dltk#interpreter#GetInterpreters(nature) " {{{
  let command = s:command_interpreters
  let command = substitute(command, '<nature>', a:nature, '')
  let interpreters = eclim#Execute(command)
  if type(interpreters) != g:LIST_TYPE || len(interpreters) == 0
    return []
  endif

  return interpreters
endfunction " }}}

function eclim#dltk#interpreter#ListInterpreters(nature) " {{{
  let command = s:command_interpreters
  let command = substitute(command, '<nature>', a:nature, '')
  let interpreters = eclim#Execute(command)
  if type(interpreters) != g:LIST_TYPE
    return
  endif
  if len(interpreters) == 0
    call eclim#util#Echo("No interpreters.")
  endif

  let pad = 0
  for interpreter in interpreters
    if interpreter.default
      let interpreter.name .= ' (default)'
    endif
    let pad = len(interpreter.name) > pad ? len(interpreter.name) : pad
  endfor

  let output = []
  let nature = ''
  for interpreter in interpreters
    if interpreter.nature != nature
      let nature = interpreter.nature
      call add(output, 'Nature: ' . interpreter.nature)
    endif
    let name = interpreter.name
    if interpreter.default
      let name .= ' (default)'
    endif
    let name = eclim#util#Pad(interpreter.name, pad)
    call add(output, '  ' . name . ' - ' . interpreter.path)
  endfor
  call eclim#util#Echo(join(output, "\n"))
endfunction " }}}

function eclim#dltk#interpreter#AddInterpreter(nature, type, path) " {{{
  return s:InterpreterAddRemove(a:nature, a:type, a:path, 'add')
endfunction " }}}

function eclim#dltk#interpreter#RemoveInterpreter(nature, path) " {{{
  return s:InterpreterAddRemove(a:nature, '', a:path, 'remove')
endfunction " }}}

function s:InterpreterAddRemove(nature, type, path, action) " {{{
  let path = a:path
  let path = substitute(path, '\ ', ' ', 'g')
  let path = substitute(path, '\', '/', 'g')
  let command = s:command_interpreter_addremove
  let command = substitute(command, '<action>', a:action, '')
  let command = substitute(command, '<nature>', a:nature, '')
  let command = substitute(command, '<path>', path, '')
  if a:action == 'add'
    let command .= ' -t ' . a:type
  endif
  let result = eclim#Execute(command)
  if result != '0'
    call eclim#util#Echo(result)
    return 1
  endif
  return 0
endfunction " }}}

function! eclim#dltk#interpreter#CommandCompleteInterpreterAdd(argLead, cmdLine, cursorPos) " {{{
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)[1:]
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]

  if argLead == '-' && args[0] == '-'
    return ['-n']
  endif

  if len(args) == 0 ||
   \ len(args) == 3 ||
   \ (len(args) == 1 && argLead !~ '^-\|^$') ||
   \ (len(args) == 2 && argLead == '')
    return eclim#util#CommandCompleteFile(a:argLead, a:cmdLine, a:cursorPos)
  endif

  return []
endfunction " }}}

" vim:ft=vim:fdm=marker
