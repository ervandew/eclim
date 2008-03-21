" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
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

runtime ftplugin/html.vim
runtime indent/html.vim
runtime ftplugin/html/eclim.vim

" Global Variables {{{
if !exists('g:EclimDjangoTemplateCompleteEndTag')
  let g:EclimDjangoTemplateCompleteEndTag = 1
endif
if !exists('g:HtmlDjangoUserBodyElements')
  let g:HtmlDjangoUserBodyElements = []
endif
" }}}

" Mappings {{{
if g:EclimDjangoTemplateCompleteEndTag
  imap <buffer> <silent> % <c-r>=eclim#python#django#template#CompleteEndTag()<cr>
endif
" }}}

let g:HtmlDjangoBodyElements = [
    \ ['block', 'endblock'],
    \ ['comment', 'endcomment'],
    \ ['filter', 'endfilter'],
    \ ['for', 'endfor'],
    \ ['if', 'else', 'endif'],
    \ ['ifchanged', 'else', 'endifchanged'],
    \ ['ifequal', 'else', 'endifequal'],
    \ ['ifnotequal', 'else', 'endifnotequal'],
    \ ['spaceless', 'else', 'endspaceless']
  \ ] + g:HtmlDjangoUserBodyElements

" add matchit.vim support for django tags
if exists("b:match_words")
  for element in g:HtmlDjangoBodyElements
    let pattern = ''
    for tag in element[:-2]
      if pattern != ''
        let pattern .= ':'
      endif
      let pattern .= '{%\s*\<' . tag . '\>.\{-}%}'
    endfor
    let pattern .= ':{%\s*\<' . element[-1:][0] . '\>\s*%}'
    let b:match_words .= ',' . pattern
  endfor
endif

" vim:ft=vim:fdm=marker
