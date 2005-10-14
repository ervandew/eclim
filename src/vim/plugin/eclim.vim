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
  if !exists("g:EclimCommand")
    let g:EclimCommand = 'eclim'
  endif
  if !exists("g:EclimIndent")
    if !&expandtab
      let g:EclimIndent = "\t"
    else
      let g:EclimIndent = ""
      let index = 0
      while index < &shiftwidth
        let g:EclimIndent = g:EclimIndent . " "
        let index = index + 1
      endwhile
    endif
  endif
  if !exists("g:EclimSeparator")
    let g:EclimSeparator = '/'
    if has("win32") || has("win64")
      let g:EclimSeparator = '\'
    endif
  endif
  if !exists("g:EclimEchoHighlight")
    let g:EclimEchoHighlight = "Statement"
  endif
" }}}

" Script Variables {{{
  let s:prompted_eclimd_start = 0
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

" ExecuteEclim(args) {{{
" Executes eclim using the supplied argument string.
function! ExecuteEclim (args, ...)
  if !exists("g:EclimPath")
    if !exists("$ECLIPSE_HOME")
      echoe "ECLIPSE_HOME must be set."
      return
    endif
    let g:EclimHome = glob(expand('$ECLIPSE_HOME') . '/plugins/org.eclim*')
    let g:EclimPath = g:EclimHome . '/bin/' . g:EclimCommand
  endif

  let command = g:EclimPath . ' ' . substitute(a:args, '*', '+', 'g')
  if g:EclimDebug
    echom "Debug: " . command
  endif

  " caller requested alternate method of execution to avoid apprent vim issue
  " that causes system() to hang on large results.
  if len(a:000) > 0
    let tempfile = tempname()
    silent exec "!" . command " > " . tempfile
    let result = join(readfile(tempfile), "\n")
    call delete(tempfile)
    silent exec "normal \<c-l>"
  else
    let result = system(command)
    let result = substitute(result, '\(.*\)\n$', '\1', '')
  endif
  let error = ''

  " check for errors
  if v:shell_error && result =~ 'Exception:'
    let error = substitute(result, '\(.\{-}\)\n.*', '\1', '')
  elseif v:shell_error
    let error = result
  endif

  if error != ''
    echoe error | echoe 'while executing command: ' . command
    return
  endif

  return result
endfunction " }}}

" Echo(message) {{{
" Echos the supplied message.
function! Echo (message)
  exec "echohl " g:EclimEchoHighlight
  echo a:message
  echohl None
endfunction " }}}

" ParseQuickfixEntries(resultsFile) {{{
" Parses the supplied list of quickfix entry lines (%f|%l col %c|%m) into a
" vim compatable list of dictionaries that can be passed to setqflist().
function! ParseQuickfixEntries (entries)
  let entries = []

  for entry in a:entries
    let file = substitute(entry, '\(.\{-}\)|.*', '\1', '')
    let line = substitute(entry, '.*|\([0-9]*\) col.*', '\1', '')
    let col = substitute(entry, '.*col \([0-9]*\)|.*', '\1', '')
    let message = substitute(entry, '.*|\(.*\)', '\1', '')

    let dict = {'filename': file, 'lnum': line, 'col': col, 'text': message}
    call add(entries, dict)
  endfor

  return entries
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

  exec "echohl " . a:highlight
  try
    " echoing the list prompt vs. using it in the input() avoids apparent vim
    " bug that causes "Internal error: get_tv_string_buf()".
    echo prompt . "\n"
    let response = input(a:prompt . ": ")
    while response!~ '[0-9]\+' || response < 0 || response > (len(a:list) - 1)
      let response = input("You must choose a value between " .
        \ 0 . " and " . (len(a:list) - 1) . ". (Ctrl-C to cancel): ")
    endwhile
  finally
    echohl None
  endtry

  return response
endfunction " }}}

" CommandCompleteFile(argLead, cmdLine, cursorPos) {{{
" Custom command completion for files.
function! CommandCompleteFile (argLead, cmdLine, cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')
  let results = split(glob(expand(argLead) . '*'), '\n')
  call map(results, 'isdirectory(v:val) ? v:val . "/" : v:val')
  call map(results, "substitute(v:val, '\\', '/', 'g')")
  call map(results, "substitute(v:val, ' ', '\\\\ ', 'g')")
  let index = 0
  return results
endfunction " }}}

" CommandCompleteDir(argLead, cmdLine, cursorPos) {{{
" Custom command completion for directories.
function! CommandCompleteDir (argLead, cmdLine, cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')
  let results = split(glob(expand(argLead) . '*'), '\n')
  let index = 0
  for result in results
    if !isdirectory(result)
      call remove(results, index)
    else
      let result = result . '/'
      let result = substitute(result, '\', '/', 'g')
      let result = substitute(result, ' ', '\\\\ ', 'g')
      exec "let results[" . index . "] = \"" . result . "\""
      let index += 1
    endif
  endfor
  return results
endfunction " }}}

" vim:ft=vim:fdm=marker
