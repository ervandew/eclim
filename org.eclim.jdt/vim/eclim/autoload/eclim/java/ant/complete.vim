" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/ant/complete.html
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
    \ '-command ant_complete -p "<project>" -f "<file>" -o <offset> -e <encoding>'
  let s:command_targets = '-command ant_targets -p "<project>" -f "<file>"'
" }}}

" CodeComplete(findstart, base) {{{
" Handles ant code completion.
function! eclim#java#ant#complete#CodeComplete(findstart, base)
  if !eclim#project#util#IsCurrentFileInProject(0)
    return a:findstart ? -1 : []
  endif

  if a:findstart
    call eclim#lang#SilentUpdate(1)

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
    if type(results) != 3
      return
    endif

    " if the word has a '.' in it (like package completion) then we need to
    " strip some off according to what is currently in the buffer.
    let prefix = substitute(getline('.'),
      \ '.\{-}\([[:alnum:].]\+\%' . col('.') . 'c\).*', '\1', '')

    for result in results
      let word = result.completion
      " removed '<' and '>' from end tag results
      let word = substitute(word, '^<\(.*\)>$', '\1', '')

      " strip off prefix if necessary.
      if word =~ '\.'
        let word = substitute(word, escape(prefix, '*'), '', '')
      endif

      let menu = eclim#html#util#HtmlToText(result.menu)
      let info = eclim#html#util#HtmlToText(result.info)

      let dict = {'word': word, 'menu': menu, 'info': info}

      call add(completions, dict)
    endfor

    return completions
  endif
endfunction " }}}

" CommandCompleteTarget(argLead, cmdLine, cursorPos) {{{
" Custom command completion for ant targets.
function! eclim#java#ant#complete#CommandCompleteTarget(argLead, cmdLine, cursorPos)
  let project = eclim#project#util#GetCurrentProjectName()
  if project == ''
    return []
  endif

  let file = eclim#java#ant#util#FindBuildFile()
  if project != "" && file != ""
    let file = eclim#project#util#GetProjectRelativeFilePath(file)
    let command = s:command_targets
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')

    let targets = eclim#ExecuteEclim(command)
    if type(targets) != 3
      return []
    endif

    let cmdTail = strpart(a:cmdLine, a:cursorPos)
    let argLead = substitute(a:argLead, cmdTail . '$', '', '')
    call filter(targets, 'v:val =~ "^' . argLead . '"')

    return targets
  endif

  return []
endfunction " }}}

" vim:ft=vim:fdm=marker
