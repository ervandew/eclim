" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/c/search.html
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
  if !exists("g:EclimCSearchSingleResult")
    " possible values ('split', 'edit', 'lopen')
    let g:EclimCSearchSingleResult = "split"
  endif
" }}}

" Script Varables {{{
  let s:search_element =
    \ '-command c_search -n "<project>" -f "<file>" ' .
    \ '-o <offset> -l <length> -e <encoding> -x <context>'
  let s:search_pattern = '-command c_search'
  let s:includepaths = '-command c_includepaths -p "<project>"'
  let s:sourcepaths = '-command c_sourcepaths -p "<project>"'
  let s:options = ['-p', '-t', '-s', '-x', '-i']
  let s:scopes = ['all', 'project']
  let s:types = [
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
    \ ]
  let s:contexts = [
      \ 'all',
      \ 'declarations',
      \ 'definitions',
      \ 'references'
    \ ]
" }}}

" Search(argline) {{{
" Executes a search.
function! eclim#c#search#Search(argline)
  return eclim#lang#Search(
    \ s:search_pattern, g:EclimCSearchSingleResult, a:argline)
endfunction " }}}

" FindInclude() {{{
" Finds the include file under the cursor
function eclim#c#search#FindInclude()
  if !eclim#project#util#IsCurrentFileInProject(1)
    return
  endif

  let file = substitute(getline('.'), '.*#include\s\+[<"]\(.*\)[>"].*', '\1', '')

  let project = eclim#project#util#GetCurrentProjectName()
  let command = substitute(s:includepaths, '<project>', project, '')
  let result =  eclim#ExecuteEclim(command)
  let paths = split(result, '\n')

  let command = substitute(s:sourcepaths, '<project>', project, '')
  let result =  eclim#ExecuteEclim(command)
  let paths += split(result, '\n')

  let dir = expand('%:p:h')
  if index(paths, dir) == -1
    call add(paths, dir)
  endif
  let results = split(globpath(join(paths, ','), file), '\n')

  if !empty(results)
    call eclim#util#SetLocationList(eclim#util#ParseLocationEntries(results))

    " single result in another file.
    if len(results) == 1 && g:EclimCSearchSingleResult != "lopen"
      let entry = getloclist(0)[0]
      call eclim#util#GoToBufferWindowOrOpen
        \ (bufname(entry.bufnr), g:EclimCSearchSingleResult)
      call eclim#display#signs#Update()
    else
      lopen
    endif
  else
    call eclim#util#EchoInfo("File not found.")
  endif
endfunction " }}}

" SearchContext() {{{
" Executes a contextual search.
function! eclim#c#search#SearchContext()
  if getline('.')[col('.') - 1] == '$'
    call cursor(line('.'), col('.') + 1)
    let cnum = eclim#util#GetCurrentElementColumn()
    call cursor(line('.'), col('.') - 1)
  else
    let cnum = eclim#util#GetCurrentElementColumn()
  endif

  if getline('.') =~ '#include\s\+[<"][A-Za-z0-9.]*\%' . cnum . 'c'
    call eclim#c#search#FindInclude()
    return
  "elseif getline('.') =~ '\<\(class\|????\)\s\+\%' . cnum . 'c'
  "  call eclim#c#search#Search('-x references')
    return
  endif

  if !eclim#c#search#Search('-x definitions')
    call eclim#c#search#Search('-x declarations')
  endif

endfunction " }}}

" CommandCompleteCSearch(argLead, cmdLine, cursorPos) {{{
" Custom command completion for CSearch
function! eclim#c#search#CommandCompleteCSearch(argLead, cmdLine, cursorPos)
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
