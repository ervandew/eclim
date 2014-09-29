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
    \ '-o <offset> -e <encoding> -l <layout>'
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
    if line[start] == ':' && line[start - 1] != ':'
      let start -= 1
    endif

    while start > 0 && line[start - 1] =~ '\w'
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
    let command = substitute(command, '<layout>', g:EclimJavaCompleteLayout, '')

    let completions = []
    let response = eclim#Execute(command)
    if type(response) != g:DICT_TYPE
      return
    endif

    if has_key(response, 'error') && len(response.completions) == 0
      call eclim#util#EchoError(response.error.message)
      return -1
    endif

    " if the word has a '.' in it (like package completion) then we need to
    " strip some off according to what is currently in the buffer.
    let prefix = substitute(getline('.'),
      \ '.\{-}\([[:alnum:].]\+\%' . col('.') . 'c\).*', '\1', '')

    " as of eclipse 3.2 it will include the parens on a completion result even
    " if the file already has them.
    let open_paren = getline('.') =~ '\%' . col('.') . 'c\s*('
    let close_paren = getline('.') =~ '\%' . col('.') . 'c\s*(\s*)'

    " when completing imports, the completions include ending ';'
    let semicolon = getline('.') =~ '\%' . col('.') . 'c\s*;'

    for result in response.completions
      let word = result.completion

      " strip off prefix if necessary.
      if word =~ '\.'
        let word = substitute(word, prefix, '', '')
      endif

      " strip off close paren if necessary.
      if word =~ ')$' && close_paren
        let word = strpart(word, 0, strlen(word) - 1)
      endif

      " strip off open paren if necessary.
      if word =~ '($' && open_paren
        let word = strpart(word, 0, strlen(word) - 1)
      endif

      " strip off semicolon if necessary.
      if word =~ ';$' && semicolon
        let word = strpart(word, 0, strlen(word) - 1)
      endif

      " " if user wants case sensitivity, then filter out completions that don't
      " " match
      " if g:EclimJavaCompleteCaseSensitive && a:base != ''
      "   if word !~ '^' . a:base . '\C'
      "     continue
      "   endif
      " endif

      let menu = result.menu
      let info = eclim#html#util#HtmlToText(result.info)

      let dict = {
          \ 'word': word,
          \ 'menu': menu,
          \ 'info': info,
          \ 'kind': result.type,
          \ 'dup': 1,
        \ }
          " \ 'icase': !g:EclimJavaCompleteCaseSensitive,

      call add(completions, dict)
    endfor

    return completions
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker

