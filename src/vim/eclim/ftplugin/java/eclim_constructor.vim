" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/constructor.html
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
if !exists(":JavaConstructor")
  command -buffer -range=0 JavaConstructor
    \ :call eclim#java#constructor#Constructor(<line1>, <line2>)
endif
" }}}

" vim:ft=vim:fdm=marker
