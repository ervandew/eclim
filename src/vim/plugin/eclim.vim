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
  if !exists("g:EclimShowCurrentError")
    let g:EclimShowCurrentError = 1
  endif

  if !exists("g:EclimLogLevel")
    let g:EclimLogLevel = 5
  endif
  if !exists("g:EclimTraceHighlight")
    let g:EclimTraceHighlight = "Normal"
  endif
  if !exists("g:EclimDebugHighlight")
    let g:EclimDebugHighlight = "Normal"
  endif
  if !exists("g:EclimInfoHighlight")
    let g:EclimInfoHighlight = "Statement"
  endif
  if !exists("g:EclimWarningHighlight")
    let g:EclimWarningHighlight = "WarningMsg"
  endif
  if !exists("g:EclimErrorHighlight")
    let g:EclimErrorHighlight = "Error"
  endif
  if !exists("g:EclimFatalHighlight")
    let g:EclimFatalHighlight = "Error"
  endif

  if !exists("g:EclimEchoErrorHighlight")
    let g:EclimEchoErrorHighlight = "Error"
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

  let g:EclimQuickfixAvailable = 1
" }}}

" Script Variables {{{
  let s:command_ping = "-command ping"
  let s:command_shutdown = "-command shutdown"
  let s:IgnoreConnectionRefused = 0
  let s:connection_refused = 'connect: Connection refused'
" }}}

" EchoTrace(message) {{{
function! EchoTrace (message)
  call s:EchoLevel(a:message, 6, g:EclimTraceHighlight)
endfunction " }}}

" EchoDebug(message) {{{
function! EchoDebug (message)
  call s:EchoLevel(a:message, 5, g:EclimDebugHighlight)
endfunction " }}}

" EchoInfo(message) {{{
function! EchoInfo (message)
  call s:EchoLevel(a:message, 4, g:EclimInfoHighlight)
endfunction " }}}

" EchoWarning(message) {{{
function! EchoWarning (message)
  call s:EchoLevel(a:message, 3, g:EclimWarningHighlight)
endfunction " }}}

" EchoError(message) {{{
function! EchoError (message)
  call s:EchoLevel(a:message, 2, g:EclimErrorHighlight)
endfunction " }}}

" EchoFatal(message) {{{
function! EchoFatal (message)
  call s:EchoLevel(a:message, 1, g:EclimFatalHighlight)
endfunction " }}}

" EchoLevel(message) {{{
" Echos the supplied message at the supplied level with the specified
" highlight.
function! s:EchoLevel (message, level, highlight)
  " only echo if the result is not 0, which signals that ExecuteEclim failed.
  if a:message != "0" && g:EclimLogLevel >= a:level
    exec "echohl " . a:highlight
    redraw
    echom a:message
    echohl None
  endif
endfunction " }}}

" Echo(message) {{{
" Echos a message using the info highlight regarless of what log level is set.
function! Echo (message)
  if a:message != "0"
    exec "echohl " . g:EclimInfoHighlight
    redraw
    echom a:message
    echohl None
  endif
endfunction " }}}

" ErrorsDisplay(errors) {{{
" Displays the supplied list of errors and warnings using vim's :sign
" functionality.  The 'errors' argument is expected to be a list of
" dictionaries just like that required for 'setqflist', the only difference
" being that an additional key/value is required in the dictionaries, with the
" key 'severity' and a value of 'error' or 'warning'.
function! ErrorsDisplay (errors)
  " clear any old signs
  call ErrorsDisplayClear()

  let warnings = copy(a:errors)
  let errors = filter(copy(a:errors), 'v:val.severity == "error"')
  let warnings = filter(warnings, 'v:val.severity == "warning"')

  call map(errors, 'v:val.lnum')
  call map(warnings, 'v:val.lnum')

  call SignsPlace("errors", ">>", g:EclimErrorHighlight, errors)
  call SignsPlace("warnings", ">>", g:EclimWarningHighlight, warnings)
endfunction " }}}

