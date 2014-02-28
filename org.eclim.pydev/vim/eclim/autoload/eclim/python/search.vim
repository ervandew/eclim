" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2013  Eric Van Dewoestine
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
  if !exists("g:EclimPythonSearchSingleResult")
    " possible values ('split', 'edit', 'lopen')
    let g:EclimPythonSearchSingleResult = g:EclimDefaultFileOpenAction
  endif
" }}}

" Script Varables {{{
  let s:search = '-command python_search'
  let s:options = ['-x']
  let s:contexts = ['declarations', 'references']
" }}}

function! eclim#python#search#Search(argline) " {{{
  return eclim#lang#Search(
    \ s:search, g:EclimPythonSearchSingleResult, a:argline)
endfunction " }}}

function! eclim#python#search#SearchContext() " {{{
  let line = getline('.')
  let col = col('.')
  let args = ''
  if line =~ '\<\(def\|class\)\>\s\+\w*\%' . col . 'c'
    let args = '-x references'
  elseif line =~ '\%' . col . 'c\w*\_s*=[^=]'
    let args = '-x references'
  endif
  call eclim#python#search#Search(args)
endfunction " }}}

function! eclim#python#search#CommandCompletePythonSearch(argLead, cmdLine, cursorPos) " {{{
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')
  if cmdLine =~ '-x\s\+[a-z]*$'
    let contexts = deepcopy(s:contexts)
    call filter(contexts, 'v:val =~ "^' . argLead . '"')
    return contexts
  elseif cmdLine =~ '\s\+[-]\?$'
    let options = deepcopy(s:options)
    let index = 0
    for option in options
      if a:cmdLine =~ option
        call remove(options, index)
      else
        let index += 1
      endif
    endfor
    return options
  endif
  return []
endfunction " }}}

" vim:ft=vim:fdm=marker
