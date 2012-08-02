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
runtime! ftplugin/xml.vim
runtime! indent/xml.vim

" Command Declarations {{{
if !exists(":MavenRepo")
  command -nargs=0 -buffer
    \ MavenRepo :call eclim#java#maven#SetClasspathVariable('Maven', 'MAVEN_REPO')
endif
if !exists(":MavenDependencySearch")
  command -nargs=1 -buffer MavenDependencySearch
    \ :call eclim#java#maven#Search('<args>', 'maven')
endif
" }}}

" vim:ft=vim:fdm=marker