" ErrorsDisplayClear() {{{
" Clears the display of all errors and warnings.
function! ErrorsDisplayClear ()
  call SignsClear("errors")
  call SignsClear("warnings")
  " sometimes vim doesn't seem to want to redraw and remove the signs from
  " display.
  exec "normal \<C-L>"
endfunction " }}}

" FillTemplate(prefix, suffix) {{{
" Used as part of a vim normal map to allow the user to fill in values for
" variables in a newly added template of code.
function! FillTemplate (prefix, suffix)
  let line = getline('.')
  let prefixCol = stridx(line, a:prefix)
  let suffixCol = stridx(line, a:suffix)
  if prefixCol != -1 && suffixCol != -1
    let line = strpart(line, 0, prefixCol) . strpart(line, suffixCol + 1)
    call setline(line('.'), line)
    call cursor(line('.'), prefixCol + 1)
    startinsert
  endif
endfunction " }}}

" FindFile(file, exclude_relative) {{{
" Searches for the supplied file in the &path.
" If exclude_relative supplied is 1, then relative &path entries ('.' and '')
" are not searched).
function! FindFile (file, exclude_relative)
  let path = &path
  if a:exclude_relative
    " remove '' path entry
    let path = substitute(path, '[,]\?[,]\?', '', 'g')
    " remove '.' path entry
    let path = substitute(path, '[,]\?\.[,]\?', '', 'g')
  endif
  return split(globpath(path, "**/" . a:file), '\n')
endfunction " }}}

" GetCharacterOffset() {{{
" Gets the character offset for the current cursor position.
function! GetCharacterOffset ()
  let curline = line('.')
  let curcol = col('.')
  let lineend = 0
  if &fileformat == "dos"
    let lineend = 1
  endif

  " count back from the current position to the beginning of the file.
  let offset = col('.') - 1
  while line('.') != 1
    call cursor(line('.') - 1, 1)
    let offset = offset + col('$') + lineend
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

