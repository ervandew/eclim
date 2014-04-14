" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2012 - 2014  Eric Van Dewoestine
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

" Options {{{

exec 'setlocal ' . g:EclimCompletionMethod . '=eclim#python#complete#CodeComplete'

call eclim#lang#DisableSyntasticIfValidationIsEnabled('python')

" }}}

" Autocmds {{{

augroup eclim_python
  autocmd! BufWritePost <buffer>
  autocmd BufWritePost <buffer> call eclim#lang#UpdateSrcFile('python')
augroup END

" }}}

" Command Declarations {{{

if !exists(":Validate")
  command! -nargs=0 -buffer Validate :call eclim#lang#UpdateSrcFile('python', 1)
endif

if !exists(":PythonSearch")
  command -buffer -nargs=*
    \ -complete=customlist,eclim#python#search#CommandCompleteSearch
    \ PythonSearch :call eclim#python#search#Search('<args>')
  command -buffer -nargs=*
    \ -complete=customlist,eclim#python#search#CommandCompleteSearchContext
    \ PythonSearchContext :call eclim#python#search#SearchContext('<args>')
endif

if !exists(':DjangoContextOpen')
  command -buffer -nargs=*
    \ -complete=customlist,eclim#python#django#find#CommandCompleteAction
    \ DjangoContextOpen :call eclim#python#django#find#ContextFind('<args>')
  command -buffer -nargs=*
    \ -complete=customlist,eclim#python#django#find#CommandCompleteAction
    \ DjangoViewOpen
    \ :call eclim#python#django#find#FindView(
      \ '<args>', eclim#python#django#util#GetProjectPath(), eclim#util#GrabUri())
  command -buffer -nargs=*
    \ -complete=customlist,eclim#python#django#find#CommandCompleteAction
    \ DjangoTemplateOpen
    \ :call eclim#python#django#find#FindTemplate(
      \ '<args>', eclim#python#django#util#GetProjectPath(), eclim#util#GrabUri())
endif

" }}}

" vim:ft=vim:fdm=marker
