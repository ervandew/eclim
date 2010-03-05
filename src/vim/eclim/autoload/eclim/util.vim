" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Utility functions.
"
"   This plugin contains shared functions that can be used regardless of the
"   current file type being edited.
"
" License:
"
" Copyright (C) 2005 - 2010  Eric Van Dewoestine
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
  let s:buffer_write_closing_commands = '^\s*\(' .
    \ 'wq\|xa\|' .
    \ '\d*w[nN]\|\d*wp\|' .
    \ 'ZZ' .
    \ '\)'

  let s:bourne_shells = ['sh', 'bash', 'dash', 'ksh', 'zsh']
  let s:c_shells = ['csh', 'tcsh']

  let s:show_current_error_displaying = 0
" }}}

" Balloon(message) {{{
" Function for use as a vim balloonexpr expression.
function! eclim#util#Balloon(message)
  let message = a:message
  if !has('balloon_multiline')
    " remove any new lines
    let message = substitute(message, '\n', ' ', 'g')
  endif
  return message
endfunction " }}}

" DelayedCommand(command, [delay]) {{{
" Executes a delayed command.  Useful in cases where one would expect an
" autocommand event (WinEnter, etc) to fire, but doesn't, or you need a
" command to execute after other autocommands have finished.
" Note: Nesting is not supported.  A delayed command cannot be invoke off
" another delayed command.
function! eclim#util#DelayedCommand(command, ...)
  let uid = fnamemodify(tempname(), ':t:r')
  if &updatetime > 1
    exec 'let g:eclim_updatetime_save' . uid . ' = &updatetime'
  endif
  exec 'let g:eclim_delayed_command' . uid . ' = a:command'
  let &updatetime = len(a:000) ? a:000[0] : 1
  exec 'augroup delayed_command' . uid
    exec 'autocmd CursorHold * ' .
      \ '  if exists("g:eclim_updatetime_save' . uid . '") | ' .
      \ '    let &updatetime = g:eclim_updatetime_save' . uid . ' | ' .
      \ '    unlet g:eclim_updatetime_save' . uid . ' | ' .
      \ '  endif | ' .
      \ '  exec g:eclim_delayed_command' . uid . ' | ' .
      \ '  unlet g:eclim_delayed_command' . uid . ' | ' .
      \ '  autocmd! delayed_command' . uid
  exec 'augroup END'
endfunction " }}}

" EchoTrace(message, [time_elapsed]) {{{
function! eclim#util#EchoTrace(message, ...)
  if a:0 > 0
    call s:EchoLevel('(' . a:1 . 's) ' . a:message, 6, g:EclimTraceHighlight)
  else
    call s:EchoLevel(a:message, 6, g:EclimTraceHighlight)
  endif
endfunction " }}}

" EchoDebug(message) {{{
function! eclim#util#EchoDebug(message)
  call s:EchoLevel(a:message, 5, g:EclimDebugHighlight)
endfunction " }}}

" EchoInfo(message) {{{
function! eclim#util#EchoInfo(message)
  call s:EchoLevel(a:message, 4, g:EclimInfoHighlight)
endfunction " }}}

" EchoWarning(message) {{{
function! eclim#util#EchoWarning(message)
  call s:EchoLevel(a:message, 3, g:EclimWarningHighlight)
endfunction " }}}

" EchoError(message) {{{
function! eclim#util#EchoError(message)
  call s:EchoLevel(a:message, 2, g:EclimErrorHighlight)
endfunction " }}}

" EchoFatal(message) {{{
function! eclim#util#EchoFatal(message)
  call s:EchoLevel(a:message, 1, g:EclimFatalHighlight)
endfunction " }}}

" s:EchoLevel(message) {{{
" Echos the supplied message at the supplied level with the specified
" highlight.
function! s:EchoLevel(message, level, highlight)
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
" Echos a message using the info highlight regardless of what log level is set.
function! eclim#util#Echo(message)
  if a:message != "0" && g:EclimLogLevel > 0
    exec "echohl " . g:EclimInfoHighlight
    redraw
    for line in split(a:message, '\n')
      echom line
    endfor
    echohl None
  endif
