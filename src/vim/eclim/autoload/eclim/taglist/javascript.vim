" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/taglist.html
"
" License:
"
" Copyright (c) 2005 - 2006
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

" FormatJavascript(types, tags) {{{
function! eclim#taglist#javascript#FormatJavascript (types, tags)
  let clnum = line('.')
  let ccnum = col('.')

  let lines = []
  let content = []

  call add(content, expand('%:t'))
  call add(lines, -1)

  let top_functions = filter(copy(a:tags), 'v:val[3] == "f"')
  let object_contents = []

  let objects = filter(copy(a:tags), 'v:val[3] == "o"')
  for object in objects
    exec 'let object_start = ' . split(object[4], ':')[1]
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

    call add(object_contents, {'object': object, 'functions': functions})
  endfor

  if len(top_functions) > 0
    call add(content, "")
    call add(lines, -1)
    call eclim#taglist#util#FormatType(
        \ a:tags, a:types['f'], top_functions, lines, content, "\t")
  endif

  for object_content in object_contents
    call add(content, "")
    call add(lines, -1)
    call add(content, "\t" . a:types['o'] . ' ' . object_content.object[0])
    call add(lines, index(a:tags, object_content.object))

    call eclim#taglist#util#FormatType(
        \ a:tags, a:types['f'], object_content.functions, lines, content, "\t\t")
  endfor

  call cursor(clnum, ccnum)

  return [lines, content]
endfunction " }}}

" vim:ft=vim:fdm=marker
