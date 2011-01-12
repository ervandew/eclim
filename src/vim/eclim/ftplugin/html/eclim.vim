" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/html/index.html
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

if !exists("g:EclimHtmlValidate")
  let g:EclimHtmlValidate = 1
endif

" }}}

" Options {{{

setlocal completefunc=eclim#html#complete#CodeComplete

" }}}

" Autocmds {{{

if g:EclimHtmlValidate
  augroup eclim_html_validate
    autocmd! BufWritePost <buffer>
    autocmd BufWritePost <buffer> call eclim#html#validate#Validate(1)
  augroup END
endif

" }}}

" Command Declarations {{{

if !exists(":Validate")
  command -nargs=0 -buffer Validate
    \ :call eclim#lang#Validate('html', 0)
endif

if !exists(":BrowserOpen")
  command -nargs=? -complete=file -buffer BrowserOpen
    \ :call eclim#html#util#OpenInBrowser(<q-args>)
endif

" }}}

" vim:ft=vim:fdm=marker
