" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/python/validate.html
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

" Script Variables {{{
  let s:warnings = '\(' . join([
      \ 'imported but unused',
      \ 'local variable .* assigned to but never used',
    \ ], '\|') . '\)'
" }}}

" Validate(on_save) {{{
" Validates the current file.
function! eclim#python#validate#Validate(on_save)
  if eclim#util#WillWrittenBufferClose()
    return
  endif

  "if !eclim#project#util#IsCurrentFileInProject(!a:on_save)
  "  return
  "endif

  let results = []
  let syntax_error = eclim#python#validate#ValidateSyntax()

  if syntax_error == ''
    if !executable('pyflakes')
      if !exists('g:eclim_python_pyflakes_warn')
        call eclim#util#EchoWarning("Unable to find 'pyflakes' command.")
        let g:eclim_python_pyflakes_warn = 1
      endif
    else
      let command = 'pyflakes "' . expand('%:p') . '"'
      let results = split(eclim#util#System(command), '\n')
      if v:shell_error > 1 " pyflakes returns 1 if there where warnings.
        call eclim#util#EchoError('Error running command: ' . command)
        let results = []
      endif
    endif

    " rope validation
    " currently too slow for running on every save.
    if eclim#project#util#IsCurrentFileInProject(0) && !a:on_save
      let project = eclim#project#util#GetCurrentProjectRoot()
      let filename = eclim#project#util#GetProjectRelativeFilePath(expand('%:p'))
      let rope_results = eclim#python#rope#Validate(project, filename)
      " currently rope gets confused with iterator var on list comprehensions
      let rope_results = filter(rope_results, "v:val !~ '^Unresolved variable'")
      let results += rope_results
    endif
  endif

  if !empty(results) || syntax_error != ''
    call filter(results, "v:val !~ 'unable to detect undefined names'")

    let errors = []
    if syntax_error != ''
      let lnum = substitute(syntax_error, '.*(line \(\d\+\))', '\1', '')
      let text = substitute(syntax_error, '\(.*\)\s\+(line .*', '\1', '')
      if lnum == syntax_error
        let lnum = 1
        let text .= ' (unknown line)'
      endif
      call add(errors, {
          \ 'filename': eclim#util#Simplify(expand('%')),
          \ 'lnum': lnum,
          \ 'text': text,
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
    call eclim#util#ClearLocationList()
  endif
endfunction " }}}

" ValidateSyntax() {{{
function eclim#python#validate#ValidateSyntax()
  let syntax_error = ''

  if has('python')

python << EOF
import re, vim
from compiler import parseFile
try:
  parseFile(vim.eval('expand("%:p")'))
except SyntaxError, se:
  vim.command("let syntax_error = \"%s\"" % re.sub(r'"', r'\"', str(se)))
EOF

  endif

  return syntax_error
endfunction " }}}

" PyLint() {{{
function eclim#python#validate#PyLint()
  let file = expand('%:p')

  if !executable('pylint')
    call eclim#util#EchoError("Unable to find 'pylint' command.")
    return
  endif

  let pylint_env = ''
  if exists('g:EclimPyLintEnv')
    let pylint_env = g:EclimPyLintEnv
  else
    let paths = []

    let django_dir = eclim#python#django#util#GetProjectPath()
    if django_dir != ''
      call add(paths, fnamemodify(django_dir, ':h'))
      let settings = fnamemodify(django_dir, ':t')
      if has('win32') || has('win64')
        let pylint_env =
          \ 'set DJANGO_SETTINGS_MODULE='. settings . '.settings && '
      else
        let pylint_env =
          \ 'DJANGO_SETTINGS_MODULE="'. settings . '.settings" '
      endif
    endif

    if eclim#project#util#IsCurrentFileInProject(0)
      let project = eclim#project#util#GetCurrentProjectRoot()
      let paths += eclim#python#rope#GetSourceDirs(project)
    endif

    if !empty(paths)
      if has('win32') || has('win64')
        let pylint_env .= 'set "PYTHONPATH=' . join(paths, ';') . '" && '
      else
        let pylint_env .= 'PYTHONPATH="$PYTHONPATH:' . join(paths, ':') . '"'
      endif
    endif
  endif

  let command = pylint_env . ' pylint --reports=n "' . file . '"'
  if has('win32') || has('win64')
    let command = 'cmd /c "' . command . '"'
  endif

  call eclim#util#Echo('Running pylint (ctrl-c to cancel) ...')
  let result = eclim#util#System(command)
  call eclim#util#Echo(' ')
  if v:shell_error
    call eclim#util#EchoError('Error running command: ' . command)
    return
  endif

  if result =~ ':'
    let errors = []
    for error in split(result, '\n')
      if error =~ '^[CWERF]\(: \)\?[0-9]'
        let line = substitute(error, '.\{-}:\s*\([0-9]\+\):.*', '\1', '')
        let message = substitute(error, '.\{-}:\s*[0-9]\+:\(.*\)', '\1', '')
        let dict = {
            \ 'filename': eclim#util#Simplify(file),
            \ 'lnum': line,
            \ 'text': message,
            \ 'type': error =~ '^E' ? 'e' : 'w',
          \ }

        call add(errors, dict)
      endif
    endfor
    call eclim#util#SetQuickfixList(errors)
  else
    call eclim#util#SetQuickfixList([], 'r')
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
