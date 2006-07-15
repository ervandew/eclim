" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/ant/complete.html
"
" License:
"
" Copyright (c) 2005 - 2006
"
" Licensed under the Apache License, Version 2.0 (the "License");
" you may not use this file except in compliance with the License.
" You may obtain a copy of the License at
"
"      http://www.apache.org/licenses/LICENSE-2.0
"
" Unless required by applicable law or agreed to in writing, software
" distributed under the License is distributed on an "AS IS" BASIS,
" WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
" See the License for the specific language governing permissions and
" limitations under the License.
"
" }}}

" Script Varables {{{
  let s:complete_command =
    \ '-filter vim -command ant_complete -p "<project>" -f "<file>" -o <offset>'
" }}}

" CodeComplete(findstart, base) {{{
" Handles ant code completion.
function! eclim#java#ant#complete#CodeComplete (findstart, base)
  if a:findstart
    " update the file before vim makes any changes.
    call eclim#java#ant#util#SilentUpdate()

    " locate the start of the word
    let line = getline('.')

    let start = col('.') - 1

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
    let offset = eclim#util#GetCharacterOffset() + len(a:base) - 1
    let project = eclim#project#GetCurrentProjectName()
    " as of now a valid project name is not necessary, but may be later.
    if project == ''
      let project = 'none'
    endif

    let filename = escape(expand('%:p'), '\')

    let command = s:complete_command
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', filename, '')
    let command = substitute(command, '<offset>', offset, '')

    let completions = []
    let results = split(eclim#ExecuteEclim(command), '\n')
    if len(results) == 1 && results[0] == '0'
      return
    endif
    for result in results
      let word = substitute(result, '\(.\{-}\)|.*', '\1', '')
      " removed '<' and '>' from end tag results
      let word = substitute(word, '^<\(.*\)>$', '\1', '')

      let menu = substitute(result, '.\{-}|\(.*\)|.*', '\1', '')
      let menu = substitute(menu, '<br/>', '', 'g')

      let info = substitute(result, '.*|\(.*\)', '\1', '')
      let info = eclim#html#util#HtmlToText(info)

      let dict = {'word': word, 'menu': menu, 'info': info}

      call add(completions, dict)
    endfor

    return completions
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
