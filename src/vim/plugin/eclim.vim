" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   Plugin which bootstraps the eclim environment.
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

if v:version < 700 || exists("g:EclimDisabled") | finish | endif

" on windows, this eclim plugin gets called first, so force taglist to be
" called prior.
runtime! plugin/taglist.vim

" add eclim dir to runtime path.
let file = findfile('plugin/eclim.vim', escape(&runtimepath, ' '))
if file == ''
  echoe 'Unable to find path to plugin/eclim.vim.  ' .
    \ 'Please report this issue on the eclim forums.'
  finish
endif

let basedir = substitute(fnamemodify(file, ':p:h:h'), '\', '/', 'g')
exec 'set runtimepath+=' . escape(basedir, ' ') . '/eclim'

" need to be manually sourced
runtime! eclim/plugin/*.vim

" vim:ft=vim:fdm=marker
