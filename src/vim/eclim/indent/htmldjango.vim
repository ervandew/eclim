" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Django Html template indent file using IndentAnything.
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

if exists("b:htmldjango_did_indent") ||
    \ (!exists('b:disableOverride') && exists('g:EclimHtmldjangoIndentDisabled'))
  finish
endif

let b:disableOverride = 1
runtime! indent/html.vim
let b:htmldjango_did_indent = 1

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

let HtmlSettings = function('HtmlIndentAnythingSettings')

" HtmlIndentAnythingSettings() {{{
function! HtmlIndentAnythingSettings ()
  call HtmlSettings()

  let b:indentTrios = [
      \ [ '<\w', '', '\(/>\|</\)' ],
      \ [ '{%\s*\(' . g:HtmlDjangoIndentOpenElements . '\)\s\+.\{-}%}',
        \ '{%\s*\(' . g:HtmlDjangoIndentMidElements . '\)\s\+.\{-}%}',
        \ '{%\s*end\w\+\s*%}' ],
    \ ]
endfunction " }}}

" vim:ft=vim:fdm=marker
