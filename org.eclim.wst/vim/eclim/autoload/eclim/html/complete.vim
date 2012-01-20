" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/html/complete.html
"
" License:
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

" Script Varables {{{
  let s:complete_command =
    \ '-command html_complete -p "<project>" -f "<file>" -o <offset> -e <encoding>'
" }}}

" CodeComplete(findstart, base) {{{
" Handles html code completion.
function! eclim#html#complete#CodeComplete(findstart, base)
  "if eclim#html#util#InJavascriptBlock()
  "  return eclim#javascript#complete#CodeComplete(a:findstart, a:base)
  "endif

  if eclim#html#util#InCssBlock()
    return eclim#css#complete#CodeComplete(a:findstart, a:base)
  endif

  if a:findstart
    call eclim#lang#SilentUpdate(1)

    " locate the start of the word
    let line = getline('.')

    let start = col('.') - 1

    while start > 0 && line[start - 1] =~ '[[:alnum:]_-]'
      let start -= 1
    endwhile

    return start
  else
    return eclim#lang#CodeComplete(s:complete_command, a:findstart, a:base)
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
