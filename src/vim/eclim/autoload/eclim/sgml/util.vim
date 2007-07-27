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
  if line[col('.') - 2] == '<' && line[col('.')] !~ '\w'
    let pos = searchpairpos('<\w', '', '</\w', 'bn')
    if pos[0]
      let line = getline(pos[0])
      let tag =  substitute(line, '.*\%' . (pos[1] + 1) . 'c\(\w\+\)\W.*', '\1', '')
      if tag != line
        return '/' . tag . '>'
      endif
    endif
  endif
  return '/'
endfunction " }}}

" vim:ft=vim:fdm=marker
