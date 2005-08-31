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

" GetCharacterOffset() {{{
" Gets the character offset for the current cursor position.
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

" GetCurrentElementPosition() {{{
" Gets the character offset and length for the element under the cursor.
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

" GetCurrentProjectName() {{{
" Gets the project name that the current file is in.
function! GetCurrentProjectName ()
  let projectName = ""
  let projectFile = findfile(".project", ".;")
  if filereadable(projectFile)
    let cmd = winrestcmd()

    silent exec "sview " . projectFile
    let line = search('<name\s*>', 'wn')
    if line != 0
      let projectName = substitute(getline(line), '.\{-}>\(.*\)<.*', '\1', '')
    endif
    silent close

    silent exec cmd
  endif

  return projectName
endfunction " }}}

" ExecuteEclim(args) {{{
" Executes eclim using the supplied argument string.
function! ExecuteEclim (args)
  if !exists("g:EclimPath")
    if !exists("$ECLIPSE_HOME")
      echoe "ECLIPSE_HOME must be set."
      return
    endif
    let g:EclimPath = glob(expand("$ECLIPSE_HOME") . "/plugins/org.eclim*") .
      \ "/bin/eclim"
  endif

  if g:EclimDebug
    echom "Debug: " . g:EclimPath . " " . a:args
  endif

  let result = system(g:EclimPath . ' ' . substitute(a:args, '*', '#', 'g'))
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
      \ '^<\(.\{-\}\)>.*<\([a-zA-Z]*[Mm]essage\)>\(.\{-\}\)<\/\2.*',
      \ '\1: \3', '')
    return
  endif

  return result
endfunction " }}}

" PopulateQuickfix(resultsFile) {{{
" Populates the quickfix window with the supplied resultsFile.
function! PopulateQuickfix (resultsFile)
  let efm_saved = &errorformat
  let &errorformat='%f|%l col %c|%m'
  silent exec 'cgetfile ' . a:resultsFile
  call delete(a:resultsFile)
  silent exec "normal \<c-l>"
  let &errorformat = efm_saved
endfunction " }}}

" PromptList(prompt, list, highlight) {{{
" Creates a prompt for the user using the supplied prompt string and list of
" items to choose from.  Returns -1 if the list is empty, and 0 if the list
" contains only one item.
function! PromptList (prompt, list, highlight)
  " no elements, no prompt
  if empty(a:list)
    return -1
  endif

  " only one elment, no need to choose.
  if len(a:list) == 1
    return 0
  endif

  let prompt = ""
  let index = 0
  for item in a:list
    let prompt = prompt . index . ") " . item . "\n"
    let index = index + 1
  endfor
  let prompt = prompt . "\n" . a:prompt . ": "

  exec "echohl " . a:highlight
  try
    let response = input(prompt)
    while response!~ '[0-9]\+' || response < 0 || response > (len(a:list) - 1)
      let response = input("You must choose a value between " .
        \ 0 . " and " . (len(a:list) - 1) . ". (Ctrl-C to cancel): ")
    endwhile
  finally
    echohl None
  endtry

  return response
endfunction
" }}}

" vim:ft=vim:fdm=marker
