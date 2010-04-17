" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/taglist.html
"
" License:
"
" Copyright (C) 2005 - 2010  Eric Van Dewoestine
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

" FormatPython(types, tags) {{{
function! eclim#taglist#lang#python#FormatPython(types, tags)
  let lines = []
  let content = []

  call add(content, expand('%:t'))
  call add(lines, -1)

  let functions = filter(copy(a:tags), 'len(v:val) > 3 && v:val[3] == "f"')
  call eclim#taglist#util#FormatType(
      \ a:tags, a:types['f'], functions, lines, content, "\t")

  let classes = filter(copy(a:tags), 'len(v:val) > 3 && v:val[3] == "c"')
  if g:Tlist_Sort_Type == 'name'
    call sort(classes)
  endif

  for class in classes
    call add(content, "")
    call add(lines, -1)
    call add(content, "\t" . a:types['c'] . ' ' . class[0])
    call add(lines, index(a:tags, class))

    let members = filter(copy(a:tags),
        \ 'len(v:val) > 5 && v:val[3] == "m" && v:val[5] == "class:" . class[0]')
    call eclim#taglist#util#FormatType(
        \ a:tags, a:types['m'], members, lines, content, "\t\t")
  endfor

  return [lines, content]
endfunction " }}}

" vim:ft=vim:fdm=marker
