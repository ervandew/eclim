" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Extension to default css syntax to fix issues.
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

source $VIMRUNTIME/syntax/css.vim

" fix issue where vim's css syntax file has issues if a curly immediately
" follows a psudo class
" Ex.
" a:hover{
"   color: #fff;
" }
syn match cssPseudoClass ":[^ {]*" contains=cssPseudoClassId,cssUnicodeEscape

" some css3 properties
syn match cssBoxProp contained "\<border-\(\(top\|right\|bottom\|left\)-\)*radius\>"
syn match cssBoxProp contained "\<box-shadow\>"
syn match cssBoxProp contained "\<opacity\>"

" css3 pseudo classes
syn match cssPseudoClassId contained "\<\(root\|empty\)\>"
syn match cssPseudoClassId contained "\<\(last\|only\)-child\>"
syn match cssPseudoClassId contained "\<\(first\|last\|only\)-of-type\>"
syn match cssPseudoClassId contained "\<nth-\(child\|last-child\|of-type\|last-of-type\)\>(\s*\(odd\|even\|[-+]\?\s*\d\+\(\s*n\(\s*[-+]\?\s*\d\+\)\?\)\?\)\s*)"
syn region cssPseudoClassId contained start="\<not\>(" end=")" contains=cssPseudoClassId

" vim:ft=vim:fdm=marker
