" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/common/maximize.html
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
  if !exists('g:MaximizeStatusLine')
    let g:MaximizeStatusLine = '%<%f\ %M\ %h%r%=%-10.(%l,%c%V\ b=%n,w=%{winnr()}%)\ %P'
  endif

  if exists('g:MaximizeStatusLineEnabled') && g:MaximizeStatusLineEnabled
    exec "set statusline=" . g:MaximizeStatusLine
  endif
" }}}

" Command Declarations {{{
if !exists(":MaximizeWindow")
  command MaximizeWindow :call eclim#display#maximize#MaximizeWindow()
endif
if !exists(":MinimizeWindow")
  command -nargs=* MinimizeWindow :call eclim#display#maximize#MinimizeWindow(<f-args>)
endif
if !exists(":MinimizeRestore")
  command MinimizeRestore
      \ :call eclim#display#maximize#ResetMinimized() |
      \ call eclim#display#maximize#RestoreWindows(0)
endif
" }}}

" vim:ft=vim:fdm=marker
