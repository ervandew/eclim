" Author:  Eric Van Dewoestine
"
" Description: {{{
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

source $VIMRUNTIME/syntax/htmldjango.vim

if !exists('g:HtmlDjangoUserTags')
  let g:HtmlDjangoUserTags = []
endif

if !exists('g:HtmlDjangoUserFilters')
  let g:HtmlDjangoUserFilters = []
endif

syn match djangoComment "{#.*#}"

if len(g:HtmlDjangoUserTags)
  exec 'syn keyword djangoStatement ' . join(g:HtmlDjangoUserTags)
endif
if len(g:HtmlDjangoUserBodyElements)
  for element in g:HtmlDjangoUserBodyElements
    exec 'syn keyword djangoStatement ' . join(element)
  endfor
endif
if len(g:HtmlDjangoUserFilters)
  exec 'syn keyword djangoFilter ' . join(g:HtmlDjangoUserFilters)
endif

" vim:ft=vim:fdm=marker
