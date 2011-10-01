" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/ruby/search.html
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

" Global Varables {{{
  if !exists("g:EclimRubySearchSingleResult")
    " possible values ('split', 'edit', 'lopen')
    let g:EclimRubySearchSingleResult = g:EclimDefaultFileOpenAction
  endif
" }}}

" Script Varables {{{
  let s:search_element =
    \ '-command dltk_search -n "<project>" -f "<file>" ' .
    \ '-o <offset> -l <length> -e <encoding> -x <context>'
  let s:search_pattern = '-command ruby_search'
  let s:options = ['-p', '-t', '-s', '-x', '-i']
  let s:scopes = ['all', 'project']
  let s:types = [
      \ 'class',
      \ 'method',
      \ 'function',
      \ 'field'
    \ ]
  let s:contexts = [
      \ 'all',
      \ 'declarations',
      \ 'references'
    \ ]
" }}}

" Search(argline) {{{
" Executes a search.
function! eclim#ruby#search#Search(argline)
  return eclim#lang#Search(
    \ s:search_pattern, g:EclimRubySearchSingleResult, a:argline)
endfunction " }}}

" SearchContext() {{{
" Executes a contextual search.
function! eclim#ruby#search#SearchContext()
  if getline('.')[col('.') - 1] == '$'
    call cursor(line('.'), col('.') + 1)
    let cnum = eclim#util#GetCurrentElementColumn()
    call cursor(line('.'), col('.') - 1)
  else
    let cnum = eclim#util#GetCurrentElementColumn()
  endif

  if getline('.') =~ '\<\(module\|class\|def\)\s\+\%' . cnum . 'c'
    call eclim#ruby#search#Search('-x references')
    return
  endif

  call eclim#ruby#search#Search('-x declarations')
endfunction " }}}

" CommandCompleteRubySearch(argLead, cmdLine, cursorPos) {{{
" Custom command completion for RubySearch
function! eclim#ruby#search#CommandCompleteRubySearch(argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')
  if cmdLine =~ '-s\s\+[a-z]*$'
    let scopes = deepcopy(s:scopes)
    call filter(scopes, 'v:val =~ "^' . argLead . '"')
    return scopes
  elseif cmdLine =~ '-t\s\+[a-z]*$'
    let types = deepcopy(s:types)
    call filter(types, 'v:val =~ "^' . argLead . '"')
    return types
  elseif cmdLine =~ '-x\s\+[a-z]*$'
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
