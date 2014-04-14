" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2011 - 2014  Eric Van Dewoestine
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

exec 'setlocal ' . g:EclimCompletionMethod . '=eclim#scala#complete#CodeComplete'

" }}}

" Autocmds {{{

augroup eclim_scala
  autocmd! BufWritePost <buffer>
  autocmd BufWritePost <buffer> call eclim#lang#UpdateSrcFile('scala')
augroup END

" }}}

" Command Declarations {{{

command! -nargs=0 -buffer Validate :call eclim#lang#UpdateSrcFile('scala', 1)

if !exists(":ScalaSearch")
  command -buffer -nargs=0
    \ -complete=customlist,eclim#scala#search#CommandCompleteSearch
    \ ScalaSearch :call eclim#scala#search#Search('<args>')
endif

if !exists(":ScalaImport")
  command -buffer -nargs=0 ScalaImport :call eclim#scala#import#Import()
endif

" }}}

" vim:ft=vim:fdm=marker
