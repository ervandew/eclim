" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/taglist.html
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

" FormatType(tags, type, values, lines, content, indent) {{{
function! eclim#taglist#util#FormatType(tags, type, values, lines, content, indent)
  if len(a:values) > 0
    call add(a:content, a:indent . a:type)
    call add(a:lines, -1)

    for value in a:values
      call add(a:content, "\t" . a:indent . eclim#taglist#util#GetVisibility(value) . value[0])
      call add(a:lines, index(a:tags, value))
    endfor
  endif
endfunction " }}}

" GetTagPattern(tag) {{{
function! eclim#taglist#util#GetTagPattern(tag)
  return strpart(a:tag[2], 1, len(a:tag[2]) - 4)
endfunction " }}}

" GetVisibility(tag) {{{
" Gets the visibility string for the supplied tag.
function! eclim#taglist#util#GetVisibility(tag)
  let pattern = eclim#taglist#util#GetTagPattern(a:tag)
  if pattern =~ '\<public\>'
    if pattern =~ '\<static\>'
      return '*'
    endif
    return '+'
  elseif pattern =~ '\<protected\>'
    return '#'
  elseif pattern =~ '\<private\>'
    return '-'
  endif
  return ''
endfunction " }}}

" vim:ft=vim:fdm=marker
