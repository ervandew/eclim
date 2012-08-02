" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Xml indent file using IndentAnything.
"
" License:
"
" Copyright (C) 2005 - 2012  Eric Van Dewoestine
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

if &indentexpr =~ 'EclimGetXmlIndent' ||
    \ (!exists('b:disableOverride') && exists('g:EclimXmlIndentDisabled'))
  finish
endif

let b:did_indent = 1
let b:disableOverride = 1
runtime eclim/indent/indentanything.vim
runtime! indent/dtd.vim

setlocal indentexpr=EclimGetXmlIndent(v:lnum)
setlocal indentkeys=o,O,*<Return>,<>>,<<>,/,{,}

" EclimGetXmlIndent(lnum) {{{
function! EclimGetXmlIndent(lnum)
  let line = line('.')
  let col = line('.')

  let adj = 0

  let doctypestart = search('<!DOCTYPE\>', 'bcW')
  if doctypestart > 0
    let doctypestart = search('\[', 'cW', doctypestart)
    let doctypeend = search('\]>', 'cW')
  endif
  call cursor(line, col)

  let cdatastart = search('<!\[CDATA\[', 'bcW')
  if cdatastart > 0
    let cdatastart = search('\[', 'cW', cdatastart)
    let cdataend = search('\]\]>', 'cW')
  endif
  call cursor(line, col)

  " Inside <DOCTYPE, let dtd indent do the work.
  if doctypestart > 0 && doctypestart < a:lnum &&
        \ (doctypeend == 0 || (doctypeend > doctypestart && a:lnum <= doctypeend))
    if a:lnum < doctypeend
      call DtdIndentAnythingSettings()
      return EclimGetDtdIndent(a:lnum)
    elseif a:lnum == doctypeend
      return indent(a:lnum) - &sw
    endif
  else
    " in a <[CDATA[ section
    if cdatastart > 0 && cdatastart < a:lnum &&
          \ (cdataend == 0 || (cdataend >= cdatastart && a:lnum <= cdataend))
      " only indent if nested text looks like xml
      if getline(a:lnum) =~ '^\s*<'
        if a:lnum == cdatastart + 1
          return indent(cdatastart) + &sw
        endif
      else
        return indent(a:lnum)
      endif

      " make sure the closing of the CDATA lines up with the opening.
      if a:lnum == cdataend
        return indent(cdatastart)
      endif
    " make sure that tag following close of CDATA is properly indented.
    elseif cdatastart > 0 && cdatastart < a:lnum &&
          \ (cdataend >= cdatastart && prevnonblank(a:lnum - 1) == cdataend)
      return indent(cdatastart) - &sw
    endif

    call XmlIndentAnythingSettings()
    let adj = s:XmlIndentAttributeWrap(a:lnum) * &sw

    " handle case where previous line is a multi-line comment (<!-- -->) on one
    " line.
    let prevline = prevnonblank(a:lnum - 1)
    if getline(prevline) =~ '^\s\+<!--.\{-}-->'
      let adj = indent(prevline)
    endif

    " handle case where comment end is on its own line.
    if getline(line) =~ '^\s*-->'
      let adj -= &sw
    endif
  endif

  return IndentAnything() + adj
endfunction " }}}

" XmlIndentAnythingSettings() {{{
function! XmlIndentAnythingSettings()
  " Syntax name REs for comments and strings.
  let b:blockCommentRE = 'xmlComment'
  let b:commentRE      = b:blockCommentRE
  let b:lineCommentRE  = 'xmlComment'
  let b:stringRE       = 'xmlString'
  let b:singleQuoteStringRE = b:stringRE
  let b:doubleQuoteStringRE = b:stringRE

  setlocal comments=sr:<!--,mb:\ ,ex0:-->
  let b:blockCommentStartRE  = '<!--'
  let b:blockCommentMiddleRE = ''
  let b:blockCommentEndRE    = '-->'
  let b:blockCommentMiddleExtra = 2

  " Indent another level for each non-closed element tag.
  let b:indentTrios = [
      \ [ '<\w', '', '\%(/>\|</\)' ],
    \ ]
endfunction " }}}

" XmlIndentAttributeWrap(lnum) {{{
" Function which indents line continued attributes an extra level for
" readability.
function! <SID>XmlIndentAttributeWrap(lnum)
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
        if close == prevnonblank(a:lnum - 1)
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
