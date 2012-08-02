" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/c/paths.html
"
" License:
"
" Copyright (C) 2005 - 2012  Eric Van Dewoestine
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
let g:tlist_eclipse_cproject_settings = {
    \ 'lang': 'cproject',
    \ 'parse': 'eclim#taglisttoo#lang#cproject#Parse',
    \ 'tags': {
      \ 'c': 'configuration',
      \ 't': 'toolchain',
      \ 'l': 'tool',
      \ 'i': 'include',
      \ 's': 'symbol',
    \ }
  \ }
" }}}

" Script Variables {{{
  let s:entry_src = "\t<classpathentry kind=\"src\" path=\"<arg>\"/>"
  let s:entry_project =
    \ "\t<classpathentry exported=\"true\" kind=\"src\" path=\"/<arg>\"/>"
  let s:entry_var =
    \ "\t<classpathentry kind=\"<kind>\" path=\"<arg>\"/>"
  "  \ "\t<classpathentry exported=\"true\" kind=\"<kind>\" path=\"<arg>\">\n" .
  "  \ "\t\t<!--\n" .
  "  \ "\t\t\tsourcepath=\"<path>\">\n" .
  "  \ "\t\t-->\n" .
  "  \ "\t\t<!--\n" .
  "  \ "\t\t<attributes>\n" .
  "  \ "\t\t\t<attribute value=\"file:<javadoc>\" name=\"javadoc_location\"/>\n" .
  "  \ "\t\t</attributes>\n" .
  "  \ "\t\t-->\n" .
  "  \ "\t</classpathentry>"
  let s:entry_jar = substitute(s:entry_var, '<kind>', 'lib', '')
  let s:entry_var = substitute(s:entry_var, '<kind>', 'var', '')
" }}}

" load any xml related functionality
runtime! ftplugin/xml.vim
runtime! indent/xml.vim

augroup eclim_xml
  autocmd! BufWritePost <buffer>
  autocmd BufWritePost <buffer> call eclim#project#util#ProjectUpdate()
augroup END

" Command Declarations {{{

if !exists(":CProjectConfigs")
  command -nargs=0 -buffer CProjectConfigs :call eclim#c#project#Configs()
endif

" }}}

" vim:ft=vim:fdm=marker