" GetPathEntry(file) {{{
" Returns the path entry that contains the supplied file (excluding '.' and '').
" The argument must be an absolute path to the file.
" &path is expected to be using commas for path delineation.
" Returns 0 if no path found.
function! GetPathEntry (file)
  let paths = split(&path, ',')
  for path in paths
    if path != "" && path != "."
      let path = substitute(expand(path), '\', '/', 'g')
      let file = substitute(expand(a:file), '\', '/', 'g')
      if file =~ '^' . path
        return path
      endif
    endif
  endfor
  return 0
endfunction " }}}

" GoToBufferWindow(bufname) {{{
" Returns the to window containing the supplied buffer name.
function! GoToBufferWindow (bufname)
  let winnr = bufwinnr(bufnr(b:filename))
  if winnr != -1
    exec winnr . "winc w"
  endif
endfunction " }}}

" ListContains(list, element) {{{
" Returns 1 if the supplied list contains the specified element, 0 otherwise.
" To determine element equality both '==' and 'is' are tried as well as
" ^element$ to support a regex supplied element string.
function! ListContains (list, element)
  for element in a:list
    if element == a:element ||
        \ element is a:element ||
        \ string(element) =~ '^' . escape(string(a:element), '\') . '$'
      return 1
    endif
  endfor
  return 0
endfunction " }}}

" ParseArgs(args) {{{
" Parses the supplied argument line into a list of args.
function! ParseArgs (args)
  let args = split(a:args, '[^\\]\s\zs')
  call map(args, 'substitute(v:val, "\\s*$", "", "")')

  return args
endfunction " }}}

" ParseQuickfixEntries(entries) {{{
" Parses the supplied list of quickfix entry lines (%f|%l col %c|%m) into a
" vim compatable list of dictionaries that can be passed to setqflist().
" In addition to the above line format, this function also supports
" %f|%l col %c|%m|%s, where %s is the severity of the entry.  The value will
" be placed in the dictionary under the 'severity' key.
function! ParseQuickfixEntries (entries)
  let entries = []

  for entry in a:entries
    let file = substitute(entry, '\(.\{-}\)|.*', '\1', '')
    let line = substitute(entry, '.*|\([0-9]\+\) col.*', '\1', '')
    let col = substitute(entry, '.*col \([0-9]\+\)|.*', '\1', '')
    let message = substitute(entry, '.*col [0-9]\+|\(.\{-}\)\(|.*\|$\)', '\1', '')
    let severity = substitute(entry, '.*|\(error\|warning\)$', '\1', '')

    let dict = {
      \ 'filename': file,
      \ 'lnum': line,
      \ 'col': col,
      \ 'text': message,
      \ 'severity': severity}

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
    " clear any previous messages
    redraw
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

" RefreshFile() {{{
function! RefreshFile ()
  "FIXME: doing an :edit clears the undo tree, but the code commented out below
  "       causes a user prompt on the write.  Need to pose this senario on the
  "       vim mailing lists.
  edit!
  "autocmd FileChangedShell nested <buffer> echom " ### file changed ### "
  "checktime
  "autocmd! FileChangedShell <buffer>

  "let saved = @"

  "1,$delete
  "silent exec "read " . expand('%:p')
  "1delete

  "let @" = saved

  silent write!
endfunction " }}}

" SetQuickfixAvailability() {{{
" Sets the global variable that tracks whether or not the user has executed a
" quickfix command containing results.  This allows other various commands to
" keep from overwriting those results.
function! s:SetQuickfixAvailability ()
  if len(getqflist()) > 0
    let g:EclimQuickfixAvailable = 0
  else
    let g:EclimQuickfixAvailable = 1
  endif
endfunction " }}}

" ShowCurrentError() {{{
" Shows the error on the cursor line if one.
function! s:ShowCurrentError ()
  let line = line('.')
  let col = col('.')

  let errornum = 0
  let index = 0
  let errors = getqflist()
  for error in errors
    let index += 1
    if error.lnum == line && error.bufnr == bufnr("%")
      if errornum == 0 || col >= error.col
        let errornum = index
      endif
    endif
  endfor

  if errornum > 0
    echo "(" . errornum . " of " . len(errors) . "): " . errors[errornum - 1].text
  endif
endfunction " }}}

" SignsClear(name) {{{
" Clears all signs in the current buffer for the supplied sign name.
function! SignsClear (name)
  silent! exec "sign undefine " . a:name
endfunction " }}}

" SignsPlace(name) {{{
" Places signs in the current buffer.
function! SignsPlace (name, text, highlight, lines)
  exec "sign define " . a:name . " text=" . a:text . " texthl=" . a:highlight
  for line in a:lines
    if line > 0
      exec "sign place " . line . " line=" . line . " name=" . a:name .
        \ " buffer=" . bufnr('%')
    endif
  endfor
endfunction " }}}

" TempWindow(name, lines) {{{
" Opens a temp window w/ the given name and contents.
function! TempWindow (name, lines)
  call TempWindowClear(a:name)

  if bufwinnr(a:name) == -1
    silent! exec "botright 10split " . a:name
    setlocal nowrap
    setlocal winfixheight
    setlocal noswapfile
    setlocal buftype=nofile
    setlocal bufhidden=delete
  else
    exec bufwinnr(a:name) . "winc w"
  endif

  call append(1, a:lines)
  retab
  let saved = @"
  1delete
  let @" = saved

  setlocal nomodified
  setlocal nomodifiable
  setlocal readonly
endfunction " }}}

" TempWindowClear(name) {{{
" Opens a temp window w/ the given name and contents.
function! TempWindowClear (name)
  if bufwinnr(a:name) != -1
    let curwinnr = winnr()
    exec bufwinnr(a:name) . "winc w"
    setlocal modifiable
    setlocal noreadonly
    let saved = @"
    1,$delete
    let @" = saved
    exec curwinnr . "winc w"
  endif
endfunction " }}}

" TempWindowCommand(command, name) {{{
" Opens a temp window w/ the given name and contents from the result of the
" supplied command.
function! TempWindowCommand (command, name)
  let filename = expand('%:p')

  let line = 1
  let col = 1
  " if the window is open, save the cursor position
  if bufwinnr(a:name) != -1
    exec bufwinnr(a:name) . "winc w"
    let line = line('.')
    let col = col('.')
  endif

  call TempWindowClear(a:name)

  let results = split(ExecuteEclim(a:command), '\n')
  if len(results) == 1 && results[0] == '0'
    return
  endif

  call TempWindow(a:name, results)

  call cursor(line, col)

  let b:filename = filename
  autocmd! BufUnload <buffer>
  autocmd BufUnload <buffer> call GoToBufferWindow(b:filename)
endfunction " }}}

" ViewInBrowser(url) {{{
" View the supplied url in a browser.
function! ViewInBrowser (url)
  if !exists("g:EclimBrowser")
    call EchoInfo("Before viewing files in a browser, you must first set" .
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

  let url = substitute(a:url, '\', '/', 'g')
  let command = escape(substitute(g:EclimBrowser, '<url>', url, ''), '#')
  silent! exec command
  exec "normal \<c-l>"
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

  call EchoDebug("eclim: executing (Ctrl-C to cancel)...")
  call EchoTrace("command: " . command)

  " caller requested alternate method of execution to avoid apprent vim issue
  " that causes system() to hang on large results.
  if len(a:000) > 0
    let tempfile = tempname()
    silent exec "!" . command " > " . tempfile . " 2>&1"
    let result = join(readfile(tempfile), "\n")
    call delete(tempfile)
    silent exec "normal \<c-l>"
  else
    let result = system(command)
    let result = substitute(result, '\(.*\)\n$', '\1', '')
  endif
  call Echo("")

  " check for errors
  let error = ''
  if v:shell_error && result =~ 'Exception:'
    let error = substitute(result, '\(.\{-}\)\n.*', '\1', '')

  " ignoring code 227 since it appears to be a false error that i can't
  " reproduce at the command line, only within vim.
  elseif v:shell_error && v:shell_error != 227
    let error = result
  endif

  if error != ''
    if s:IgnoreConnectionRefused && error == s:connection_refused
      return
    elseif error == s:connection_refused
      call EchoWarning("eclimd not running.")
      return
    endif
    echoe error | echoe 'while executing command: ' . command
    return
  endif

  return result
endfunction " }}}

" PingEclim(echo) {{{
" Pings the eclimd server.
" If echo is non 0, then the result is echoed to the user.
function! PingEclim (echo)
  try
    let s:IgnoreConnectionRefused = 1
    let result = ExecuteEclim(s:command_ping)
    if a:echo
      if result == '0'
        call Echo("Connection Refused")
      else
        call Echo(result)
      endif
    else
      return result != '0'
    endif
  finally
    let s:IgnoreConnectionRefused = 0
  endtry
endfunction " }}}

" ShutdownEclim() {{{
" Shuts down the eclimd server.
function! s:ShutdownEclim ()
  call ExecuteEclim(s:command_shutdown)
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

  return ParseCommandCompletionResults(argLead, results)
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
  return ParseCommandCompletionResults(argLead, results)
endfunction " }}}

" ParseCommandCompletionResults(args) {{{
" Bit of a hack for vim's lack of support for escaped spaces in custom
" completion.
function! ParseCommandCompletionResults (argLead, results)
  let results = a:results
  if stridx(a:argLead, ' ') != -1
    let removePrefix = escape(substitute(a:argLead, '\(.*\s\).*', '\1', ''), '\')
    call map(results, "substitute(v:val, '^" . removePrefix . "', '', '')")
  endif
  return results
endfunction " }}}

" Command Declarations {{{
if !exists(":PingEclim")
  command PingEclim :call PingEclim(1)
endif
if !exists(":ShutdownEclim")
  command ShutdownEclim :call <SID>ShutdownEclim()
endif
" }}}

" Auto Commands{{{
if g:EclimShowCurrentError
  augroup eclim_show_error
    autocmd!
    autocmd CursorHold * call s:ShowCurrentError()
  augroup END
endif

augroup eclim_quickfix_cmd
  autocmd!
  autocmd QuickFixCmdPost * call s:SetQuickfixAvailability()
augroup END
" }}}

" vim:ft=vim:fdm=marker
