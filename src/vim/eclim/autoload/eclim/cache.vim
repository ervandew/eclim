" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Caching functionality
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

" Global Variables {{{
  if !exists('g:EclimCacheDir')
    let g:EclimCacheDir = expand('~/.eclim/cache')
  endif
" }}}

" Set(key, content, metadata) {{{
" Adds the supplied content (list of lines) along with the supplied metadata
" (dictionary of key / value pairs) to the cache under the specified key.
function! eclim#cache#Set(key, content, ...)
  if !s:InitCache()
    return
  endif

  call eclim#cache#Delete(a:key)

  let file = s:GetCachedFilename(a:key)
  let content = a:content
  if a:0 > 0 && len(a:1) > 0
    let content = [string(a:1)] + content
  else
    let content = [''] + content
  endif
  call writefile(content, file)

  if executable('gzip')
    call eclim#util#System('gzip "' . file . '"')
  endif
endfunction " }}}

" Get(key [, valid]) {{{
" Gets the content stored under the specified key.  An optional 'valid'
" argument may be supplied which must be a FuncRef which will be passed the
" metadata and must determine if the " cached value is still valid by
" returning 1 for valid and 0 for invalid.
" Returns a dictionary containing keys 'metadata' and 'content' or an empty
" dictionary if no valid cache value found.
function! eclim#cache#Get(key, ...)
  if !s:InitCache()
    return
  endif

  let file = s:GetCachedFilename(a:key)
  if filereadable(file . '.gz')
    call eclim#util#System('gzip -d "' . file . '.gz"')
    let contents = readfile(file)
    call eclim#util#System('gzip "' . file . '"')
  elseif filereadable(file)
    let contents = readfile(file)
  else
    return {}
  endif

  let metadata = contents[0] != '' ? eval(contents[0]) : {}
  if len(a:000) > 0
    let Function = a:000[0]
    if !Function(metadata)
      call eclim#cache#Delete(a:key)
      return {}
    endif
  endif

  return {'metadata': metadata, 'content': contents[1:]}
endfunction " }}}

" Delete(key) {{{
" Delete any cached content under the specified key.
function! eclim#cache#Delete(key)
  if !s:InitCache()
    return
  endif

  let file = s:GetCachedFilename(a:key)
  if filereadable(file . '.gz')
    call delete(file . '.gz')
  endif

  if filereadable(file)
    call delete(file)
  endif
endfunction " }}}

" s:GetCachedFilename(key) {{{
function! s:GetCachedFilename(key)
  return g:EclimCacheDir . '/' . substitute(a:key, '\W\+', '_', 'g') . '.cache'
endfunction " }}}

" s:InitCache() {{{
" Initializes the cache.
function! s:InitCache()
  if !isdirectory(g:EclimCacheDir)
    call mkdir(g:EclimCacheDir, 'p')
  endif

  return 1
endfunction " }}}

" vim:ft=vim:fdm=marker
