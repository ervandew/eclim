" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/ruby/index.html
"
" License:
"
" Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
  let s:command_add = '-command ruby_add_interpreter -p "<path>"'
" }}}

function eclim#ruby#interpreter#AddInterpreter(args) " {{{
  let args = eclim#util#ParseCmdLine(a:args)
  if len(args) != 1 && len(args) != 3
    call eclim#util#EchoError(
      \ "You must supply either just the path to the interpreter or\n" .
      \ "-n followed by the name to give to the interpreter followed\n" .
      \ "by the interpreter path.")
    return 0
  endif

  let path = args[-1]
  let path = substitute(path, '\ ', ' ', 'g')
  let path = substitute(path, '\', '/', 'g')

  let command = s:command_add
  let command = substitute(command, '<path>', path, '')
  if args[0] == '-n'
    let name = args[1]
    let command .= ' -n "' . name . '"'
  endif

  let result = eclim#Execute(command)
  if result != '0'
    call eclim#util#Echo(result)
    return 1
  endif
  return 0
endfunction " }}}

function! eclim#ruby#interpreter#CommandCompleteInterpreterPath(argLead, cmdLine, cursorPos) " {{{
  " Custom command completion for ruby interpreter paths.
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')

  let interpreters = eclim#dltk#interpreter#GetInterpreters('ruby')
  call map(interpreters, 'v:val.path')
  if cmdLine !~ '[^\\]\s$'
    call filter(interpreters, 'v:val =~ "^' . argLead . '"')
  endif

  return interpreters
endfunction " }}}

" vim:ft=vim:fdm=marker
