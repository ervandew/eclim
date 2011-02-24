" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/python/django.html
"
" License:
"
" Copyright (C) 2005 - 2011  Eric Van Dewoestine
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

" Script Variables {{{
let s:starttag = '{%\s*\(end\)\@!\(\w\+\)\s*\([^}]\+\)\?\s*%}'
let s:endtag = '{%\s*end\w\+\s*%}'

let s:body_tags = {}
function! s:InitBodyTags()
  for elements in g:HtmlDjangoBodyElements
    let s:body_tags[elements[0]] = elements[-1]
  endfor
endfunction
call s:InitBodyTags()
" }}}

" CompleteEndTag() {{{
" Function to complete a django template end tag.
" Ex. imap <silent> % <c-r>=eclim#python#django#template#CompleteEndTag()<cr>
function eclim#python#django#template#CompleteEndTag()
  let line = getline('.')
  let match_start = '.*{%\s*\%' . col('.') . 'c'
  if line =~ match_start . '\(\s\|\s*%}\|$\)'
    let tag = s:GetStartTag(line('.'))
    if tag != '' && tag != 'endif'
      let ops = ''
      " account for case where the closing %} already exists (delete it)
      if line =~ match_start . '\s*%}'
        let chars = substitute(line, match_start . '\(\s*%}\).*', '\1', '')
        let ops = substitute(chars, '.', "\<del>", "g")
      endif
      return tag . ops . ' %}'
    endif
  endif
  return 'e'
endfunction " }}}

" s:GetStartTag(line) {{{
function s:GetStartTag(line)
  let pairpos = searchpairpos(s:starttag, '', '{%', 'bnW')
  if pairpos[0]
    let line = getline(pairpos[0])
    let pos = getpos('.')
    call cursor(pairpos[0], pairpos[1])
    try
      let tags = s:ExtractTags(line)
      " place the cursor at the end of the line
      call cursor(line('.'), col('$'))
      for tag in reverse(tags)
        " find first tag searching backwards
        call search('{%\s*' . tag[0] . '\s*\([^}]\+\)\?\s*%}', 'b', line('.'))

        " see if the tag has a matching close tag
        let pairpos = searchpairpos(
          \ '{%\s*' . tag[0] . '\s*\([^}]\+\)\?\s*%}', '',
          \ '{%\s*' . tag[1], 'nW')
          "\ '{%\s*' . tag[1] . '\s*%}', 'nW')
        if !pairpos[0] || pairpos[0] > a:line
          return tag[1]
        endif
      endfor
      call cursor(line('.'), 1)
      return s:GetStartTag(a:line)
    finally
      call setpos('.', pos)
    endtry
  endif
  return ''
endfunction " }}}

" s:ExtractTags() {{{
" Extracts a list of open tag names from the current line.
function s:ExtractTags(line)
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
