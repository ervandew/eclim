" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/javascript/validate.html
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

" Global Variables {{{
  if !exists('g:EclimJavascriptLintConf')
    let g:EclimJavascriptLintConf = '~/.jslrc'
  endif
" }}}

" Script Variables {{{
  let s:warnings = '\(' . join([
      \ 'imported but unused',
    \ ], '\|') . '\)'
" }}}

" Validate(on_save) {{{
" Validates the current file.
function! eclim#javascript#validate#Validate (on_save)
  if eclim#util#WillWrittenBufferClose()
    return
  endif

  "if !eclim#project#util#IsCurrentFileInProject(!a:on_save)
  "  return
  "endif

  let result = ''

  if !executable('jsl')
    if !exists('g:eclim_javascript_jsl_warn')
      call eclim#util#EchoWarning("Unable to find 'jsl' command.")
      let g:eclim_javascript_jsl_warn = 1
    endif
  else
    let command = 'jsl -process "' . expand('%:p') . '"'
    let conf = expand(g:EclimJavascriptLintConf)
    if filereadable(conf)
      let command .= ' -conf "' . conf . '"'
    endif
    let result = eclim#util#System(command)
    if v:shell_error == 2 "|| v:shell_error == 4
      call eclim#util#EchoError('Error running command: ' . command)
      return
    endif
  endif

  if result =~ ':'
    let results = split(result, '\n')
    let errors = []
    for error in results
      if error =~ '.\{-}(\d\+): .\{-}: .\{-}'
        let file = substitute(error, '\(.\{-}\)([0-9]\+):.*', '\1', '')
        let line = substitute(error, '.\{-}(\([0-9]\+\)):.*', '\1', '')
        let message = substitute(error, '.\{-}([0-9]\+):.\{-}: \(.*\)', '\1', '')
        let dict = {
            \ 'filename': eclim#util#Simplify(file),
            \ 'lnum': line,
            \ 'text': message,
            \ 'type': error =~ ': \(lint \)\?warning:' ? 'w' : 'e',
          \ }

        call add(errors, dict)
      endif
    endfor

    call eclim#util#SetLocationList(errors)
  else
    call eclim#util#SetLocationList([], 'r')
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
