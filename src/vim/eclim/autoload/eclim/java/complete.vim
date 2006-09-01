" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/complete.html
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

" Global Varables {{{
  if !exists("g:EclimJavaCompleteLayout")
    if &completeopt !~ 'preview' && &completeopt =~ 'menu'
      let g:EclimJavaCompleteLayout = 'standard'
    else
      let g:EclimJavaCompleteLayout = 'compact'
    endif
  endif
" }}}

" Script Varables {{{
  let s:complete_command =
    \ '-filter vim -command java_complete ' .
    \ '-p "<project>" -f "<file>" -o <offset> -l <layout>'
" }}}

" CodeComplete(findstart, base) {{{
" Handles java code completion.
function! eclim#java#complete#CodeComplete (findstart, base)
  if a:findstart
    " update the file before vim makes any changes.
    call eclim#java#util#SilentUpdate()

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
    if !eclim#project#IsCurrentFileInProject()
      return []
    endif

    let offset = eclim#util#GetCharacterOffset() + len(a:base)
    let project = eclim#project#GetCurrentProjectName()
    let filename = eclim#java#util#GetFilename()

    let command = s:complete_command
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', filename, '')
    let command = substitute(command, '<offset>', offset, '')
    let command = substitute(command, '<layout>', g:EclimJavaCompleteLayout, '')

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
      let kind = substitute(result, '\(.\{-}\)|.*', '\1', '')

      let word = substitute(result, '.\{-}|\(.\{-}\)|.*', '\1', '')
      let menu = substitute(result, '.\{-}|.\{-}|\(.\{-}\)|.*', '\1', '')

      let info = substitute(result, '.\{-}|.\{-}|.\{-}|\(.*\)', '\1', '')
      let info = eclim#html#util#HtmlToText(info)

      " strip off prefix if necessary.
      if word =~ '\.'
        let word = substitute(word, prefix, '', '')
      endif

      let dict = {
          \ 'word': word,
          \ 'menu': menu,
          \ 'info': info,
          \ 'kind': kind,
          \ 'dup': 1
        \ }

      call add(completions, dict)
    endfor

    return completions
  endif
endfunction " }}}

" CompletionFilter(filter) {{{
" Filter current completions.
"function! eclim#java#complete#CompletionFilter (filter)
"  let start = JavaCodeComplete(1, "")
"  while col('.') > start + 1
"    normal <BS>
"  endwhile
"  echom " #### filter = " . a:filter . " start = " . start
"  return "\<C-X>\<C-U>"
"endfunction " }}}

" vim:ft=vim:fdm=marker
