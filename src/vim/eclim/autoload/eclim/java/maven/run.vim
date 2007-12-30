" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/maven/run.html
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

" Maven(bang, args) {{{
" Executes maven 1.x using the supplied arguments.
function! eclim#java#maven#run#Maven (bang, args)
  call eclim#util#MakeWithCompiler('eclim_maven', a:bang, a:args)
endfunction " }}}

" Mvn(bang, args) {{{
" Executes maven 2.x using the supplied arguments.
function! eclim#java#maven#run#Mvn (bang, args)
  call eclim#util#MakeWithCompiler('eclim_mvn', a:bang, a:args)
endfunction " }}}

" vim:ft=vim:fdm=marker
