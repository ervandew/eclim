" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/vim/find.html
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
if !exists(":FindFunctionDef")
  command -buffer -nargs=? -bang FindFunctionDef
    \ :call eclim#vim#find#FindFunctionDef('<args>', '<bang>')
endif
if !exists(":FindFunctionRef")
  command -buffer -nargs=? -bang FindFunctionRef
    \ :call eclim#vim#find#FindFunctionRef('<args>', '<bang>')
endif
if !exists(":FindVariableDef")
  command -buffer -nargs=? -bang FindVariableDef
    \ :call eclim#vim#find#FindVariableDef('<args>', '<bang>')
endif
if !exists(":FindVariableRef")
  command -buffer -nargs=? -bang FindVariableRef
    \ :call eclim#vim#find#FindVariableRef('<args>', '<bang>')
endif
" }}}

" vim:ft=vim:fdm=marker
