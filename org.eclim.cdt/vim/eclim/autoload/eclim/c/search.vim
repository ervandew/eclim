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
  let s:search = '-command c_search'
  let s:includepaths = '-command c_includepaths -p "<project>"'
  let s:sourcepaths = '-command c_sourcepaths -p "<project>"'
  let s:options_map = {
      \ '-a': ['split', 'vsplit', 'edit', 'tabnew', 'lopen'],
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
  let results = split(globpath(join(paths, ','), file), '\n')

  if !empty(results)
    call eclim#util#SetLocationList(eclim#util#ParseLocationEntries(results))

    let [action_args, argline] = eclim#util#ExtractCmdArgs(a:argline, '-a:')
    let action = len(action_args) == 2 ? action_args[1] : g:EclimCSearchSingleResult

    " single result in another file.
    if len(results) == 1 && action != "lopen"
      let entry = getloclist(0)[0]
      call eclim#util#GoToBufferWindowOrOpen(bufname(entry.bufnr), action)
      call eclim#display#signs#Update()

    " multiple results and user specified an action other than lopen
    elseif len(results) && len(action_args) && action != 'lopen'
      let locs = getloclist(0)
      let files = map(copy(locs),  'printf(' .
        \ '"%s|%s col %s| %s", ' .
        \ 'bufname(v:val.bufnr), v:val.lnum, v:val.col, v:val.text)')
      let response = eclim#util#PromptList(
        \ 'Please choose the file to ' . action,
        \ files, g:EclimHighlightInfo)
      if response == -1
        return
      endif
      let entry = locs[response]
      let name = substitute(bufname(entry.bufnr), '\', '/', 'g')
      call eclim#util#GoToBufferWindowOrOpen(name, action)
      call eclim#display#signs#Update()
      call cursor(entry.lnum, entry.col)

    else
      exec 'lopen ' . g:EclimLocationListHeight
    endif
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
