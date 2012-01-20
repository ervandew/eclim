" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/python/complete.html
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

" CodeComplete(findstart, base) {{{
" Handles python code completion.
function! eclim#python#complete#CodeComplete(findstart, base)
  if !eclim#project#util#IsCurrentFileInProject(0)
    return a:findstart ? -1 : []
  endif

  if a:findstart
    call eclim#lang#SilentUpdate(1)

    " locate the start of the word
    let line = getline('.')

    let start = col('.') - 1

    "exceptions that break the rule
    if line[start] =~ '\.'
      let start -= 1
    endif

    while start > 0 && line[start - 1] =~ '\w'
      let start -= 1
    endwhile

    return start
  else
    let offset = eclim#python#rope#GetOffset() + len(a:base)
    let encoding = eclim#util#GetEncoding()
    let project = eclim#project#util#GetCurrentProjectRoot()
    let file = eclim#lang#SilentUpdate(1, 0)
    if file == ''
      return []
    endif

    let completions = []
    let results = eclim#python#rope#Completions(project, file, offset, encoding)

    let open_paren = getline('.') =~ '\%' . col('.') . 'c\s*('
    let close_paren = getline('.') =~ '\%' . col('.') . 'c\s*(\s*)'

    for result in results
      let word = result[0]
      let kind = result[1]
      let info = ''
      if result[2] != ''
        let word .= '('
        let info = result[0] . '(' . result[2] . ')'
        let menu = info
      else
        if kind == 'f'
          let word .= '()'
        endif
        let menu = word
      endif

      " map 'a' (attribute) to 'v'
      if kind == 'a'
        let kind = 'v'

      " map 'c' (class) to 't'
      elseif kind == 'c'
        let kind = 't'
      endif

      " strip off close paren if necessary.
      if word =~ ')$' && close_paren
        let word = strpart(word, 0, strlen(word) - 1)
      endif

      " strip off open paren if necessary.
      if word =~ '($' && open_paren
        let word = strpart(word, 0, strlen(word) - 1)
      endif

      let dict = {
          \ 'word': word,
          \ 'kind': kind,
          \ 'menu': menu,
          \ 'info': info,
        \ }

      call add(completions, dict)
    endfor

    return completions
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
