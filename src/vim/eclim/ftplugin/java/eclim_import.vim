" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/complete.html
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

" Command Declarations {{{
if !exists(":JavaImport")
  command -buffer JavaImport :call eclim#java#import#Import()
endif
if !exists(":JavaImportSort")
  command -buffer JavaImportSort :call eclim#java#import#SortImports()
endif
if !exists(":JavaImportClean")
  command -buffer JavaImportClean :call eclim#java#import#CleanImports()
endif
" }}}

" vim:ft=vim:fdm=marker
