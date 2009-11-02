" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/vim/index.html
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

" Command Declarations {{{

if !exists(":VimDoc")
  command -buffer -nargs=? VimDoc :call eclim#vim#doc#FindDoc('<args>')
endif

if !exists(":FindByContext")
  command -buffer -nargs=0 -bang FindByContext
    \ :call eclim#vim#find#FindByContext('<bang>')
endif
if !exists(":FindCommandDef")
  command -buffer -nargs=? -bang FindCommandDef
    \ :call eclim#vim#find#FindCommandDef('<args>', '<bang>')
endif
if !exists(":FindCommandRef")
  command -buffer -nargs=? -bang FindCommandRef
    \ :call eclim#vim#find#FindCommandRef('<args>', '<bang>')
endif
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
