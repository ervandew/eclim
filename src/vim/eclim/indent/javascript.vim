" Author: Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Javascript indent file using IndentAnything.
"   Based on initial version developed by:
"     Tye Z. <zdro@yahoo.com>
"   The version accounts for a couple edge cases not handled in the ideal
"   manner by IndentAnything.
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
if &indentexpr =~ 'EclimGetJavascriptIndent'
  finish
endif

runtime indent/indentanything.vim

setlocal indentexpr=EclimGetJavascriptIndent(v:lnum)
setlocal indentkeys+=0),0},),;

" EclimGetJavascriptIndent(lnum) {{{
function! EclimGetJavascriptIndent (lnum)
  let line = getline(a:lnum)
  let prevlnum = prevnonblank(a:lnum - 1)
  let prevline = getline(prevlnum)
  let pattern_heads = '\(' . join(map(copy(b:indentTrios), 'v:val[0]'), '\|') . '\)'

  for trio in b:indentTrios
    " if the current line starts with any of the ending trios, then set the
    " current line indent to the same indent as the line starting that trio.
    if line =~ '^\s*' . trio[2]
      let col = col('.')
      call cursor(0, 1)
      let matchstart = searchpair(trio[0], '', trio[2], 'bnW', 'InCommentOrString()')
      call cursor(0, col)

      if matchstart > 0
        return indent(matchstart)
      endif

    " if the previous line starts with any of the ending trios, then indent
    " one level to compensate for our adjustment above.
    elseif prevline =~ '^\s*' . trio[2] && prevline !~ pattern_heads . '$'
      let col = col('.')
      call cursor(a:lnum - 1, 1)
      let matchstart = searchpair(trio[0], '', trio[2], 'bnW', 'InCommentOrString()')
      call cursor(0, col)

      " if the matching opener is on it's own line, then use the previous line
      " indent.
      if matchstart > 0 && getline(matchstart) =~ '^\s*' . trio[0]
        return indent(prevnonblank(matchstart - 1))
      endif
      return indent(prevlnum)
    endif
  endfor
  return IndentAnything()
endfunction " }}}

" JavascriptIndentAnythingSettings() {{{
function! JavascriptIndentAnythingSettings ()
  " Syntax name REs for comments and strings.
  let b:commentRE      = 'javaScript\(Line\)\?Comment'
  let b:lineCommentRE  = 'javaScriptLineComment'
  let b:blockCommentRE = 'javaScriptComment'
  let b:stringRE            = 'javaScriptString\(S\|D\)'
  let b:singleQuoteStringRE = 'javaScriptStringS'
  let b:doubleQuoteStringRE = 'javaScriptStringD'

  " Setup for C-style comment indentation.
  let b:blockCommentStartRE  = '/\*'
  let b:blockCommentMiddleRE = '\*'
  let b:blockCommentEndRE    = '\*/'
  let b:blockCommentMiddleExtra = 1

  " Indent another level for each non-closed paren/'(' and brace/'{' on the
  " previous line.
  let b:indentTrios = [
        \ [ '(', '', ')' ],
        \ [ '\[', '', '\]' ],
        \ [ '{', '\(default:\|case.*:\)', '}' ]
  \]


  " Line continuations.  Lines that are continued on the next line are
  " if/for/while statements that are NOT followed by a '{' block and operators
  " at the end of a line.
  let b:lineContList = [
    \ { 'pattern' : '^\s*\(if\|for\|while\)\s*(.*)\s*\(\(//.*\)\|/\*.*\*/\s*\)\?\_$\(\_s*{\)\@!' },
    \ { 'pattern' : '^\s*else' . '\s*\(\(//.*\)\|/\*.*\*/\s*\)\?\_$\(\_s*{\)\@!' },
    \ { 'pattern' : '\(+\|=\|+=\|-=\)\s*\(\(//.*\)\|/\*.*\*/\s*\)\?$' }
  \]

  " If a continued line and its continuation can have line-comments between
  " them, then this should be true.  For example,
  "
  "       if (x)
  "           // comment here
  "           statement
  "
  let b:contTraversesLineComments = 1
endfunction " }}}

call JavascriptIndentAnythingSettings()

" vim:ft=vim:fdm=marker
