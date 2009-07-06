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

" CommandCompleteInterpreterPath(argLead, cmdLine, cursorPos) {{{
" Custom command completion for ruby interpreter paths.
function! eclim#ruby#interpreter#CommandCompleteInterpreterPath(
    \ argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')

  let interpreters = eclim#dltk#interpreter#GetInterpreters('ruby')
  if cmdLine !~ '[^\\]\s$'
    call filter(interpreters, 'v:val =~ "^' . argLead . '"')
  endif

  return interpreters
endfunction " }}}

" vim:ft=vim:fdm=marker
