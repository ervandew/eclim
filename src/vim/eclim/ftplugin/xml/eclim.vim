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
if !exists('g:EclimSgmlCompleteEndTag')
  let g:EclimSgmlCompleteEndTag = 1
endif

" }}}

" Options {{{

setlocal completefunc=eclim#xml#complete#CodeComplete

" }}}

" Autocmds {{{

if g:EclimXmlValidate
  augroup eclim_xml
    autocmd! BufWritePost <buffer>
    autocmd BufWritePost <buffer> call eclim#xml#validate#Validate('', 1)
  augroup END
endif

" }}}

" Mappings {{{

if g:EclimSgmlCompleteEndTag
  imap <buffer> <silent> / <c-r>=eclim#sgml#util#CompleteEndTag()<cr>
endif

" }}}

" Command Declarations {{{

if !exists(":Validate")
  command -nargs=? -complete=file -bang -buffer Validate
    \ :call eclim#xml#validate#Validate('<args>', 0, '<bang>')

  command -nargs=? -buffer DtdDefinition
    \ :call eclim#xml#definition#DtdDefinition('<args>')
  command -nargs=? -buffer XsdDefinition
    \ :call eclim#xml#definition#XsdDefinition('<args>')
endif

if !exists(":XmlFormat")
  "command -buffer -range XmlFormat :call eclim#xml#format#Format(<line1>, <line2>)
  command -buffer XmlFormat :call eclim#xml#format#Format()
endif

" }}}

" vim:ft=vim:fdm=marker
