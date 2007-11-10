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
    let tag = s:GetStartTag(line('.'))
    if tag != ''
      return '/' . tag . '>'
    endif
  endif
  return '/'
endfunction " }}}

" s:GetStartTag(line) {{{
function s:GetStartTag (line)
  let pos = searchpairpos('<\w', '', '</\w', 'bnW')
  if pos[0]
    " test if tag found is self closing
    if search('\%' . pos[0] . 'l\%' . pos[1] . 'c\_[^>]*/>', 'bcnW')
      let lnum = line('.')
      let cnum = col('.')
      call cursor(pos[0], pos[1])
      try
        return s:GetStartTag(a:line)
      finally
        call cursor(lnum, cnum)
      endtry
    endif

    let line = getline(pos[0])
    let lnum = line('.')
    let cnum = col('.')
    call cursor(pos[0], pos[1])
    try
      let tags = s:ExtractTags(line)
      for tag in reverse(tags)
        " place the cursor at the end of the line
        call cursor(line('.'), col('$'))
        " find first non self closing tag searching backwards
        call search('<' . tag . '\>[^/]\{-}>', 'b', line('.'))

        " see if the tag as a matching close tag
        let pos = searchpairpos('<' . tag . '\>', '', '</' . tag . '\>', 'nW')
        if !pos[0] || pos[0] > a:line
          return tag
        endif
      endfor
      call cursor(line('.'), 1)
      return s:GetStartTag(a:line)
    finally
      call cursor(lnum, cnum)
    endtry
  endif
  return ''
endfunction " }}}

" s:ExtractTags() {{{
" Extracts a list of open tag names from the current line.
function s:ExtractTags (line)
  let line = a:line
  let tags = []
  while line =~ '<\w\+'
    let tag = substitute(line, '.\{-}<\([a-zA-Z0-9:_]\+\).*', '\1', '')
    if line !~ '<' . tag . '[^>]\{-}/>'
      call add(tags, tag)
    endif
    let line = substitute(line, '.\{-}<' . tag . '\(.*\)', '\1', '')
  endwhile
  return tags
endfunction " }}}

" vim:ft=vim:fdm=marker
