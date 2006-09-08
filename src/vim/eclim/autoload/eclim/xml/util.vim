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

  let name = substitute(name, '\s\|/', '', 'g')

  return name
endfunction " }}}

" GetParentElementName() {{{
" Get the parent element name relative to the current cursor position.
" Depends on 'at' visual selection ability.
function! eclim#xml#util#GetParentElementName ()
  let clnum = line('.')
  let ccnum = col('.')

  " select tags (best solution I can think of).
  silent! normal v2at
  normal v

  call cursor(line("'<"), col("'<"))
  let parent = eclim#xml#util#GetElementName()

  call cursor(clnum, ccnum)

  if eclim#xml#util#GetElementName() == parent
    return ''
  endif

  return parent
endfunction " }}}

" vim:ft=vim:fdm=marker
