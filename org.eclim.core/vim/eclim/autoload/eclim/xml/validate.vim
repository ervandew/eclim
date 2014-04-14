" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2005 - 2014  Eric Van Dewoestine
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
let s:command_validate = '-command xml_validate -p "<project>" -f "<file>"'
" }}}

function! eclim#xml#validate#Validate(on_save, ...) " {{{
  " Optional args:
  "   bang: '!' or '', where '!' indicates that we should not jump to the
  "         first error.
  if a:on_save && (!g:EclimXmlValidate || eclim#util#WillWrittenBufferClose())
    return
  endif

  if eclim#EclimAvailable(0)
    if !eclim#project#util#IsCurrentFileInProject()
      return
    endif

    let project = eclim#project#util#GetCurrentProjectName()
    let file = eclim#project#util#GetProjectRelativeFilePath()
    let command = s:command_validate
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')
    if search('xsi:schemaLocation', 'cnw')
      let command .= ' -s'
    endif

    let result = eclim#Execute(command)
    if type(result) == g:LIST_TYPE && len(result) > 0
      let errors = eclim#util#ParseLocationEntries(
        \ result, g:EclimValidateSortResults)
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
      let file = substitute(expand('%:p'), '\', '/', 'g')
      call eclim#util#MakeWithCompiler('eclim_xmllint', '', file)
      call eclim#display#signs#Update()
    elseif !a:on_save
      call eclim#util#EchoWarning("eclimd not running.")
    endif
  endif
  return 0
endfunction " }}}

" vim:ft=vim:fdm=marker
