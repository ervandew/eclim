" Author:  Daniel Leong
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
  let s:complete_command =
    \ '-command android_xml_complete -p "<project>" -f "<file>" ' .
    \ '-o <offset> -e <encoding>'
" }}}

function! eclim#android#xml#complete#CodeComplete(findstart, base) " {{{
  if !eclim#project#util#IsCurrentFileInProject(0)
    return a:findstart ? -1 : []
  endif

  if a:findstart
    call eclim#lang#SilentUpdate(1)

    " locate the start of the word
    let line = getline('.')

    let start = col('.') - 1

    "exceptions that break the rule
    if line[start] == ':' || line[start] == '/' || line[start] == '@'
      let start -= 1
    endif

    while start > 0 && line[start - 1] =~ '[0-9A-Za-z_/@]'
      let start -= 1
    endwhile

    return start
  else
    let offset = eclim#util#GetOffset() + len(a:base)
    let project = eclim#project#util#GetCurrentProjectName()
    let file = eclim#lang#SilentUpdate(1, 0)
    if file == ''
      return []
    endif

    let command = s:complete_command
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')
    let command = substitute(command, '<offset>', offset, '')
    let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')

    let completions = []
    let response = eclim#Execute(command)
    if type(response) != g:DICT_TYPE
      return
    endif

    if has_key(response, 'error') && len(response.completions) == 0
      call eclim#util#EchoError(response.error.message)
      return -1
    endif

    " attribute namespaces, if already typed, should be stripped
    let attribPrefix = substitute(getline('.'),
      \ '.\{-}\([[:alnum:]:]\+\%' . col('.') . 'c\).*', '\1', '')

    " also, already-typed part of resources
    let resPrefix = substitute(getline('.'),
      \ '.\{-}\([[:alnum:]@+/]\+\%' . col('.') . 'c\).*', '\1', '')

    for result in response.completions
      let word = result.completion

      " strip off prefixes if necessary.
      if word =~ '\:'
        let word = substitute(word, attribPrefix, '', '')
      endif

      if word =~ '@'
        let word = substitute(word, resPrefix, '', '')
      endif

      " strip off trailing slash; if they type it
      "  with YCM, it will start autocomplete
      if word =~ '/$' 
        let word = strpart(word, 0, strlen(word) - 1)
      endif

      let menu = result.menu
      let info = eclim#html#util#HtmlToText(result.info)

      let dict = {
          \ 'word': word,
          \ 'abbr': result.completion,
          \ 'menu': menu,
          \ 'info': info,
          \ 'kind': result.type,
          \ 'dup': 1,
        \ }

      call add(completions, dict)
    endfor

    return completions
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker

