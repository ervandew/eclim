" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2005 - 2014  Eric Van Dewoestine
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

  let s:command_setting = '-command setting -s <setting>'

  let s:log_levels = {
      \ 'trace': 5,
      \ 'debug': 4,
      \ 'info': 3,
      \ 'warning': 2,
      \ 'error': 1,
      \ 'off': 0,
    \ }
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

" CompilerExists(compiler) {{{
" Check whether a particular vim compiler is available.
function! eclim#util#CompilerExists(compiler)
  if !exists('s:compilers')
    redir => compilers
    silent compiler
    redir END
    let s:compilers = split(compilers, '\n')
    call map(s:compilers, 'fnamemodify(v:val, ":t:r")')
  endif
  return index(s:compilers, a:compiler) != -1
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

function! eclim#util#EchoTrace(message, ...) " {{{
  " Optional args:
  "   time_elapsed
  let message = a:message
  if a:0 > 0
    let message = '(' . a:1 . 's) ' . message
  endif
  call s:EchoLevel(message, 'trace', g:EclimHighlightTrace)
endfunction " }}}

function! eclim#util#EchoDebug(message) " {{{
  call s:EchoLevel(a:message, 'debug', g:EclimHighlightDebug)
endfunction " }}}

function! eclim#util#EchoInfo(message) " {{{
  call s:EchoLevel(a:message, 'info', g:EclimHighlightInfo)
endfunction " }}}

function! eclim#util#EchoWarning(message) " {{{
  call s:EchoLevel(a:message, 'warning', g:EclimHighlightWarning)
endfunction " }}}

function! eclim#util#EchoError(message) " {{{
  call s:EchoLevel(a:message, 'error', g:EclimHighlightError)
endfunction " }}}

function! s:EchoLevel(message, level, highlight) " {{{
  " Echos the supplied message at the supplied level with the specified
  " highlight.

  " don't echo if the message is 0, which signals an eclim#Execute failure.
  if type(a:message) == g:NUMBER_TYPE && a:message == 0
    return
  endif

  if s:log_levels[g:EclimLogLevel] < s:log_levels[a:level]
    return
  endif

  if type(a:message) == g:LIST_TYPE
    let messages = a:message
  else
    let messages = split(a:message, '\n')
  endif

  exec "echohl " . a:highlight
  redraw
  if mode() == 'n' || mode() == 'c' || s:log_levels[a:level] > s:log_levels['info']
    " Note: in command mode, the message won't display, but the user can view
    " it using :messages
    for line in messages
      echom line
    endfor
  else
    " if we aren't in normal mode then use regular 'echo' since echom
    " messages won't be displayed while the current mode is displayed in
    " vim's command line (but still use echom above for debug/verbose messages
    " so the user can get at them with :messages).
    echo join(messages, "\n") . "\n"
  endif
  echohl None
endfunction " }}}

function! eclim#util#Echo(message) " {{{
  " Echos a message using the info highlight regardless of what log level is set.
  if a:message != "0"
    exec "echohl " . g:EclimHighlightInfo
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

" Exec(cmd [,output]) {{{
" Used when executing ! commands that may be disrupted by non default vim
" options.
function! eclim#util#Exec(cmd, ...)
  let exec_output = len(a:000) > 0 ? a:000[0] : 0
  return eclim#util#System(a:cmd, 1, exec_output)
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

" GetOffset([line, col]) {{{
" Gets the byte offset for the current cursor position or supplied line, col.
function! eclim#util#GetOffset(...)
  let lnum = a:0 > 0 ? a:000[0] : line('.')
  let cnum = a:0 > 1 ? a:000[1] : col('.')
  let offset = 0

  " handle case where display encoding differs from the underlying file
  " encoding
  if &fileencoding != '' && &encoding != '' && &fileencoding != &encoding
    let prev = lnum - 1
    if prev > 0
      let lineEnding = &ff == 'dos' ? "\r\n" : "\n"
      " convert each line to the file encoding and sum their lengths
      let offset = eval(
        \ join(
        \   map(
        \     range(1, prev),
        \     'len(iconv(getline(v:val), &encoding, &fenc) . "' . lineEnding . '")'),
        \   '+'))
    endif

  " normal case
  else
    let offset = line2byte(lnum) - 1
  endif

  let offset += cnum - 1
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

