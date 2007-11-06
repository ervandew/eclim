" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Various sgml relatd functions.
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

" CompleteEndTag() {{{
" Function to complete an sgml end tag name.
" Ex. imap <silent> / <c-r>=eclim#sgml#util#CompleteEndTag()<cr>
function eclim#sgml#util#CompleteEndTag ()
  let line = getline('.')
  if line[col('.') - 2] == '<' && line[col('.') - 1] !~ '\w'
    let tag = s:GetStartTag(line('.'), [])
    if tag != ''
      return '/' . tag . '>'
    endif
  endif
  return '/'
endfunction " }}}

" s:GetStartTag(line, lastpos) {{{
function s:GetStartTag (line, lastpos)
  let pos = searchpairpos('<\w', '', '</\w', 'bnW')
  if pos[0]
    if search('\%' . pos[0] . 'l\%' . pos[1] . 'c\_[^>]*/>', 'bcnW') ||
     \ (a:line != pos[0] && !(indent(pos[0]) < indent(a:line)))
      let lnum = line('.')
      let cnum = col('.')
      call cursor(pos[0], pos[1])
      try
        let tag = s:GetStartTag(a:line, pos)
      finally
        call cursor(lnum, cnum)
      endtry
      return tag
    endif

    let line = getline(pos[0])
    let tag =  substitute(
      \ line, '.*\%' . (pos[1] + 1) . 'c\([0-9a-zA-Z_\-:]\+\)\W.*', '\1', '')
    if tag != line
      return tag
    endif
  elseif len(a:lastpos) > 0 && a:lastpos[0]
    let line = getline(a:lastpos[0])
    let tag =  substitute(
      \ line, '.*\%' . (a:lastpos[1] + 1) . 'c\([0-9a-zA-Z_\-:]\+\)\W.*', '\1', '')
    if tag != line
      return tag
    endif
  endif
  return ''
endfunction " }}}

" vim:ft=vim:fdm=marker
