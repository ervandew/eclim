" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/html/validate.html
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

" Validate(on_save) {{{
" Validates the current html file.
function! eclim#html#validate#Validate(on_save)
  if eclim#util#WillWrittenBufferClose()
    return
  endif

  if !eclim#project#util#IsCurrentFileInProject(!a:on_save)
    return
  endif

  " prevent closing of sign column between validation methods
  call eclim#display#signs#SetPlaceholder()

  call eclim#lang#Validate('html', a:on_save)

  " prevent closing of sign column between validation methods
  call eclim#display#signs#SetPlaceholder()

  let html_errors = getloclist(0)
  let css_errors = []
  let js_errors = []

  if search('<script', 'cnw')
    call eclim#javascript#util#UpdateSrcFile(a:on_save)
    let js_errors = getloclist(0)
  endif

  " prevent closing of sign column between validation methods
  call eclim#display#signs#SetPlaceholder()

  if search('<style', 'cnw')
    call eclim#lang#Validate('css', a:on_save)
    let css_errors = getloclist(0)
  endif

  call eclim#util#SetLocationList(html_errors + css_errors + js_errors)
  call eclim#display#signs#RemovePlaceholder()
endfunction " }}}

" vim:ft=vim:fdm=marker
