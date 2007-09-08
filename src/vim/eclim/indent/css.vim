" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Css indent file using IndentAnything.
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

let b:did_indent = 1
if &indentexpr =~ 'EclimGetCssIndent'
  finish
endif

runtime indent/indentanything.vim

setlocal indentexpr=EclimGetCssIndent(v:lnum)
setlocal indentkeys=0{,0},!^F,o,O

" EclimGetCssIndent(lnum) {{{
function! EclimGetCssIndent (lnum)
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
function! CssIndentAnythingSettings ()
  " Syntax name REs for comments and strings.
  let b:commentRE      = 'cssComment'
  let b:lineCommentRE  = 'cssComment'
  let b:blockCommentRE = 'cssComment'
  let b:stringRE            = 'cssStringQ\(Q\)\?'
  "let b:singleQuoteStringRE = 'javaScriptStringS'
  "let b:doubleQuoteStringRE = 'javaScriptStringD'

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
