" Author:  Eric Van Dewoestine
"
" Description: {{{
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

" Parse(file, settings) {{{
function! eclim#taglisttoo#lang#cproject#Parse(file, settings)
  let tags = taglisttoo#util#Parse(a:file, a:settings, [
      \ ['c', "<configuration\\s+[^>]*?name=['\"](.*?)['\"]", 1],
      \ ['t', "<toolChain\\s+[^>]*?name=['\"](.*?)['\"]", 1],
      \ ['l', "<tool\\s+[^>]*?name=['\"](.*?)['\"]", 1],
      \ ['i', "<option\\s+[^>]*?valueType=['\"]includePath['\"]", 'includes'],
      \ ['s', "<option\\s+[^>]*?valueType=['\"]definedSymbols['\"]", 'symbols'],
    \ ])

  call taglisttoo#util#SetNestedParents(
    \ a:settings.tags, tags, ['c'], '<configuration', '</configuration')

  return tags
endfunction " }}}

" vim:ft=vim:fdm=marker
