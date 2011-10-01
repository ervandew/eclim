" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Django Html template indent file using IndentAnything.
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

if !exists('b:disableOverride') && exists('g:EclimHtmldjangoIndentDisabled')
  finish
endif

let b:disableOverride = 1
runtime! indent/html.vim

let g:HtmlDjangoIndentOpenElements = ''
let g:HtmlDjangoIndentMidElements = ''
for element in g:HtmlDjangoBodyElements
  if len(g:HtmlDjangoIndentOpenElements) > 0
    let g:HtmlDjangoIndentOpenElements .= '\|'
  endif
  let g:HtmlDjangoIndentOpenElements .= element[0]

  for tag in element[1:-2]
    if len(g:HtmlDjangoIndentMidElements) > 0
      let g:HtmlDjangoIndentMidElements .= '\|'
    endif
    let g:HtmlDjangoIndentMidElements .= tag
  endfor
endfor

" HtmlDjangoIndentAnythingSettings() {{{
function! HtmlDjangoIndentAnythingSettings()
  call HtmlIndentAnythingSettings()

  let b:indentTrios = [
      \ [ '<\w', '', '\(/>\|</\)' ],
      \ [ '{%\s*\%(' . g:HtmlDjangoIndentOpenElements . '\)\(\s\+.\{-}\)\?%}',
        \ '{%\s*\%(' . g:HtmlDjangoIndentMidElements . '\)\(\s\+.\{-}\)\?%}',
        \ '{%\s*end\w\+\s*%}' ],
    \ ]
endfunction " }}}

let b:indent_settings = 'HtmlDjangoIndentAnythingSettings'

" vim:ft=vim:fdm=marker
