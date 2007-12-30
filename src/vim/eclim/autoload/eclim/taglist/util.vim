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

" FormatType(tags, type, values, lines, content, indent) {{{
function! eclim#taglist#util#FormatType (tags, type, values, lines, content, indent)
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
function! eclim#taglist#util#GetTagPattern (tag)
  return strpart(a:tag[2], 1, len(a:tag[2]) - 4)
endfunction " }}}

" GetVisibility(tag) {{{
" Gets the visibility string for the supplied tag.
function! eclim#taglist#util#GetVisibility (tag)
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
