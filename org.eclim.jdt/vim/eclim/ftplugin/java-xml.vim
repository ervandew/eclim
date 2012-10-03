" Author:  Eric Van Dewoestine
"
" License: {{{
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

" Functionality exposed to java xml files (web.xml, spring xml files, etc.).

" Global Variables {{{

if !exists("g:EclimJavaSearchMapping")
  let g:EclimJavaSearchMapping = 1
endif

" }}}

" Mappings {{{

if g:EclimJavaSearchMapping
  noremap <silent> <buffer> <cr> :call eclim#java#search#FindClassDeclaration()<cr>
endif

" }}}

" vim:ft=vim:fdm=marker
