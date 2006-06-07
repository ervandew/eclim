" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/complete.html
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

setlocal completefunc=eclim#java#complete#CodeComplete

"inoremap <C-T> <C-R>=eclim#java#complete#CompletionFilter('t')<CR>
"inoremap <C-M> <C-R>=eclim#java#complete#CompletionFilter('m')<CR>
"inoremap <C-F> <C-R>=eclim#java#complete#CompletionFilter('f')<CR>

" vim:ft=vim:fdm=marker
