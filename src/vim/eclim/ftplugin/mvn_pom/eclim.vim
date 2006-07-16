" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
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

" load any xml related functionality
runtime ftplugin/xml.vim

" Command Declarations {{{
if !exists(":MvnDependencySearch")
  command -nargs=1 -buffer
    \ MvnDependencySearch :call eclim#java#maven#dependency#Search('<args>')
endif
" }}}

" vim:ft=vim:fdm=marker
