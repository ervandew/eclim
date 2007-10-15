" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/python/validate.html
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

" Validate(on_save) {{{
" Validates the current file.
function! eclim#python#validate#Validate (on_save)
  if eclim#util#WillWrittenBufferClose()
    return
  endif

  "if !eclim#project#util#IsCurrentFileInProject(!a:on_save)
  "  return
  "endif

  let command = 'pyflakes "' . expand('%:p') . '"'
  let result = system(command)
  if v:shell_error
    call eclim#util#EchoError('Error running command: ' . command)
    return
  endif

  if result =~ ':'
    let errors = []

    for error in split(result, '\n')
      let file = substitute(error, '\(.\{-}\):[0-9]\+:.*', '\1', '')
      let line = substitute(error, '.\{-}:\([0-9]\+\):.*', '\1', '')
      let message = substitute(error, '.\{-}:[0-9]\+:\(.*\)', '\1', '')
      let dict = {
          \ 'filename': eclim#util#Simplify(file),
          \ 'lnum': line,
          \ 'text': message,
          \ 'type': 'e',
        \ }

      call add(errors, dict)
    endfor
    call eclim#util#SetLocationList(errors)
  else
    call eclim#util#SetLocationList([], 'r')
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
