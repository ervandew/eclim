" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Extension to default java syntax to fix issues or make improvements.
"
" License:
"
" Copyright (C) 2005 - 2008  Eric Van Dewoestine
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

source $VIMRUNTIME/syntax/java.vim

" annotations can be fully qualified.
syn match   javaAnnotation      "@[_$a-zA-Z][_$a-zA-Z0-9_.]*\>"

" allow folding of blocks and java doc comments.
syn region javaBlockFold start="{" end="}" transparent fold
syn region javaDocComment start="/\*\*" end="\*/" keepend contains=javaCommentTitle,@javaHtml,javaDocTags,javaDocSeeTag,javaTodo,@Spell fold

" vim:ft=vim:fdm=marker
