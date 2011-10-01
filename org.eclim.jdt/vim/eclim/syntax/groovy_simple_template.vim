" Author:  Eric Van Dewoestine
"
" Description: {{{
"  Syntax file for template files using groovy's simple template syntax.
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

syn region groovySimpleTemplateSection start="<%" end="%>"
syn match groovySimpleTemplateVariable '\${.\{-}}'

hi link groovySimpleTemplateSection Statement
hi link groovySimpleTemplateVariable Constant

" vim:ft=vim:fdm=marker
