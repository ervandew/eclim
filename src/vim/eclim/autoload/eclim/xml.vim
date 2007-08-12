" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/xml/validate.html
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

" Global Variables {{{
if !exists("g:EclimXmlValidate")
  let g:EclimXmlValidate = 1
endif
" }}}

" Script Variables {{{
let s:command_validate = '-command xml_validate -p "<project>" -f "<file>"'

let s:element_def{'dtd'} = '<!ELEMENT\s\+<name>\>\(\s\|(\|$\)'
let s:element_def{'xsd'} =
    \ '<\s*\(.\{-}:\)\?element\>\_[^>]*name\s*=\s*' .
    \ g:EclimQuote . '<name>' . g:EclimQuote
" }}}

" Validate(file, on_save, ...) {{{
" Validate the supplied file.
function! eclim#xml#Validate (file, on_save, ...)
  if a:on_save && (!g:EclimXmlValidate || eclim#util#WillWrittenBufferClose())
    return
  endif

  let project = eclim#project#GetCurrentProjectName()
  if project == ""
    return
  endif

  let file = a:file
  if file == ""
    let file = expand('%:p')
    update
  else
    let file = fnamemodify(file, ':p')
  endif
  let file = substitute(file, '\', '/', 'g')

  if !filereadable(file)
    call eclim#util#EchoError("File not readable or does not exist.")
    return
  endif

  if eclim#PingEclim(0)
    let filename = eclim#project#GetProjectRelativeFilePath(file)
    let command = s:command_validate
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', filename, '')

    if substitute(expand('%:p'), '\', '/', 'g') != file
      let restore = winrestcmd()
      exec 'split ' . file
    endif
    if search('xsi:schemaLocation', 'cnw')
      let command .= ' -s'
    endif
    if exists('restore')
      close
      exec restore
    endif

    let result = eclim#ExecuteEclim(command)
    if result =~ '|'
      let errors = eclim#util#ParseLocationEntries(split(result, '\n'))
      call eclim#util#SetLocationList(errors)
      " bang arg supplied, but no bang, so jump to first error.
      if len(a:000) > 0 && a:000[0] == ''
        lfirst
      endif
      return 1
    else
      call eclim#util#SetLocationList([], 'r')
      return 0
    endif
  else
    " alternative method via xmllint
    if !a:on_save && executable('xmllint')
      call eclim#util#MakeWithCompiler('eclim_xmllint', '', file)
      call eclim#signs#Update()
    else
      call eclim#util#EchoDebug("Eclimd not running.")
    endif
  endif
  return 0
endfunction " }}}

" DtdDefinition(element) {{{
" Opens the current xml file's dtd definition and optionally jumps to an
" element if an element name supplied.
function! eclim#xml#DtdDefinition (element)
  let dtd = eclim#xml#util#GetDtd()
  let element = a:element == '' ? eclim#xml#util#GetElementName() : a:element
  call s:OpenDefinition(dtd, element, 'dtd')
endfunction " }}}

" XsdDefinition(element) {{{
" Opens the current xml file's xsd definition and optionally jumps to an
" element if an element name supplied.
function! eclim#xml#XsdDefinition (element)
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
