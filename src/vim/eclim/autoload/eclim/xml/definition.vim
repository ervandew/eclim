" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/xml/definition.html
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
let s:element_def{'dtd'} = '<!ELEMENT\s\+<name>\>\(\s\|(\|$\)'
let s:element_def{'xsd'} =
    \ '<\s*\(.\{-}:\)\?element\>\_[^>]*name\s*=\s*' .
    \ g:EclimQuote . '<name>' . g:EclimQuote
" }}}

" DtdDefinition(element) {{{
" Opens the current xml file's dtd definition and optionally jumps to an
" element if an element name supplied.
function! eclim#xml#definition#DtdDefinition(element)
  let dtd = eclim#xml#util#GetDtd()
  let element = a:element == '' ? eclim#xml#util#GetElementName() : a:element
  call s:OpenDefinition(dtd, element, 'dtd')
endfunction " }}}

" XsdDefinition(element) {{{
" Opens the current xml file's xsd definition and optionally jumps to an
" element if an element name supplied.
function! eclim#xml#definition#XsdDefinition(element)
  let element = a:element == '' ? eclim#xml#util#GetElementName() : a:element
  if element =~ ':'
    let namespace = substitute(element, ':.*', '', '')
    let element = substitute(element, '.*:', '', '')
    let xsd = eclim#xml#util#GetXsd(namespace)
  else
    let xsd = eclim#xml#util#GetXsd()
  endif
  call s:OpenDefinition(xsd, element, 'xsd')
endfunction " }}}

" OpenDefinition(file, element, type) {{{
" Open the supplied definition file and jump to the element if supplied.
function! s:OpenDefinition(file, element, type)
  if a:file == ''
    call eclim#util#EchoWarning('Unable to locate ' . a:type . ' in current file.')
    return
  endif

  " see if file is already open.
  let winnr = bufwinnr(a:file)
  if winnr != -1
    exec winnr . 'winc w'
  else
    exec 'split ' . a:file
  endif

  " jump to element definition if supplied
  if a:element != ''
    let search = substitute(s:element_def{a:type}, '<name>', a:element, 'g')
    call search(search, 'w')
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
