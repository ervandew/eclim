" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2013 - 2014  Eric Van Dewoestine
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
  let s:search = '-command python_search'
  let s:options = ['-a', '-x']
  let s:options_map = {
      \ '-a': ['split', 'vsplit', 'edit', 'tabnew', 'lopen'],
      \ '-x': ['declarations', 'references'],
    \ }
" }}}

function! eclim#python#search#Search(argline) " {{{
  return eclim#lang#Search(s:search, g:EclimPythonSearchSingleResult, a:argline)
endfunction " }}}

function! eclim#python#search#SearchContext(argline) " {{{
  let line = getline('.')
  let col = col('.')
  let args = a:argline
  if line =~ '\<\(def\|class\)\>\s\+\w*\%' . col . 'c'
    let args .= ' -x references'
  elseif line =~ '\%' . col . 'c\w*\_s*=[^=]'
    let args .= ' -x references'
  endif
  call eclim#python#search#Search(args)
endfunction " }}}

function! eclim#python#search#CommandCompleteSearch(argLead, cmdLine, cursorPos) " {{{
  return eclim#util#CommandCompleteOptions(
    \ a:argLead, a:cmdLine, a:cursorPos, s:options_map)
endfunction " }}}

function! eclim#python#search#CommandCompleteSearchContext(argLead, cmdLine, cursorPos) " {{{
  let options_map = {'-a': s:options_map['-a']}
  return eclim#util#CommandCompleteOptions(
    \ a:argLead, a:cmdLine, a:cursorPos, options_map)
endfunction " }}}

" vim:ft=vim:fdm=marker
