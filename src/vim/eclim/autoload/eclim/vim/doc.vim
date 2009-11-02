" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/vim/doc.html
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

" Script Variables {{{
  let s:keywords = {
      \ '-complete':    'command-completion',
      \ '-nargs':       'E175',
      \ '-range':       'E177',
      \ '-count':       'E177',
      \ '-bang':        'E177',
      \ '-bar':         'E177',
      \ '-buffer':      'E177',
      \ '-register':    'E177',
      \ 'silent':       ':silent',
    \}
" }}}

" FindDoc(arg) {{{
function! eclim#vim#doc#FindDoc(arg)
  let arg = a:arg
  if arg == ''
    let arg = substitute(getline('.'),
      \  '.\{-}\(\([&<-]\|[gv]:\)\?\w*\%' . col('.') . 'c\w*[>(]\?\).*', '\1', '')

    " alternate regex
    " don't think anything meaningful can be represented in 1 char
    if len(arg) < 2
      let arg = substitute(getline('.'),
        \ '\(.*\s\|^\)\(.*\%' . col('.') . 'c.\{-}\)\(\s.*\|$\)', '\2', '')
      let arg = substitute(arg, '[!]\?\(.\{-}\)[=!>/\\].*', '\1', '')
    endif
  endif

  if arg !~ '[a-zA-Z]'
    call eclim#util#EchoInfo('Not a valid vim keyword.')
    return
  endif

  " check if word in keyword map
  if has_key(s:keywords, arg)
    let arg = s:keywords[arg]

  " reference to vim option
  elseif arg =~ '^&'
    let arg = "'" . arg[1:] . "'"
  endif

  silent exec "help " . arg
endfunction " }}}

" vim:ft=vim:fdm=marker
