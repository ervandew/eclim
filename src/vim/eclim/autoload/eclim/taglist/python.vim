" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/taglist.html
"
" License:
"
" Copyright (c) 2005 - 2008
"
" Licensed under the Apache License, Version 2.0 (the "License");
" you may not use this file except in compliance with the License.
" You may obtain a copy of the License at
"
"      http://www.apache.org/licenses/LICENSE-2.0
"
" Unless required by applicable law or agreed to in writing, software
" distributed under the License is distributed on an "AS IS" BASIS,
" WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
" See the License for the specific language governing permissions and
" limitations under the License.
"
" }}}

" FormatPython(types, tags) {{{
function! eclim#taglist#python#FormatPython (types, tags)
  let lines = []
  let content = []

  call add(content, expand('%:t'))
  call add(lines, -1)

  let functions = filter(copy(a:tags), 'len(v:val) > 3 && v:val[3] == "f"')
  call eclim#taglist#util#FormatType(
      \ a:tags, a:types['f'], functions, lines, content, "\t")

  let classes = filter(copy(a:tags), 'len(v:val) > 3 && v:val[3] == "c"')
  for class in classes
    call add(content, "")
    call add(lines, -1)
    call add(content, "\t" . a:types['c'] . ' ' . class[0])
    call add(lines, index(a:tags, class))

    let members = filter(copy(a:tags),
        \ 'len(v:val) > 5 && v:val[3] == "m" && v:val[5] == "class:" . class[0]')
    for member in members
      call add(content, "\t\t" . member[0])
      call add(lines, index(a:tags, member))
    endfor
  endfor

  return [lines, content]
endfunction " }}}

" vim:ft=vim:fdm=marker
