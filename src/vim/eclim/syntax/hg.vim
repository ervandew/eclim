" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Syntax file for hg commit messages.
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

if exists("b:current_syntax")
  finish
endif

syn match hgModified '^HG: changed .*$'
syn match hgProperty '^HG: \(user:\|branch\) .*$'

hi link hgModified Special
hi link hgProperty Special

let b:current_syntax = "hg"

" vim:ft=vim:fdm=marker
