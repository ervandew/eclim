" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/ruby/index.html
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
  let s:command_interpreters = '-command dltk_interpreters -n <nature>'
  let s:command_interpreter_addremove =
    \ '-command dltk_<action>_interpreter -n <nature> -t <type> -i "<path>"'
" }}}

" GetInterpreters(nature) {{{
function eclim#dltk#interpreters#GetInterpreters(nature)
  let command = s:command_interpreters
  let command = substitute(command, '<nature>', a:nature, '')
  let interpreters = split(eclim#ExecuteEclim(command), '\n')
  if len(interpreters) == 0 || (len(interpreters) == 1 && interpreters[0] == '0')
    return []
  endif

  call filter(interpreters, 'v:val =~ ".* - "')
  call map(interpreters, 'substitute(v:val, ".\\{-} - \\(.*\\)", "\\1", "")')
  return interpreters
endfunction " }}}

" ListInterpreters(nature) {{{
function eclim#dltk#interpreters#ListInterpreters(nature)
  let command = s:command_interpreters
  let command = substitute(command, '<nature>', a:nature, '')
  let interpreters = split(eclim#ExecuteEclim(command), '\n')
  if len(interpreters) == 0
    call eclim#util#Echo("No interpreters.")
  endif
  if len(interpreters) == 1 && interpreters[0] == '0'
    return
  endif
  call eclim#util#Echo(join(interpreters, "\n"))
endfunction " }}}

" AddInterpreter(nature, type, path) {{{
function eclim#dltk#interpreters#AddInterpreter(nature, type, path)
  return s:InterpreterAddRemove(a:nature, a:type, a:path, 'add')
endfunction " }}}

" RemoveInterpreter(nature, type, path) {{{
function eclim#dltk#interpreters#RemoveInterpreter(nature, type, path)
  return s:InterpreterAddRemove(a:nature, a:type, a:path, 'remove')
endfunction " }}}

" s:InterpreterAddRemove(nature, type, path, action) {{{
function s:InterpreterAddRemove(nature, type, path, action)
  let command = s:command_interpreter_addremove
  let command = substitute(command, '<action>', a:action, '')
  let command = substitute(command, '<nature>', a:nature, '')
  let command = substitute(command, '<type>', a:type, '')
  let command = substitute(command, '<path>', a:path, '')
  let result = eclim#ExecuteEclim(command)
  if result != '0'
    call eclim#util#Echo(result)
    return 1
  endif
  return 0
endfunction " }}}

" vim:ft=vim:fdm=marker
