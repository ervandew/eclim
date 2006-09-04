" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Utility functions for xml plugins.
"
"   This plugin contains shared functions that can be used regardless of the
"   current file type being edited.
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

" Script Variables {{{
let s:dtd = '.*' . g:EclimQuote . '\(.*\)' . g:EclimQuote . '\s*>.*'
let s:xsd = '.\{-}<ns>:schemaLocation\s*=\s*' .
  \ g:EclimQuote . '\(.\{-}\)' . g:EclimQuote . '.*'
let s:element = '.\{-}<\([a-zA-Z].\{-}\)\(\s\|>\|$\).*'
" }}}

" GetDtd() {{{
" Get the dtd defined in the current file.
function! eclim#xml#util#GetDtd ()
  let linenum = search('<!DOCTYPE\s\+\_.\{-}>', 'bcnw')
  if linenum > 0
    let line = ''
    while getline(linenum) !~ '>'
      let line = line . getline(linenum)
      let linenum += 1
    endwhile
    let line = line . getline(linenum)

    let dtd = substitute(line, s:dtd, '\1', '')
    if dtd != line
      return dtd
    endif
  endif
  return ''
endfunction " }}}

" GetXsd() {{{
" Get the schema defined in the current file, for the optionally provided
" namespace prefix, or the default namespace.
function! eclim#xml#util#GetXsd (...)
  let namespace = ''
  if len(a:000) > 0
    let namespace = a:000[0]
  endif

  " if no namespace given, try 'xsi' as that is a common default.
  if namespace == ''
    let xsd = eclim#xml#util#GetXsd('xsi')
    if xsd != ''
      return xsd
    endif
  endif

  let linenum = search(namespace . ':schemaLocation\>', 'bcnw')
  if linenum > 0
    let line = ''
    while getline(linenum) !~ '>'
      let line = line . getline(linenum)
      let linenum += 1
    endwhile
    let line = line . getline(linenum)

    let pattern = substitute(s:xsd, '<ns>', namespace, '')
    let xsd = substitute(line, pattern, '\1', '')
    if xsd != line
      " last http definition is the schema
      return strpart(xsd, strridx(xsd, 'http://'))
    endif
  endif
  return ''
endfunction " }}}

" GetElementName() {{{
" Get name of the element that the cursor is currently on.
function! eclim#xml#util#GetElementName ()
  let line = getline('.')
  let cnum = col('.')
  if line[cnum - 1] == '<'
    let cnum += 1
  endif
  if line[cnum - 1] == '>'
    let cnum -= 1
  endif

  let name = substitute(line,
    \ '.*</\?\s*\(.*\%' . cnum . 'c.\{-}\)\(\s.*\|\s*/\?>.*\|$\)', '\1', '')

  if name == line || name =~ '<\|>' || name =~ '\S\s\S'
    return ''
  endif

  let name = substitute(name, '\s', '', 'g')

  return name
endfunction " }}}

" GetParentElement() {{{
" FIXME: Work in progress, probably not suitable for all situations.
" Get the parent element name relative to the current cursor position.
" Works great on well formed xml and requires matchit.vim to be functioning
" properly (% on an element jumps to the corresponding start/end tag).
" Moves the cursor to find the parent (restores it of course).
function! eclim#xml#util#GetParentElement (...)
  let curline = line('.')
  let curcol = col('.')

  " attempt to ensure cursor is on the nearest starting element.
  " make sure we are starting on a non blank line
  if getline('.') =~ '^\s*$'
    call cursor(prevnonblank(curline), 1)
    call cursor(line('.'), col('$'))
  endif

  if len(a:000) == 2
    let startline = a:000[0]
    let startcol = a:000[1]
  else
    let startline = line('.')
    let startcol = col('.')
    if getline(startline)[startcol - 1] == '<'
      let startcol = col('.') + 1
    endif
  endif

  " move cursor to end of closest element
  normal F>
  " if no end of element on current line, then move to beginning of current
  " element.
  if col('.') == curcol
    normal F<
  endif

  let element = ''
  let linenum = search('<[a-zA-Z]\+\_.\{-}[^/]>', 'bW')
  if linenum > 0
    let line = getline(linenum)
    let element = substitute(line, s:element, '\1', '')
    if element != line
      " make sure element found wraps starting position.
      call cursor(line('.'), col('.') + 1)
      let lastline = line('.')
      let lastcol = col('.')
      normal %
      if line('.') == lastline && col('.') == lastcol
        " no matching tag...
      elseif !( (line('.') == startline && col('.') > startcol) ||
          \ (line('.') > startline) )
        call cursor(lastline, lastcol)
        let element = eclim#xml#util#GetParentElement(startline, startcol)
      endif
    endif
  endif

  call cursor(curline, curcol)
  return element
endfunction " }}}

" vim:ft=vim:fdm=marker
