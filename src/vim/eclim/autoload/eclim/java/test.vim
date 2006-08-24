" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   Shared functions for java unit testing.
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

" Script Variables {{{
let s:command_src_find = '-filter vim -command java_src_find -c <classname>'

let s:entry_match{'junit'} = '] Tests run:'
let s:entry_match{'testng'} = 'eclim testng:'

let s:entry_text_replace{'junit'} = ''
let s:entry_text_with{'junit'} = ''

let s:entry_text_replace{'testng'} = '.*eclim testng: .\{-}:'
let s:entry_text_with{'testng'} = ''
" }}}

" ResolveQuickfixResults(framework) {{{
" Invoked after a :make to resolve any junit results in the quickfix entries.
function! eclim#java#test#ResolveQuickfixResults (framework)
  let entries = getqflist()
  let newentries = []
  for entry in entries
    let filename = bufname(entry.bufnr)
    let text = entry.text
    if entry.text =~ s:entry_match{a:framework}
      let filename = fnamemodify(filename, ':t')
      let text = substitute(text,
        \ s:entry_text_replace{a:framework}, s:entry_text_with{a:framework}, '')

      let command = s:command_src_find
      let command = substitute(command, '<classname>', filename, '')
      let filename = eclim#ExecuteEclim(command)
      if filename == ''
        " file not found.
        continue
      endif
    endif

    let newentry = {
        \ 'filename': filename,
        \ 'lnum': entry.lnum,
        \ 'col': entry.col,
        \ 'text': text
      \ }
    call add(newentries, newentry)
  endfor

  call setqflist(newentries, 'r')
endfunction " }}}

" vim:ft=vim:fdm=marker
