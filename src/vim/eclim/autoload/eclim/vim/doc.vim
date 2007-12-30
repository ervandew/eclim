" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/vim/doc.html
"
" License:
"
" Copyright (c) 2005 - 2008
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
function! eclim#vim#doc#FindDoc (arg)
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
