" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/import.html
"
" License:
"
" Copyright (C) 2005 - 2012  Eric Van Dewoestine
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

" Script Variables {{{
let s:command_import =
  \ '-command java_import -p "<project>" -f "<file>" -o <offset> -e <encoding>'
let s:command_organize =
  \ '-command java_import_organize -p "<project>" -f "<file>" -o <offset> -e <encoding>'
" }}}

function! eclim#java#import#Import(...) " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  if !a:0
    let name = expand('<cword>')
    if !eclim#java#util#IsValidIdentifier(name) ||
       \ eclim#java#util#IsKeyword(name)
      call eclim#util#EchoError("'" . name . "' not a classname.")
      return
    endif
  endif

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#lang#SilentUpdate()
  let offset = eclim#util#GetOffset()
  let command = s:command_import
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<offset>', offset, '')
  let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')
  if a:0
    let command .= ' -t ' . a:1
  endif
  let result = eclim#ExecuteEclim(command)

  if type(result) == g:STRING_TYPE
    call eclim#util#EchoError(result)
    return
  endif

  if type(result) == g:DICT_TYPE
    call eclim#util#Reload({'pos': [result.line, result.column]})
    call eclim#lang#UpdateSrcFile('java', 1)
    if result.offset != offset
      call eclim#util#Echo('Imported ' . (a:0 ? a:1 : ''))
    endif
    return
  endif

  if type(result) != g:LIST_TYPE
    return
  endif

  let choice = eclim#java#import#ImportPrompt(result)
  if choice != ''
    call eclim#java#import#Import(choice)
  endif
endfunction " }}}

function! eclim#java#import#OrganizeImports(...) " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#lang#SilentUpdate()
  let offset = eclim#util#GetOffset()
  let command = s:command_organize
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<offset>', offset, '')
  let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')
  if a:0
    let command .= ' -t ' . join(a:1, ',')
  endif
  let result = eclim#ExecuteEclim(command)

  if type(result) == g:STRING_TYPE
    call eclim#util#EchoError(result)
    return
  endif

  if type(result) == g:DICT_TYPE
    call eclim#util#Reload({'pos': [result.line, result.column]})
    call eclim#lang#UpdateSrcFile('java', 1)
    return
  endif

  if type(result) != g:LIST_TYPE
    return
  endif

  let chosen = []
  for choices in result
    let choice = eclim#java#import#ImportPrompt(choices)
    if choice == ''
      return
    endif
    call add(chosen, choice)
  endfor

  if len(chosen)
    call eclim#java#import#OrganizeImports(chosen)
  endif
endfunction " }}}

function! eclim#java#import#ImportPrompt(choices) " {{{
  " prompt the user to choose the class to import.
  let response = eclim#util#PromptList("Choose the class to import", a:choices)
  if response == -1
    return ''
  endif

  return get(a:choices, response)
endfunction " }}}

" vim:ft=vim:fdm=marker
