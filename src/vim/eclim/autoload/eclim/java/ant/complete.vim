" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/ant/complete.html
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

" Script Varables {{{
  let s:complete_command =
    \ '-command ant_complete -p "<project>" -f "<file>" -o <offset> -e <encoding>'
" }}}

" CodeComplete(findstart, base) {{{
" Handles ant code completion.
function! eclim#java#ant#complete#CodeComplete(findstart, base)
  if a:findstart
    " update the file before vim makes any changes.
    call eclim#java#ant#util#SilentUpdate()

    " locate the start of the word
    let line = getline('.')

    let start = col('.') - 1

    "exceptions that break the rule
    if line[start - 1] == '.'
      let start -= 1
    endif

    " always start in front of the the '<'
    if line[start] == '<'
      let start += 1
    endif

    while start > 0 && line[start - 1] =~ '\w'
      let start -= 1
    endwhile

    " if prev char is '/' then back off the start pos, since the completion
    " result will contain the '/'.
    if line[start - 1] == '/'
      let start -= 1
    endif

    return start
  else
    let offset = eclim#util#GetOffset() + len(a:base) - 1
    let project = eclim#project#util#GetCurrentProjectName()
    if project == ''
      return []
    endif

    let filename = eclim#project#util#GetProjectRelativeFilePath(expand("%:p"))

    let command = s:complete_command
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', filename, '')
    let command = substitute(command, '<offset>', offset, '')
    let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')

    let completions = []
    let results = split(eclim#ExecuteEclim(command), '\n')
    if len(results) == 1 && results[0] == '0'
      return
    endif

    " if the word has a '.' in it (like package completion) then we need to
    " strip some off according to what is currently in the buffer.
    let prefix = substitute(getline('.'),
      \ '.\{-}\([[:alnum:].]\+\%' . col('.') . 'c\).*', '\1', '')

    for result in results
      let word = substitute(result, '\(.\{-}\)|.*', '\1', '')
      " removed '<' and '>' from end tag results
      let word = substitute(word, '^<\(.*\)>$', '\1', '')

      let menu = substitute(result, '.\{-}|\(.*\)|.*', '\1', '')
      let menu = eclim#html#util#HtmlToText(menu)

      let info = substitute(result, '.*|\(.*\)', '\1', '')
      let info = eclim#html#util#HtmlToText(info)

      " strip off prefix if necessary.
      if word =~ '\.'
        let word = substitute(word, escape(prefix, '*'), '', '')
      endif

      let dict = {'word': word, 'menu': menu, 'info': info}

      call add(completions, dict)
    endfor

    return completions
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
