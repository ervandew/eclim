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

  let s:browsers = {
      \ 'opera':
        \ {'unix': 'opera -newpage', 'windows': 'start opera -newpage'},
      \ 'firefox':
        \ {'unix': 'firefox', 'windows': 'start firefox'},
      \ 'konqueror':
        \ {'unix': 'konqueror', 'windows': 'start konqueror'},
      \ 'epiphany':
        \ {'unix': 'epiphany', 'windows': 'start epiphany'},
      \ 'mozilla':
        \ {'unix': 'mozilla', 'windows': 'start mozilla'},
      \ 'netscape':
        \ {'unix': 'netscape', 'windows': 'start netscape'}
    \ }
" }}}

" OpenUrl(url) {{{
" Opens the supplied url in a web browser or opens the url under the cursor.
function! eclim#web#OpenUrl (url)
  if !exists('s:browser') || s:browser == ''
    let s:browser = s:DetermineBrowser()
  endif

  if s:browser == ''
    return
  endif

  let url = a:url
  if url == ''
    let url = eclim#util#GrabUri()
  endif

  if url == ''
    call eclim#util#EchoError(
      \ 'No url supplied at command line or found under the cursor.')
    return
  endif

  " prepend http:// or file:// if no protocol defined.
  if url !~ '^\w\+:\/\/'
    " absolute file on windows or unix
    if url =~ '^\([a-zA-Z]:[/\\]\|/\)'
      let url = 'file://' . url

    " everything else
    else
      let url = 'http://' . url
    endif
  endif

  for pattern in g:EclimOpenUrlInVimPatterns
    if url =~ pattern
      exec g:EclimOpenUrlInVimAction . ' ' . url
      return
    endif
  endfor

  let url = substitute(url, '\', '/', 'g')
  let url = escape(url, '&%')
  let url = escape(url, '%')
  let command = escape(substitute(s:browser, '<url>', url, ''), '#')
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

" DetermineBrowser() {{{
function! s:DetermineBrowser ()
  let browser = ''
  if exists("g:EclimBrowser")
    let browser = g:EclimBrowser
    " add "<url>" if necessary
    if browser !~ '<url>'
      let browser = substitute(browser,
        \ '^\([[:alnum:][:blank:]-/\\_.:]\+\)\(.*\)$',
        \ '\1 "<url>" \2', '')
    endif

    if has("win32") || has("win64")
      " add 'start' to run process in background if necessary.
      if browser !~ '^[!]\?start'
        let browser = 'start ' . browser
      endif
    else
      " add '&' to run process in background if necessary.
      if browser !~ '&\s*$'
        let browser = browser . ' &'
      endif

      " add redirect of std out and error if necessary.
      if browser !~ '/dev/null'
        let browser = substitute(browser, '\s*&\s*$', '&> /dev/null &', '')
      endif
    endif

    if browser !~ '^\s*!'
      let browser = '!' . browser
    endif
  else
    if has("win32") || has("win64")
      if executable('rundll32')
        let browser = '!rundll32 url.dll,FileProtocolHandler <url>'
      endif
    endif

    if browser == ''
      for key in keys(s:browsers)
        if executable(key)
          if has("win32") || has("win64")
            let browser = s:browsers[key].windows
          else
            let browser = s:browsers[key].unix
          endif
          break
        endif
      endfor
      let g:EclimBrowser = browser
      let browser = s:DetermineBrowser()
    endif
  endif

  if browser == ''
    call eclim#util#EchoError("Unable to determine browser.  " .
      \ "Please set g:EclimBrowser to your preferred browser.")
  endif
  return browser
endfunction " }}}

" vim:ft=vim:fdm=marker
