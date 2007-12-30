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

" FormatJava(types, tags) {{{
function! eclim#taglist#java#FormatJava (types, tags)
  let lines = []
  let content = []

  call add(content, expand('%:t'))
  call add(lines, -1)

  let package = filter(copy(a:tags), 'v:val[3] == "p"')
  call eclim#taglist#util#FormatType(
      \ a:tags, a:types['p'], package, lines, content, "\t")

  let classes = filter(copy(a:tags), 'v:val[3] == "c"')
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
