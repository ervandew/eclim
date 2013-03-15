" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/c/validate.html
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

" Global Variables {{{

if !exists("g:EclimCppSyntasticEnabled")
  let g:EclimCppSyntasticEnabled = 0
endif

" }}}

" Options {{{

" disable syntastic
if exists('g:loaded_syntastic_plugin') && !g:EclimCppSyntasticEnabled
  let g:syntastic_cpp_checkers = []
endif

" }}}

runtime eclim/ftplugin/c.vim

" vim:ft=vim:fdm=marker
