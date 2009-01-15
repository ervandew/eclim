" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Extension to default css syntax to fix issues.
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

source $VIMRUNTIME/syntax/css.vim

" fix issue where vim's css syntax file has issues if a curly immediately
" follows a psudo class
" Ex.
" a:hover{
"   color: #fff;
" }
syn match cssPseudoClass ":[^ {]*" contains=cssPseudoClassId,cssUnicodeEscape

" vim:ft=vim:fdm=marker
