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

" Script Varables {{{
  let s:search = '-command scala_search'
  let s:options_map = {'-a': ['split', 'vsplit', 'edit', 'tabnew', 'lopen']}
" }}}

function! eclim#scala#search#Search(argline) " {{{
  return eclim#lang#Search(s:search, g:EclimScalaSearchSingleResult, a:argline)
endfunction " }}}

function! eclim#scala#search#CommandCompleteSearch(argLead, cmdLine, cursorPos) " {{{
  return eclim#util#CommandCompleteOptions(
    \ a:argLead, a:cmdLine, a:cursorPos, s:options_map)
endfunction " }}}

" vim:ft=vim:fdm=marker
