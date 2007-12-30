" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/css/complete.html
"
" License:
"
" Copyright (c) 2005 - 2008
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
    \ '-command css_complete -p "<project>" -f "<file>" -o <offset>'
" }}}

" CodeComplete(findstart, base) {{{
" Handles css code completion.
function! eclim#css#complete#CodeComplete (findstart, base)
  if a:findstart
    update

    " locate the start of the word
    let line = getline('.')

    let start = col('.') - 1

    while start > 0 && line[start - 1] =~ '[[:alnum:]_-]'
      let start -= 1
    endwhile

    return start
  else
    if !eclim#project#util#IsCurrentFileInProject()
      return []
    endif

    let offset = eclim#util#GetCharacterOffset() + len(a:base)
    let project = eclim#project#util#GetCurrentProjectName()
    let filename = eclim#project#util#GetProjectRelativeFilePath(expand("%:p"))

    let command = s:complete_command
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', filename, '')
    let command = substitute(command, '<offset>', offset, '')

    let completions = []
    let results = split(eclim#ExecuteEclim(command), '\n')
    if len(results) == 1 && results[0] == '0'
      return
    endif

    let filter = 0
    for result in results
      let word = substitute(result, '\(.\{-}\)|.*', '\1', '')
      if word =~ '^:'
        let word = strpart(word, 1)
        let filter = 1
      endif

      let menu = substitute(result, '.\{-}|\(.*\)|.*', '\1', '')
      let info = substitute(result, '.*|\(.*\)', '\1', '')

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
