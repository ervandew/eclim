" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Dtd indent file using IndentAnything.
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
if &indentexpr =~ 'EclimGetDtdIndent' ||
    \ (!exists('b:disableOverride') && exists('g:EclimDtdIndentDisabled'))
  finish
endif

runtime indent/indentanything.vim

setlocal indentexpr=EclimGetDtdIndent(v:lnum)
setlocal indentkeys=o,O,*<Return>,<>>,<<>

" EclimGetDtdIndent(lnum) {{{
function! EclimGetDtdIndent(lnum)
  let adj = 0
  " handle case where previous line is a multi-line comment (<!-- -->) on one
  " line, which IndentAnything doesn't handle properly.
  let prevline = prevnonblank(a:lnum - 1)
  if getline(prevline) =~ '^\s\+<!--.\{-}-->'
    let adj = indent(prevline)
  endif
  return IndentAnything() + adj
endfunction " }}}

" DtdIndentAnythingSettings() {{{
function! DtdIndentAnythingSettings()
  " Syntax name REs for comments and strings.
  let b:blockCommentRE = 'dtdComment\|xmlComment'
  let b:commentRE      = b:blockCommentRE
  let b:lineCommentRE      = b:blockCommentRE
  let b:stringRE       = 'dtdString\|xmlString'
  let b:singleQuoteStringRE = b:stringRE
  let b:doubleQuoteStringRE = b:stringRE

  setlocal comments=sr:<!--,m:-,e:-->
  let b:blockCommentStartRE  = '<!--'
  let b:blockCommentMiddleRE = '-'
  let b:blockCommentEndRE    = '-->'
  let b:blockCommentMiddleExtra = 2

  " Indent another level for each non-closed element tag.
  let b:indentTrios = [
      \ [ '<\!\w', '', '>' ],
      \ [ '(', '', ')' ],
    \ ]
endfunction " }}}

call DtdIndentAnythingSettings()

" vim:ft=vim:fdm=marker
