" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Compiler for make.
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
let current_compiler = "eclim_make"

if !exists('g:EclimMakeCompilerAdditionalErrorFormat')
  let g:EclimMakeCompilerAdditionalErrorFormat = ''
endif

CompilerSet makeprg=make

" With the exception of the last two lines, this is a straight copy from the
" vim default.
exec 'CompilerSet errorformat=' .
  \ '%*[^\"]\"%f\"%*\\D%l:\ %m,' .
  \ '\"%f\"%*\\D%l:\ %m,'.
  \ '%-G%f:%l:\ (Each\ undeclared\ identifier\ is\ reported\ only\ once,' .
  \ '%-G%f:%l:\ for\ each\ function\ it\ appears\ in.),' .
  \ '%f:%l:%c:%m,' .
  \ '%f(%l):%m,' .
  \ '%f:%l:%m,' .
  \ '\"%f\"\\,\ line\ %l%*\\D%c%*[^\ ]\ %m,' .
  \ "%D%*\\\\a[%*\\\\d]:\\ Entering\\ directory\\ `%f'," .
  \ "%X%*\\\\a[%*\\\\d]:\\ Leaving\\ directory\\ `%f'," .
  \ "%D%*\\\\a:\\ Entering\\ directory\\ `%f'," .
  \ "%X%*\\\\a:\\ Leaving\\ directory\\ `%f'," .
  \ '%DMaking\ %*\\a\ in\ %f,' .
  \ '%f\|%l\|\ %m,' .
  \ g:EclimMakeCompilerAdditionalErrorFormat .
  \ '\%-G%.%#'

" vim:ft=vim:fdm=marker
