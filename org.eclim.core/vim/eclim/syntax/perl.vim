" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Extension to default perl syntax to support spell checking in comments and
"   plain POD syntax.
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

source $VIMRUNTIME/syntax/perl.vim

syn match perlComment "#.*" contains=perlTodo,@Spell

if !exists("perl_include_pod")
  if exists("perl_fold")
    syn region perlPOD start="^=[a-z]" end="^=cut" fold contains=@Spell
  else
    syn region perlPOD start="^=[a-z]" end="^=cut" contains=@Spell
  endif
endif

" vim:ft=vim:fdm=marker
