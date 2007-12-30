" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/xml/definition.html
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

" Script Variables {{{
let s:element_def{'dtd'} = '<!ELEMENT\s\+<name>\>\(\s\|(\|$\)'
let s:element_def{'xsd'} =
    \ '<\s*\(.\{-}:\)\?element\>\_[^>]*name\s*=\s*' .
    \ g:EclimQuote . '<name>' . g:EclimQuote
" }}}

" DtdDefinition(element) {{{
" Opens the current xml file's dtd definition and optionally jumps to an
" element if an element name supplied.
function! eclim#xml#definition#DtdDefinition (element)
  let dtd = eclim#xml#util#GetDtd()
  let element = a:element == '' ? eclim#xml#util#GetElementName() : a:element
  call s:OpenDefinition(dtd, element, 'dtd')
endfunction " }}}

" XsdDefinition(element) {{{
" Opens the current xml file's xsd definition and optionally jumps to an
" element if an element name supplied.
function! eclim#xml#definition#XsdDefinition (element)
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
function! s:OpenDefinition (file, element, type)
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
