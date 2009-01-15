" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Compiler for maven 2.x.
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

if exists("current_compiler")
  finish
endif
let current_compiler = "eclim_maven"

if !exists('g:EclimMvnCompilerAdditionalErrorFormat')
  let g:EclimMvnCompilerAdditionalErrorFormat = ''
endif

CompilerSet makeprg=mvn\ $*

" Lines 1 - 3: javac
" Lines 4 - 7: javadoc
exec 'CompilerSet errorformat=' .
  \ '\%A%f:[%l\\,%c]\ %m,' .
  \ '\%Csymbol%.%#:\ %m,' .
  \ '\%Zlocation%.%#:\ %m,' .
  \ '\%AEmbedded\ error:%.%#\ -\ %f:%l:\ %m,' .
  \ '\%-Z\ %p^,' .
  \ '\%A%f:%l:\ %m,' .
  \ '\%-Z\ %p^,' .
  \ '\%ARunning\ %f,' .
  \ '\%+ZTests\ run%.%#FAILURE!,' .
  \ g:EclimMvnCompilerAdditionalErrorFormat .
  \ '\%-G%.%#'

" vim:ft=vim:fdm=marker
