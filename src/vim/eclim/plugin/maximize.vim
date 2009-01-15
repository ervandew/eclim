" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/common/maximize.html
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
