" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   Compiler for maven 2.x.
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
  \ '\%+Z%.%#Failures:\ %[%^0]%.%#\ Time\ elapsed:\ %.%#,' .
  \ g:EclimMvnCompilerAdditionalErrorFormat .
  \ '\%-G%.%#'

" vim:ft=vim:fdm=marker
