" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Caching functionality
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

" Global Variables {{{
  if !exists('g:EclimCacheDir')
    let g:EclimCacheDir = expand('~/.eclim/cache')
  endif
" }}}

" Set(key, content, metadata) {{{
" Adds the supplied content (list of lines) along with the supplied metadata
" (dictionary of key / value pairs) to the cache under the specified key.
function! eclim#cache#Set (key, content, metadata)
  if !s:InitCache()
    return
  endif

  call eclim#cache#Delete(a:key)

  let file = s:GetCachedFilename(a:key)
  call writefile([string(a:metadata)] + a:content, file)

  if executable('gzip')
    call system('gzip "' . file . '"')
  endif
endfunction " }}}

" Get(key [, valid]) {{{
" Gets the content stored under the specified key.  An optional 'valid'
" argument may be supplied which must be a FuncRef which will be passed the
" metadata and must determine if the " cached value is still valid by
" returning 1 for valid and 0 for invalid.
" Returns a dictionary containing keys 'metadata' and 'content' or an empty
" dictionary if no valid cache value found.
function! eclim#cache#Get (key, ...)
  if !s:InitCache()
    return
  endif

  let file = s:GetCachedFilename(a:key)
  if filereadable(file . '.gz')
    call system('gunzip "' . file . '.gz"')
    let contents = readfile(file)
    call system('gzip "' . file . '"')
  elseif filereadable(file)
    let contents = readfile(file)
  else
    return {}
  endif

  let metadata = eval(contents[0])
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
function! eclim#cache#Delete (key)
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
function! s:GetCachedFilename (key)
  return g:EclimCacheDir . '/' . substitute(a:key, '\W\+', '_', 'g') . '.cache'
endfunction " }}}

" s:InitCache() {{{
" Initializes the cache.
function! s:InitCache ()
  if !isdirectory(g:EclimCacheDir)
    call mkdir(g:EclimCacheDir, 'p')
  endif

  return 1
endfunction " }}}

" vim:ft=vim:fdm=marker
