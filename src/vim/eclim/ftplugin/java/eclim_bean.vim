" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/bean.html
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
if !exists(":JavaGet")
  command -buffer -range JavaGet
    \ :call eclim#java#bean#GetterSetter(<line1>, <line2>, "getter")
endif
if !exists(":JavaSet")
  command -buffer -range JavaSet
    \ :call eclim#java#bean#GetterSetter(<line1>, <line2>, "setter")
endif
if !exists(":JavaGetSet")
  command -buffer -range JavaGetSet
    \ :call eclim#java#bean#GetterSetter(<line1>, <line2>, "getter_setter")
endif
" }}}

" vim:ft=vim:fdm=marker
