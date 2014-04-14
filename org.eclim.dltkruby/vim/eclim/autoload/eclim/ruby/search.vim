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

" Script Varables {{{
  let s:search_element =
    \ '-command dltk_search -n "<project>" -f "<file>" ' .
    \ '-o <offset> -l <length> -e <encoding> -x <context>'
  let s:search_pattern = '-command ruby_search'
  let s:options_map = {
      \ '-a': ['split', 'vsplit', 'edit', 'tabnew', 'lopen'],
      \ '-s': ['all', 'project'],
      \ '-i': [],
      \ '-p': [],
      \ '-t': ['class', 'method', 'function', 'field'],
      \ '-x': ['all', 'declarations', 'references'],
    \ }
" }}}

function! eclim#ruby#search#Search(argline) " {{{
  return eclim#lang#Search(
    \ s:search_pattern, g:EclimRubySearchSingleResult, a:argline)
endfunction " }}}

function! eclim#ruby#search#SearchContext(argline) " {{{
  if getline('.')[col('.') - 1] == '$'
    call cursor(line('.'), col('.') + 1)
    let cnum = eclim#util#GetCurrentElementColumn()
    call cursor(line('.'), col('.') - 1)
  else
    let cnum = eclim#util#GetCurrentElementColumn()
  endif

  if getline('.') =~ '\<\(module\|class\|def\)\s\+\%' . cnum . 'c'
    call eclim#ruby#search#Search(a:argline . ' -x references')
    return
  endif

  call eclim#ruby#search#Search(a:argline . ' -x declarations')
endfunction " }}}

function! eclim#ruby#search#CommandCompleteSearch(argLead, cmdLine, cursorPos) " {{{
  return eclim#util#CommandCompleteOptions(
    \ a:argLead, a:cmdLine, a:cursorPos, s:options_map)
endfunction " }}}

function! eclim#ruby#search#CommandCompleteSearchContext(argLead, cmdLine, cursorPos) " {{{
  let options_map = {'-a': s:options_map['-a']}
  return eclim#util#CommandCompleteOptions(
    \ a:argLead, a:cmdLine, a:cursorPos, options_map)
endfunction " }}}

" vim:ft=vim:fdm=marker
