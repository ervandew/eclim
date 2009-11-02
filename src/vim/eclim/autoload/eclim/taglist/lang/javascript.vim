" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/taglist.html
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

" FormatJavascript(types, tags) {{{
function! eclim#taglist#lang#javascript#FormatJavascript(types, tags)
  let pos = getpos('.')

  let lines = []
  let content = []

  call add(content, expand('%:t'))
  call add(lines, -1)

  let object_contents = []

  let objects = filter(copy(a:tags), 'v:val[3] == "o"')
  let members = filter(copy(a:tags), 'v:val[3] == "m"')
  let functions = filter(copy(a:tags),
    \ 'v:val[3] == "f" && v:val[2] =~ "\\<function\\>"')
  let object_bounds = {}
  for object in objects
    exec 'let object_start = ' . split(object[4], ':')[1]
    call cursor(object_start, 1)
    while search('{', 'W') && s:SkipComments()
      " no op
    endwhile
    let object_end = searchpair('{', '', '}', 'W', 's:SkipComments()')

    let methods = []
    for fct in members
      if len(fct) > 3
        exec 'let fct_line = ' . split(fct[4], ':')[1]
        if fct_line > object_start && fct_line < object_end
          call add(methods, fct)
        endif
      endif
    endfor

    let indexes = []
    let index = 0
    for fct in functions
      if len(fct) > 3
        exec 'let fct_line = ' . split(fct[4], ':')[1]
        if fct_line > object_start && fct_line < object_end
          call add(methods, fct)
          call add(indexes, index)
        elseif fct_line == object_start
          call add(indexes, index)
        endif
      endif
      let index += 1
    endfor
    call reverse(indexes)
    for i in indexes
      call remove(functions, i)
    endfor

    if len(methods) > 0
      let parent_object = s:GetParentObject(
        \ object_contents, object_bounds, object_start, object_end)
      " remove methods from the parent if necessary
      if len(parent_object)
        call filter(parent_object.methods, 'index(methods, v:val) == -1')
      endif
      let object_bounds[string(object)] = [object_start, object_end]
      call sort(methods)
      call add(object_contents, {'object': object, 'methods': methods})
    endif
  endfor

  if len(functions) > 0
    call add(content, "")
    call add(lines, -1)
    call eclim#taglist#util#FormatType(
        \ a:tags, a:types['f'], functions, lines, content, "\t")
  endif

  for object_content in object_contents
    call add(content, "")
    call add(lines, -1)
    call add(content, "\t" . a:types['o'] . ' ' . object_content.object[0])
    call add(lines, index(a:tags, object_content.object))

    call eclim#taglist#util#FormatType(
        \ a:tags, a:types['f'], object_content.methods, lines, content, "\t\t")
  endfor

  call setpos('.', pos)

  return [lines, content]
endfunction " }}}

" s:SkipComments() {{{
function s:SkipComments()
  let synname = synIDattr(synID(line('.'), col('.'), 1), "name")
  return synname =~ '\([Cc]omment\|[Ss]tring\)'
endfunction " }}}

" s:GetParentObject(objects, bounds, start, end) {{{
function s:GetParentObject(objects, bounds, start, end)
  for key in keys(a:bounds)
    let range = a:bounds[key]
    if range[0] < a:start && range[1] > a:end
      for object_content in a:objects
        if string(object_content.object) == key
          return object_content
        endif
      endfor
      break
    endif
  endfor
  return {}
endfunction " }}}

" vim:ft=vim:fdm=marker
