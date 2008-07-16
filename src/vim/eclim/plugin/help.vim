" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Commands view / search eclim help files.
"
" License:
"
" Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
if !exists(':EclimHelp')
  command -nargs=1 -complete=customlist,eclim#help#CommandCompleteTag
    \ EclimHelp :call eclim#help#Help('<args>')
endif
if !exists(':EclimHelpGrep')
  command -nargs=+ EclimHelpGrep :call eclim#help#HelpGrep(<q-args>)
endif
" }}}

" vim:ft=vim:fdm=marker
