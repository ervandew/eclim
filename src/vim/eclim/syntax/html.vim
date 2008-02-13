" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Extension to default html syntax to support spell checking in plain html
"   text, and set syntax group for doctype to fix indenting issue w/
"   IndentAnything.
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

source $VIMRUNTIME/syntax/html.vim

syn region htmlBody start="<body\>" end="</body>"me=e-7 end="</html\>"me=e-7 contains=htmlTag,htmlEndTag,htmlSpecialChar,htmlPreProc,htmlComment,htmlLink,htmlTitle,javaScript,cssStyle,@htmlPreproc,@Spell
syn region htmlDoctype start=+<!DOCTYPE+ keepend end=+>+

hi link htmlDoctype Comment
hi link javaScript Normal

" vim:ft=vim:fdm=marker
