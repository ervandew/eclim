" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Dtd indent file using IndentAnything.
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

let b:did_indent = 1
if &indentexpr =~ 'EclimGetDtdIndent' ||
    \ (!exists('b:disableOverride') && exists('g:EclimDtdIndentDisabled'))
  finish
endif

runtime indent/indentanything.vim

setlocal indentexpr=EclimGetDtdIndent(v:lnum)
setlocal indentkeys=o,O,*<Return>,<>>,<<>

" EclimGetDtdIndent(lnum) {{{
function! EclimGetDtdIndent (lnum)
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
function! DtdIndentAnythingSettings ()
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
