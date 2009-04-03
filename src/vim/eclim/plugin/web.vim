" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Commands for looking up info via the web (google, dictionary, wikipedia,
"   etc.).
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
if !exists(":OpenUrl")
  command -bang -range -nargs=? OpenUrl
    \ :call eclim#web#OpenUrl('<args>', '<bang>', <line1>, <line2>)
endif
if !exists(":Google")
  command -nargs=* Google :call eclim#web#Google(<q-args>, 0, 0)
endif
if !exists(":Clusty")
  command -nargs=* Clusty :call eclim#web#Clusty(<q-args>, 0, 0)
endif
if !exists(":Dictionary")
  command -nargs=? Dictionary :call eclim#web#Dictionary('<args>')
endif
if !exists(":Thesaurus")
  command -nargs=? Thesaurus :call eclim#web#Thesaurus('<args>')
endif
if !exists(":Wikipedia")
  command -nargs=* Wikipedia :call eclim#web#Wikipedia('<args>', 0, 0)
endif
" }}}

" vim:ft=vim:fdm=marker