endfunction " }}}

" EscapeBufferName(name) {{{
" Escapes the supplied buffer name so that it can be safely used by buf*
" functions.
function! eclim#util#EscapeBufferName(name)
  let name = a:name
  " escaping the space in cygwin could lead to the dos path error message that
  " cygwin throws when a dos path is referenced.
  if !has('win32unix')
    let name = escape(a:name, ' ')
  endif
  return substitute(name, '\(.\{-}\)\[\(.\{-}\)\]\(.\{-}\)', '\1[[]\2[]]\3', 'g')
endfunction " }}}

" Exec(cmd) {{{
" Used when executing ! commands that may be disrupted by non default vim
" options.
function! eclim#util#Exec(cmd)
  call eclim#util#System(a:cmd, 1)
endfunction " }}}

" ExecWithoutAutocmds(cmd, [events]) {{{
" Execute a command after disabling all autocommands (borrowed from taglist.vim)
function! eclim#util#ExecWithoutAutocmds(cmd, ...)
  let save_opt = &eventignore
  let events = len(a:000) == 0 ? 'all' : a:000[0]
  exec 'set eventignore=' . events
  try
    exec a:cmd
  finally
    let &eventignore = save_opt
  endtry
endfunction " }}}

" FindFileInPath(file, exclude_relative) {{{
" Searches for the supplied file in the &path.
" If exclude_relative supplied is 1, then relative &path entries ('.' and '')
" are not searched).
function! eclim#util#FindFileInPath(file, exclude_relative)
  let path = &path
  if a:exclude_relative
    " remove '' path entry
    let path = substitute(path, '[,]\?[,]\?', '', 'g')
    " remove '.' path entry
    let path = substitute(path, '[,]\?\.[,]\?', '', 'g')
  endif
  return split(eclim#util#Globpath(path, "**/" . a:file), '\n')
endfunction " }}}

" Findfile(name, [path, count]) {{{
" Used to issue a findfile() handling any vim options that may otherwise
" disrupt it.
function! eclim#util#Findfile(name, ...)
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

" GetEncoding() {{{
" Gets the encoding of the current file.
function! eclim#util#GetEncoding()
  let encoding = &fileencoding
  if encoding == ''
    let encoding = &encoding
  endif

  " handle vim's compiled without multi-byte support
  if encoding == ''
    let encoding = 'utf-8'
  endif

  return encoding
endfunction " }}}

" GetOffset() {{{
" Gets the byte offset for the current cursor position.
function! eclim#util#GetOffset()
  let offset = line2byte(line('.')) - 1
  let offset += col('.') - 1
  return offset
endfunction " }}}

" GetCurrentElementColumn() {{{
" Gets the column for the element under the cursor.
function! eclim#util#GetCurrentElementColumn()
  let pos = getpos('.')

  let line = getline('.')
  " cursor not on the word
  if line[col('.') - 1] =~ '\W'
    silent normal! w

  " cursor not at the beginning of the word
  elseif line[col('.') - 2] =~ '\w'
    silent normal! b
  endif

  let col = col('.')

  " restore the cursor position.
  call setpos('.', pos)

  return col
endfunction " }}}

" GetCurrentElementPosition() {{{
" Gets the byte offset and length for the element under the cursor.
function! eclim#util#GetCurrentElementPosition()
  let offset = eclim#util#GetCurrentElementOffset()
  let word = expand('<cword>')

  return offset . ";" . strlen(word)
endfunction " }}}

" GetCurrentElementOffset() {{{
" Gets the byte offset for the element under the cursor.
function! eclim#util#GetCurrentElementOffset()
  let pos = getpos('.')

  let line = getline('.')
  " cursor not on the word
  if line[col('.') - 1] =~ '\W'
    silent normal! w

  " cursor not at the beginning of the word
  elseif line[col('.') - 2] =~ '\w'
    silent normal! b
  endif

  let offset = eclim#util#GetOffset()

  " restore the cursor position.
  call setpos('.', pos)

  return offset
endfunction " }}}

" GetIndent(level) {{{
" Gets an indentation string for the supplied indentation level.
function! eclim#util#GetIndent(level)
  let result = ''

  if a:level
    if !exists('b:eclim_indent')
      if exists('g:EclimIndent')
        let b:eclim_indent = g:EclimIndent
      else
        if !&expandtab
          let b:eclim_indent = "\t"
        else
          let b:eclim_indent = ''
          let index = 0
          while index < &shiftwidth
            let b:eclim_indent = b:eclim_indent . " "
            let index = index + 1
          endwhile
        endif
      endif
    endif

    let num = a:level
    while num > 0
      let result .= b:eclim_indent
      let num -= 1
    endwhile
  endif

  return result
endfunction " }}}

" GetLineError(line) {{{
" Gets the error (or message) for the supplie line number if one.
function! eclim#util#GetLineError(line)
  let line = line('.')
  let col = col('.')

  let errornum = 0
  let errorcol = 0
  let index = 0

  let locerrors = getloclist(0)
  let qferrors = getqflist()
  let bufname = expand('%')
  let lastline = line('$')
  for error in qferrors + locerrors
    let index += 1
    if bufname(error.bufnr) == bufname &&
        \ (error.lnum == line || (error.lnum > lastline && line == lastline))
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
      \ . substitute(errors[errornum - 1].text, '^\s\+', '', '')
    return message
  endif
  return ''
endfunction " }}}

" GetPathEntry(file) {{{
" Returns the path entry that contains the supplied file (excluding '.' and '').
" The argument must be an absolute path to the file.
" &path is expected to be using commas for path delineation.
" Returns 0 if no path found.
function! eclim#util#GetPathEntry(file)
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

" GetVisualSelection(line1, line2, default) {{{
" Returns the contents of, and then clears, the last visual selection.
" If default is set, the default range will be honor.
function! eclim#util#GetVisualSelection(line1, line2, default)
  let lines = a:default ? getline(a:line1, a:line2) : []
  let mode = visualmode(1)
  if mode != '' && line("'<") == a:line1
    if len(lines) == 0
      let lines = getline(a:line1, a:line2)
    endif
    if mode == "v"
      let start = col("'<") - 1
      let end = col("'>") - 1
      " slice in end before start in case the selection is only one line
      let lines[-1] = lines[-1][: end]
      let lines[0] = lines[0][start :]
    elseif mode == "\<c-v>"
      let start = col("'<")
      if col("'>") < start
        let start = col("'>")
      endif
      let start = start - 1
      call map(lines, 'v:val[start :]')
    endif
  endif
  return join(lines, "\n")
endfunction " }}}

" Glob(expr, [honor_wildignore]) {{{
" Used to issue a glob() handling any vim options that may otherwise disrupt
" it.
function! eclim#util#Glob(expr, ...)
  if len(a:000) == 0
    let savewig = &wildignore
    set wildignore=""
  endif

  let paths = split(a:expr, '\n')
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
function! eclim#util#Globpath(path, expr, ...)
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

" GoToBufferWindow(buf) {{{
" Focuses the window containing the supplied buffer name or buffer number.
" Returns 1 if the window was found, 0 otherwise.
function! eclim#util#GoToBufferWindow(buf)
  if type(a:buf) == 0
    let winnr = bufwinnr(a:buf)
  else
    let name = eclim#util#EscapeBufferName(a:buf)
    let winnr = bufwinnr(bufnr('^' . name))
  endif
  if winnr != -1
    exec winnr . "winc w"
    call eclim#util#DelayedCommand('doautocmd WinEnter')
    return 1
  endif
  return 0
endfunction " }}}

" GoToBufferWindowOrOpen(name, cmd) {{{
" Gives focus to the window containing the buffer for the supplied file, or if
" none, opens the file using the supplied command.
function! eclim#util#GoToBufferWindowOrOpen(name, cmd)
  let name = eclim#util#EscapeBufferName(a:name)
  let winnr = bufwinnr(bufnr('^' . name))
  if winnr != -1
    exec winnr . "winc w"
    call eclim#util#DelayedCommand('doautocmd WinEnter')
  else
    let cmd = a:cmd
    " if splitting and the buffer is a unamed empty buffer, then switch to an
    " edit.
    if cmd == 'split' && expand('%') == '' &&
     \ !&modified && line('$') == 1 && getline(1) == ''
      let cmd = 'edit'
    endif
    silent exec cmd . ' ' . escape(eclim#util#Simplify(a:name), ' ')
  endif
endfunction " }}}

" GoToBufferWindowRegister(buf) {{{
" Registers the autocmd for returning the user to the supplied buffer when the
" current buffer is closed.
function! eclim#util#GoToBufferWindowRegister(buf)
  exec 'autocmd BufWinLeave <buffer> ' .
    \ 'call eclim#util#GoToBufferWindow("' . escape(a:buf, '\') . '") | ' .
    \ 'doautocmd BufEnter'
endfunction " }}}

" GrabUri([line, col]) {{{
" Grabs an uri from the file's current cursor position.
function! eclim#util#GrabUri(...)
  if len(a:000) == 2
    let lnum = a:000[0]
    let cnum = a:000[1]
  else
    let lnum = line('.')
    let cnum = col('.')
  endif
  let line = getline(lnum)
  let uri = substitute(line,
    \ "\\(.*[[:space:]\"',(\\[{><]\\|^\\)\\(.*\\%" .
    \ cnum . "c.\\{-}\\)\\([[:space:]\"',)\\]}<>].*\\|$\\)",
    \ '\2', '')

  return uri
endfunction " }}}

" ListContains(list, element) {{{
" Returns 1 if the supplied list contains the specified element, 0 otherwise.
" To determine element equality both '==' and 'is' are tried as well as
" ^element$ to support a regex supplied element string.
function! eclim#util#ListContains(list, element)
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
" Note: on windows the make program will be executed manually if the 'tee'
" progam is available (only the cygwin version is currenty supported) to allow
" the display of the make program output while running.
function! eclim#util#MakeWithCompiler(compiler, bang, args, ...)
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
  if has('win32') || has('win64')
    let saved_shellpipe = &shellpipe
    set shellpipe=>\ %s\ 2<&1
  endif

  try
    unlet! g:current_compiler b:current_compiler
    exec 'compiler ' . a:compiler
    let make_cmd = substitute(&makeprg, '\$\*', a:args, '')

    " windows machines where 'tee' is available
    if (has('win32') || has('win64')) && executable('tee')
      let outfile = g:EclimTempDir . '/eclim_make_output.txt'
      let teefile = eclim#cygwin#CygwinPath(outfile)
      let command = '!cmd /c "' . make_cmd . ' 2>&1 | tee "' . teefile . '" "'

      doautocmd QuickFixCmdPre make
      call eclim#util#Exec(command)
      if filereadable(outfile)
        if a:bang == ''
          exec 'cfile ' . escape(outfile, ' ')
        else
          exec 'cgetfile ' . escape(outfile, ' ')
        endif
        call delete(outfile)
      endif
      doautocmd QuickFixCmdPost make

    " all other platforms
    else
      call eclim#util#EchoTrace('make: ' . make_cmd)
      exec 'make' . a:bang . ' ' . a:args
    endif
  finally
    if exists('saved_compiler')
      unlet! g:current_compiler b:current_compiler
      exec 'compiler ' . saved_compiler
      unlet saved_compiler
    else
      let &makeprg = saved_makeprg
      let &errorformat = saved_errorformat
    endif
    if has('win32') || has('win64')
      let &shellpipe = saved_shellpipe
    endif
  endtry
endfunction " }}}

" MarkRestore(markLine) {{{
" Restores the ' mark with the new line.
function! eclim#util#MarkRestore(markLine)
  let pos = getpos('.')
  call cursor(a:markLine, s:markCol)
  mark '
  call setpos('.', pos)
endfunction " }}}

" MarkSave() {{{
" Saves the ' mark and returns the line.
function! eclim#util#MarkSave()
  let s:markCol = col("'`")
  return line("''")
endfunction " }}}

" ParseArgs(args) {{{
" Parses the supplied argument line into a list of args, handling quoted
" strings, escaped spaces, etc.
function! eclim#util#ParseArgs(args)
  let args = []
  let arg = ''
  let quote = ''
  let escape = 0
  let index = 0
  while index < len(a:args)
    let char = a:args[index]
    let index += 1
    if char == ' ' && quote == '' && !escape
      if arg != ''
        call add(args, arg)
        let arg = ''
      endif
    elseif char == '\'
      if escape
        let arg .= char
      endif
      let escape = !escape
    elseif char == '"' || char == "'"
      if !escape
        if quote != '' && char == quote
          let quote = ''
        elseif quote == ''
          let quote = char
        else
          let arg .= char
        endif
      else
        let arg .= char
        let escape = 0
      endif
    else
      let arg .= char
      let escape = 0
    endif
  endwhile

  if arg != ''
    call add(args, arg)
  endif

  return args
endfunction " }}}

" ParseLocationEntries(entries, [sort]) {{{
" Parses the supplied list of location entry lines (%f|%l col %c|%m) into a
" vim compatable list of dictionaries that can be passed to setqflist() or
" setloclist().
" In addition to the above line format, this function also supports
" %f|%l col %c|%m|%s, where %s is the type of the entry.  The value will
" be placed in the dictionary under the 'type' key.
" The optional 'sort' parameter currently only supports 'severity' as an
" argument.
function! eclim#util#ParseLocationEntries(entries, ...)
  if len(a:000) > 0 && a:1 == 'severity'
    let entries = {}
  else
    let entries = []
  endif

  for entry in a:entries
    let dict = s:ParseLocationEntry(entry)

    " partition by severity
    if type(entries) == 4 " dictionary
      " empty key not allowed
      let type = dict.type == '' ? ' ' : tolower(dict.type)
      if !has_key(entries, type)
        let entries[type] = []
      endif
      call add(entries[type], dict)

    " default sort
    else
      call add(entries, dict)
    endif
  endfor

  " re-assemble severity partitioned results
  if type(entries) == 4 " dictionary
    let results = []
    if has_key(entries, 'e')
      let results += remove(entries, 'e')
    endif
    if has_key(entries, 'w')
      let results += remove(entries, 'w')
    endif
    if has_key(entries, 'i')
      let results += remove(entries, 'i')
    endif
    " should only be key '' (no type), but we don't want to accidentally
    " filter out other possible types.
    let keys = keys(entries)
    call reverse(sort(keys))
    for key in keys
      let results += entries[key]
    endfor
    return results
  endif

  return entries
endfunction " }}}

" s:ParseLocationEntry(entry) {{{
function! s:ParseLocationEntry(entry)
  let entry = a:entry
  let file = substitute(entry, '\(.\{-}\)|.*', '\1', '')
  let line = substitute(entry, '.*|\([0-9]\+\) col.*', '\1', '')
  let col = substitute(entry, '.*col \([0-9]\+\)|.*', '\1', '')
  let message = substitute(entry, '.*col [0-9]\+|\(.\{-}\)\(|.*\|$\)', '\1', '')
  let type = substitute(entry, '.*|\(e\|w\)$', '\1', '')
  if type == entry
    let type = ''
  endif

  if has('win32unix')
    let file = eclim#cygwin#CygwinPath(file)
  endif

  let dict = {
      \ 'filename': eclim#util#Simplify(file),
      \ 'lnum': line,
      \ 'col': col,
      \ 'text': message,
      \ 'type': type
    \ }

  return dict
endfunction " }}}

" PromptList(prompt, list, highlight) {{{
" Creates a prompt for the user using the supplied prompt string and list of
" items to choose from.  Returns -1 if the list is empty or if the user
" canceled, and 0 if the list contains only one item.
function! eclim#util#PromptList(prompt, list, highlight)
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

" PromptConfirm(prompt, highlight) {{{
" Creates a yes/no prompt for the user using the supplied prompt string.
" Returns -1 if the user canceled, otherwise 1 for yes, and 0 for no.
function! eclim#util#PromptConfirm(prompt, highlight)
  exec "echohl " . a:highlight
  try
    " clear any previous messages
    redraw
    echo a:prompt . "\n"
    let response = input("(y/n): ")
    while response != '' && response !~ '^\c\s*\(y\(es\)\?\|no\?\|\)\s*$'
      let response = input("You must choose either y or n. (Ctrl-C to cancel): ")
    endwhile
  finally
    echohl None
  endtry

  if response == ''
    return -1
  endif

  return response =~ '\c\s*\(y\(es\)\?\)\s*'
endfunction " }}}

" RefreshFile() {{{
function! eclim#util#RefreshFile()
  "FIXME: doing an :edit clears the undo tree, but the code commented out below
  "       causes a user prompt on the write.  Need to pose this senario on the
  "       vim mailing lists.
  edit!
  "autocmd FileChangedShell nested <buffer> echom " ### file changed ### "
  "checktime
  "autocmd! FileChangedShell <buffer>

  "1,$delete _
  "silent exec "read " . expand('%:p')
  "1delete _

  silent write!
endfunction " }}}

" SetLocationList(list, [action]) {{{
" Sets the contents of the location list for the current window.
function! eclim#util#SetLocationList(list, ...)
  let loclist = a:list

  " filter the list if the current buffer defines a list of filters.
  if exists('b:EclimLocationListFilter')
    let newlist = []
    for item in loclist
      let addit = 1

      for filter in b:EclimLocationListFilter
        if item.text =~ filter
          let addit = 0
          break
        endif
      endfor

      if addit
        call add(newlist, item)
      endif
    endfor
    let loclist = newlist
  endif

  if a:0 == 0
    call setloclist(0, loclist)
  else
    call setloclist(0, loclist, a:1)
  endif
  if g:EclimShowCurrentError && len(loclist) > 0
    call eclim#util#DelayedCommand('call eclim#util#ShowCurrentError()')
  endif
  call eclim#display#signs#Update()
endfunction " }}}

" ClearLocationList([namespace, namespace, ...]) {{{
" Clears the current location list.  Optionally 'namespace' arguments can be
" supplied which will only clear items with text prefixed with '[namespace]'.
" Also the special namespace 'global' may be supplied which will only remove
" items with no namepace prefix.
function! eclim#util#ClearLocationList(...)
  if a:0 > 0
    let loclist = getloclist(0)
    if len(loclist) > 0
      let pattern = ''
      for ns in a:000
        if pattern != ''
          let pattern .= '\|'
        endif
        if ns == 'global'
          let pattern .= '\(\[\w\+\]\)\@!'
        else
          let pattern .= '\[' . ns . '\]'
        endif
      endfor
      let pattern = '^\(' . pattern . '\)'

      call filter(loclist, 'v:val.text !~ pattern')
      call setloclist(0, loclist, 'r')
    endif
  else
    call setloclist(0, [], 'r')
  endif
  call eclim#display#signs#Update()
endfunction " }}}

" SetQuickfixList(list, [action]) {{{
" Sets the contents of the quickfix list.
function! eclim#util#SetQuickfixList(list, ...)
  let qflist = a:list
  if exists('b:EclimQuickfixFilter')
    let newlist = []
    for item in qflist
      let addit = 1

      for filter in b:EclimQuickfixFilter
        if item.text =~ filter
          let addit = 0
          break
        endif
      endfor

      if addit
        call add(newlist, item)
      endif
    endfor
    let qflist = newlist
  endif
  if a:0 == 0
    call setqflist(qflist)
  else
    call setqflist(qflist, a:1)
  endif
  if g:EclimShowCurrentError && len(qflist) > 0
    call eclim#util#DelayedCommand('call eclim#util#ShowCurrentError()')
  endif
  call eclim#display#signs#Update()
endfunction " }}}

" ShowCurrentError() {{{
" Shows the error on the cursor line if one.
function! eclim#util#ShowCurrentError()
  let message = eclim#util#GetLineError(line('.'))
  if message != ''
    " remove any new lines
    let message = substitute(message, '\n', ' ', 'g')

    if len(message) > (&columns - 1)
      let message = strpart(message, 0, &columns - 4) . '...'
    endif

    call eclim#util#WideMessage('echo', message)
    let s:show_current_error_displaying = 1
  else
    " clear the message if one of our error messages was displaying
    if s:show_current_error_displaying
      call eclim#util#WideMessage('echo', message)
      let s:show_current_error_displaying = 0
    endif
  endif
endfunction " }}}

" Simplify(file) {{{
" Simply the supplied file to the shortest valid name.
function! eclim#util#Simplify(file)
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

" System(cmd, [exec]) {{{
" Executes system() accounting for possibly disruptive vim options.
function! eclim#util#System(cmd, ...)
  let saveshell = &shell
  let saveshellcmdflag = &shellcmdflag
  let saveshellpipe = &shellpipe
  let saveshellquote = &shellquote
  let saveshellredir = &shellredir
  let saveshellslash = &shellslash
  let saveshelltemp = &shelltemp
  let saveshellxquote = &shellxquote

  if has("win32") || has("win64")
    set shell=cmd.exe
    set shellcmdflag=/c
    set shellpipe=>%s\ 2>&1
    set shellquote=
    set shellredir=>%s\ 2>&1
    set noshellslash
    set shelltemp
    set shellxquote=
  else
    if executable('/bin/bash')
      set shell=/bin/bash
    else
      set shell=/bin/sh
    endif
    set shell=/bin/sh
    set shellcmdflag=-c
    set shellpipe=2>&1\|\ tee
    set shellquote=
    set shellredir=>%s\ 2>&1
    set noshellslash
    set shelltemp
    set shellxquote=
  endif

  if len(a:000) > 0 && a:000[0]
    let result = ''
    let begin = localtime()
    try
      exec a:cmd
    finally
      call eclim#util#EchoTrace('exec: ' . a:cmd, localtime() - begin)
    endtry
  else
    let begin = localtime()
    try
      let result = system(a:cmd)
    finally
      call eclim#util#EchoTrace('system: ' . a:cmd, localtime() - begin)
    endtry
  endif

  let &shell = saveshell
  let &shellcmdflag = saveshellcmdflag
  let &shellquote = saveshellquote
  let &shellslash = saveshellslash
  let &shelltemp = saveshelltemp
  let &shellxquote = saveshellxquote

  " If a System call is executed at startup, it appears to interfere with
  " vim's setting of 'shellpipe' and 'shellredir' to their shell specific
  " values.  So, if we detect that the values we are restoring look like
  " uninitialized defaults, then attempt to mimic vim's documented
  " (:h 'shellpipe' :h 'shellredir') logic for setting the proper values based
  " on the shell.
  " Note: still doesn't handle more obscure shells
  if saveshellredir == '>'
    if index(s:bourne_shells, fnamemodify(&shell, ':t')) != -1
      set shellpipe=2>&1\|\ tee
      set shellredir=>%s\ 2>&1
    elseif index(s:c_shells, fnamemodify(&shell, ':t')) != -1
      set shellpipe=\|&\ tee
      set shellredir=>&
    else
      let &shellpipe = saveshellpipe
      let &shellredir = saveshellredir
    endif
  else
    let &shellpipe = saveshellpipe
    let &shellredir = saveshellredir
  endif

  return result
endfunction " }}}

" TempWindow(name, lines, [readonly]) {{{
" Opens a temp window w/ the given name and contents which is readonly unless
" specified otherwise.
function! eclim#util#TempWindow(name, lines, ...)
  let filename = expand('%:p')
  let winnr = winnr()

  call eclim#util#TempWindowClear(a:name)
  let name = eclim#util#EscapeBufferName(a:name)

  if bufwinnr(name) == -1
    silent! noautocmd exec "botright 10sview " . escape(a:name, ' ')
    let b:eclim_temp_window = 1

    " play nice with maximize.vim
    if eclim#display#maximize#GetMaximizedWindow()
      call eclim#display#maximize#AdjustFixedWindow(10, 1)
    endif

    setlocal nowrap
    setlocal winfixheight
    setlocal noswapfile
    setlocal nobuflisted
    setlocal buftype=nofile
    setlocal bufhidden=delete
  else
    exec bufwinnr(name) . "winc w"
  endif

  setlocal modifiable
  setlocal noreadonly
  call append(1, a:lines)
  retab
  silent 1,1delete _

  if len(a:000) == 0 || a:000[0]
    setlocal nomodified
    setlocal nomodifiable
    setlocal readonly
  endif

  silent doautocmd BufEnter

  " Store filename and window number so that plugins can use it if necessary.
  if filename != expand('%:p')
    let b:filename = filename
    let b:winnr = winnr

    augroup eclim_temp_window
      autocmd! BufWinLeave <buffer>
      call eclim#util#GoToBufferWindowRegister(b:filename)
    augroup END
  endif
endfunction " }}}

" TempWindowClear(name) {{{
" Clears the contents of the temp window with the given name.
function! eclim#util#TempWindowClear(name)
  let name = eclim#util#EscapeBufferName(a:name)
  if bufwinnr(name) != -1
    let curwinnr = winnr()
    exec bufwinnr(name) . "winc w"
    setlocal modifiable
    setlocal noreadonly
    silent 1,$delete _
    exec curwinnr . "winc w"
  endif
endfunction " }}}

" TempWindowCommand(command, name, [port]) {{{
" Opens a temp window w/ the given name and contents from the result of the
" supplied command.
function! eclim#util#TempWindowCommand(command, name, ...)
  let name = eclim#util#EscapeBufferName(a:name)

  let line = 1
  let col = 1
  " if the window is open, save the cursor position
  if bufwinnr(name) != -1
    exec bufwinnr(name) . "winc w"
    let line = line('.')
    let col = col('.')
  endif

  if len(a:000) > 0
    let port = a:000[0]
    let result = eclim#ExecuteEclim(a:command, port)
  else
    let result = eclim#ExecuteEclim(a:command)
  endif

  let results = split(result, '\n')
  if len(results) == 1 && results[0] == '0'
    return 0
  endif

  call eclim#util#TempWindow(name, results)

  call cursor(line, col)

  return 1
endfunction " }}}

" WideMessage(command, message) {{{
" Executes the supplied echo command and forces vim to display as much as
" possible without the "Press Enter" prompt.
" Thanks to vimtip #1289
function! eclim#util#WideMessage(command, message)
  let saved_ruler = &ruler
  let saved_showcmd = &showcmd

  let message = substitute(a:message, '^\s\+', '', '')

  set noruler noshowcmd
  redraw
  exec a:command . ' "' . escape(message, '"\') . '"'

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
function! eclim#util#WillWrittenBufferClose()
  return histget("cmd") =~ s:buffer_write_closing_commands
endfunction " }}}

" CommandCompleteFile(argLead, cmdLine, cursorPos) {{{
" Custom command completion for files.
function! eclim#util#CommandCompleteFile(argLead, cmdLine, cursorPos)
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
function! eclim#util#CommandCompleteDir(argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]
  let results = split(eclim#util#Glob(expand(argLead) . '*', 1), '\n')
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

" ParseCmdLine(args) {{{
" Parses the supplied argument line into a list of args.
function! eclim#util#ParseCmdLine(args)
  let args = split(a:args, '[^\\]\s\zs')
  call map(args, 'substitute(v:val, "\\([^\\\\]\\)\\s\\+$", "\\1", "")')

  return args
endfunction " }}}

" ParseCommandCompletionResults(args) {{{
" Bit of a hack for vim's lack of support for escaped spaces in custom
" completion.
function! eclim#util#ParseCommandCompletionResults(argLead, results)
  let results = a:results
  if stridx(a:argLead, ' ') != -1
    let removePrefix = escape(substitute(a:argLead, '\(.*\s\).*', '\1', ''), '\')
    call map(results, "substitute(v:val, '^" . removePrefix . "', '', '')")
  endif
  return results
endfunction " }}}

" vim:ft=vim:fdm=marker
