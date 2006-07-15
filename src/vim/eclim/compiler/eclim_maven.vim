" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   Compiler for maven 1.x.
"
" License:
"
" Copyright (c) 2005 - 2006
"
" Licensed under the Apache License, Version 2.0 (the "License");
" you may not use this file except in compliance with the License.
" You may obtain a copy of the License at
"
"      http://www.apache.org/licenses/LICENSE-2.0
"
" Unless required by applicable law or agreed to in writing, software
" distributed under the License is distributed on an "AS IS" BASIS,
" WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
" See the License for the specific language governing permissions and
" limitations under the License.
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

" Lines 12 - 15: javac (must be last to prevent picking up other errors in the
" wrong format).
exec 'CompilerSet errorformat=' .
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
  \ '\%A%f:%l:\ %m,' .
  \ '\%Csymbol%.%#:\ %m,' .
  \ '\%Clocation%.%#:\ %m,' .
  \ '\%-Z\ %p^,' .
  \ g:EclimMavenCompilerAdditionalErrorFormat .
  \ '\%-G%.%#'

" vim:ft=vim:fdm=marker
