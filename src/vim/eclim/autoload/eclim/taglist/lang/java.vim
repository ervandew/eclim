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

" FormatJava(types, tags) {{{
function! eclim#taglist#lang#java#FormatJava(types, tags)
  let lines = []
  let content = []

  call add(content, expand('%:t'))
  call add(lines, -1)

  let package = filter(copy(a:tags), 'v:val[3] == "p"')
  call eclim#taglist#util#FormatType(
      \ a:tags, a:types['p'], package, lines, content, "\t")

  let classes = filter(copy(a:tags), 'v:val[3] == "c"')

  " sort classes alphabetically except for the primary containing class.
  if len(classes) > 1 && g:Tlist_Sort_Type == 'name'
    let classes = [classes[0]] + sort(classes[1:])
  endif

  for class in classes
    call add(content, "")
    call add(lines, -1)
    let visibility = eclim#taglist#util#GetVisibility(class)
    call add(content, "\t" . visibility . a:types['c'] . ' ' . class[0])
    call add(lines, index(a:tags, class))

    let fields = filter(copy(a:tags),
      \ 'v:val[3] == "f" && len(v:val) > 5 && v:val[5] =~ "class:.*\\<" . class[0] . "$"')
    call eclim#taglist#util#FormatType(
        \ a:tags, a:types['f'], fields, lines, content, "\t\t")

    let methods = filter(copy(a:tags),
      \ 'v:val[3] == "m" && len(v:val) > 5 && v:val[5] =~ "class:.*\\<" . class[0] . "$"')
    call eclim#taglist#util#FormatType(
        \ a:tags, a:types['m'], methods, lines, content, "\t\t")
  endfor

  let interfaces = filter(copy(a:tags), 'v:val[3] == "i"')
  if g:Tlist_Sort_Type == 'name'
    call sort(interfaces)
  endif
  for interface in interfaces
    call add(content, "")
    call add(lines, -1)
    let visibility = eclim#taglist#util#GetVisibility(interface)
    call add(content, "\t" . visibility . a:types['i'] . ' ' . interface[0])
    call add(lines, index(a:tags, interface))

    let fields = filter(copy(a:tags),
      \ 'v:val[3] == "f" && len(v:val) > 5 && v:val[5] =~ "interface:.*\\<" . interface[0] . "$"')
    call eclim#taglist#util#FormatType(
        \ a:tags, a:types['f'], fields, lines, content, "\t\t")

    let methods = filter(copy(a:tags),
      \ 'v:val[3] == "m" && len(v:val) > 5 && v:val[5] =~ "interface:.*\\<" . interface[0] . "$"')
    call eclim#taglist#util#FormatType(
        \ a:tags, a:types['m'], methods, lines, content, "\t\t")
  endfor

  return [lines, content]
endfunction " }}}

" vim:ft=vim:fdm=marker
