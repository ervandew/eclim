" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Plugin for archive related functionality.
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

" Global Variables {{{

if !exists('g:EclimArchiveViewerEnabled')
  let g:EclimArchiveViewerEnabled = 1
endif

if g:EclimArchiveViewerEnabled
  " disable tar.vim autocmds... tar.vim is now included w/ vim7
  let g:loaded_tarPlugin = 1

  " disable zipPlugin.vim autocmds... zipPlugin.vim is now included w/ vim7
  let g:loaded_zipPlugin = 1
endif

" }}}

" Autocommands Variables {{{

augroup eclim_archive_read
  autocmd!
  autocmd BufReadCmd
    \ jar:/*,jar:\*,jar:file:/*,jar:file:\*,
    \tar:/*,tar:\*,tar:file:/*,tar:file:\*,
    \tbz2:/*,tgz:\*,tbz2:file:/*,tbz2:file:\*,
    \tgz:/*,tgz:\*,tgz:file:/*,tgz:file:\*,
    \zip:/*,zip:\*,zip:file:/*,zip:file:\*
    \ call eclim#common#archive#ReadFile()
augroup END

if g:EclimArchiveViewerEnabled
  augroup eclim_archive
    autocmd!
    autocmd BufReadCmd *.egg     call eclim#common#archive#List()
    autocmd BufReadCmd *.jar     call eclim#common#archive#List()
    autocmd BufReadCmd *.war     call eclim#common#archive#List()
    autocmd BufReadCmd *.ear     call eclim#common#archive#List()
    autocmd BufReadCmd *.zip     call eclim#common#archive#List()
    autocmd BufReadCmd *.tar     call eclim#common#archive#List()
    autocmd BufReadCmd *.tgz     call eclim#common#archive#List()
    autocmd BufReadCmd *.tar.gz  call eclim#common#archive#List()
    autocmd BufReadCmd *.tar.bz2 call eclim#common#archive#List()
  augroup END
endif

" }}}

" vim:ft=vim:fdm=marker
