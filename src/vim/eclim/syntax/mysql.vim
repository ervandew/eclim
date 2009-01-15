" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Extension to default mysql syntax to add additional syntax support.
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

source $VIMRUNTIME/syntax/mysql.vim

syn keyword mysqlKeyword engine
syn keyword mysqlKeyword if elseif else loop leave while
syn keyword mysqlKeyword before close cursor each fetch open set trigger
syn keyword mysqlKeyword begin call declare return
syn keyword mysqlKeyword delimiter
syn keyword mysqlKeyword truncate
syn keyword mysqlKeyword duplicate union
syn keyword mysqlKeyword interval

syn keyword sqlTodo TODO FIXME NOTE

syn match mysqlEscaped "`.\{-}`"

syn region mysqlVariable start="\(NEW\|OLD\)\." end="\W"

hi def link sqlComment Comment
hi def link sqlTodo Todo

" vim:ft=vim:fdm=marker
