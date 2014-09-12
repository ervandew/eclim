" Author:  Kannan Rajah
"
" License: {{{
"
" Copyright (C) 2014  Eric Van Dewoestine
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

" Folding to create a tree structure
function! eclim#display#fold#GetTreeFold(lnum) " {{{
  " The default VIM fold shows the first line of a block separately.
  " But we want to show it with its contents. This is more compact and
  " easier to read.
  " Code is taken from:
  " http://learnvimscriptthehardway.stevelosh.com/chapters/49.html
  if getline(a:lnum) =~? '\v^\s*$'
    return '-1'
  endif

  let this_indent = eclim#display#fold#IndentLevel(a:lnum)
  let next_line = eclim#display#fold#NextNonBlankLine(a:lnum)
  let next_indent = eclim#display#fold#IndentLevel(next_line)

  if next_indent == this_indent
    return this_indent
  elseif next_indent < this_indent
    return this_indent
  elseif next_indent > this_indent
    return '>' . next_indent
  endif
endfunction " }}}

function! eclim#display#fold#IndentLevel(lnum) " {{{
  return indent(a:lnum) / &shiftwidth
endfunction " }}}

function! eclim#display#fold#NextNonBlankLine(lnum) " {{{
  let numlines = line('$')
  let current = a:lnum + 1

  while current <= numlines
    if getline(current) =~? '\v\S'
      return current
    endif

    let current += 1
  endwhile

  return -2
endfunction " }}}

function! eclim#display#fold#TreeFoldText() " {{{
  let line = substitute(getline(v:foldstart), '▾', '▸', 'g')
  return line
endfunction " }}}

" vim:ft=vim:fdm=marker
