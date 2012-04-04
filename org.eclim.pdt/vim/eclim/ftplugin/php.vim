" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/php/index.html
"
" License:
"
" Copyright (C) 2005 - 2012  Eric Van Dewoestine
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

if !exists("g:EclimPhpValidate")
  let g:EclimPhpValidate = 1
endif

if !exists("g:EclimPhpHtmlValidate")
  let g:EclimPhpHtmlValidate = g:EclimPhpValidate
endif

" }}}

" Options {{{

setlocal completefunc=eclim#php#complete#CodeComplete

" }}}

" Autocmds {{{

augroup eclim_html_validate
  autocmd!
augroup END

augroup eclim_php
  autocmd! BufWritePost <buffer>
  autocmd BufWritePost <buffer> call eclim#php#util#UpdateSrcFile(0)
augroup END

" }}}

" Command Declarations {{{

command! -nargs=0 -buffer Validate :call eclim#php#util#UpdateSrcFile(1)

if !exists(":PhpSearch")
  command -buffer -nargs=*
    \ -complete=customlist,eclim#php#search#CommandCompletePhpSearch
    \ PhpSearch :call eclim#php#search#Search('<args>')
endif

if !exists(":PhpSearchContext")
  command -buffer PhpSearchContext :call eclim#php#search#SearchContext()
endif

" }}}

" vim:ft=vim:fdm=marker
