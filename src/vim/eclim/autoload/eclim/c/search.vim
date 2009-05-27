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
  let s:search_pattern = '-command c_search -n "<project>" <args>'
  let s:includepaths = '-command c_includepaths -p "<project>"'
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
      \ 'references'
    \ ]
" }}}

" Search(...) {{{
" Executes a search.
function! eclim#c#search#Search(...)
  if !eclim#project#util#IsCurrentFileInProject(1)
    return
  endif

  let argline = ""
  let index = 1
  while index <= a:0
    if index != 1
      let argline = argline . " "
    endif
    let argline = argline . a:{index}
    let index = index + 1
  endwhile

  if argline == ''
    call eclim#util#EchoError('You must supply a search pattern.')
    return
  endif

  " check if pattern supplied without -p.
  if argline !~ '^\s*-[a-z]'
    let argline = '-p ' . argline
  endif

  let project = eclim#project#util#GetCurrentProjectName()

  let search_cmd = s:search_pattern
  let search_cmd = substitute(search_cmd, '<project>', project, '')
  let search_cmd = substitute(search_cmd, '<args>', argline, '')
  " quote the search pattern
  let search_cmd =
    \ substitute(search_cmd, '\(.*-p\s\+\)\(.\{-}\)\(\s\|$\)\(.*\)', '\1"\2"\3\4', '')
  let result =  eclim#ExecuteEclim(search_cmd)
  let results = split(result, '\n')
  if len(results) == 1 && results[0] == '0'
    return
  endif

  if !empty(results)
    call eclim#util#SetLocationList(eclim#util#ParseLocationEntries(results))
    " if only one result and it's for the current file, just jump to it.
    " note: on windows the expand result must be escaped
    if len(results) == 1 && results[0] =~ escape(expand('%:p'), '\') . '|'
      if results[0] !~ '|1 col 1|'
        lfirst
      endif

    " single result in another file.
    elseif len(results) == 1 && g:EclimCSearchSingleResult != "lopen"
      let entry = getloclist(0)[0]
      call eclim#util#GoToBufferWindowOrOpen
        \ (bufname(entry.bufnr), g:EclimCSearchSingleResult)
      call eclim#util#SetLocationList(eclim#util#ParseLocationEntries(results))
      call eclim#display#signs#Update()

      call cursor(entry.lnum, entry.col)
    else
      lopen
    endif
  else
    let searchedFor = substitute(argline, '.*-p \(.\{-}\)\( .*\|$\)', '\1', '')
    call eclim#util#EchoInfo("Pattern '" . searchedFor . "' not found.")
  endif

endfunction " }}}

" FindDefinition(context) {{{
" Finds the defintion of the element under the cursor.
function eclim#c#search#FindDefinition(context)
  if !eclim#project#util#IsCurrentFileInProject(1)
    return
  endif

  " update the file.
  call eclim#util#ExecWithoutAutocmds('silent update')

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath(expand("%:p"))
  let position = eclim#util#GetCurrentElementPosition()
  let offset = substitute(position, '\(.*\);\(.*\)', '\1', '')
  let length = substitute(position, '\(.*\);\(.*\)', '\2', '')

  let search_cmd = s:search_element
  let search_cmd = substitute(search_cmd, '<project>', project, '')
  let search_cmd = substitute(search_cmd, '<file>', file, '')
  let search_cmd = substitute(search_cmd, '<offset>', offset, '')
  let search_cmd = substitute(search_cmd, '<length>', length, '')
  let search_cmd = substitute(search_cmd, '<context>', a:context, '')
  let search_cmd = substitute(search_cmd, '<encoding>', eclim#util#GetEncoding(), '')

  let result =  eclim#ExecuteEclim(search_cmd)
  let results = split(result, '\n')
  if len(results) == 1 && results[0] == '0'
    return
  endif

  if !empty(results)
    call eclim#util#SetLocationList(eclim#util#ParseLocationEntries(results))

    " if only one result and it's for the current file, just jump to it.
    " note: on windows the expand result must be escaped
    if len(results) == 1 && results[0] =~ escape(expand('%:p'), '\') . '|'
      if results[0] !~ '|1 col 1|'
        lfirst
      endif

    " single result in another file.
    elseif len(results) == 1 && g:EclimCSearchSingleResult != "lopen"
      let entry = getloclist(0)[0]
      call eclim#util#GoToBufferWindowOrOpen
        \ (bufname(entry.bufnr), g:EclimCSearchSingleResult)
      call eclim#util#SetLocationList(eclim#util#ParseLocationEntries(results))
      call eclim#display#signs#Update()

      call cursor(entry.lnum, entry.col)
    else
      lopen
    endif
  else
    call eclim#util#EchoInfo("Element not found.")
  endif
endfunction " }}}

" FindInclude() {{{
" Finds the include file under the cursor
function eclim#c#search#FindInclude()
  if !eclim#project#util#IsCurrentFileInProject(1)
    return
  endif

  let file = substitute(getline('.'), '.*#include\s\+<\(.*\)>.*', '\1', '')

  let project = eclim#project#util#GetCurrentProjectName()
  let command = substitute(s:includepaths, '<project>', project, '')
  let result =  eclim#ExecuteEclim(command)
  let paths = split(result, '\n')

  let results = split(globpath(expand('%:h') . ',' . join(paths, ','), file), '\n')

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

  if getline('.') =~ '#include\s\+<[A-Za-z0-9.]*\%' . cnum . 'c'
    call eclim#c#search#FindInclude()
    return
  "elseif getline('.') =~ '\<\(class\|????\)\s\+\%' . cnum . 'c'
  "  call eclim#c#search#FindDefinition('references')
    return
  endif

  call eclim#c#search#FindDefinition('declarations')

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
