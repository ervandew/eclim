" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Dtd indent file using IndentAnything.
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

" Only load this indent file when no other was loaded.
if exists("b:dtd_did_indent")
  finish
endif

let b:dtd_did_indent = 1

setlocal indentexpr=GetDtdIndent(v:lnum)
setlocal indentkeys=o,O,*<Return>,<>>,<<>

" GetDtdIndent(lnum) {{{
function! GetDtdIndent (lnum)
  return IndentAnything()
endfunction " }}}

" DtdIndentAnythingSettings() {{{
function! DtdIndentAnythingSettings ()
  " Syntax name REs for comments and strings.
  let b:blockCommentRE = 'dtdComment'
  let b:commentRE      = b:blockCommentRE
  let b:stringRE       = 'dtdString'
  let b:singleQuoteStringRE = b:stringRE
  let b:doubleQuoteStringRE = b:stringRE

  setl comments=sr:<!--,m:-,e:-->
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
