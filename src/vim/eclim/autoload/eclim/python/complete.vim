" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/python/complete.html
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

" CodeComplete(findstart, base) {{{
" Handles python code completion.
function! eclim#python#complete#CodeComplete(findstart, base)
  if a:findstart
    " update the file before vim makes any changes.
    call eclim#util#ExecWithoutAutocmds('silent update')

    if !eclim#project#util#IsCurrentFileInProject(0) || !filereadable(expand('%'))
      return -1
    endif

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
    if !eclim#project#util#IsCurrentFileInProject(0) || !filereadable(expand('%'))
      return []
    endif

    let offset = eclim#python#rope#GetOffset() + len(a:base)
    let encoding = eclim#util#GetEncoding()
    let project = eclim#project#util#GetCurrentProjectRoot()
    let filename = eclim#project#util#GetProjectRelativeFilePath(expand('%:p'))

    let completions = []
    let results = eclim#python#rope#Completions(project, filename, offset, encoding)

    for result in results
      let menu = result[1]
      let info = ''
      if result[2] != ''
        let info = result[0] . '(' . result[2] . ')'
        let menu = info
      endif
      let dict = {
          \ 'word': result[0],
          \ 'menu': menu,
          \ 'info': info,
        \ }

      call add(completions, dict)
    endfor

    return completions
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
