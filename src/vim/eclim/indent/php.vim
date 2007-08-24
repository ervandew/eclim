" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Php indent file using IndentAnything.
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

if exists("b:did_indent")
  finish
endif

source $VIMRUNTIME/indent/php.vim
unlet b:did_indent
runtime indent/html.vim
let b:did_indent = 1

setlocal indentexpr=GetPhpHtmlIndent(v:lnum)
setlocal indentkeys=0{,0},0),:,!^F,o,O,e,*<Return>,=?>,=<?,=*/,<>>,<bs>,{,}

" GetPhpHtmlIndent(lnum) {{{
function! GetPhpHtmlIndent (lnum)
  " FIXME: may get confused if either of these occur in a comment.
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
  return GetHtmlIndent(a:lnum)
endfunction " }}}

" vim:ft=vim:fdm=marker
