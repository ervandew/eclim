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
    let g:EclimCommand = "eclim"
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
    let g:EclimHome = glob(expand("$ECLIPSE_HOME") . "/plugins/org.eclim*")
    let g:EclimPath = g:EclimHome . "/bin/" . g:EclimCommand
  endif

  if g:EclimDebug
    echom "Debug: " . g:EclimPath . " " . a:args
  endif

  let result = system(g:EclimPath . ' ' . substitute(a:args, '*', '#', 'g'))
  let result = substitute(result, '\(.*\)\n$', '\1', '')
  let g:EclimLastResult = result
  let error = ''

  " check for client side exception
  if v:shell_error && result =~ 'Exception in thread "main"'
    let error = substitute(result, '.*"main" \(.\{-\}:\)\(.\{-\}\):.*', '\1\2', '')

  " check for other client side exceptions
  elseif v:shell_error
    let error = result

  " check for server side exception
  elseif result =~ '^<.\{-\}Exception>'
    let error = substitute(result,
      \ '^<\(.\{-\}\)>.*<\([a-zA-Z]*[Mm]essage\)>\(.\{-\}\)<\/\2.*', '\1: \3', '')
  endif

  if error != ''
    "if error =~ 'Connection refused' && !s:prompted_eclimd_start
    "  if EclimdStartupPrompt()
    "    return ExecuteEclim(a:args)
    "  else
    "    return
    "  endif
    "endif
    echoe error
    return
  endif

  return result
endfunction " }}}

" EclimdStartupPrompt() {{{
" Prompts the user to choose whether or not to startup eclimd.
" returns 1 if eclimd is started, 0 otherwise.
""" FIXME: Not working yet.  Vim kills all processes started from it when
"""        closed.
"function! EclimdStartupPrompt ()
"  echohl Statement
"  try
"    let response = input("eclimd not started.\nStart now?\n(y)es (n)o\n: ")
"    while response !~ '\([yn]\|yes\|no\)\c'
"      let response = input("Please choose (y)es or (n)o\n: ")
"    endwhile
"    let s:prompted_eclimd_start = 1
"    if response =~ '^y'
"      let eclimdPath = g:EclimHome . "/bin/eclimd"
"      let result = system(eclimdPath)
"      if v:shell_error
"        echoe result
"        return 0
"      endif
"      echo "Starting eclimd..."
"      if WaitFor(g:EclimHome . "/log/eclim.log", "Server started", 10, 1)
"        echo "eclimd started."
"      else
"        echom "Timed out waiting for eclimd to start."
"        return 0
"      endif
"    endif
"  finally
"    echohl None
"  endtry
"  return 1
"endfunction " }}}

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

" WaitFor(file, regex, timeout, interval) {{{
" Waits for a file to be created and an optional regex to exist in the file.
" Returns 1 if the file was created and the regex found, 0 otherwise.
""" FIXME: Not working yet.
"function! WaitFor (file, regex, timeout, interval)
"  " ensure a valid interval
"  let interval = a:interval
"  if interval <= 0
"    let interval = 1
"  endif
"
"  let time = 0
"  while time < a:timeout
"    if filereadable(a:file)
"      try
"echom "vimgrep /" . a:regex . "/ " . a:file
"        exec "vimgrep /" . a:regex . "/ " . a:file
"        let matches = getqflist()
"        if len(matches) > 0
"          return 1
"        endif
"      catch
"        " ignore
"      endtry
"    endif
"    let time = time + interval
"    exec interval . "sleep"
"  endwhile
"
"  return 0
"endfunction " }}}

" vim:ft=vim:fdm=marker
