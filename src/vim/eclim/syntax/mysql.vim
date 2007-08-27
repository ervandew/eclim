" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Extension to default mysql syntax to add additional syntax support.
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

source $VIMRUNTIME/syntax/mysql.vim

syn keyword mysqlKeyword if elseif else loop leave
syn keyword mysqlKeyword before close cursor each fetch open set trigger
syn keyword mysqlKeyword begin call declare return
syn keyword mysqlKeyword delimiter

syn keyword sqlTodo TODO FIXME NOTE

syn match mysqlEscaped "`.\{-}`"

syn region mysqlVariable start="\(NEW\|OLD\)\." end="\W"

hi def link sqlComment Comment
hi def link sqlTodo Todo

" vim:ft=vim:fdm=marker
