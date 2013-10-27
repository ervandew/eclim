" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/common/web.html
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

" Global Variables {{{
if !exists("g:EclimOpenUrlInVimPatterns")
  let g:EclimOpenUrlInVimPatterns = []
endif
if !exists("g:EclimOpenUrlInVimAction")
  let g:EclimOpenUrlInVimAction = g:EclimDefaultFileOpenAction
endif
" }}}

" Script Variables {{{
  let s:win_browsers = [
      \ 'C:/Program Files/Opera/Opera.exe',
      \ 'C:/Program Files/Mozilla Firefox/firefox.exe',
      \ 'C:/Program Files/Internet Explorer/iexplore.exe'
    \ ]

  let s:browsers = [
      \ 'xdg-open', 'chromium', 'opera', 'firefox', 'konqueror',
      \ 'epiphany', 'mozilla', 'netscape', 'iexplore'
    \ ]
" }}}

function! eclim#web#OpenUrl(url, ...) " {{{
  " Opens the supplied url in a web browser or opens the url under the cursor.

  if !exists('s:browser') || s:browser == ''
    let s:browser = s:DetermineBrowser()

    " slight hack for IE which doesn't like the url to be quoted.
    if s:browser =~ 'iexplore' && !has('win32unix')
      let s:browser = substitute(s:browser, '"', '', 'g')
    endif
  endif

  if s:browser == ''
    return
  endif

  let url = a:url
  if url == ''
    if len(a:000) > 2
      let start = a:000[1]
      let end = a:000[2]
      while start <= end
        call eclim#web#OpenUrl(eclim#util#GrabUri(start, col('.')), a:000[0])
        let start += 1
      endwhile
      return
    else
      let url = eclim#util#GrabUri()
    endif
  endif

  if url == ''
    call eclim#util#EchoError(
      \ 'No url supplied at command line or found under the cursor.')
    return
  endif

  " prepend http:// or file:// if no protocol defined.
  if url !~ '^\(https\?\|file\):'
    " absolute file on windows or unix
    if url =~ '^\([a-zA-Z]:[/\\]\|/\)'
      let url = 'file://' . url

    " everything else
    else
      let url = 'http://' . url
    endif
  endif

  if len(a:000) == 0 || a:000[0] == ''
    for pattern in g:EclimOpenUrlInVimPatterns
      if url =~ pattern
        exec g:EclimOpenUrlInVimAction . ' ' . url
        return
      endif
    endfor
  endif

  let url = substitute(url, '\', '/', 'g')
  let url = escape(url, '&%!')
  let url = escape(url, '%!')
  let command = escape(substitute(s:browser, '<url>', url, ''), '#')
  silent call eclim#util#Exec(command)
  redraw!

  if v:shell_error
    call eclim#util#EchoError("Unable to open browser:\n" . s:browser .
      \ "\nCheck that the browser executable is in your PATH " .
      \ "or that you have properly configured g:EclimBrowser")
  endif
endfunction " }}}

function! eclim#web#SearchEngine(url, args, line1, line2) " {{{
  " Function to use a search engine to search for a word or phrase.

  let search_string = a:args
  if search_string == ''
    let search_string = eclim#util#GetVisualSelection(a:line1, a:line2, 0)
    if search_string == ''
      let search_string = expand('<cword>')
    endif
  endif

  let search_string = eclim#html#util#UrlEncode(search_string)
  let url = substitute(a:url, '<query>', search_string, '')

  call eclim#web#OpenUrl(url)
endfunction " }}}

function! eclim#web#WordLookup(url, word) " {{{
  " Function to lookup a word on an online dictionary, thesaurus, etc.

  let word = a:word
  if word == ''
    let word = expand('<cword>')
  endif

  let url = substitute(a:url, '<query>', word, '')

  call eclim#web#OpenUrl(url)
endfunction " }}}

function! s:DetermineBrowser() " {{{
  let browser = ''

  " user specified a browser, we just need to fill in any gaps if necessary.
  if exists("g:EclimBrowser")
    let browser = g:EclimBrowser
    " add "<url>" if necessary
    if browser !~ '<url>'
      let browser = substitute(browser,
        \ '^\([[:alnum:][:blank:]-/\\_.:"]\+\)\(.*\)$',
        \ '\1 "<url>" \2', '')
    endif

    if has("win32") || has("win64")
      " add 'start' to run process in background if necessary.
      if browser !~ '^[!]\?start'
        let browser = 'start ' . browser
      endif
    else
      " add '&' to run process in background if necessary.
      if browser !~ '&\s*$' &&
       \ browser !~ '^\(/[/a-zA-Z0-9]\+/\)\?\<\(links\|lynx\|elinks\|w3m\)\>'
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

  " user did not specify a browser, so attempt to find a suitable one.
  else
    if has('win32') || has('win64') || has('win32unix')
      " Note: this version may not like .html suffixes on windows 2000
      if executable('rundll32')
        let browser = 'rundll32 url.dll,FileProtocolHandler <url>'
      endif
      " this doesn't handle local files very well or '&' in the url.
      "let browser = '!cmd /c start <url>'
      if browser == ''
        for name in s:win_browsers
          if has('win32unix')
            let name = eclim#cygwin#CygwinPath(name)
          endif
          if executable(name)
            let browser = name
            if has('win32unix')
              let browser = '"' . browser . '"'
            endif
            break
          endif
        endfor
      endif
    elseif has('mac')
      let browser = '!open "<url>"'
    else
      for name in s:browsers
        if executable(name)
          let browser = name
          break
        endif
      endfor
    endif

    if browser != ''
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
