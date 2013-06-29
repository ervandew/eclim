" Author:  Eric Van Dewoestine
"
" Description: {{{
"
" License:
"
" Copyright (C) 2005 - 2013  Eric Van Dewoestine
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

" Global Variables {{{

if !exists("g:EclimCValidate")
  let g:EclimCValidate = 1
endif

if !exists("g:EclimCSyntasticEnabled")
  let g:EclimCSyntasticEnabled = 0
endif

if !exists('g:EclimCCallHierarchyDefaultAction')
  let g:EclimCCallHierarchyDefaultAction = g:EclimDefaultFileOpenAction
endif

" }}}

" Options {{{

exec 'setlocal ' . g:EclimCompletionMethod . '=eclim#c#complete#CodeComplete'

" disable syntastic
if exists('g:loaded_syntastic_plugin') && !g:EclimCSyntasticEnabled
  let g:syntastic_c_checkers = []
endif

" }}}

" Autocmds {{{

augroup eclim_c
  autocmd! BufWritePost <buffer>
  autocmd BufWritePost <buffer> call eclim#lang#UpdateSrcFile('c')
augroup END

" }}}

" Command Declarations {{{

command! -nargs=0 -buffer Validate :call eclim#lang#UpdateSrcFile('c', 1)

if !exists(":CSearch")
  command -buffer -nargs=*
    \ -complete=customlist,eclim#c#search#CommandCompleteCSearch
    \ CSearch :call eclim#c#search#Search('<args>')
endif

if !exists(":CSearchContext")
  command -buffer CSearchContext :call eclim#c#search#SearchContext()
endif

if !exists(":CCallHierarchy")
  command -buffer -bang CCallHierarchy
    \ :call eclim#lang#hierarchy#CallHierarchy(
      \ 'c', g:EclimCCallHierarchyDefaultAction, '<bang>')
endif

" }}}

" vim:ft=vim:fdm=marker
