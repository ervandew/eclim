" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/php/validate.html
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

" Global Variables {{{
if !exists("g:EclimPhpValidate")
  let g:EclimPhpValidate = 1
endif
" }}}

augroup eclim_html_validate
  autocmd!
augroup END

augroup eclim_php
  autocmd!
  autocmd BufWritePost *.php call eclim#php#util#UpdateSrcFile(0)
augroup END

" Command Declarations {{{
command! -nargs=0 -buffer Validate :call eclim#php#util#UpdateSrcFile(1)
" }}}

" vim:ft=vim:fdm=marker
