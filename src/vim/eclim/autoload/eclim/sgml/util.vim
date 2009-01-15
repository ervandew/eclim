" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Various sgml relatd functions.
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

" CompleteEndTag() {{{
" Function to complete an sgml end tag name.
" Ex. imap <silent> / <c-r>=eclim#sgml#util#CompleteEndTag()<cr>
function eclim#sgml#util#CompleteEndTag()
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
function s:GetStartTag(line)
  let pairpos = searchpairpos('<\w', '', '</\w', 'bnW')
  if pairpos[0]
    " test if tag found is self closing
    if search('\%' . pairpos[0] . 'l\%' . pairpos[1] . 'c\_[^>]*/>', 'bcnW')
      let pos = getpos('.')
      call cursor(pairpos[0], pairpos[1])
      try
        return s:GetStartTag(a:line)
      finally
        call setpos('.', pos)
      endtry
    endif

    let line = getline(pairpos[0])
    let pos = getpos('.')
    call cursor(pairpos[0], pairpos[1])
    try
      let tags = s:ExtractTags(line)
      " place the cursor at the end of the line
      call cursor(line('.'), col('$'))
      for tag in reverse(tags)
        " find first non self closing tag searching backwards
        call search('<' . tag . '\>\([^>]\{-}[^/]\)\?>', 'b', line('.'))

        " see if the tag has a matching close tag
        let pairpos = searchpairpos('<' . tag . '\>', '', '</' . tag . '\>', 'nW')
        if !pairpos[0] || pairpos[0] > a:line
          return tag
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
  while line =~ '<\w\+'
    let tag = substitute(line, '.\{-}<\([a-zA-Z0-9:_]\+\).*', '\1', '')
    if line !~ '<' . tag . '[^>]\{-}/>' && !s:IgnoreTag(tag)
      call add(tags, tag)
    endif
    let line = substitute(line, '.\{-}<' . tag . '\(.*\)', '\1', '')
  endwhile
  return tags
endfunction " }}}

" s:IgnoreTag(tag) {{{
" Determines if a tag should be ignored.
function s:IgnoreTag(tag)
  if exists('b:EclimSgmlCompleteEndTagIgnore')
    for ignore in b:EclimSgmlCompleteEndTagIgnore
      if a:tag == ignore
        return 1
      endif
    endfor
  endif
  return 0
endfunction " }}}

" vim:ft=vim:fdm=marker
