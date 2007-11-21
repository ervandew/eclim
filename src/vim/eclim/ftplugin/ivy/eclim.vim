" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/classpath.html
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
runtime indent/xml.vim

" turn off xml validation
augroup eclim_xml
  autocmd! BufWritePost <buffer>
  autocmd BufWritePost <buffer> call eclim#java#ivy#UpdateClasspath()
augroup END

" Command Declarations {{{
if !exists(":IvyRepo")
  command -nargs=1 -complete=dir -buffer IvyRepo
    \ :call eclim#java#ivy#SetRepo('<args>')
endif
if !exists(":IvyDependencySearch")
  command -nargs=1 -buffer IvyDependencySearch
    \ :call eclim#java#maven#dependency#Search('<args>', 'ivy')
endif
" }}}

" vim:ft=vim:fdm=marker
