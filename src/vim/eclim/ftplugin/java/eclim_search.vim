" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/search.html
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
if !exists("g:EclimJavaSearchMapping")
  let g:EclimJavaSearchMapping = 1
endif
" }}}

" Command Declarations {{{
if !exists(":JavaSearch")
  command -buffer -nargs=*
    \ -complete=customlist,eclim#java#search#CommandCompleteJavaSearch
    \ JavaSearch :call eclim#java#search#SearchAndDisplay('java_search', '<args>')
endif
if !exists(":JavaSearchContext")
  command -buffer JavaSearchContext
    \ :call eclim#java#search#SearchAndDisplay('java_search', '')
endif
if !exists(":JavaDocSearch")
  command -buffer -nargs=*
    \ -complete=customlist,eclim#java#search#CommandCompleteJavaSearch
    \ JavaDocSearch :call eclim#java#search#SearchAndDisplay('java_docsearch', '<args>')
endif
" }}}

" vim:ft=vim:fdm=marker
