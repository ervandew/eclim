" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/php/complete.html
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

" Script Varables {{{
  let s:complete_command =
    \ '-command php_complete -p "<project>" -f "<file>" -o <offset> -e <encoding>'
" }}}

" CodeComplete(findstart, base) {{{
" Handles php code completion.
function! eclim#php#complete#CodeComplete(findstart, base)
  let line = line('.')
  let phpstart = search('<?php', 'bcnW')
  let phpend = search('?>', 'bcnW', line('w0'))
  if phpstart == 0 || (phpend != 0 && line > phpend)
    return eclim#html#complete#CodeComplete(a:findstart, a:base)
  endif

  return eclim#lang#CodeComplete(s:complete_command, a:findstart, a:base)
endfunction " }}}

" vim:ft=vim:fdm=marker
