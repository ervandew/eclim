" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2012 - 2014  Eric Van Dewoestine
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

if !exists(":PythonInterpreter")
  command! -nargs=?
    \ -complete=customlist,eclim#python#project#CommandCompletePathOrInterpreterName
    \ PythonInterpreter
    \ :call eclim#python#project#ProjectInterpreter('<args>')
  command! -nargs=0 PythonInterpreterList
    \ :call eclim#python#project#InterpreterList()
  command! -nargs=*
    \ -complete=customlist,eclim#python#project#CommandCompleteInterpreterAdd
    \ PythonInterpreterAdd
    \ :call eclim#python#project#InterpreterAdd('<args>')
  command! -nargs=1
    \ -complete=customlist,eclim#python#project#CommandCompleteInterpreterPath
    \ PythonInterpreterRemove
    \ :call eclim#python#project#InterpreterRemove('<args>')
endif

" }}}

" vim:ft=vim:fdm=marker
