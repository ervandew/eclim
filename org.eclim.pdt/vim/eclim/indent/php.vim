" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Php indent file using IndentAnything.
"
" License:
"
" Copyright (C) 2005 - 2011  Eric Van Dewoestine
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

if &indentexpr =~ 'EclimGetPhpHtmlIndent' ||
    \ (!exists('b:disableOverride') && exists('g:EclimPhpIndentDisabled'))
  finish
endif

unlet! b:did_indent
source $VIMRUNTIME/indent/php.vim
let b:did_indent = 1

let b:disableOverride = 1
runtime! indent/html.vim

setlocal indentexpr=EclimGetPhpHtmlIndent(v:lnum)
setlocal indentkeys=0{,0},0),:,!^F,o,O,e,*<Return>,=?>,=<?,=*/,<>>,<bs>,{,}

" EclimGetPhpHtmlIndent(lnum) {{{
function! EclimGetPhpHtmlIndent(lnum)
  if ! eclim#php#util#IsPhpCode(a:lnum)
    return EclimGetHtmlIndent(a:lnum)
  endif

  let indent = GetPhpIndent()
  " default php indent pushes first line of php code to left margin and
  " indents all following php code relative to that. So just make sure that
  " the first line of php after the opening php tag is indented at the same
  " level as the opening tag.
  if indent <= 0
    let phpstart = search('<?php', 'bcnW')
    let prevline = prevnonblank(a:lnum - 1)
    if prevline == phpstart
      return indent + indent(phpstart)
    endif
  endif
  return indent
endfunction " }}}

" vim:ft=vim:fdm=marker
