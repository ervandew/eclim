" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Commands for looking up info via the web (google, dictionary, wikipedia,
"   etc.).
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

" Command Declarations {{{
if !exists(":OpenUrl")
  command -nargs=? OpenUrl :call eclim#web#OpenUrl('<args>')
endif
if !exists(":Google")
  command -nargs=* Google :call eclim#web#Google('<args>', 0, 0)
endif
if !exists(":Clusty")
  command -nargs=* Clusty :call eclim#web#Clusty('<args>', 0, 0)
endif
if !exists(":Dictionary")
  command -nargs=? Dictionary :call eclim#web#Dictionary('<args>')
endif
if !exists(":Thesaurus")
  command -nargs=? Thesaurus :call eclim#web#Thesaurus('<args>')
endif
if !exists(":Wikipedia")
  command -nargs=* Wikipedia :call eclim#web#Wikipedia('<args>', 0, 0)
endif
" }}}

" vim:ft=vim:fdm=marker
