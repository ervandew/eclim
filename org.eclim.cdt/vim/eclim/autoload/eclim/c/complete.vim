" Author:  Eric Van Dewoestine
"
" License: {{{
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

" Script Varables {{{
  let s:complete_command =
    \ '-command c_complete -p "<project>" -f "<file>" ' .
    \ '-o <offset> -e <encoding> -l <layout>'
" }}}

function! eclim#c#complete#CodeComplete(findstart, base) " {{{
  if a:findstart && getline('.') =~ '#include\s\+<'
    call eclim#lang#SilentUpdate(0)
    return stridx(getline('.'), '<')
  endif
  return eclim#lang#CodeComplete(
    \ s:complete_command, a:findstart, a:base,
    \ {'temp': 0, 'layout': g:EclimCCompleteLayout})
endfunction " }}}

" vim:ft=vim:fdm=marker
