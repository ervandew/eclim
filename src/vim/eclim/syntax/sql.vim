" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Replacement of vim's sql.vim which ensures that ALL db specific syntax
"   files are sourced.
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

" Vim syntax file loader
if exists("b:eclim_sql_current_syntax")
  finish
endif
let b:eclim_sql_current_syntax = 1

" Default to the standard Vim distribution file
let filename = 'sqloracle'

" Check for overrides.  Buffer variables have the highest priority.
if exists("b:sql_type_override")
  " Check the runtimepath to see if the file exists
  if globpath(&runtimepath, 'syntax/' . b:sql_type_override . '.vim') != ''
    let filename = b:sql_type_override
  endif
elseif exists("g:sql_type_default")
  if globpath(&runtimepath, 'syntax/' . g:sql_type_default . '.vim') != ''
    let filename = g:sql_type_default
  endif
endif

" Source the appropriate files
exec 'runtime! syntax/' . filename . '.vim'

" vim:ft=vim:fdm=marker
