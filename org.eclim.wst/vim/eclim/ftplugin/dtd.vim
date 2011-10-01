" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/dtd/index.html
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

" Global Variables {{{

if !exists("g:EclimDtdValidate")
  let g:EclimDtdValidate = 1
endif

" }}}

" Autocmds {{{

if g:EclimDtdValidate
  augroup eclim_css_validate
    autocmd! BufWritePost <buffer>
    autocmd BufWritePost <buffer> call eclim#lang#Validate('dtd', 1)
  augroup END
endif

" }}}

" Command Declarations {{{

if !exists(":Validate")
  command -nargs=0 -buffer Validate :call eclim#lang#Validate('dtd', 0)
endif

" }}}

" vim:ft=vim:fdm=marker
