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

" Script Variables {{{
  let s:warnings = '\(' . join([
      \ 'imported but unused',
    \ ], '\|') . '\)'
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

  let result = ''
  let syntax_error = eclim#python#validate#ValidateSyntax()

  if syntax_error == ''
    if !executable('pyflakes')
      if !exists('g:eclim_python_pyflakes_warn')
        call eclim#util#EchoWarning("Unable to find 'pyflakes' command.")
        let g:eclim_python_pyflakes_warn = 1
      endif
    else
      let command = 'pyflakes "' . expand('%:p') . '"'
      let result = system(command)
      if v:shell_error
        call eclim#util#EchoError('Error running command: ' . command)
        return
      endif
    endif
  endif

  if result =~ ':' || syntax_error != ''
    let results = split(result, '\n')
    call filter(results, "v:val !~ 'unable to detect undefined names'")

    let errors = []
    if syntax_error != ''
      call add(errors, {
          \ 'filename': eclim#util#Simplify(expand('%')),
          \ 'lnum': substitute(syntax_error, '.*(line \(\d\+\))', '\1', ''),
          \ 'text': substitute(syntax_error, '\(.*\)\s\+(line .*', '\1', ''),
          \ 'type': 'e'
        \ })
    endif

    if syntax_error == ''
      for error in results
        let file = substitute(error, '\(.\{-}\):[0-9]\+:.*', '\1', '')
        let line = substitute(error, '.\{-}:\([0-9]\+\):.*', '\1', '')
        let message = substitute(error, '.\{-}:[0-9]\+:\(.*\)', '\1', '')
        let dict = {
            \ 'filename': eclim#util#Simplify(file),
            \ 'lnum': line,
            \ 'text': message,
            \ 'type': message =~ s:warnings ? 'w' : 'e',
          \ }

        call add(errors, dict)
      endfor
    endif

    call eclim#util#SetLocationList(errors)
  else
    call eclim#util#SetLocationList([], 'r')
  endif
endfunction " }}}

" ValidateSyntax() {{{
function eclim#python#validate#ValidateSyntax ()
  let syntax_error = ''

python << EOF
import vim
from compiler import parseFile
try:
  parseFile(vim.eval('expand("%:p")'))
except SyntaxError, se:
  vim.command("let syntax_error = '%s'" % str(se))
EOF

  return syntax_error
endfunction " }}}

" vim:ft=vim:fdm=marker
