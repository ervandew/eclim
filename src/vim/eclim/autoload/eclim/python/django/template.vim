" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/python/django.html
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

" Script Variables {{{
let s:starttag = '{%\s*\(end\)\@!\(\w\+\)\s*\([^}]\+\)\?\s*%}'
let s:endtag = '{%\s*end\w\+\s*%}'

let s:body_tags = {}
for elements in g:HtmlDjangoBodyElements
  let s:body_tags[elements[0]] = elements[1:]
endfor
" }}}

" CompleteEndTag() {{{
" Function to complete a django template end tag.
" Ex. imap <silent> % <c-r>=eclim#python#django#template#CompleteEndTag()<cr>
function eclim#python#django#template#CompleteEndTag ()
  let line = getline('.')
  if line =~ '.*{%\s*\%' . col('.') . 'c\(\s\|$\)'
    let tag = s:GetStartTag(line('.'))
    echom 'result = ' . string(tag)
    if len(tag) == 1
      return tag[0] . ' %}'
    elseif len(tag) > 1
      call complete(col('.'), tag)
      return ''
    endif
  endif
  return 'e'
endfunction " }}}

" s:GetStartTag(line) {{{
function s:GetStartTag (line)
  let pos = searchpairpos(s:starttag, '', '{%', 'bnW')
  if pos[0]
    let line = getline(pos[0])
    let lnum = line('.')
    let cnum = col('.')
    call cursor(pos[0], pos[1])
    try
      let tags = s:ExtractTags(line)
      " place the cursor at the end of the line
      call cursor(line('.'), col('$'))
      for tag in reverse(tags)
        " find first tag searching backwards
        call search('{%\s*' . tag[0] . '\s*\([^}]\+\)\?\s*%}', 'b', line('.'))

        " see if the tag has a matching close tag
        let pos = searchpairpos(
          \ '{%\s*' . tag[0] . '\s*\([^}]\+\)\?\s*%}', '',
          \ '{%\s*' . tag[1][-1], 'nW')
          "\ '{%\s*' . tag[1] . '\s*%}', 'nW')
        if !pos[0] || pos[0] > a:line
          return tag[1]
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
  while line =~ s:starttag
    let tag = substitute(line, '.\{-}' . s:starttag . '.*', '\2', '')
    if line !~ '{%\s*end' . tag . '\s*%}' && has_key(s:body_tags, tag)
      call add(tags, [tag, s:body_tags[tag]])
    endif
    let line = substitute(line, '.\{-}{%\s*' . tag . '\>.\{-}%}', '\1', '')
  endwhile
  return tags
endfunction " }}}

" vim:ft=vim:fdm=marker
