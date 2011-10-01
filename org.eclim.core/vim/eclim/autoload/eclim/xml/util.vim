" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Utility functions for xml plugins.
"
"   This plugin contains shared functions that can be used regardless of the
"   current file type being edited.
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

" Script Variables {{{
let s:dtd = '.*' . g:EclimQuote . '\(.*\)' . g:EclimQuote . '\s*>.*'
let s:xsd = '.\{-}<ns>:schemaLocation\s*=\s*' .
  \ g:EclimQuote . '\(.\{-}\)' . g:EclimQuote . '.*'
let s:element = '.\{-}<\([a-zA-Z].\{-}\)\(\s\|>\|$\).*'
" }}}

" GetDtd() {{{
" Get the dtd defined in the current file.
function! eclim#xml#util#GetDtd()
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
function! eclim#xml#util#GetXsd(...)
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
function! eclim#xml#util#GetElementName()
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

  let name = substitute(name, '\s\|/', '', 'g')

  return name
endfunction " }}}

" GetParentElementName() {{{
" Get the parent element name relative to the current cursor position.
" Depends on 'at' visual selection ability.
function! eclim#xml#util#GetParentElementName()
  let pos = getpos('.')

  " select tags (best solution I can think of).
  silent! normal! v2at
  normal! v

  call cursor(line("'<"), col("'<"))
  let parent = eclim#xml#util#GetElementName()

  call setpos('.', pos)

  if eclim#xml#util#GetElementName() == parent
    return ''
  endif

  return parent
endfunction " }}}

" vim:ft=vim:fdm=marker
