" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/python/index.html
"
" License:
"
" Copyright (C) 2005 - 2011  Eric Van Dewoestine
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

if !exists("g:EclimPythonValidate")
  let g:EclimPythonValidate = 1
endif

" }}}

" Options {{{

setlocal completefunc=eclim#python#complete#CodeComplete

" }}}

" Autocmds {{{

if g:EclimPythonValidate
  augroup eclim_python_validate
    autocmd! BufWritePost <buffer>
    autocmd BufWritePost <buffer> call eclim#python#validate#Validate(1)
  augroup END
endif

" }}}

" Command Declarations {{{

if !exists(":PythonFindDefinition")
  command -buffer PythonFindDefinition :call eclim#python#search#Find('definition')
endif
if !exists(":PythonSearchContext")
  command -buffer PythonSearchContext :call eclim#python#search#SearchContext()
endif

if !exists(':PythonImportClean')
  command -buffer PythonImportClean :call eclim#python#import#CleanImports()
endif
if !exists(':PythonImportSort')
  command -buffer PythonImportSort :call eclim#python#import#SortImports()
endif

if !exists(":Validate")
  command -nargs=0 -buffer Validate :call eclim#python#validate#Validate(0)
endif
if !exists(":PyLint")
  command -nargs=0 -buffer PyLint :call eclim#python#validate#PyLint()
endif

if !exists(':DjangoTemplateOpen')
  command -buffer DjangoTemplateOpen :call eclim#python#django#find#FindTemplate(
    \ eclim#python#django#util#GetProjectPath(), eclim#util#GrabUri())
endif
if !exists(':DjangoViewOpen')
  command -buffer DjangoViewOpen :call eclim#python#django#find#FindView(
    \ eclim#python#django#util#GetProjectPath(), eclim#util#GrabUri())
endif
if !exists(':DjangoContextOpen')
  command -buffer DjangoContextOpen :call eclim#python#django#find#ContextFind()
endif

" }}}

" vim:ft=vim:fdm=marker
