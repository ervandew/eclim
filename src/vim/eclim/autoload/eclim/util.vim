" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Utility functions.
"
"   This plugin contains shared functions that can be used regardless of the
"   current file type being edited.
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
  let s:buffing_write_closing_commands = '^\s*\(' .
    \ 'wq\|xa\|' .
    \ '\d*w[nN]\|\d*wp\|' .
    \ 'ZZ' .
    \ '\)'
" }}}

" Abbreviate(abbreviation) {{{
function! eclim#util#Abbreviate (abbreviation)
  " gobble up the space char used to kick off the abbreviation
  let char = nr2char(getchar())

  " support <indent> placemark to combat indenting issues when attempting to
  " start the cursor on a blank line, and possibly else where.
  let indent = eclim#util#GetIndent(indent(line('.')))
  let abbrev = substitute(a:abbreviation, '<indent>', indent, 'g')

  " insert the abbreviation text.
  exec "normal i" . abbrev

  return "\<right>"
endfunction " }}}

" EchoTrace(message) {{{
function! eclim#util#EchoTrace (message)
  call s:EchoLevel(a:message, 6, g:EclimTraceHighlight)
endfunction " }}}

" EchoDebug(message) {{{
function! eclim#util#EchoDebug (message)
  call s:EchoLevel(a:message, 5, g:EclimDebugHighlight)
endfunction " }}}

" EchoInfo(message) {{{
function! eclim#util#EchoInfo (message)
  call s:EchoLevel(a:message, 4, g:EclimInfoHighlight)
endfunction " }}}

" EchoWarning(message) {{{
function! eclim#util#EchoWarning (message)
  call s:EchoLevel(a:message, 3, g:EclimWarningHighlight)
endfunction " }}}

" EchoError(message) {{{
function! eclim#util#EchoError (message)
  call s:EchoLevel(a:message, 2, g:EclimErrorHighlight)
endfunction " }}}

" EchoFatal(message) {{{
function! eclim#util#EchoFatal (message)
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
    for line in split(a:message, '\n')
      echom line
    endfor
    echohl None
  endif
endfunction " }}}

" Echo(message) {{{
" Echos a message using the info highlight regarless of what log level is set.
function! eclim#util#Echo (message)
  if a:message != "0" && g:EclimLogLevel > 0
    exec "echohl " . g:EclimInfoHighlight
    redraw
    for line in split(a:message, '\n')
      echom line
    endfor
    echohl None
  endif
endfunction " }}}

" Exec(cmd) {{{
" Used when executing ! commands that may be disrupted by non default vim
" options.
function! eclim#util#Exec (cmd)
  call eclim#util#System(a:cmd, 1)
endfunction " }}}

" ExecWithoutAutocmds(cmd) {{{
" Execute a command after disabling all autocommands (borrowed from taglist.vim)
function! eclim#util#ExecWithoutAutocmds (cmd)
  let save_opt = &eventignore
  set eventignore=all
  try
    exec a:cmd
  finally
    let &eventignore = save_opt
  endtry
endfunction " }}}

" FillTemplate(prefix, suffix) {{{
" Used as part of a vim normal map to allow the user to fill in values for
" variables in a newly added template of code.
function! eclim#util#FillTemplate (prefix, suffix)
  let line = getline('.')
  let prefixCol = stridx(line, a:prefix)
  let suffixCol = stridx(line, a:suffix, prefixCol)
  if prefixCol != -1 && suffixCol != -1
    let line = strpart(line, 0, prefixCol) . strpart(line, suffixCol + 1)
    call setline(line('.'), line)
    call cursor(line('.'), prefixCol + 1)
    startinsert
  endif
endfunction " }}}

