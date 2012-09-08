" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/scala/search.html
"
" License:
"
" Copyright (C) 2012  Eric Van Dewoestine
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

" Global Varables {{{
  if !exists("g:EclimScalaSearchSingleResult")
    " possible values ('split', 'edit', 'lopen')
    let g:EclimScalaSearchSingleResult = g:EclimDefaultFileOpenAction
  endif
" }}}

" Script Varables {{{
  let s:search = '-command scala_search'
" }}}

function! eclim#scala#search#Search(argline) " {{{
  return eclim#lang#Search(
    \ s:search, g:EclimScalaSearchSingleResult, a:argline)
endfunction " }}}

" vim:ft=vim:fdm=marker
