" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/css/complete.html
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

" Script Varables {{{
  let s:complete_command =
    \ '-command css_complete -p "<project>" -f "<file>" -o <offset> -e <encoding>'
" }}}

" CodeComplete(findstart, base) {{{
" Handles css code completion.
function! eclim#css#complete#CodeComplete(findstart, base)
  if !eclim#project#util#IsCurrentFileInProject(0)
    return a:findstart ? -1 : []
  endif

  if a:findstart
    call eclim#lang#SilentUpdate(1)

    " locate the start of the word
    let line = getline('.')

    let start = col('.') - 1

    while start > 0 && line[start - 1] =~ '[[:alnum:]_-]'
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
    let results = eclim#ExecuteEclim(command)
    if type(results) != g:LIST_TYPE
      return
    endif

    let filter = 0
    for result in results
      let word = result.completion
      if word =~ '^:'
        let word = strpart(word, 1)
        let filter = 1
      endif

      let menu = result.menu
      let info = result.info

      let dict = {'word': tolower(word), 'menu': menu, 'info': info}

      call add(completions, dict)
    endfor

    " eclipse doesn't filter out :results properly.
    if filter
      call filter(completions, 'v:val.word =~ "^" . a:base')
    endif

    return completions
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
