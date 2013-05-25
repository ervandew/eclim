" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Utility functions for cygwin usage.
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

function! eclim#cygwin#CygwinPath(path) " {{{
  return s:Cygpath(a:path, 'cygwin')
endfunction " }}}

function! eclim#cygwin#WindowsPath(path) " {{{
  if type(a:path) == g:STRING_TYPE && a:path =~? '^[a-z]:'
    return substitute(a:path, '\', '/', 'g')
  endif
  return s:Cygpath(a:path, 'windows')
endfunction " }}}

function! eclim#cygwin#WindowsHome() " {{{
  if !exists('s:cygpath_winhome')
    let dir = s:Cygpath('-D', 'cygwin')
    let s:cygpath_winhome = dir != '-D' ? fnamemodify(dir, ':h') : ''
  endif
  return s:cygpath_winhome
endfunction " }}}

function! s:Cygpath(paths, type) " {{{
  if executable('cygpath')
    let paths = type(a:paths) == g:LIST_TYPE ? a:paths : [a:paths]
    let paths = map(paths, "'\"' . substitute(v:val, '\\', '/', 'g') . '\"'")

    let args = a:type == 'windows' ? '-m ' : ''
    let results = split(eclim#util#System('cygpath ' . args . join(paths)), "\n")

    if type(a:paths) == g:LIST_TYPE
      return results
    endif
    return results[0]
  endif
  return a:paths
endfunction " }}}

" vim:ft=vim:fdm=marker
