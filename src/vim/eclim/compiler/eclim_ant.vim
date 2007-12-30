" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Compiler for ant (enhancement to default ant compiler provided w/ vim).
"
" License:
"
" Copyright (c) 2005 - 2008
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
let current_compiler = "eclim_ant"

if !exists('g:EclimAntCompilerAdditionalErrorFormat')
  let g:EclimAntCompilerAdditionalErrorFormat = ''
endif

if !exists('g:EclimAntErrorFormat')
  let g:EclimAntErrorFormat = '\%A%f:%l:\ %m,'
endif

CompilerSet makeprg=ant\ -find\ build.xml\ $*

" The two entries before the last one, are for catching ant build file names
" and error line numbers.
exec 'CompilerSet errorformat=' .
  \ '\%-G%.%#[javac]\ %.%#:\ warning:\ unmappable\ character\ %.%#,' .
  \ '\%A%.%#[javac]\ %f:%l:\ %m,' .
  \ '\%C%.%#[javac]\ symbol\ %#:\ %m,' .
  \ '\%-Z%.%#[javac]\ %p^,' .
  \ '\%A%.%#[javadoc]\ %f:%l:\ %m,' .
  \ '\%-C%.%#[javadoc]\ location:\ %.%#,' .
  \ '\%-C%.%#[javadoc]\ %#,' .
  \ '\%-Z%.%#[javadoc]\ %p^,' .
  \ '\%-G%.%#[javadoc]\ Note:%.%#,' .
  \ '\%-G%.%#[javadoc]\ javadoc:%.%#,' .
  \ '\%.%#[javadoc]\ %f:\ %m,' .
  \ '\%.%#[java]\ org\.apache\.jasper\.JasperException:\ file:%f(%l\\,%c)\ %m,' .
  \ '\%+A%.%#[junit]\ %.%#Failures:\ %[%^0]%.%#\ Time\ elapsed:\ %.%#,' .
  \ '\%-Z%.%#[junit]\ Test\ %f\ FAILED,' .
  \ '\%+A%.%#[junit]\ %.%#Errors:\ %[%^0]%.%#\ Time\ elapsed:\ %.%#,' .
  \ '\%-Z%.%#[junit]\ Test\ %f\ FAILED,' .
  \ '\%+A%.%#[cactus]\ %.%#Failures:\ %[%^0]%.%#\ Time\ elapsed:\ %.%#,' .
  \ '\%-Z%.%#[cactus]\ Test\ %f\ FAILED,' .
  \ '\%+A%.%#[cactus]\ %.%#Errors:\ %[%^0]%.%#\ Time\ elapsed:\ %.%#,' .
  \ '\%-Z%.%#[cactus]\ Test\ %f\ FAILED,' .
  \ '\%+A%.%#eclim\ testng:\ %f:%m,' .
  \ '\%A%.%#\ ERROR\ %.%#\ line\ %l\ in\ file:\ %.%f%.:\ %m,' .
  \ g:EclimAntCompilerAdditionalErrorFormat .
  \ '\%A%.%#[exec]\ %f:%l:%c:\ %m,' .
  \ '\%A%.%#[exec]\ %f:%l:\ %m,' .
  \ '\%A%f:%l:%c:\ %m,' .
  \ g:EclimAntErrorFormat .
  \ '\%-G%.%#'

" vim:ft=vim:fdm=marker
