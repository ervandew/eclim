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

" ParseWebXml(file, settings) {{{
function! eclim#taglisttoo#lang#webxml#ParseWebXml(file, settings)
  return taglisttoo#util#Parse(a:file, a:settings, [
      \ ['p', '<context-param\s*>\s*<param-name\s*>\s*(.*?)\s*</param-name\s*>', 1],
      \ ['f', '<filter\s*>\s*<filter-name\s*>\s*(.*?)\s*</filter-name\s*>', 1],
      \ ['i', '<filter-mapping\s*>\s*<filter-name\s*>\s*(.*?)\s*</filter-name\s*>', 1],
      \ ['l', '<listener\s*>\s*<listener-class\s*>\s*(.*?)\s*</listener-class\s*>', 1],
      \ ['s', '<servlet\s*>\s*<servlet-name\s*>\s*(.*?)\s*</servlet-name\s*>', 1],
      \ ['v', '<servlet-mapping\s*>\s*<servlet-name\s*>\s*(.*?)\s*</servlet-name\s*>', 1],
    \ ])
endfunction " }}}

" ParseTld(file, settings) {{{
function! eclim#taglisttoo#lang#webxml#ParseTld(file, settings)
  return taglisttoo#util#Parse(a:file, a:settings, [
      \ ['t', '<tag\s*>\s*<name\s*>\s*(.*?)\s*</name\s*>', 1],
    \ ])
endfunction " }}}

" vim:ft=vim:fdm=marker
