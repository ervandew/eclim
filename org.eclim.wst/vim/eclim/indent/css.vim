" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Css indent file using IndentAnything.
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

let b:did_indent = 1
if &indentexpr =~ 'EclimGetCssIndent' ||
    \ (!exists('b:disableOverride') && exists('g:EclimCssIndentDisabled'))
  finish
endif

runtime indent/indentanything.vim

setlocal indentexpr=EclimGetCssIndent(v:lnum)
setlocal indentkeys=0{,0},!^F,o,O

" EclimGetCssIndent(lnum) {{{
function! EclimGetCssIndent(lnum)
  let adj = 0
  let prevline = prevnonblank(a:lnum - 1)

  " handle case where previous line is a multi-line comment (/* */) on one
  " line, which IndentAnything doesn't handle properly.
  if getline(prevline) =~ '^\s\+/\*.\{-}\*/\s*$'
    let adj = indent(prevline)
  endif

  return IndentAnything() + adj
endfunction " }}}

" CssIndentAnythingSettings() {{{
function! CssIndentAnythingSettings()
  " Syntax name REs for comments and strings.
  let b:commentRE      = 'cssComment'
  let b:lineCommentRE  = 'cssComment'
  let b:blockCommentRE = 'cssComment'
  let b:stringRE            = 'cssStringQ\(Q\)\?'

  " Setup for C-style comment indentation.
  let b:blockCommentStartRE  = '/\*'
  let b:blockCommentMiddleRE = '\*'
  let b:blockCommentEndRE    = '\*/'
  let b:blockCommentMiddleExtra = 1

  " Indent another level for each non-closed paren/'(' and brace/'{' on the
  " previous line.
  let b:indentTrios = [
        \ [ '{', '', '}' ]
  \ ]
endfunction " }}}

call CssIndentAnythingSettings()

" vim:ft=vim:fdm=marker
