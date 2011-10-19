" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/scala/complete.html
"
" License:
"
" Copyright (C) 2011  Eric Van Dewoestine
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

" Global Varables {{{
  if !exists("g:EclimScalaCompleteLayout")
    if &completeopt !~ 'preview' && &completeopt =~ 'menu'
      let g:EclimScalaCompleteLayout = 'standard'
    else
      let g:EclimScalaCompleteLayout = 'compact'
    endif
  endif
" }}}

" Script Varables {{{
  let s:complete_command =
    \ '-command scala_complete -p "<project>" -f "<file>" ' .
    \ '-o <offset> -e <encoding> -l <layout>'
" }}}

" CodeComplete(findstart, base) {{{
" Handles code completion.
function! eclim#scala#complete#CodeComplete(findstart, base)
  return eclim#lang#CodeComplete(
    \ s:complete_command, a:findstart, a:base, {'layout': g:EclimScalaCompleteLayout})
endfunction " }}}

" vim:ft=vim:fdm=marker
