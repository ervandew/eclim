" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2005 - 2013  Eric Van Dewoestine
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

function! eclim#python#django#template#CompleteTag(tag_prefix, tag_suffix, body_elements) " {{{
  let line = getline('.')
  let match_start = '.*' . a:tag_prefix . '\%' . col('.') . 'c'
  if line =~ match_start . '\(\s\|' . a:tag_suffix . '\|$\)'
    let [start_line, start_col, tags] = s:GetTagComplete(
      \ line('.'), a:tag_prefix, a:tag_suffix, a:body_elements)
    if start_line == 0
      return 'e'
    endif

    let prefix = substitute(line, '.*\(' . match_start . '\).*', '\1', '')
    let start = searchpos('{%', 'bn')[1]
    let indent = indent('.') - indent(start_line)
    if !&expandtab
      let indent = indent / shiftwidth()
    endif
    let start -= indent

    if len(tags) == 1
      " Append suffix, if it's not there already.
      let compl_suffix = (line !~ match_start . a:tag_suffix ? ' %}' : ' ')
      return (eclim#util#Complete(start, [prefix . 'e', prefix . tags[0] . compl_suffix])
            \ ? '' : 'e')

    elseif len(tags)
      if line !~ match_start . a:tag_suffix
        call map(tags, 'prefix . (v:val != "elif" ? v:val . " %}" : v:val . " ")')
      elseif line !~ match_start . '\s'
        call map(tags, 'v:val . " "')
      endif
      return (eclim#util#Complete(start, [prefix . 'e'] + reverse(tags))
            \ ? '' : 'e')
    endif
  endif
  return 'e'
endfunction " }}}

function! s:GetTagComplete(line, tag_prefix, tag_suffix, body_elements) " {{{
  let start_tag = a:tag_prefix . '\(end\)\@!\(\w\+\)\s*\([^}]\+\)\?' . a:tag_suffix
  let pairpos = searchpairpos(start_tag, '', '{%', 'bnW')
  if pairpos[0]
    let line = getline(pairpos[0])
    let pos = getpos('.')
    call cursor(pairpos[0], pairpos[1])
    try
      let tags = s:ExtractTags(line, a:tag_prefix, a:tag_suffix, a:body_elements)
      " place the cursor at the end of the line
      call cursor(line('.'), col('$'))
      for tag in reverse(tags)
        " find first tag searching backwards
        call search(
          \ a:tag_prefix . tag[0] . '\s*\([^}]\+\)\?' . a:tag_suffix,
          \ 'b', line('.'))

        " see if the tag has a matching close tag
        let pairpos = searchpairpos(
          \ a:tag_prefix . tag[0] . '\s*\([^}]\+\)\?' . a:tag_suffix, '',
          \ a:tag_prefix . tag[1][-1], 'nW')
          "\ a:tag_prefix . tag[1] . a:tag_suffix, 'nW')
        if !pairpos[0] || indent(pairpos[0]) < indent(a:line)
          return [line('.'), col('.'), tag[1]]
        elseif pairpos[0] > a:line && len(tag[1]) > 1
          return [line('.'), col('.'), tag[1][:-2]]
        endif
      endfor
      call cursor(line('.'), 1)
      return s:GetTagComplete(a:line, a:tag_prefix, a:tag_suffix, a:body_elements)
    finally
      call setpos('.', pos)
    endtry
  endif
  return [0, 0, '']
endfunction " }}}

function! s:ExtractTags(line, tag_prefix, tag_suffix, body_elements) " {{{
  " Extracts a list of open tag names from the current line.
  let line = a:line
  let tags = []
  let tags_dict = {}
  for elements in a:body_elements
    let tags_dict[elements[0]] = elements[1:]
  endfor
  let start_tag = a:tag_prefix . '\(end\)\@!\(\w\+\)\s*\([^}]\+\)\?' . a:tag_suffix
  while line =~ start_tag
    let tag = substitute(line, '.\{-}' . start_tag . '.*', '\2', '')
    if line !~ a:tag_prefix . 'end' . tag . a:tag_suffix && has_key(tags_dict, tag)
      call add(tags, [tag, tags_dict[tag]])
    endif
    let line = substitute(
      \ line,
      \ '.\{-}' . a:tag_prefix . tag . '\>.\{-}' . a:tag_suffix,
      \ '\1', '')
  endwhile
  return tags
endfunction " }}}

" vim:ft=vim:fdm=marker
