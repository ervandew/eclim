" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/maven/run.html
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

" Maven(bang, args) {{{
" Executes maven 1.x using the supplied arguments.
function! eclim#java#maven#run#Maven(bang, args)
  call eclim#util#MakeWithCompiler('eclim_maven', a:bang, a:args)
endfunction " }}}

" Mvn(bang, args) {{{
" Executes maven 2.x using the supplied arguments.
function! eclim#java#maven#run#Mvn(bang, args)
  call eclim#util#MakeWithCompiler('eclim_mvn', a:bang, a:args)
endfunction " }}}

" vim:ft=vim:fdm=marker
