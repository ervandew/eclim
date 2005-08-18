" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   Plugin that integrates vim with the eclipse plugin eclim (ECLipse
"   IMproved).
"
"   This plugin contains shared functions that can be used regardless of the
"   current file type being edited.
"
" Platform:
"   All platforms that are support by both eclipse and vim.
"
" Dependencies:
"   Requires eclipse sdk 3.1.0 or above.
"
" Usage:
"
" Configuration:
"
" License:
"
" Copyright (c) 2004 - 2005
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
  if !exists("g:EclimDebug")
    let g:EclimDebug = 0
  endif
" }}}

" GoToCharacterOffset(offset) - Moves the cursor to the character offset specified. {{{
function! GoToCharacterOffset (offset)
  mark '
  call cursor(1,1)
  let offset = a:offset
  while offset > col('$')
    let offset = offset - col('$')
    call cursor(line('.') + 1, 1)
  endwhile
  call cursor(line('.'), offset + 1)
endfunction " }}}

" GetCharacterOffset() - Gets the character offset for the current cursor position. {{{
function! GetCharacterOffset ()
  let curline = line('.')
  let curcol = col('.')

  " count back from the current position to the beginning of the file.
  let offset = col('.') - 1
  while line('.') != 1
    call cursor(line('.') - 1, 1)
    let offset = offset + col('$')
  endwhile

  " restore the cursor position.
  call cursor(curline, curcol)

  return offset
endfunction " }}}

" GetCurrentElementPosition() - Gets the character offset and length for the element under the cursor. {{{
function! GetCurrentElementPosition ()
  let curline = line('.')
  let curcol = col('.')

  let word = expand('<cword>')

  " cursor not at the beginning of the word
  if col('.') > stridx(getline('.'), word) + 1
    silent normal b

  " cursor is on a space before the word.
  elseif col('.') < stridx(getline('.'), word) + 1
    silent normal w
  endif

  let offset = GetCharacterOffset()

  " restore the cursor position.
  call cursor(curline, curcol)

  return offset . ";" . strlen(word)
endfunction " }}}

" ConvertCharacterOffsetToLineColumn (offset) Converts the supplied offset to a line column string. {{{
function! ConvertCharacterOffsetToLineColumn (offset)
  let line = line('.')
  let col = col('.')

  call GoToCharacterOffset(a:offset)
  let offsetLine = line('.')
  let offsetCol = col('.')

  call cursor(line, col)

  return offsetLine . ' col ' . offsetCol
endfunction
" }}}

" ExecuteEclim() - Executes eclim using the supplied argument string. {{{
function! ExecuteEclim (args)
  if !exists("g:EclimPath")
    if !exists("$ECLIPSE_HOME")
      echoe "ECLIPSE_HOME must be set."
      return
    endif
    let g:EclimPath = glob(expand("$ECLIPSE_HOME") . "/plugins/org.eclim*") . "/bin/eclim"
  endif

  if g:EclimDebug
    echom "Debug: " . g:EclimPath . " " . a:args
  endif

  let result = system(g:EclimPath . " " . a:args)
  let result = substitute(result, '\(.*\)\n$', '\1', '')
  let g:EclimLastResult = result

  " check for client side exception
  if v:shell_error && result =~ 'Exception in thread "main"'
    echoe substitute(result, '.*"main" \(.\{-\}:\)\(.\{-\}\):.*', '\1\2', '')
    return
  endif

  " check for other client side exceptions
  if v:shell_error
    echoe result
    return
  endif

  " check for server side exception
  if result =~ '^<.\{-\}Exception>'
    echoe substitute(result,
      \ '^<\(.\{-\}\)>.*<\([a-zA-Z]*[Mm]essage\)>\(.\{-\}\)<\/\2.*', '\1: \3', '')
    return
  endif

  return result
endfunction " }}}

" PopulateQuickfix(results) Populates the quickfix window with the supplied " results {{{
function! PopulateQuickfix (results)
  let efm_saved = &errorformat
  let &errorformat='%f|%l|%m'
  let tmpfile = tempname()
  silent exec '!echo "' . a:results . '" > ' . tmpfile
  silent exec 'cgetfile ' . tmpfile
  silent exec '!rm ' . tmpfile
  silent exec "normal \<c-l>"
  let &errorformat = efm_saved
endfunction " }}}

" vim:ft=vim:fdm=marker
