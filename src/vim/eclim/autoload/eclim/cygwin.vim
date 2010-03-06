" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Utility functions for cygwin usage.
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

" Script variable {{{
  let s:cygpath_cache = {}
  let s:cygpath_previous = ['', '', '']
" }}}

" CygwinPath(path, [cache]) {{{
function! eclim#cygwin#CygwinPath(path, ...)
  return s:Cygpath(a:path, 'cygwin', a:0 > 0 ? a:1 : 0)
endfunction " }}}

" WindowsPath(path, [cache]) {{{
function! eclim#cygwin#WindowsPath(path, ...)
  return s:Cygpath(a:path, 'windows', a:0 > 0 ? a:1 : 0)
endfunction " }}}

" WindowsHome() {{{
function! eclim#cygwin#WindowsHome()
  let dir = s:Cygpath('-D', 'cygwin', 1)
  if dir == '-D'
    return ''
  endif
  return fnamemodify(dir, ':h')
endfunction " }}}

" s:CygwinPath(path, type, cache) {{{
function! s:Cygpath(path, type, cache)
  if executable('cygpath')
    let path = substitute(a:path, '\', '/', 'g')

    " try the cache if requested
    if a:cache
      let key = '[' . a:type . ']' . a:path
      let result = get(s:cygpath_cache, key, '')
      if result != ''
        return result
      endif
    endif

    " check the last requested path to see if it is being requested again.
    if s:cygpath_previous[0] == a:type && s:cygpath_previous[1] == a:path
      return s:cygpath_previous[2]
    endif

    if a:type == 'windows'
      let path = eclim#util#System('cygpath -m "' . path . '"')
    else
      let path = eclim#util#System('cygpath "' . path . '"')
    endif
    let path = substitute(path, '\n$', '', '')

    " store in the cache if requested
    if a:cache
      let s:cygpath_cache[key] = path
    endif

    " store in previous request var
    let s:cygpath_previous = [a:type, a:path, path]

    return path
  endif
  return a:path
endfunction " }}}

" vim:ft=vim:fdm=marker
