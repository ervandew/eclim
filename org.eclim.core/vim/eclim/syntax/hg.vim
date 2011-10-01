" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Syntax file for hg commit messages.
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

if exists("b:current_syntax")
  finish
endif

if has("spell")
  syn spell toplevel
endif
syn match hgComment '^HG:.*' contains=@NoSpell
syn match hgModified '^HG: changed .*$' contained containedin=hgComment contains=@NoSpell
syn match hgProperty '^HG: \(user:\|branch\|added\|changed\|removed\)'hs=s+3 contained containedin=hgComment nextgroup=hgPropertyValue contains=@NoSpell
syn match hgPropertyValue '.*' contained contains=@NoSpell
syn match hgAction '^HG: \(added\|changed\|removed\)'hs=s+3 contained containedin=hgComment contains=@NoSpell nextgroup=hgFile
syn match hgFile '.*' contained contains=@NoSpell

hi link hgComment Comment
hi link hgModified Special
hi link hgProperty Special
hi link hgPropertyValue Special
hi link hgAction Special
hi link hgFile Constant

let b:current_syntax = "hg"

" vim:ft=vim:fdm=marker
