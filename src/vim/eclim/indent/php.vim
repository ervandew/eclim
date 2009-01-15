" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Php indent file using IndentAnything.
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
  " FIXME: may get confused if either of these occur in a comment.
  "        can fix with searchpos and checking syntax name on result.
  let phpstart = search('<?php', 'bcnW')
  let phpend = search('?>', 'bcnW')
  if phpstart > 0 && phpstart < a:lnum && (phpend == 0 || phpend < phpstart)
    let indent = GetPhpIndent()
    " default php indent pushes first line of php code to left margin and
    " indents all following php code relative to that. So just make sure that
    " the first line of php after the opening php tag is indented at the same
    " level as the opening tag.
    if indent <= 0
      let prevline = prevnonblank(a:lnum - 1)
      if prevline == phpstart
        return indent + indent(phpstart)
      endif
    endif
    return indent
  endif
  return EclimGetHtmlIndent(a:lnum)
endfunction " }}}

" vim:ft=vim:fdm=marker
