" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2005 - 2014  Eric Van Dewoestine
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

exec 'setlocal ' . g:EclimCompletionMethod . '=eclim#java#ant#complete#CodeComplete'

" }}}

" Autocmds {{{

if g:EclimAntValidate
  augroup eclim_xml
    autocmd! BufWritePost <buffer>
    autocmd BufWritePost <buffer> call eclim#lang#Validate('ant', 1)
  augroup END
endif

" }}}

" Command Declarations {{{

if !exists(":AntDoc")
  command -buffer -nargs=? AntDoc :call eclim#java#ant#doc#FindDoc('<args>')
endif

command! -nargs=0 -buffer Validate :call eclim#lang#Validate('ant', 0)

" }}}

" vim:ft=vim:fdm=marker
