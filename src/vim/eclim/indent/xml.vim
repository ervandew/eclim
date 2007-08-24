" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Xml indent file using IndentAnything.
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
if exists("b:xml_did_indent")
  finish
endif

runtime indent/dtd.vim

let b:xml_did_indent = 1

setlocal indentexpr=GetXmlIndent(v:lnum)
setlocal indentkeys=o,O,*<Return>,<>>,<<>,/,{,}

" GetXmlIndent(lnum) {{{
function! GetXmlIndent (lnum)
  let line = line('.')
  let col = line('.')

  let adj = 0

  let doctypestart = search('<!DOCTYPE\>', 'bcW')
  if doctypestart > 0
    let doctypestart = search('\[', 'cW', doctypestart)
    let doctypeend = search('\]>', 'cW')
  endif
  call cursor(line, col)

  " Inside <DOCTYPE, let dtd indent do the work.
  if doctypestart > 0 && doctypestart < a:lnum &&
        \ (doctypeend == 0 || (doctypeend > doctypestart && a:lnum <= doctypeend))
    if a:lnum < doctypeend
      call DtdIndentAnythingSettings()
      return GetDtdIndent(a:lnum)
    elseif a:lnum == doctypeend
      let indent = indent(a:lnum) - &sw
      echom "End doctype " . indent
      return indent
    endif
  else
    call XmlIndentAnythingSettings()
    let adj = s:XmlIndentAttributeWrap(a:lnum) * &sw
  endif

  return IndentAnything() + adj
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

" XmlIndentAnythingSettings() {{{
function! XmlIndentAnythingSettings ()
  " Syntax name REs for comments and strings.
  let b:blockCommentRE = 'xmlComment'
  let b:commentRE      = b:blockCommentRE
  let b:stringRE       = 'xmlString'
  let b:singleQuoteStringRE = b:stringRE
  let b:doubleQuoteStringRE = b:stringRE

  setl comments=sr:<!--,m:-,e:-->
  let b:blockCommentStartRE  = '<!--'
  let b:blockCommentMiddleRE = '-'
  let b:blockCommentEndRE    = '-->'
  let b:blockCommentMiddleExtra = 2

  " Indent another level for each non-closed element tag.
  let b:indentTrios = [
      \ [ '<\w', '', '\(/>\|</\)' ],
    \ ]
endfunction " }}}

" XmlIndentAttributeWrap(lnum) {{{
" Function which indents line continued attributes an extra level for
" readability.
function! <SID>XmlIndentAttributeWrap (lnum)
  let line = line('.')
  let col = col('.')
  let adj = 0
  try
    " mover cursor to start of line to avoid matching start tag on first line
    " of nested content.
    call cursor(line, 1)
    let open = search('<\w\|<!DOCTYPE', 'bW')
    if open > 0
      let close = search('>', 'cW')
      if open != close
        " continuation line
        if close == 0 || close >= a:lnum
          " first continuation line
          if a:lnum == open + 1
            return 1
          endif
          " additional continuation lines
          return 0
        endif

        " line after last continuation line
        if close != 0 && a:lnum == close + 1
          " inner content
          return -1
        endif
      endif
    endif
  finally
    call cursor(line, col)
  endtry
endfunction " }}}

" vim:ft=vim:fdm=marker
