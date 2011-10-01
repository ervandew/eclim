" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Compiler for maven 1.x.
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

if !exists('g:EclimMavenCompilerAdditionalErrorFormat')
  let g:EclimMavenCompilerAdditionalErrorFormat = ''
endif

CompilerSet makeprg=maven\ --find\ project.xml\ $*

" Lines 17 - 20: javac minus adornments (must be last to prevent picking up
" other errors in the wrong format).
exec 'CompilerSet errorformat=' .
  \ '\%A%.%#[javac]\ %f:%l:\ %m,' .
  \ '\%C%.%#[javac]\ symbol%.%#:\ %m,' .
  \ '\%C%.%#[javac]\ location%.%#:\ %m,' .
  \ '\%-Z%.%#[javac]\ %p^,' .
  \ '\%W%.%#[javadoc]\ %f:%l:\ warning\ -\ %m,' .
  \ '\%E%.%#[javadoc]\ %f:%l:\ error\ -\ %m,' .
  \ '\%A%.%#[javadoc]\ %f:%l:\ %m,' .
  \ '\%-C%.%#[javadoc]\ location:\ %.%#,' .
  \ '\%-Z%.%#[javadoc]\ %p^,' .
  \ '\%-G%.%#[javadoc]\ Note:%.%#,' .
  \ '\%-G%.%#[javadoc]\ javadoc:%.%#,' .
  \ '\%+A%.%#[junit]\ %.%#Failures:\ %[%^0]%.%#\ Time\ elapsed:\ %.%#,' .
  \ '\%-Z%.%#[junit]%.%#\ Test\ %f\ FAILED,' .
  \ '\%+A%.%#[junit]%.%#\ %.%#Errors:\ %[%^0]%.%#\ Time\ elapsed:\ %.%#,' .
  \ '\%-Z%.%#[junit]\ Test\ %f\ FAILED,' .
  \ g:EclimMavenCompilerAdditionalErrorFormat .
  \ '\%A%f:%l:\ %m,' .
  \ '\%Csymbol%.%#:\ %m,' .
  \ '\%Clocation%.%#:\ %m,' .
  \ '\%-Z\ %p^,' .
  \ '\%-G%.%#'

" vim:ft=vim:fdm=marker