" GetSetting(setting, [workspace]) {{{
" Gets a global setting from eclim.  Returns '' if the setting does not
" exist, 0 if an error occurs communicating with the server.
function! eclim#util#GetSetting(setting, ...)
  let command = s:command_setting
  let command = substitute(command, '<setting>', a:setting, '')

  let workspace = a:0 > 0 ? a:1 : ''
  let result = eclim#Execute(command, {'workspace': workspace})
  if result == '0'
    return result
  endif

  if result == ''
    call eclim#util#EchoWarning("Setting '" . a:setting . "' does not exist.")
  endif
  return result
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
  if type(a:buf) == g:NUMBER_TYPE
    let winnr = bufwinnr(a:buf)
  else
    let name = eclim#util#EscapeBufferName(a:buf)
    let winnr = bufwinnr(bufnr('^' . name . '$'))
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
  let winnr = bufwinnr(bufnr('^' . name . '$'))
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

" GoToTabAwareBufferWindowOrOpen(name, cmd) {{{
" Gives focus to the window containing the buffer for the supplied file even
" if it is in a different tab. If none is found, then opens the file using the
" supplied command.
function! eclim#util#GoToTabAwareBufferWindowOrOpen(name, cmd)
  let name = eclim#util#EscapeBufferName(a:name)
  let bufnr = bufnr('^' . name . '$')
  if bufnr != -1
    try 
      " Backup switchbuf option before resetting it
      let old_switchbuf = &switchbuf
      exec 'set switchbuf=usetab,newtab'

      exec 'sb ' . bufnr
      call eclim#util#DelayedCommand('doautocmd WinEnter')
    finally 
      " Restore switchbuf option to original value
      let &switchbuf = old_switchbuf
    endtry
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
  let string = type(a:element) == g:STRING_TYPE ?
    \ a:element : escape(string(a:element), '\')
  for element in a:list
    if element is a:element ||
        \ (type(element) == type(a:element) && element == a:element)
      return 1
    else
      let estring = type(element) == g:STRING_TYPE ? element : string(element)
      if estring =~ '^' . string . '$'
        return 1
      endif
    endif
  endfor
  return 0
endfunction " }}}

function! eclim#util#ListDedupe(list) " {{{
  " assumes the list is presorted.
  if exists('*uniq')
    return uniq(copy(a:list))
  endif
  return filter(copy(a:list), 'index(a:list, v:val, v:key + 1) == -1')
endfunction " }}}

function! eclim#util#Make(bang, args) " {{{
  " Executes make using the supplied arguments.

  let makefile = findfile('makefile', '.;')
  let makefile2 = findfile('Makefile', '.;')
  if len(makefile2) > len(makefile)
    let makefile = makefile2
  endif
  let cwd = getcwd()
  let save_mlcd = g:EclimMakeLCD
  exec 'lcd ' . fnamemodify(makefile, ':h')
  let g:EclimMakeLCD = 0
  try
    call eclim#util#MakeWithCompiler('eclim_make', a:bang, a:args)
  finally
    exec 'lcd ' . escape(cwd, ' ')
    let g:EclimMakeLCD = save_mlcd
  endtry
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

    if g:EclimMakeLCD && eclim#EclimAvailable(0)
      let w:quickfix_dir = getcwd()
      let dir = eclim#project#util#GetCurrentProjectRoot()
      if dir != ''
        exec 'lcd ' . escape(dir, ' ')
      endif
    endif

    " use dispatch if available and not disabled
    if exists(':Dispatch') == 2 && g:EclimMakeDispatchEnabled
      call eclim#util#EchoTrace('dispatch: ' . make_cmd)
      " since dispatch is intended to run the make cmd in the background, make
      " sure the errorformat doesn't suppress all the non-error output so the
      " user can see the full build output in the quickfix window.
      let &l:errorformat=substitute(&errorformat, '\M,%-G%.%#$', '', '')
      exec 'Dispatch' . a:bang . ' _ ' . a:args

    " windows machines where 'tee' is available
    elseif (has('win32') || has('win64')) &&
         \ (executable('tee') || executable('wtee'))
      doautocmd QuickFixCmdPre make
      let resultfile = eclim#util#Exec(make_cmd, 2)
      if filereadable(resultfile)
        if a:bang == ''
          exec 'cfile ' . escape(resultfile, ' ')
        else
          exec 'cgetfile ' . escape(resultfile, ' ')
        endif
        call delete(resultfile)
      endif
      silent doautocmd QuickFixCmdPost make

    " all other platforms
    else
      call eclim#util#EchoTrace('make: ' . make_cmd)
      exec 'make' . a:bang . ' ' . a:args
    endif
  catch /E42\>/
    " ignore 'E42: No Errors' which occurs when the make has qf results, but a
    " QuickFixCmdPost filters them all out.
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
    if exists('w:quickfix_dir')
      exec 'lcd ' . escape(w:quickfix_dir, ' ')
      unlet w:quickfix_dir
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

" Pad(string, length, [char]) {{{
" Pad the supplied string.
function! eclim#util#Pad(string, length, ...)
  let char = a:0 > 0 ? a:1 : ' '

  let string = a:string
  while len(string) < a:length
    let string .= char
  endwhile
  return string
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
      if escape && char != ' '
        let arg .= '\'
      endif
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
    if type(entries) == g:DICT_TYPE
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
  if type(entries) == g:DICT_TYPE
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
  if type(entry) == g:DICT_TYPE
    let file = entry.filename
    let line = entry.line
    let col = entry.column
    let message = entry.message
    let type = ''
    if has_key(entry, 'type')
      let type = entry.type[0]
    elseif has_key(entry, 'warning')
      let type = entry.warning ? 'w' : 'e'
    endif

  " FIXME: should be safe to remove this block after all commands have gone
  " through the json conversion.
  else
    let file = substitute(entry, '\(.\{-}\)|.*', '\1', '')
    let line = substitute(entry, '.*|\([0-9]\+\) col.*', '\1', '')
    let col = substitute(entry, '.*col \([0-9]\+\)|.*', '\1', '')
    let message = substitute(entry, '.*col [0-9]\+|\(.\{-}\)\(|.*\|$\)', '\1', '')
    let type = substitute(entry, '.*|\(e\|w\)$', '\1', '')
    if type == entry
      let type = ''
    endif
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

" Prompt(prompt, [validator], [highlight]) {{{
" Creates a prompt for the user using the supplied prompt string, validator
" and highlight. The prompt can be either a just a string to be displayed to
" the user or a 2 item list where the first item is the prompt and the second
" is the defaut value. The validator may return 0 to indicate an invalid input
" or a message indicating why the input is invalid, which will be displayed to
" the user. The validator should return 1 or the empty string to indicate
" valid input. Returns an empty string if the user doesn't enter a value or
" cancels the prompt.
function! eclim#util#Prompt(prompt, ...)
  " for unit testing
  if exists('g:EclimTestPromptQueue') && len(g:EclimTestPromptQueue)
    return remove(g:EclimTestPromptQueue, 0)
  endif

  let highlight = g:EclimHighlightInfo
  if a:0 > 0
    if type(a:1) == g:FUNCREF_TYPE
      let Validator = a:1
    elseif type(a:1) == g:STRING_TYPE
      let highlight = a:1
    endif
  endif

  if a:0 > 1
    if type(a:2) == g:FUNCREF_TYPE
      let Validator = a:2
    elseif type(a:2) == g:STRING_TYPE
      let highlight = a:2
    endif
  endif

  if type(a:prompt) == g:LIST_TYPE
    let prompt = a:prompt[0]
    let default = a:prompt[1]
  else
    let prompt = a:prompt
  endif

  exec "echohl " . highlight
  try
    if exists('l:default')
      let result = input(prompt . ': ', default)
    else
      let result = input(prompt . ': ')
    endif
    while result != ''
      if exists('l:Validator')
        let valid = Validator(result)
        if type(valid) == g:STRING_TYPE && valid != ''
          let result = input(valid . " (Ctrl-C to cancel): ", result)
        elseif type(valid) == g:NUMBER_TYPE && !valid
          let result = input(prompt, result)
        else
          return result
        endif
      else
        return result
      endif
    endwhile
  finally
    echohl None
  endtry

  return result
endfunction " }}}

" PromptList(prompt, list, [highlight]) {{{
" Creates a prompt for the user using the supplied prompt string and list of
" items to choose from.  Returns -1 if the list is empty or if the user
" canceled, and 0 if the list contains only one item.
function! eclim#util#PromptList(prompt, list, ...)
  " for unit testing
  if exists('g:EclimTestPromptQueue') && len(g:EclimTestPromptQueue)
    return remove(g:EclimTestPromptQueue, 0)
  endif

  " no elements, no prompt
  if empty(a:list)
    return -1
  endif

  " only one element, no need to choose.
  if len(a:list) == 1
    return 0
  endif

  let prompt = ""
  let index = g:EclimPromptListStartIndex
  for item in a:list
    let prompt = prompt . index . ") " . item . "\n"
    let index = index + 1
  endfor
  let maxindex = index - 1

  exec "echohl " . (a:0 ? a:1 : g:EclimHighlightInfo)
  try
    " clear any previous messages
    redraw
    try
      let response = input(prompt . "\n" . a:prompt . ": ")
    catch
      " echoing the list prompt vs. using it in the input() avoids apparent vim
      " bug that causes "Internal error: get_tv_string_buf()".
      echo prompt . "\n"
      let response = input(a:prompt . ": ")
    endtry
    while response !~ '\(^$\|^[0-9]\+$\)' ||
        \ response < g:EclimPromptListStartIndex ||
        \ response > maxindex
      let response = input("You must choose a value between " .
        \ g:EclimPromptListStartIndex . " and " . maxindex .
        \ ". (Ctrl-C to cancel): ")
    endwhile
  finally
    echohl None
    redraw!
  endtry

  if response == ''
    return -1
  endif

  return response - g:EclimPromptListStartIndex
endfunction " }}}

" PromptConfirm(prompt, [highlight]) {{{
" Creates a yes/no prompt for the user using the supplied prompt string.
" Returns -1 if the user canceled, otherwise 1 for yes, and 0 for no.
function! eclim#util#PromptConfirm(prompt, ...)
  " for unit testing
  if exists('g:EclimTestPromptQueue') && len(g:EclimTestPromptQueue)
    let choice = remove(g:EclimTestPromptQueue, 0)
    return choice =~ '\c\s*\(y\(es\)\?\)\s*'
  endif

  exec "echohl " . (a:0 ? a:1 : g:EclimHighlightInfo)
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

" Complete(start, completions) {{{
" Returns 1 if completion has been setup/triggered, 0 if not (because it is
" active already) and any input trigger would need to be inserted by the
" caller.
function! eclim#util#Complete(start, completions) " {{{
  if !exists('##CompleteDone')
    call complete(a:start, a:completions)
    return 1
  endif

  let b:eclim_complete_temp_start = a:start
  let b:eclim_complete_temp_completions = a:completions

  " If the temporary completion is active already, stop here and indicate
  " that the trigger needs to be inserted manually by returning 0.
  " (e.g. '{% e<BS>e' in a Django template)
  if &completefunc == 'eclim#util#CompleteTemp'
    return 0
  endif

  let b:eclim_complete_temp_func = &completefunc
  let b:eclim_complete_temp_opt = &completeopt
  augroup eclim_complete_temp
    autocmd!
    autocmd CompleteDone <buffer> call eclim#util#CompleteTempReset()
  augroup END
  setlocal completefunc=eclim#util#CompleteTemp
  setlocal completeopt=menuone,longest
  call feedkeys("\<c-x>\<c-u>", "n")
  return 1
endfunction " }}}

function! eclim#util#CompleteTemp(findstart, base) " {{{
  if a:findstart
    " complete() is 1 based, but omni completion functions are 0 based
    return b:eclim_complete_temp_start - 1
  endif
  return b:eclim_complete_temp_completions
endfunction " }}}

function! eclim#util#CompleteTempReset() " {{{
  silent! let &completefunc = b:eclim_complete_temp_func
  silent! let &completeopt = b:eclim_complete_temp_opt
  silent! unlet b:eclim_complete_temp_start
  silent! unlet b:eclim_complete_temp_completions
  silent! unlet b:eclim_complete_temp_func
  silent! unlet b:eclim_complete_temp_opt
  augroup eclim_complete_temp
    autocmd!
  augroup END
endfunction " }}}

function! eclim#util#Reload(options) " {{{
  " Reload the current file using ':edit' and perform other operations based on
  " the options supplied.
  " Supported Options:
  "   retab: Issue a retab of the file.
  "   pos: A line/column pair indicating the new cursor position post edit. When
  "     this pair is supplied, this function will attempt to preserve the
  "     current window's viewport.

  let winview = winsaveview()
  " save expand tab in case an indent detection plugin changes it based on code
  " inserted by eclipse, which may not yet match the user's actual settings.
  let save_expandtab = &expandtab

  edit!

  let &expandtab = save_expandtab

  if has_key(a:options, 'pos') && len(a:options.pos) == 2
    let lnum = a:options.pos[0]
    let cnum = a:options.pos[1]
    if winheight(0) < line('$')
      let winview.topline += lnum - winview.lnum
      let winview.lnum = lnum
      let winview.col = cnum - 1
      call winrestview(winview)
    else
      call cursor(lnum, cnum)
    endif
  endif

  if has_key(a:options, 'retab') && a:options.retab && &expandtab
    " set tabstop to the same value as shiftwidth if we may be expanding tabs
    let save_tabstop = &tabstop
    let &tabstop = &shiftwidth

    try
      retab
    finally
      let &tabstop = save_tabstop
    endtry
  endif
endfunction " }}}

function! eclim#util#SetLocationList(list, ...) " {{{
  " Sets the contents of the location list for the current window.
  " Optional args:
  "   action: The action passed to the setloclist() function call.
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

  silent let projectName = eclim#project#util#GetCurrentProjectName()
  if projectName != ''
    " setbufvar seems to have the side affect of changing to the buffer's dir
    " when autochdir is set.
    let save_autochdir = &autochdir
    set noautochdir

    for item in getloclist(0)
      call setbufvar(item.bufnr, 'eclim_project', projectName)
    endfor

    let &autochdir = save_autochdir
  endif

  if g:EclimShowCurrentError && len(loclist) > 0
    call eclim#util#DelayedCommand('call eclim#util#ShowCurrentError()')
  endif

  let b:eclim_loclist = 1
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
  unlet! b:eclim_loclist
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
  if mode() != 'n' || expand('%') == ''
    return
  endif

  let message = eclim#util#GetLineError(line('.'))
  if message != ''
    " remove any new lines
    let message = substitute(message, '\n', ' ', 'g')
    " convert tabs to spaces to ensure a consistent char to display length
    let message = substitute(message, '\t', '  ', 'g')

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

" System(cmd, [exec, exec_results]) {{{
" Executes system() accounting for possibly disruptive vim options.
" exec (0 or 1): whether or not to use exec instead of system
" exec_results (0, 1, or 2): 0 to not return the results of an exec, 1 to
"   return the results, or 2 to return the filename containing the results.
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
    set shellcmdflag=-c
    set shellpipe=2>&1\|\ tee
    set shellquote=
    set shellredir=>%s\ 2>&1
    set noshellslash
    set shelltemp
    set shellxquote=
  endif

  try
    " use exec
    if len(a:000) > 0 && a:000[0]
      let cmd = a:cmd
      let begin = localtime()
      let exec_output = len(a:000) > 1 ? a:000[1] : 0
      if exec_output
        let outfile = g:EclimTempDir . '/eclim_exec_output.txt'
        if has('win32') || has('win64') || has('win32unix')
          let cmd = substitute(cmd, '^!', '', '')
          if has('win32unix')
            let cmd = '!cmd /c "' . cmd . ' 2>&1 " | tee "' . outfile . '"'
          elseif executable('tee') || executable('wtee')
            let tee = executable('wtee') ? 'wtee' : 'tee'
            let cmd = '!cmd /c "' . cmd . ' 2>&1 | ' . tee . ' "' . outfile . '" "'
          else
            let cmd = '!cmd /c "' . cmd . ' >"' . outfile . '" 2>&1 "'
          endif
        else
          let cmd .= ' 2>&1| tee "' . outfile . '"'
        endif
      endif

      try
        exec cmd
      finally
        call eclim#util#EchoTrace('exec: ' . cmd, localtime() - begin)
      endtry

      let result = ''
      if exec_output == 1 && filereadable(outfile)
        let result = join(readfile(outfile), "\n")
        call delete(outfile)
      elseif exec_output == 2
        let result = outfile
      endif

    " use system
    else
      let begin = localtime()
      let cmd = a:cmd
      try
        " Dos is pretty bad at dealing with quoting of commands resulting in
        " eclim calls failing if the path to the eclim bat/cmd file is quoted
        " and there is a quoted arg in that command as well. We can fix this
        " by wrapping the whole command in quotes with a space between the
        " quotes and the actual command.
        if (has('win32') || has('win64')) && a:cmd =~ '^"'
          let cmd = '" ' . cmd . ' "'
        " same issue, but handle the fact that we prefix eclim calls with
        " 'cmd /c' for cygwin
        elseif has('win32unix') && a:cmd =~? '^cmd /c "[a-z]'
          let cmd = 'cmd /c " ' . substitute(cmd, '^cmd /c ', '', '') . ' "'
        endif
        let result = system(cmd)
      finally
        call eclim#util#EchoTrace('system: ' . cmd, localtime() - begin)
      endtry
    endif
  finally
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
  endtry

  return result
endfunction " }}}

function! eclim#util#TempWindow(name, lines, ...) " {{{
  " Opens a temp window w/ the given name and contents which is readonly unless
  " specified otherwise.
  let options = a:0 > 0 ? a:1 : {}
  let filename = expand('%:p')
  let winnr = winnr()

  let bufname = eclim#util#EscapeBufferName(a:name)
  let name = escape(a:name, ' ')
  if has('unix')
    let name = escape(name, '[]')
  endif

  let line = 1
  let col = 1

  if get(options, 'singleWinOnly', 0)
    if (bufnr(bufname) != -1 && bufwinnr(bufname) == -1)
      " Pass the unescaped name as it will be escaped in the function
      call eclim#util#DeleteBuffer(a:name)
    endif
  endif

  if bufwinnr(bufname) == -1
    let orient = get(options, 'orientation', 'horizontal')
    if orient == 'vertical'
      let width = get(options, 'width', 50)
      let split_cmd = "belowright vertical " . width . " sview "
      silent! noautocmd exec "keepalt " . split_cmd . name
    else
      let height = get(options, 'height', 10)
      let split_cmd = "botright " . height . " sview "
      silent! noautocmd exec "keepalt " . split_cmd . name
    endif

    setlocal nowrap
    setlocal winfixheight
    setlocal noswapfile
    setlocal nobuflisted
    setlocal buftype=nofile
    setlocal bufhidden=wipe
    silent doautocmd WinEnter
  else
    let temp_winnr = bufwinnr(bufname)
    if temp_winnr != winnr()
      exec temp_winnr . 'winc w'
      silent doautocmd WinEnter
      if get(options, 'preserveCursor', 0)
        let line = line('.')
        let col = col('.')
      endif
    endif
  endif

  call eclim#util#TempWindowClear(a:name)

  setlocal modifiable
  setlocal noreadonly
  call append(1, a:lines)
  retab

  let undolevels = &undolevels
  set undolevels=-1
  silent 1,1delete _
  let &undolevels = undolevels

  call cursor(line, col)

  if get(options, 'readonly', 1)
    setlocal nomodified
    setlocal nomodifiable
    setlocal readonly
    nmap <buffer> q :q<cr>
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

function! eclim#util#TempWindowClear(name) " {{{
  " Clears the contents of the temp window with the given name.
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

function! eclim#util#DeleteBuffer(name) " {{{
  " Deletes the buffer with given name and closes window holding it.
  let name = eclim#util#EscapeBufferName(a:name)
  let bufnr = bufnr(name)
  if bufnr != -1
    exec 'bd' . bufnr
  endif
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
  let vimwidth = &columns * &cmdheight
  if len(message) > vimwidth - 1
    let remove = len(message) - vimwidth
    let start = (len(message) / 2) - (remove / 2) - 4
    let end = start + remove + 4
    let message = substitute(message, '\%' . start . 'c.*\%' . end . 'c', '...', '')
  endif
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

function! eclim#util#CommandCompleteFile(argLead, cmdLine, cursorPos) " {{{
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')
  let results = split(eclim#util#Glob(argLead . '*', 1), '\n')
  call map(results, 'isdirectory(v:val) ? v:val . "/" : v:val')
  call map(results, "substitute(v:val, '\\', '/', 'g')")
  call map(results, "substitute(v:val, ' ', '\\\\ ', 'g')")

  return eclim#util#ParseCommandCompletionResults(argLead, results)
endfunction " }}}

function! eclim#util#CommandCompleteDir(argLead, cmdLine, cursorPos) " {{{
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

function! eclim#util#CommandCompleteOptions(argLead, cmdLine, cursorPos, options_map) " {{{
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')
  for [key, values] in items(a:options_map)
    if cmdLine =~ key . '\s\+[a-z]*$'
      return filter(copy(values), 'v:val =~ "^' . argLead . '"')
    endif
  endfor
  if cmdLine =~ '\s\+[-]\?$'
    let options = keys(a:options_map)
    let index = 0
    for option in options
      if a:cmdLine =~ option
        call remove(options, index)
      else
        let index += 1
      endif
    endfor
    return options
  endif
  return []
endfunction " }}}

function! eclim#util#ParseCmdLine(args) " {{{
  " Parses the supplied argument line into a list of args.
  let args = split(a:args, '[^\\]\s\zs')
  call map(args, 'substitute(v:val, "\\([^\\\\]\\)\\s\\+$", "\\1", "")')
  return args
endfunction " }}}

function! eclim#util#ParseCommandCompletionResults(argLead, results) " {{{
  " Bit of a hack for vim's lack of support for escaped spaces in custom
  " completion.
  let results = a:results
  if stridx(a:argLead, ' ') != -1
    let removePrefix = escape(substitute(a:argLead, '\(.*\s\).*', '\1', ''), '\')
    call map(results, "substitute(v:val, '^" . removePrefix . "', '', '')")
  endif
  return results
endfunction " }}}

function! eclim#util#ExtractCmdArgs(argline, extract) " {{{
  " Extracts one or more args from the given argline.
  " The 'extract' arg here is a list of args in the form '-x' where the -x arg
  " would be extracted. You can also use the getopts like syntax of '-x:'
  " (trailing colon) to indicate that you want the arg to the -x option to be
  " extracted as well.
  "
  " Returns a tuple with a list of the extracted args and the updated argline.
  let extract = type(a:extract) == g:LIST_TYPE ? a:extract : [a:extract]
  let args = eclim#util#ParseCmdLine(a:argline)
  let extracted_args = []
  let remaining_args = []
  let extract_next = 0
  for arg in args
    if extract_next
      call add(extracted_args, arg)
      let extract_next = 0
      continue
    endif
    for e in extract
      let has_value = 0
      if e =~ ':$'
        let e = e[:-2]
        let has_value = 1
      endif
      if arg == e
        call add(extracted_args, arg)
        let extract_next = has_value
      else
        call add(remaining_args, arg)
      endif
    endfor
  endfor

  return [extracted_args, join(remaining_args)]
endfunction "}}}

" vim:ft=vim:fdm=marker
