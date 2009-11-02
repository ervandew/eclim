" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/xml/validate.html
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

" Global Variables {{{
if !exists("g:EclimXmlValidate")
  let g:EclimXmlValidate = 1
endif
" }}}

" Script Variables {{{
let s:command_validate = '-command xml_validate -p "<project>" -f "<file>"'
" }}}

" Validate(file, on_save, ...) {{{
" Validate the supplied file.
function! eclim#xml#validate#Validate(file, on_save, ...)
  if a:on_save && (!g:EclimXmlValidate || eclim#util#WillWrittenBufferClose())
    return
  endif

  let project = eclim#project#util#GetCurrentProjectName()
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
    let filename = eclim#project#util#GetProjectRelativeFilePath(file)
    let command = s:command_validate
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', filename, '')

    if substitute(expand('%:p'), '\', '/', 'g') != file
      let restore = winrestcmd()
      exec 'sview ' . file
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
      let errors = eclim#util#ParseLocationEntries(
        \ split(result, '\n'), g:EclimValidateSortResults)
      call eclim#util#SetLocationList(errors)
      " bang arg supplied, but no bang, so jump to first error.
      if len(a:000) > 0 && a:000[0] == ''
        lfirst
      endif
      return 1
    else
      call eclim#util#ClearLocationList()
      return 0
    endif
  else
    " alternative method via xmllint
    if !a:on_save && executable('xmllint')
      call eclim#util#MakeWithCompiler('eclim_xmllint', '', file)
      call eclim#display#signs#Update()
    else
      call eclim#util#EchoDebug("Eclimd not running.")
    endif
  endif
  return 0
endfunction " }}}

" vim:ft=vim:fdm=marker
