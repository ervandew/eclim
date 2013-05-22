" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2005 - 2013  Eric Van Dewoestine
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

if !exists(":RubyInterpreterList")
  command -buffer RubyInterpreterList
    \ :call eclim#dltk#interpreter#ListInterpreters('ruby')
  command -buffer -nargs=*
    \ -complete=customlist,eclim#dltk#interpreter#CommandCompleteInterpreterAdd
    \ RubyInterpreterAdd
    \ :call eclim#ruby#interpreter#AddInterpreter('<args>')
  command -buffer -nargs=1
    \ -complete=customlist,eclim#ruby#interpreter#CommandCompleteInterpreterPath
    \ RubyInterpreterRemove
    \ :call eclim#dltk#interpreter#RemoveInterpreter('ruby', '<args>')
endif

" }}}

" vim:ft=vim:fdm=marker
