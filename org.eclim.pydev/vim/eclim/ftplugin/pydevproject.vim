" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/classpath.html
"
" License:
"
" Copyright (C) 2005 - 2013  Eric Van Dewoestine
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

" load any xml related functionality
runtime! ftplugin/xml.vim
" pydev doesn't really parse the .pydevproject all that well, so avoid
" additional indentation, etc. like they do by default.
"runtime! indent/xml.vim
setlocal indentexpr=

augroup eclim_xml
  autocmd! BufWritePost <buffer>
  autocmd BufWritePost <buffer> call eclim#project#util#ProjectUpdate()
augroup END

" Command Declarations {{{
if !exists(":NewSrcEntry")
  command -nargs=1 -buffer
    \ -complete=customlist,eclim#project#util#CommandCompleteAbsoluteOrProjectRelativeDir
    \ NewSrcEntry call eclim#python#project#NewPathEntry('<args>')
endif
" }}}

" vim:ft=vim:fdm=marker
