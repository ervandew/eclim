" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2005 - 2015  Eric Van Dewoestine
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
  let s:search = '-command c_search'
  let s:includepaths = '-command c_includepaths -p "<project>"'
  let s:sourcepaths = '-command c_sourcepaths -p "<project>"'
  let s:options_map = {
      \ '-a': ['split', 'vsplit', 'edit', 'tabnew'],
      \ '-s': ['all', 'project'],
      \ '-i': [],
      \ '-p': [],
      \ '-t': [
        \ 'class_struct',
        \ 'function',
        \ 'variable',
        \ 'union',
        \ 'method',
        \ 'field',
        \ 'enum',
        \ 'enumerator',
        \ 'namespace',
        \ 'typedef',
        \ 'macro'
      \ ],
      \ '-x': ['all', 'declarations', 'definitions', 'references'],
    \ }
" }}}

function! eclim#c#search#Search(argline) " {{{
  return eclim#lang#Search(s:search, g:EclimCSearchSingleResult, a:argline)
endfunction " }}}

function eclim#c#search#FindInclude(argline) " {{{
  if !eclim#project#util#IsCurrentFileInProject(1)
    return
  endif

  let [action_args, argline] = eclim#util#ExtractCmdArgs(a:argline, '-a:')
  let action = len(action_args) == 2 ? action_args[1] : g:EclimCSearchSingleResult

  let file = substitute(getline('.'), '.*#include\s*[<"]\(.*\)[>"].*', '\1', '')

  let project = eclim#project#util#GetCurrentProjectName()
  let command = substitute(s:includepaths, '<project>', project, '')
  let result =  eclim#Execute(command)
  let paths = type(result) == g:LIST_TYPE ? result : []

  let command = substitute(s:sourcepaths, '<project>', project, '')
  let result =  eclim#Execute(command)
  let paths += type(result) == g:LIST_TYPE ? result : []

  let dir = expand('%:p:h')
  if index(paths, dir) == -1
    call add(paths, dir)
  endif
  let results = map(
    \ split(globpath(join(paths, ','), file), '\n'),
    \ '{"filename": v:val, "line": 0, "column": 0}')

  if !empty(results)
    call eclim#lang#SearchResults(results, action)
    return 1
  else
    call eclim#util#EchoInfo("File not found.")
  endif
endfunction " }}}

function! eclim#c#search#SearchContext(argline) " {{{
  if getline('.')[col('.') - 1] == '$'
    call cursor(line('.'), col('.') + 1)
    let cnum = eclim#util#GetCurrentElementColumn()
    call cursor(line('.'), col('.') - 1)
  else
    let cnum = eclim#util#GetCurrentElementColumn()
  endif

  if getline('.') =~ '#include\s*[<"][A-Za-z0-9.]*\%' . cnum . 'c'
    call eclim#c#search#FindInclude(a:argline)
    return
  "elseif getline('.') =~ '\<\(class\|????\)\s\+\%' . cnum . 'c'
  "  call eclim#c#search#Search(a:argline . ' -x references')
    return
  endif

  call eclim#c#search#Search(a:argline . ' -x context')
endfunction " }}}

function! eclim#c#search#CommandCompleteSearch(argLead, cmdLine, cursorPos) " {{{
  return eclim#util#CommandCompleteOptions(
    \ a:argLead, a:cmdLine, a:cursorPos, s:options_map)
endfunction " }}}

function! eclim#c#search#CommandCompleteSearchContext(argLead, cmdLine, cursorPos) " {{{
  let options_map = {'-a': s:options_map['-a']}
  return eclim#util#CommandCompleteOptions(
    \ a:argLead, a:cmdLine, a:cursorPos, options_map)
endfunction " }}}

" vim:ft=vim:fdm=marker
