" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Default xml.vim only defines the xmlRegion if xml folding is enabled, but
"   xmlRegion is needed to allow spell check highlighting of xml text.
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

source $VIMRUNTIME/syntax/xml.vim

" the c# syntax file loads syntax/xml.vim, but the below changes may break
" syntax highlighting in c#
if &ft == 'cs'
  finish
endif

if !exists('g:xml_syntax_folding')
  " taken from syntax/xml.vim, but removed unecessary portions.
  syn region   xmlRegion
    \ start=+<\z([^ /!?<>"']\+\)+
    \ skip=+<!--\_.\{-}-->+
    \ end=+</\z1\_\s\{-}>+
    \ contains=xmlTag,xmlEndTag,xmlCdata,xmlRegion,xmlComment,xmlEntity,xmlProcessing,@xmlRegionHook,@Spell
endif

" vim:ft=vim:fdm=marker
