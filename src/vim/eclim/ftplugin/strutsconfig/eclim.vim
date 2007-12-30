" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
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

runtime ftplugin/xml.vim
runtime indent/xml.vim
runtime ftplugin/java/eclim_search.vim
runtime ftplugin/java/eclim_util.vim

if g:EclimJavaSearchMapping
  noremap <silent> <buffer> <cr> :call eclim#java#search#FindClassDeclaration()<cr>
endif

" vim:ft=vim:fdm=marker
