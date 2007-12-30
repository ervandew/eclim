" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/ant/run.html
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

" Auto Commands {{{
augroup eclim_ant_make
  autocmd!
  autocmd QuickFixCmdPost make call eclim#java#test#ResolveQuickfixResults('junit')
  autocmd QuickFixCmdPost make call eclim#java#test#ResolveQuickfixResults('testng')
augroup END
" }}}

" Command Declarations {{{
if !exists(":Ant")
  command -bang -nargs=* -complete=customlist,eclim#java#ant#run#CommandCompleteTarget
    \ Ant :call eclim#java#ant#run#Ant('<bang>', '<args>')
endif
" }}}

" vim:ft=vim:fdm=marker
