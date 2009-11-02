" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/regex.html
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
  let s:command_regex = '-command java_regex -f "<file>"'
" }}}

" Evaluate(file) {{{
function eclim#java#regex#Evaluate(file)
  let command = s:command_regex
  let command = substitute(command, '<file>', a:file, '')
  if exists('b:eclim_regex_type')
    let command .= ' -t ' . b:eclim_regex_type
  endif
  return eclim#ExecuteEclim(command)
endfunction " }}}

" vim:ft=vim:fdm=marker
