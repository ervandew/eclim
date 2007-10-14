" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Compiler for xmllint.
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

if exists("current_compiler")
  finish
endif
let current_compiler = "eclim_xmllint"

CompilerSet makeprg=xmllint\ --valid\ --noout\ $*

CompilerSet errorformat=
  \%E%f:%l:\ %.%#\ error\ :\ %m,
  \%W%f:%l:\ %.%#\ warning\ :\ %m,
  \%-Z%p^,
  \%-C%.%#,
  \%-G%.%#

" vim:ft=vim:fdm=marker
