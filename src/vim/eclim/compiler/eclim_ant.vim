" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   Compiler for ant (enhancement to default ant compiler provided w/ vim).
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
let current_compiler = "eclim_ant"

CompilerSet makeprg=ant\ -find\ build.xml\ $*
CompilerSet errorformat=
  \%-G%.%#[javac]\ %.%#:\ warning:\ unmappable\ character\ %.%#,
  \%A%.%#[javac]\ %f:%l:\ %m,
  \%C%.%#[javac]\ symbol\ %#:\ %m,
  \%-Z%.%#[javac]\ %p^,
  \%A%.%#[javadoc]\ %f:%l:\ %m,
  \%-C%.%#[javadoc]\ location:\ %.%#,
  \%-C%.%#[javadoc]\ %#,
  \%-Z%.%#[javadoc]\ %p^,
  \%-G%.%#[javadoc]\ Note:%.%#,
  \%-G%.%#[javadoc]\ javadoc:%.%#,
  \%.%#[javadoc]\ %f:\ %m,
  \%.%#[java]\ org\.apache\.jasper\.JasperException:\ file:%f(%l\\,%c)\ %m,
  \%A%.%#[junit]\ %m\\,\ Time\ elapsed:\ %.%#,
  \%Z%.%#[junit]\ Test\ %f\ FAILED,
  \%A%.%#[cactus]\ %m\\,\ Time\ elapsed:\ %.%#,
  \%Z%.%#[cactus]\ Test\ %f\ FAILED,
  \%-G%.%#

" vim:ft=vim:fdm=marker
