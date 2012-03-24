" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Extension to default html syntax to support spell checking in plain html
"   text, and set syntax group for doctype to fix indenting issue w/
"   IndentAnything.
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

source $VIMRUNTIME/syntax/html.vim

syn region htmlBody start="<body\>" end="</body>"me=e-7 end="</html\>"me=e-7 contains=htmlTag,htmlEndTag,htmlSpecialChar,htmlPreProc,htmlComment,htmlLink,htmlTitle,javaScript,cssStyle,@htmlPreproc,@Spell
syn region htmlDoctype start=+<!DOCTYPE+ keepend end=+>+

syn region htmlTemplate start=+<script[^>]*type=['"]text/template['"][^>]*>+ keepend end=+</script>+me=s-1 contains=htmlTag,htmlEndTag,htmlSpecialChar,htmlComment,htmlLink,javaScript
syn clear javaScript
syn region javaScript start=+<script\s*\(type\s*=\s*['"]\(text\|application\)/\(java\|ecma\)script['"]\)\?\s*>+ keepend end=+</script>+me=s-1 contains=@htmlJavaScript,htmlCssStyleComment,htmlScriptTag,@htmlPreproc

hi link htmlDoctype Comment
hi link javaScript Normal

" vim:ft=vim:fdm=marker
