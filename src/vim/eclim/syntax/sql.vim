" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Replacement of vim's sql.vim which ensures that ALL db specific syntax
"   files are sourced.
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
