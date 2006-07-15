" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
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
let s:command_validate = '-filter vim -command xml_validate -f "<file>"'
" }}}

" Validate(file, on_save, ...) {{{
" Validate the supplied file.
function! eclim#xml#Validate (file, on_save, ...)
  if a:on_save && (!g:EclimXmlValidate || eclim#util#WillWrittenBufferClose())
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
    let command = substitute(s:command_validate, '<file>', file, '')

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

" vim:ft=vim:fdm=marker
