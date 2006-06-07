" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/web.html
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
if !exists("g:EclimOpenUrlInVimPatterns")
  let g:EclimOpenUrlInVimPatterns = []
endif
if !exists("g:EclimOpenUrlInVimAction")
  let g:EclimOpenUrlInVimAction = 'split'
endif
" }}}

" Script Variables {{{
  let s:google = 'http://www.google.com/search?q=<query>'
  let s:clusty = 'http://www.clusty.com/search?query=<query>'
  let s:wikipedia = 'http://en.wikipedia.org/wiki/Special:Search?search=<query>'
  let s:dictionary = 'http://dictionary.reference.com/search?q=<query>'
  let s:thesaurus = 'http://thesaurus.reference.com/search?q=<query>'
" }}}

" OpenUrl(url) {{{
" Opens the supplied url in a web browser or opens the url under the cursor.
function! eclim#web#OpenUrl (url)
  let url = a:url
  if url == ''
    let line = getline('.')
    let url = substitute(line,
      \ "\\(.*[[:space:]\"'(\\[{>]\\|^\\)\\(.*\\%" .
      \ col('.') . "c.\\{-}\\)\\([[:space:]\"')\\]}<].*\\|$\\)",
      \ '\2', '')
  endif

  " url must at least have a . in the string with a word char on each side to
  " be a url.
  " 192.168.0.123   blah.com
  " Exception would be file:///home/blah/somefile, but user should probably be
  " using netrw for that.
  if url !~ '\w\.\w'
    call eclim#util#EchoInfo("Not a valid url or hostname.")
    return
  endif

  for pattern in g:EclimOpenUrlInVimPatterns
    if url =~ pattern
      if url !~ '\w:\/\/'
        let url = 'http://' . url
      endif
      exec g:EclimOpenUrlInVimAction . ' ' . url
      return
    endif
  endfor

  if !exists("g:EclimBrowser")
    call eclim#util#EchoInfo("Before viewing files in a browser, you must first set" .
      \ " g:EclimBrowser to the proper value for your system.")
    echo "Firefox - let g:EclimBrowser = 'firefox \"<url>\"'"
    echo "Mozilla - let g:EclimBrowser = 'mozilla \"<url>\"'"
    echo "IE      - let g:EclimBrowser = 'iexplore <url>'"
    echo "Note: The above examples assume that the browser executable " .
      \ "is in your path."
    return
  endif

  if has("win32") || has("win64")
    if g:EclimBrowser !~ '^[!]\?start'
      let g:EclimBrowser = 'start ' . g:EclimBrowser
    endif
  else
    if g:EclimBrowser !~ '&\s*$'
      let g:EclimBrowser = g:EclimBrowser . ' &'
    endif
  endif

  if g:EclimBrowser !~ '^\s*!'
    let g:EclimBrowser = '!' . g:EclimBrowser
  endif

  let url = substitute(url, '\', '/', 'g')
  let url = escape(url, '&%')
  let url = escape(url, '%')
  let command = escape(substitute(g:EclimBrowser, '<url>', url, ''), '#')
  silent! exec command
  redraw!
endfunction " }}}

" Google(args, quote, visual) {{{
function! eclim#web#Google (args, quote, visual)
  call eclim#web#SearchEngine(s:google, a:args, a:quote, a:visual)
endfunction " }}}

" Clusty(args, quote, visual) {{{
function! eclim#web#Clusty (args, quote, visual)
  call eclim#web#SearchEngine(s:clusty, a:args, a:quote, a:visual)
endfunction " }}}

" Wikipedia(args, quote, visual) {{{
function! eclim#web#Wikipedia (args, quote, visual)
  call eclim#web#SearchEngine(s:wikipedia, a:args, a:quote, a:visual)
endfunction " }}}

" Dictionary(word) {{{
function! eclim#web#Dictionary (word)
  call eclim#web#WordLookup(s:dictionary, a:word)
endfunction " }}}

" Thesaurus(word) {{{
function! eclim#web#Thesaurus (word)
  call eclim#web#WordLookup(s:thesaurus, a:word)
endfunction " }}}

" SearchEngine(url, args, quote, visual) {{{
function! eclim#web#SearchEngine (url, args, quote, visual)
  let search_string = a:args
  if search_string == ''
    if a:visual
      echom "visual"
      let saved = @"
      normal gvy
      let search_string = substitute(@", '\n', '', '')
      let @" = saved
    else
      let search_string = expand('<cword>')
    endif
  endif

  if a:quote
    let search_string = '"' . search_string . '"'
  endif

  let search_string = eclim#html#util#UrlEncode(search_string)
  let url = substitute(a:url, '<query>', search_string, '')

  call eclim#web#OpenUrl(url)
endfunction " }}}

" WordLookup (url, word) {{{
function! eclim#web#WordLookup (url, word)
  let word = a:word
  if word == ''
    let word = expand('<cword>')
  endif

  let url = substitute(a:url, '<query>', word, '')

  call eclim#web#OpenUrl(url)
endfunction " }}}

" vim:ft=vim:fdm=marker
