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
  let s:search = '-command php_search'
  let s:buildpaths = '-command dltk_buildpaths -p "<project>"'
  let s:options_map = {
      \ '-a': ['split', 'vsplit', 'edit', 'tabnew', 'lopen'],
      \ '-s': ['all', 'project'],
      \ '-i': [],
      \ '-p': [],
      \ '-t': ['class', 'function', 'constant'],
      \ '-x': ['all', 'declarations', 'references'],
    \ }
" }}}

function! eclim#php#search#Search(argline) " {{{
  return eclim#lang#Search(s:search, g:EclimPhpSearchSingleResult, a:argline)
endfunction " }}}

function! eclim#php#search#FindInclude(argline) " {{{
  if !eclim#project#util#IsCurrentFileInProject(1)
    return
  endif

  let file = substitute(getline('.'),
    \ ".*\\<\\(require\\|include\\)\\(_once\\)\\?\\s*[(]\\?['\"]\\([^'\"]*\\)['\"].*", '\3', '')

  let project = eclim#project#util#GetCurrentProjectName()
  let command = s:buildpaths
  let command = substitute(command, '<project>', project, '')
  let paths =  eclim#Execute(command)
  if type(paths) != g:LIST_TYPE
    return
  endif

  let results = split(globpath(expand('%:h') . ',' . join(paths, ','), file), '\n')

  if !empty(results)
    call eclim#util#SetLocationList(eclim#util#ParseLocationEntries(results))

    let [action_args, argline] = eclim#util#ExtractCmdArgs(a:argline, '-a:')
    let action = len(action_args) == 2 ? action_args[1] : g:EclimPhpSearchSingleResult

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

function! eclim#php#search#SearchContext(argline) " {{{
  if getline('.')[col('.') - 1] == '$'
    call cursor(line('.'), col('.') + 1)
    let cnum = eclim#util#GetCurrentElementColumn()
    call cursor(line('.'), col('.') - 1)
  else
    let cnum = eclim#util#GetCurrentElementColumn()
  endif

  if getline('.') =~ "\\<\\(require\\|include\\)\\(_once\\)\\?\\s*[(]\\?['\"][^'\"]*\\%" . cnum . "c"
    call eclim#php#search#FindInclude(a:argline)
    return
  elseif getline('.') =~ '\<\(class\|function\)\s\+\%' . cnum . 'c'
    call eclim#php#search#Search('-x references')
    return
  elseif getline('.') =~ "\\<define\\s*(['\"]\\%" . cnum . "c"
    call eclim#util#EchoInfo("TODO: Search constant references")
    return
  "elseif getline('.') =~ '\<var\s\+[$]\?\%' . cnum . 'c'
  "  call eclim#util#EchoInfo("TODO: Search var references")
  "  return
  endif

  call eclim#php#search#Search(a:argline . ' -x declarations')
endfunction " }}}

function! eclim#php#search#CommandCompleteSearch(argLead, cmdLine, cursorPos) " {{{
  return eclim#util#CommandCompleteOptions(
    \ a:argLead, a:cmdLine, a:cursorPos, s:options_map)
endfunction " }}}

function! eclim#php#search#CommandCompleteSearchContext(argLead, cmdLine, cursorPos) " {{{
  let options_map = {'-a': s:options_map['-a']}
  return eclim#util#CommandCompleteOptions(
    \ a:argLead, a:cmdLine, a:cursorPos, options_map)
endfunction " }}}

" vim:ft=vim:fdm=marker