" FindFileInPath(file, exclude_relative) {{{
" Searches for the supplied file in the &path.
" If exclude_relative supplied is 1, then relative &path entries ('.' and '')
" are not searched).
function! eclim#util#FindFileInPath (file, exclude_relative)
  let path = &path
  if a:exclude_relative
    " remove '' path entry
    let path = substitute(path, '[,]\?[,]\?', '', 'g')
    " remove '.' path entry
    let path = substitute(path, '[,]\?\.[,]\?', '', 'g')
  endif
  return split(eclim#util#Globpath(path, "**/" . a:file), '\n')
endfunction " }}}

" Findfile(name, [, path [, count]]) {{{
" Used to issue a findfile() handling any vim options that may otherwise
" disrupt it.
function! eclim#util#Findfile (name, ...)
  let savewig = &wildignore
  set wildignore=""
  if len(a:000) == 0
    let result = findfile(a:name)
  elseif len(a:000) == 1
    let result = findfile(a:name, expand(escape(a:000[0], '*')))
  elseif len(a:000) == 2
    let result = findfile(a:name, expand(escape(a:000[0], '*')), a:000[1])
  endif
  let &wildignore = savewig

  return result
endfunction " }}}

" GetCharacterOffset() {{{
" Gets the character offset for the current cursor position.
function! eclim#util#GetCharacterOffset ()
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

" GetCurrentElementColumn() {{{
" Gets the column for the element under the cursor.
function! eclim#util#GetCurrentElementColumn ()
  let curline = line('.')
  let curcol = col('.')

  let line = getline('.')
  " cursor not on the word
  if line[col('.') - 1] =~ '\W'
    silent normal w

  " cursor not at the beginning of the word
  elseif line[col('.') - 2] =~ '\w'
    silent normal b
  endif

  let col = col('.')

  " restore the cursor position.
  call cursor(curline, curcol)

  return col
endfunction " }}}

" GetCurrentElementPosition() {{{
" Gets the character offset and length for the element under the cursor.
function! eclim#util#GetCurrentElementPosition ()
  let offset = eclim#util#GetCurrentElementOffset()
  let word = expand('<cword>')

  return offset . ";" . strlen(word)
endfunction " }}}

" GetCurrentElementOffset() {{{
" Gets the character offset for the element under the cursor.
function! eclim#util#GetCurrentElementOffset ()
  let curline = line('.')
  let curcol = col('.')

  let line = getline('.')
  " cursor not on the word
  if line[col('.') - 1] =~ '\W'
    silent normal w

  " cursor not at the beginning of the word
  elseif line[col('.') - 2] =~ '\w'
    silent normal b
  endif

  let offset = eclim#util#GetCharacterOffset()

  " restore the cursor position.
  call cursor(curline, curcol)

  return offset
endfunction " }}}

" GetIndent(indent) {{{
" Gets an indentation string for the supplied number of spaces the indent
" consists of.  Ex. eclim#util#GetIndent(indent(line('.')))
function! eclim#util#GetIndent (indent)
  let result = ''

  if a:indent
    let num = a:indent / &sw
    while num >= 0
      let result .= g:EclimIndent
      let num -= 1
    endwhile
  endif

  return result
endfunction " }}}

" GetPathEntry(file) {{{
" Returns the path entry that contains the supplied file (excluding '.' and '').
" The argument must be an absolute path to the file.
" &path is expected to be using commas for path delineation.
" Returns 0 if no path found.
function! eclim#util#GetPathEntry (file)
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

" Glob(expr, [honor_wildignore]) {{{
" Used to issue a glob() handling any vim options that may otherwise disrupt
" it.
function! eclim#util#Glob (expr, ...)
  if len(a:000) == 0
    let savewig = &wildignore
    set wildignore=""
  endif

  let paths = split(expand(a:expr), '\n')
  if len(paths) == 1
    let result = glob(paths[0])
  else
    let result = join(paths, "\n")
  endif

  if len(a:000) == 0
    let &wildignore = savewig
  endif

  return result
endfunction " }}}

" Globpath(path, expr, [honor_wildignore]) {{{
" Used to issue a globpath() handling any vim options that may otherwise disrupt
" it.
function! eclim#util#Globpath (path, expr, ...)
  if len(a:000) == 0
    let savewig = &wildignore
    set wildignore=""
  endif

  let result = globpath(a:path, a:expr)

  if len(a:000) == 0
    let &wildignore = savewig
  endif

  return result
endfunction " }}}

" GoToBufferWindow(bufname) {{{
" Returns to the window containing the supplied buffer name.
function! eclim#util#GoToBufferWindow (bufname)
  let winnr = bufwinnr(bufnr(a:bufname))
  if winnr != -1
    exec winnr . "winc w"
  endif
endfunction " }}}

" GoToBufferWindowRegister(bufname) {{{
" Registers the autocmd for returning the user to the supplied buffer when the
" current buffer is closed.
function! eclim#util#GoToBufferWindowRegister (bufname)
  exec 'autocmd BufUnload <buffer> call eclim#util#GoToBufferWindow("' .
    \ escape(a:bufname, '\') . '")'
endfunction " }}}

" GrabUri() {{{
" Grabs an uri from the file's current cursor position.
function! eclim#util#GrabUri ()
  let line = getline('.')
  let uri = substitute(line,
    \ "\\(.*[[:space:]\"',(\\[{><]\\|^\\)\\(.*\\%" .
    \ col('.') . "c.\\{-}\\)\\([[:space:]\"',)\\]}<>].*\\|$\\)",
    \ '\2', '')

  return uri
endfunction " }}}

" ListContains(list, element) {{{
" Returns 1 if the supplied list contains the specified element, 0 otherwise.
" To determine element equality both '==' and 'is' are tried as well as
" ^element$ to support a regex supplied element string.
function! eclim#util#ListContains (list, element)
  let string = type(a:element) == 1 ? a:element : escape(string(a:element), '\')
  for element in a:list
    if element is a:element ||
        \ (type(element) == type(a:element) && element == a:element)
      return 1
    else
      let estring = type(element) == 1 ? element : string(element)
      if estring =~ '^' . string . '$'
        return 1
      endif
    endif
  endfor
  return 0
endfunction " }}}

" MakeWithCompiler(compiler, bang, args) {{{
" Executes :make using the supplied compiler.
function! eclim#util#MakeWithCompiler (compiler, bang, args)
  if exists('g:current_compiler')
    let saved_compiler = g:current_compiler
  endif
  if exists('b:current_compiler')
    let saved_compiler = b:current_compiler
  endif
  if !exists('saved_compiler')
    let saved_makeprg = &makeprg
    let saved_errorformat = &errorformat
  endif

  try
    unlet! g:current_compiler b:current_compiler
    exec 'compiler ' . a:compiler
    exec 'make' . a:bang . ' ' . a:args
  finally
    if exists('saved_compiler')
      unlet! g:current_compiler b:current_compiler
      exec 'compiler ' . saved_compiler
      unlet saved_compiler
    else
      let &makeprg = saved_makeprg
      let &errorformat = saved_errorformat
    endif
  endtry
endfunction " }}}

" MarkRestore(markLine) {{{
" Restores the ' mark with the new line.
function! eclim#util#MarkRestore (markLine)
  let line = line('.')
  let col = col('.')
  call cursor(a:markLine, s:markCol)
  mark '
  call cursor(line, col)
endfunction " }}}

" MarkSave() {{{
" Saves the ' mark and returns the line.
function! eclim#util#MarkSave ()
  let s:markCol = col("'`")
  return line("''")
endfunction " }}}

" ParseArgs(args) {{{
" Parses the supplied argument line into a list of args.
function! eclim#util#ParseArgs (args)
  let args = split(a:args, '[^\\]\s\zs')
  call map(args, 'substitute(v:val, "\\([^\\\\]\\)\\s\\+$", "\\1", "")')

  return args
endfunction " }}}

" ParseLocationEntries(entries) {{{
" Parses the supplied list of location entry lines (%f|%l col %c|%m) into a
" vim compatable list of dictionaries that can be passed to setqflist() or
" setloclist().
" In addition to the above line format, this function also supports
" %f|%l col %c|%m|%s, where %s is the type of the entry.  The value will
" be placed in the dictionary under the 'type' key.
function! eclim#util#ParseLocationEntries (entries)
  let entries = []

  for entry in a:entries
    let file = substitute(entry, '\(.\{-}\)|.*', '\1', '')
    let line = substitute(entry, '.*|\([0-9]\+\) col.*', '\1', '')
    let col = substitute(entry, '.*col \([0-9]\+\)|.*', '\1', '')
    let message = substitute(entry, '.*col [0-9]\+|\(.\{-}\)\(|.*\|$\)', '\1', '')
    let type = substitute(entry, '.*|\(e\|w\)$', '\1', '')

    let dict = {
      \ 'filename': eclim#util#Simplify(file),
      \ 'lnum': line,
      \ 'col': col,
      \ 'text': message,
      \ 'type': type}

    call add(entries, dict)
  endfor

  return entries
endfunction " }}}

" PromptList(prompt, list, highlight) {{{
" Creates a prompt for the user using the supplied prompt string and list of
" items to choose from.  Returns -1 if the list is empty or if the user
" canceled, and 0 if the list contains only one item.
function! eclim#util#PromptList (prompt, list, highlight)
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
    while response !~ '\(^$\|^[0-9]\+$\)' ||
        \ response < 0 ||
        \ response > (len(a:list) - 1)
      let response = input("You must choose a value between " .
        \ 0 . " and " . (len(a:list) - 1) . ". (Ctrl-C to cancel): ")
    endwhile
  finally
    echohl None
  endtry

  if response == ''
    return -1
  endif

  return response
endfunction " }}}

" RefreshFile() {{{
function! eclim#util#RefreshFile ()
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

" SetLocationList(list, ...) {{{
" Sets the contents of the location list for the current window.
function! eclim#util#SetLocationList (list, ...)
  if a:0 == 0
    call setloclist(0, a:list)
  else
    call setloclist(0, a:list, a:1)
  endif
  call eclim#signs#Update()
endfunction " }}}

" ShowCurrentError() {{{
" Shows the error on the cursor line if one.
function! eclim#util#ShowCurrentError ()
  let line = line('.')
  let col = col('.')

  let errornum = 0
  let errorcol = 0
  let index = 0

  let locerrors = getloclist(0)
  let qferrors = getqflist()
  for error in qferrors + locerrors
    let index += 1
    if bufname(error.bufnr) == expand("%") && error.lnum == line
      if errornum == 0 || (col >= error.col && error.col != errorcol)
        let errornum = index
        let errorcol = error.col
      endif
    endif
  endfor

  if errornum > 0
    let src = 'qf'
    let cnt = len(qferrors)
    let errors = qferrors
    if errornum > cnt
      let errornum -= cnt
      let src = 'loc'
      let cnt = len(locerrors)
      let errors = locerrors
    endif

    let message = src . ' - (' . errornum . ' of ' . cnt . '): '
      \ . errors[errornum - 1].text
    " remove any new lines
    let message = substitute(message, '\n', ' ', 'g')

    if len(message) > (&columns - 1)
      let message = strpart(message, 0, &columns - 4) . '...'
    endif

    call eclim#util#WideMessage('echo', message)
  endif
endfunction " }}}

" Simplify(file) {{{
" Simply the supplied file to the shortest valid name.
function! eclim#util#Simplify (file)
  let file = a:file

  " Don't run simplify on url files, it will screw them up.
  if file !~ '://'
    let file = simplify(file)
  endif

  " replace all '\' chars with '/' except those escaping spaces.
  let file = substitute(file, '\\\([^[:space:]]\)', '/\1', 'g')
  let cwd = substitute(getcwd(), '\', '/', 'g')
  if cwd !~ '/$'
    let cwd .= '/'
  endif

  if file =~ '^' . cwd
    let file = substitute(file, '^' . cwd, '', '')
  endif

  return file
endfunction " }}}

" System(cmd [, exec]) {{{
" Executes system() accounting for possibly disruptive vim options.
function! eclim#util#System (cmd, ...)
  " save
  if has("win32") || has("win64")
    let saveshell = &shell
    let saveshellcmdflag = &shellcmdflag
    let saveshellpipe = &shellpipe
    let saveshellquote = &shellquote
    let saveshellredir = &shellredir
    let saveshellslash = &shellslash
    let saveshelltemp = &shelltemp
    let saveshellxquote = &shellxquote

    set shell=cmd.exe
    set shellcmdflag=/c
    set shellpipe=>%s\ 2>&1
    set shellquote=
    set shellredir=>%s\ 2>&1
    set noshellslash
    set shelltemp
    set shellxquote=
  endif

  if len(a:000) > 0 && a:000[0]
    let result = ''
    exec a:cmd
  else
    let result = system(a:cmd)
  endif

  " restore
  if has("win32") || has("win64")
    let &shell = saveshell
    let &shellcmdflag = saveshellcmdflag
    let &shellpipe = saveshellpipe
    let &shellquote = saveshellquote
    let &shellredir = saveshellredir
    let &shellslash = saveshellslash
    let &shelltemp = saveshelltemp
    let &shellxquote = saveshellxquote
  endif

  return result
endfunction " }}}

" TempWindow(name, lines [, readonly]) {{{
" Opens a temp window w/ the given name and contents.
function! eclim#util#TempWindow (name, lines, ...)
  call eclim#util#TempWindowClear(a:name)

  if bufwinnr(a:name) == -1
    silent! exec "botright 10split " . a:name
    if len(a:000) == 0 || a:000[0]
      setlocal nowrap
      setlocal winfixheight
      setlocal noswapfile
      setlocal buftype=nofile
      setlocal bufhidden=delete
    endif
  else
    exec bufwinnr(a:name) . "winc w"
  endif

  call append(1, a:lines)
  retab
  let saved = @"
  silent 1,1delete
  let @" = saved

  if len(a:000) == 0 || a:000[0]
    setlocal nomodified
    setlocal nomodifiable
    setlocal readonly
  endif
endfunction " }}}

" TempWindowClear(name) {{{
" Opens a temp window w/ the given name and contents.
function! eclim#util#TempWindowClear (name)
  if bufwinnr(a:name) != -1
    let curwinnr = winnr()
    exec bufwinnr(a:name) . "winc w"
    setlocal modifiable
    setlocal noreadonly
    let saved = @"
    silent 1,$delete
    let @" = saved
    exec curwinnr . "winc w"
  endif
endfunction " }}}

" TempWindowCommand(command, name) {{{
" Opens a temp window w/ the given name and contents from the result of the
" supplied command.
function! eclim#util#TempWindowCommand (command, name)
  let filename = expand('%:p')
  let name = escape(a:name, ' ')

  let line = 1
  let col = 1
  " if the window is open, save the cursor position
  if bufwinnr(name) != -1
    exec bufwinnr(name) . "winc w"
    let line = line('.')
    let col = col('.')
  endif

  call eclim#util#TempWindowClear(name)

  let results = split(eclim#ExecuteEclim(a:command), '\n')
  if len(results) == 1 && results[0] == '0'
    return 0
  endif

  call eclim#util#TempWindow(name, results)

  call cursor(line, col)

  " Store filename so that plugins can use it if necessary.
  let b:filename = filename

  augroup temp_window
    autocmd! BufUnload <buffer>
    call eclim#util#GoToBufferWindowRegister(filename)
  augroup END

  return 1
endfunction " }}}

" WideMessage(command,message) {{{
" Executes the supplied echo command and forces vim to display as much as
" possible without the "Press Enter" prompt.
" Thanks to vimtip #1289
function! eclim#util#WideMessage (command, message)
  let saved_ruler = &ruler
  let saved_showcmd = &showcmd

  set noruler noshowcmd
  redraw
  exec a:command . ' "' . escape(a:message, '"') . '"'

  let &ruler = saved_ruler
  let &showcmd = saved_showcmd
endfunction " }}}

" WillWrittenBufferClose() {{{
" Returns 1 if the current buffer is to be hidden/closed/deleted after it is
" written, or 0 otherwise.  This function is useful during a post write auto
" command for determining whether or not to perform some operation based on
" whether the buffer will still be visible to the user once the current
" command has finished.
" Note: This function only detects command typed by the user at the
" command (:) prompt, not any normal mappings which may hide/close/delete the
" buffer.
function! eclim#util#WillWrittenBufferClose ()
  return histget("cmd") =~ s:buffing_write_closing_commands
endfunction " }}}

" CommandCompleteFile(argLead, cmdLine, cursorPos) {{{
" Custom command completion for files.
function! eclim#util#CommandCompleteFile (argLead, cmdLine, cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')
  let results = split(eclim#util#Glob(argLead . '*', 1), '\n')
  call map(results, 'isdirectory(v:val) ? v:val . "/" : v:val')
  call map(results, "substitute(v:val, '\\', '/', 'g')")
  call map(results, "substitute(v:val, ' ', '\\\\ ', 'g')")

  return eclim#util#ParseCommandCompletionResults(argLead, results)
endfunction " }}}

" CommandCompleteDir(argLead, cmdLine, cursorPos) {{{
" Custom command completion for directories.
function! eclim#util#CommandCompleteDir (argLead, cmdLine, cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')
  let results = split(eclim#util#Glob(argLead . '*', 1), '\n')
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
  return eclim#util#ParseCommandCompletionResults(argLead, results)
endfunction " }}}

" ParseCommandCompletionResults(args) {{{
" Bit of a hack for vim's lack of support for escaped spaces in custom
" completion.
function! eclim#util#ParseCommandCompletionResults (argLead, results)
  let results = a:results
  if stridx(a:argLead, ' ') != -1
    let removePrefix = escape(substitute(a:argLead, '\(.*\s\).*', '\1', ''), '\')
    call map(results, "substitute(v:val, '^" . removePrefix . "', '', '')")
  endif
  return results
endfunction " }}}

" vim:ft=vim:fdm=marker
