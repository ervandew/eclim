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

" FormatPhp(types, tags) {{{
function! eclim#taglist#lang#php#FormatPhp(types, tags)
  let pos = getpos('.')

  let lines = []
  let content = []

  call add(content, expand('%:t'))
  call add(lines, -1)

  let top_functions = filter(copy(a:tags), 'v:val[3] == "f"')

  let class_contents = []
  let classes = filter(copy(a:tags), 'v:val[3] == "c"')
  if g:Tlist_Sort_Type == 'name'
    call sort(classes)
  endif
  for class in classes
    exec 'let object_start = ' . split(class[4], ':')[1]
    call cursor(object_start, 1)
    call search('{', 'W')
    let object_end = searchpair('{', '', '}', 'W')

    let functions = []
    let indexes = []
    let index = 0
    for fct in top_functions
      if len(fct) > 3
        exec 'let fct_line = ' . split(fct[4], ':')[1]
        if fct_line > object_start && fct_line < object_end
          call add(functions, fct)
          call add(indexes, index)
        endif
      endif
      let index += 1
    endfor
    call reverse(indexes)
    for i in indexes
      call remove(top_functions, i)
    endfor

    call add(class_contents, {'class': class, 'functions': functions})
  endfor

  let interface_contents = []
  let interfaces = filter(copy(a:tags), 'v:val[3] == "i"')
  if g:Tlist_Sort_Type == 'name'
    call sort(interfaces)
  endif
  for interface in interfaces
    exec 'let object_start = ' . split(interface[4], ':')[1]
    call cursor(object_start, 1)
    call search('{', 'W')
    let object_end = searchpair('{', '', '}', 'W')

    let functions = []
    let indexes = []
    let index = 0
    for fct in top_functions
      if len(fct) > 3
        exec 'let fct_line = ' . split(fct[4], ':')[1]
        if fct_line > object_start && fct_line < object_end
          call add(functions, fct)
          call add(indexes, index)
        endif
      endif
      let index += 1
    endfor
    call reverse(indexes)
    for i in indexes
      call remove(top_functions, i)
    endfor

    call add(interface_contents, {'interface': interface, 'functions': functions})
  endfor

  if len(top_functions) > 0
    call add(content, "")
    call add(lines, -1)
    call eclim#taglist#util#FormatType(
        \ a:tags, a:types['f'], top_functions, lines, content, "\t")
  endif

  for class_content in class_contents
    call add(content, "")
    call add(lines, -1)
    call add(content, "\t" . a:types['c'] . ' ' . class_content.class[0])
    call add(lines, index(a:tags, class_content.class))

    call eclim#taglist#util#FormatType(
        \ a:tags, a:types['f'], class_content.functions, lines, content, "\t\t")
  endfor

  for interface_content in interface_contents
    call add(content, "")
    call add(lines, -1)
    call add(content, "\t" . a:types['i'] . ' ' . interface_content.interface[0])
    call add(lines, index(a:tags, interface_content.interface))

    call eclim#taglist#util#FormatType(
        \ a:tags, a:types['f'], interface_content.functions, lines, content, "\t\t")
  endfor

  call setpos('.', pos)

  return [lines, content]
endfunction " }}}

" vim:ft=vim:fdm=marker
