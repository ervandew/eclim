" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Html indent file using IndentAnything.
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

if &indentexpr =~ 'EclimGetHtmlIndent' ||
    \ (!exists('b:disableOverride') && exists('g:EclimHtmlIndentDisabled'))
  finish
endif

let b:disableOverride = 1
runtime! indent/javascript.vim
runtime! indent/css.vim

setlocal indentexpr=EclimGetHtmlIndent(v:lnum)
setlocal indentkeys+=>,},0),0},),;,0{,!^F,o,O

" EclimGetHtmlIndent(lnum) {{{
function! EclimGetHtmlIndent (lnum)
  let line = line('.')
  let col = line('.')

  let adj = 0

  let scriptstart = search('<script\>', 'bcW')
  if scriptstart > 0
    let scriptstart = search('>', 'cW', scriptstart)
    let scriptend = search('</script\s*>', 'cW')
  endif
  call cursor(line, col)

  let stylestart = search('<style\>', 'bcW')
  if stylestart > 0
    let stylestart = search('>', 'cW', stylestart)
    let styleend = search('</style\s*>', 'cW')
  endif
  call cursor(line, col)

  " Inside <script> tags... let javascript indent file do the work.
  if scriptstart > 0 && scriptstart < a:lnum &&
        \ (scriptend == 0 || (scriptend > scriptstart && a:lnum < scriptend))
    call JavascriptIndentAnythingSettings()
    if a:lnum == scriptstart + 1
      let adj = &sw
    endif
    return EclimGetJavascriptIndent(a:lnum) + adj

  " Inside <style> tags... let css indent file do the work.
  elseif stylestart > 0 && stylestart < a:lnum &&
        \ (styleend == 0 || (styleend > stylestart && a:lnum < styleend))
    call CssIndentAnythingSettings()
    if a:lnum == stylestart + 1
      let adj = &sw
    endif
    return EclimGetCssIndent(a:lnum) + adj

  " Indenting html code, do our work.
  else
    call HtmlIndentAnythingSettings()
    let adj = s:HtmlIndentAttributeWrap(a:lnum) * &sw

    " handle case where previous line is a multi-line comment (<!-- -->) on one
    " line, which IndentAnything doesn't handle properly.
    let prevlnum = prevnonblank(a:lnum - 1)
    let prevline = getline(prevlnum)
    if prevline =~ '^\s\+<!--.\{-}-->'
      let adj = indent(prevlnum)

    " handle <br> tags without '/>'
    elseif prevline =~ '<br\s*>'
      let line = prevline
      let occurrences = 0
      while line =~ '<br\s*>'
        let occurrences += 1
        let line = substitute(line, '<br\s*>', '', '')
      endwhile
      let adj = 0 - (&sw * occurrences)

    " handle <input> tags without '/>'
    elseif prevline =~ '<input[^/]\{-}>' " FIXME: handle wrapped input tag
      let adj = 0 - &sw
    endif
  endif

  return IndentAnything() + adj
endfunction " }}}

" HtmlIndentAnythingSettings() {{{
if !exists('*HtmlIndentAnythingSettings') || &ft == 'html'
function! HtmlIndentAnythingSettings ()
  " Syntax name REs for comments and strings.
  let b:blockCommentRE = 'htmlComment'
  let b:commentRE      = b:blockCommentRE
  let b:stringRE       = 'htmlString'
  let b:singleQuoteStringRE = b:stringRE
  let b:doubleQuoteStringRE = b:stringRE

  setlocal comments=sr:<!--,m:-,e:-->
  let b:blockCommentStartRE  = '<!--'
  let b:blockCommentMiddleRE = '-'
  let b:blockCommentEndRE    = '-->'
  let b:blockCommentMiddleExtra = 2

  " Indent another level for each non-closed element tag.
  let b:indentTrios = [
      \ [ '<\w', '', '\(/>\|</\)' ],
    \ ]

  "let b:lineContList = [
  "    \ {'pattern' : '^<!DOCTYPE.*[^>]\s*$' },
  "  \ ]
endfunction
endif " }}}

" HtmlIndentAttributeWrap(lnum) {{{
" Function which indents line continued attributes an extra level for
" readability.
function! <SID>HtmlIndentAttributeWrap (lnum)
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

call HtmlIndentAnythingSettings()

" vim:ft=vim:fdm=marker
