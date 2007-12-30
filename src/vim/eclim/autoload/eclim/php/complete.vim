" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/php/complete.html
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
    \ '-command php_complete -p "<project>" -f "<file>" -o <offset>'
" }}}

" CodeComplete(findstart, base) {{{
" Handles php code completion.
function! eclim#php#complete#CodeComplete (findstart, base)
  let line = line('.')
  let phpstart = search('<?php', 'bcnW')
  let phpend = search('?>', 'bcnW', line('w0'))
  if phpstart == 0 || (phpend != 0 && line > phpend)
    return eclim#html#complete#CodeComplete(a:findstart, a:base)
  endif

  if a:findstart
    " update the file before vim makes any changes.
    call eclim#util#ExecWithoutAutocmds('silent update')

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
    if !eclim#project#util#IsCurrentFileInProject()
      return []
    endif

    let offset = eclim#util#GetCharacterOffset() + len(a:base)
    let project = eclim#project#util#GetCurrentProjectName()
    let filename = eclim#project#util#GetProjectRelativeFilePath(expand('%:p'))

    let command = s:complete_command
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', filename, '')
    let command = substitute(command, '<offset>', offset, '')

    let completions = []
    let results = split(eclim#ExecuteEclim(command), '\n')
    if len(results) == 1 && results[0] == '0'
      return
    endif

    " as of eclipse 3.2 it will include the parens on a completion result even
    " if the file already has them.
    let open_paren = getline('.') =~ '\%' . col('.') . 'c\s*('
    let close_paren = getline('.') =~ '\%' . col('.') . 'c\s*(\s*)'

    for result in results
      let word = substitute(result, '\(.\{-}\)|.*', '\1', '')
      let menu = substitute(result, '.\{-}|\(.\{-}\)|.*', '\1', '')
      let info = substitute(result, '.\{-}|.\{-}|\(.\{-}\)', '\1', '')
      let info = eclim#html#util#HtmlToText(info)

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
          \ 'menu': menu,
          \ 'info': info,
        \ }

      call add(completions, dict)
    endfor

    return completions
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
