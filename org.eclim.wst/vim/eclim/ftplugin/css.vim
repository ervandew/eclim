" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/css/index.html
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

if !exists("g:EclimCssValidate")
  let g:EclimCssValidate = 1
endif

" }}}

" Options {{{

setlocal completefunc=eclim#css#complete#CodeComplete

" }}}

" Autocmds {{{

if g:EclimCssValidate
  augroup eclim_css_validate
    autocmd! BufWritePost <buffer>
    autocmd BufWritePost <buffer>
      \ call eclim#lang#Validate('css', 1, 'eclim#css#validate#Filter')
  augroup END
endif

" }}}

" Command Declarations {{{

if !exists(":Validate")
  command -nargs=0 -buffer Validate
    \ :call eclim#lang#Validate('css', 0, 'eclim#css#validate#Filter')
endif

" }}}

" vim:ft=vim:fdm=marker
