" Author:  Eric Van Dewoestine
"
" Description: {{{
"
" License:
"
" Copyright (C) 2005 - 2010  Eric Van Dewoestine
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

runtime ftplugin/xml.vim
runtime indent/xml.vim

if !exists("g:tlist_junitresult_settings")
  let g:tlist_junitresult_settings = {
      \ 'lang': 'junitresult',
      \ 'parse': 'eclim#taglisttoo#lang#junit#Parse',
      \ 'tags': {'t': 'testcase', 'f': 'failure', 'o': 'output'}
    \ }
endif

" vim:ft=vim:fdm=marker
