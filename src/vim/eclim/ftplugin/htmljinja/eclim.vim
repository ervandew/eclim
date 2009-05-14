" Author:  Eric Van Dewoestine
"
" Description: {{{
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

runtime ftplugin/html.vim
runtime indent/html.vim
runtime ftplugin/html/eclim.vim

" Global Variables {{{
if !exists('g:HtmlJinjaCompleteEndTag')
  let g:HtmlJinjaCompleteEndTag = 1
endif
" }}}

" Mappings {{{
if g:HtmlJinjaCompleteEndTag
  imap <buffer> <silent> e <c-r>=eclim#python#jinja#CompleteEndTag()<cr>
endif
" }}}

let g:HtmlJinjaBodyElements = [
    \ ['block', 'endblock'],
    \ ['call', 'endcall'],
    \ ['filter', 'endfilter'],
    \ ['for', 'endfor'],
    \ ['if', 'elif', 'else', 'endif'],
    \ ['macro', 'endmacro'],
  \ ]
" excluding 'else' on for until matchit.vim can support a duplicate word
" (doesn't break the matching of 'else' for 'if' statements.
"    \ ['for', 'else', 'endfor'],

" add matchit.vim support for jinja tags
if exists("b:match_words")
  for element in g:HtmlJinjaBodyElements
    let pattern = ''
    for tag in element[:-2]
      if pattern != ''
        let pattern .= ':'
      endif
      let pattern .= '{%-\?\s*\<' . tag . '\>' "\_.\{-}-\?%}'
    endfor
    let pattern .= ':{%-\?\s*\<' . element[-1:][0] . '\>\s*-\?%}'
    let b:match_words .= ',' . pattern
  endfor
endif

" vim:ft=vim:fdm=marker
