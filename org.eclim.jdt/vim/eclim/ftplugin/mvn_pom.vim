" Author:  Eric Van Dewoestine
"
" Description: {{{
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

" load any xml related functionality
runtime ftplugin/xml.vim
runtime indent/xml.vim

" turn off xml validation
augroup eclim_xml
  autocmd! BufWritePost <buffer>
augroup END

" Global Variables {{{
if !exists('g:EclimMavenPomClasspathUpdate')
  let g:EclimMavenPomClasspathUpdate = 1
endif
" }}}

" Autocmds {{{
if g:EclimMavenPomClasspathUpdate
  augroup eclim_mvn
    autocmd! BufWritePost <buffer>
    autocmd BufWritePost <buffer> call eclim#java#maven#UpdateClasspath()
  augroup END
endif
" }}}

" Command Declarations {{{
if !exists(":MvnRepo")
  command -nargs=0 -buffer
    \ MvnRepo :call eclim#java#maven#SetClasspathVariable('Mvn', 'M2_REPO')
endif
if !exists(":MvnDependencySearch")
  command -nargs=1 -buffer MvnDependencySearch
    \ :call eclim#java#maven#Search('<args>', 'mvn')
endif
" }}}

" vim:ft=vim:fdm=marker
