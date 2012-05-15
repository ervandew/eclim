" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Jinja Html template indent file using IndentAnything.
"
" License:
"
" Copyright (C) 2005 - 2012  Eric Van Dewoestine
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

if !exists('b:disableOverride') && exists('g:EclimHtmljinjaIndentDisabled')
  finish
endif

let b:disableOverride = 1
runtime! indent/html.vim

let g:HtmlJinjaIndentOpenElements = ''
let g:HtmlJinjaIndentMidElements = ''
for element in g:HtmlJinjaBodyElements
  if len(g:HtmlJinjaIndentOpenElements) > 0
    let g:HtmlJinjaIndentOpenElements .= '\|'
  endif
  let g:HtmlJinjaIndentOpenElements .= element[0]

  for tag in element[1:-2]
    if len(g:HtmlJinjaIndentMidElements) > 0
      let g:HtmlJinjaIndentMidElements .= '\|'
    endif
    let g:HtmlJinjaIndentMidElements .= tag
  endfor

  exec 'setlocal indentkeys+==end' . element[0]
endfor

" HtmlJinjaIndentAnythingSettings() {{{
function! HtmlJinjaIndentAnythingSettings()
  if exists('*HtmlSettings')
    call HtmlIndentAnythingSettings()
  endif

  let b:indentTrios = [
      \ [ '<\w', '', '\(/>\|</\)' ],
      \ [ '{%-\?\s*\(' . g:HtmlJinjaIndentOpenElements . '\)\(\s\+.\{-}\)\?-\?%}',
        \ '{%-\?\s*\(' . g:HtmlJinjaIndentMidElements . '\)\(\s\+.\{-}\)\?-\?%}',
        \ '{%-\?\s*end\w\+\s*-\?%}' ],
    \ ]
  if exists('b:jinja_line_statement_prefix')
    call add(b:indentTrios, [
        \ b:jinja_line_statement_prefix . '\s\+\(' . g:HtmlJinjaIndentOpenElements . '\)\>',
        \ b:jinja_line_statement_prefix . '\s\+\(' . g:HtmlJinjaIndentMidElements . '\)\>',
        \ b:jinja_line_statement_prefix . '\s\+end\w\+\>',
      \ ])
  endif
endfunction " }}}

let b:indent_settings = 'HtmlJinjaIndentAnythingSettings'

" vim:ft=vim:fdm=marker
