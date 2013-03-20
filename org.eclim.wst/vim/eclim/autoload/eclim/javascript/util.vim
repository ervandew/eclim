" Author:  Eric Van Dewoestine
"
" Description: {{{
"
" License:
"
" Copyright (C) 2005 - 2013  Eric Van Dewoestine
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

" Global Variables {{{
  if !exists("g:EclimJavascriptLintEnabled")
    " enabling by default until jsdt validation is mature enough to use.
    "let g:EclimJavascriptLintEnabled = 0
    let g:EclimJavascriptLintEnabled = 1
  endif

  if !exists('g:EclimJavascriptLintConf')
    let g:EclimJavascriptLintConf = eclim#UserHome() . '/.jslrc'
  endif
" }}}

" Script Variables {{{
  let s:warnings = '\(' . join([
      \ 'imported but unused',
    \ ], '\|') . '\)'
" }}}

function! eclim#javascript#util#UpdateSrcFile(on_save) " {{{
  " Optional arg:
  "   validate: when 1 force the validation to execute, when 0 prevent it.

  " Disabled until the jsdt matures.
  "if !a:on_save
  "  call eclim#lang#UpdateSrcFile('javascript', 1)
  "else
  "  call eclim#lang#UpdateSrcFile('javascript')
  "endif

  let validate = !a:on_save || (
    \ g:EclimJavascriptValidate &&
    \ (!exists('g:EclimFileTypeValidate') || g:EclimFileTypeValidate))

  if validate && g:EclimJavascriptLintEnabled
    call eclim#javascript#util#Jsl()
  endif
endfunction " }}}

function! eclim#javascript#util#Jsl() " {{{
  " Runs jsl (javascript lint) on the current file.

  if eclim#util#WillWrittenBufferClose()
    return
  endif

  let result = ''

  if !executable('jsl')
    if !exists('g:eclim_javascript_jsl_warn')
      call eclim#util#EchoWarning("Unable to find 'jsl' command.")
      let g:eclim_javascript_jsl_warn = 1
    endif
  else
    if !exists('g:EclimJavascriptLintVersion')
      call eclim#util#System('jsl --help')
      let g:EclimJavascriptLintVersion = v:shell_error == 2 ? 'c' : 'python'
    endif

    let conf = expand(g:EclimJavascriptLintConf)

    " the c version
    if g:EclimJavascriptLintVersion == 'c'
      let command = 'jsl -process "' . expand('%:p') . '"'
      if filereadable(conf)
        let command .= ' -conf "' . conf . '"'
      endif

    " the new python version
    else
      let command = 'jsl "' . expand('%:p') . '"'
      if filereadable(conf)
        let command .= ' --conf "' . conf . '"'
      endif
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
            \ 'text': "[jsl] " . message,
            \ 'type': error =~ ': \(lint \)\?warning:' ? 'w' : 'e',
          \ }

        call add(errors, dict)
      endif
    endfor

    call eclim#display#signs#SetPlaceholder()
    call eclim#util#ClearLocationList('jsl')
    if &ft == 'javascript'
      call eclim#util#SetLocationList(errors)
    else
      call eclim#util#SetLocationList(errors, 'a')
    endif
    call eclim#display#signs#RemovePlaceholder()
  else
    call eclim#util#ClearLocationList('jsl')
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
