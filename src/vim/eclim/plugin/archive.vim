" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Plugin for archive related functionality.
"
" License:
"
" Copyright (C) 2005 - 2009  Eric Van Dewoestine
"
" This program is free software: you can redistribute it and/or modify
" it under the terms of the GNU General Public License as published by
" the Free Software Foundation, either version 3 of the License, or
" (at your option) any later version.
"
" This program is distributed in the hope that it will be useful,
" but WITHOUT ANY WARRANTY; without even the implied warranty of
" MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
" GNU General Public License for more details.
"
" You should have received a copy of the GNU General Public License
" along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
