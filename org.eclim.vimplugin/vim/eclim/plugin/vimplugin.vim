" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Setup for eclim's vimplugin (gvim in eclipse) support.
"
" License:
"
" Copyright (C) 2011 - 2012  Eric Van Dewoestine
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

" Auto Commands{{{
if exists('g:vimplugin_running')
  augroup eclim_vimplugin
    " autocommands used to work around the fact that the "unmodified" event in
    " vim's netbean support is commentted out for some reason.
    autocmd BufWritePost * call eclim#vimplugin#BufferWritten()
    autocmd CursorHold,CursorHoldI * call eclim#vimplugin#BufferModified()
    autocmd BufWinLeave * call eclim#vimplugin#BufferClosed()
    autocmd BufEnter * call eclim#vimplugin#BufferEnter()
  augroup END
endif
" }}}

" vim:ft=vim:fdm=marker
