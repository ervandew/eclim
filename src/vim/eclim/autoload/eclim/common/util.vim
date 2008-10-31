" Author:  Eric Van Dewoestine
" Version: $Revision: 1197 $
"
" Description: {{{
"   Various functions that are useful in and out of eclim.
"
" License:
"
" Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
let s:command_locate = '-command locate_file -n "<project>" -p "<pattern>"'
" }}}

" DiffLastSaved() {{{
" Diff a modified file with the last saved version.
function! eclim#common#util#DiffLastSaved ()
  if &modified
    let winnum = winnr()
    let filetype=&ft
    vertical botright new | r #
    1,1delete _

    diffthis
    setlocal buftype=nofile
    setlocal bufhidden=wipe
    setlocal nobuflisted
    setlocal noswapfile
    setlocal readonly
    exec "setlocal ft=" . filetype
    let diffnum = winnr()

    augroup diff_saved
      autocmd! BufUnload <buffer>
      autocmd BufUnload <buffer> :diffoff!
    augroup END

    exec winnum . "winc w"
    diffthis

    " for some reason, these settings only take hold if set here.
    call setwinvar(diffnum, "&foldmethod", "diff")
    call setwinvar(diffnum, "&foldlevel", "0")
  else
    echo "No changes"
  endif
endfunction " }}}

" FindInPath(file, path) {{{
" Find a file in the supplied path returning a list of results.
function! eclim#common#util#FindInPath (file, path)
  let results = split(eclim#util#Globpath(a:path . '/**', a:file, 1), '\n')
  "let results = split(eclim#util#Globpath(a:path, a:file, 1), '\n') + results
  call map(results, "fnamemodify(v:val, ':p')")
  return results
endfunction " }}}

" GetFiles(dir, arg) {{{
" Parses the supplied arg to obtain a list of files based in the supplied
" directory.
function eclim#common#util#GetFiles (dir, arg)
  let dir = a:dir
  if dir != '' && dir !~ '[/\]$'
    let dir .= '/'
  endif

  let results = []
  let files = split(a:arg, '[^\\]\zs\s')
  for file in files
    " wildcard filename
    if file =~ '\*'
      let glob = split(eclim#util#Glob(dir . file), '\n')
      call map(glob, "escape(v:val, ' ')")
      if len(glob) > 0
        let results += glob
      endif

    " regular filename
    else
      call add(results, dir . file)
    endif
  endfor
  return results
endfunction " }}}

" GrepRelative(command, args) {{{
" Executes the supplied vim grep command with the specified pattern against
" one or more file patterns.
function! eclim#common#util#GrepRelative (command, args)
  let rel_dir = expand('%:p:h')
  let cwd = getcwd()
  try
    silent exec 'lcd ' . rel_dir
    silent! exec a:command . ' ' . a:args
  finally
    silent exec 'lcd ' . cwd
    " force quickfix / location list signs to update.
    call eclim#display#signs#Update()
  endtry
  if a:command =~ '^l'
    let numresults = len(getloclist(0))
  else
    let numresults = len(getqflist())
  endif

  if numresults == 0
    call eclim#util#EchoInfo('No results found.')
  endif
endfunction " }}}

" LocateFile(command, file) {{{
" Locates a file using the specified command for opening the file when found.
"   command - 'split', 'edit', etc.
"   file - 'somefile.txt',
"          '', (kick off completion mode),
"          '<cursor>' (locate the file under the cursor)
function eclim#common#util#LocateFile (command, file)
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let results = []
  let file = a:file
  if file == ''
    call s:LocateFileCompletionInit(a:command)
    return
  elseif file == '<cursor>'
    let file = eclim#util#GrabUri()

    " if grabbing a relative url, remove any anchor info or query parameters
    let file = substitute(file, '[#?].*', '', '')
  endif

  let name = fnamemodify(file, ':t')
  if name == ''
    call eclim#util#Echo('Please supplie more than just a directory name.')
    return
  endif

  let pattern = '.*' . file . '.*'
  let pattern = substitute(pattern, '\*\*', '*', 'g')
  let pattern = substitute(pattern, '\([^.]\)\*', '\1.*', 'g')
  let pattern = substitute(pattern, '\([^*]\)?', '\1.', 'g')
  let pattern = substitute(pattern, '\.\([^*]\)', '\\.\1', 'g')
  let project = eclim#project#util#GetCurrentProjectName()
  let command = s:command_locate
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<pattern>', pattern, '')

  let results = split(eclim#ExecuteEclim(command), '\n')
  if len(results) == 1 && results[0] == '0'
    return
  endif

  call map(results, "split(v:val, '|')[2]")

  let result = ''
  " One result.
  if len(results) == 1
    let result = results[0]

  " More than one result.
  elseif len(results) > 1
    let message = "Multiple results, choose the file to open"
    let response = eclim#util#PromptList(message, results, g:EclimInfoHighlight)
    if response == -1
      return
    endif

    let result = results[response]

  " No results
  else
    call eclim#util#Echo('Unable to locate file named "' . file . '".')
    return
  endif

  call eclim#util#GoToBufferWindowOrOpen(
    \ escape(eclim#util#Simplify(result), ' '), a:command)
  call eclim#util#Echo(' ')
endfunction " }}}

" s:LocateFileCompletionInit(command) {{{
function s:LocateFileCompletionInit (command)
  let file = expand('%')
  let project = eclim#project#util#GetCurrentProjectName()

  topleft 10split [Locate\ Results]
  set filetype=locate_results
  setlocal nonumber nowrap
  setlocal noswapfile nobuflisted
  setlocal buftype=nofile bufhidden=delete

  let results_bufnum = bufnr('%')

  topleft 1split [Locate]
  call setline(1, '> ')
  call cursor(1, col('$'))
  set filetype=locate_prompt
  syntax match Keyword /^>/
  set winfixheight
  setlocal nonumber
  setlocal nolist
  setlocal noswapfile nobuflisted
  setlocal buftype=nofile bufhidden=delete

  let b:file = file
  let b:command = a:command
  let b:project = project
  let b:results_bufnum = results_bufnum
  let b:selection = 1
  let b:updatetime = &updatetime

  set updatetime=300

  augroup locate_file_init
    autocmd!
    exec 'autocmd InsertLeave <buffer> let &updatetime = ' . b:updatetime . ' | ' .
      \ 'bd ' . b:results_bufnum . ' | ' .  'bd | ' .
      \ 'call eclim#util#GoToBufferWindow("' .  escape(b:file, '\') . '")'
  augroup END

  call s:LocateFileCompletionAutocmd()

  imap <buffer> <silent> <tab> <c-r>=<SID>LocateFileSelection('n')<cr>
  imap <buffer> <silent> <s-tab> <c-r>=<SID>LocateFileSelection('p')<cr>
  imap <buffer> <silent> <cr> <c-r>=<SID>LocateFileSelect()<cr>

  startinsert!
endfunction " }}}

" LocateFileCompletion() {{{
function eclim#common#util#LocateFileCompletion ()
  let completions = []
  let display = []
  let name = substitute(getline('.'), '^>\s*', '', '')
  if name !~ '^\s*$'
    let pattern = name
    let pattern = '.*' . substitute(pattern, '\(.\)', '\1.*?', 'g')
    let pattern = substitute(pattern, '\.\([^*]\)', '\\.\1', 'g')
    let command = s:command_locate
    let command = substitute(command, '<project>', b:project, '')
    let command = substitute(command, '<pattern>', pattern, '')
    let results = split(eclim#ExecuteEclim(command), '\n')
    if len(results) == 1 && results[0] == '0'
      return
    endif
    if !empty(results)
      for result in results
        let parts = split(result, '|')
        let dict = {'word': parts[0], 'menu': parts[1], 'info': parts[2]}
        call add(completions, dict)
        call add(display, parts[0] . '  ' . parts[1])
      endfor
    endif
  endif
  let b:completions = completions
  let winnr = winnr()
  exec bufwinnr(b:results_bufnum) . 'winc w'
  1,$delete _
  call append(1, display)
  1,1delete _
  exec winnr . 'winc w'

  call s:LocateFileSelection(1)
endfunction " }}}

" s:LocateFileCompletionAutocmd() {{{
function s:LocateFileCompletionAutocmd ()
  augroup locate_file
    autocmd!
    autocmd CursorHoldI <buffer> call eclim#common#util#LocateFileCompletion()
  augroup END
endfunction " }}}

" s:LocateFileSelection(sel) {{{
function s:LocateFileSelection (sel)
  " pause completion while tabbing though results
  augroup locate_file
    autocmd!
  augroup END

  let sel = a:sel
  let prev_sel = b:selection

  let winnr = winnr()
  exec bufwinnr(b:results_bufnum) . 'winc w'

  if sel == 'n'
    let sel = prev_sel < line('$') ? prev_sel + 1 : 1
  elseif sel == 'p'
    let sel = prev_sel > 1 ? prev_sel - 1 : line('$')
  endif

  syntax clear
  exec 'syntax match PmenuSel /\%' . sel . 'l.*/'
  exec 'call cursor(' . sel . ', 1)'
  normal zt

  exec winnr . 'winc w'

  exec 'let b:selection = ' . sel

  " restore completion on the next key typed
  augroup locate_file
    autocmd CursorMovedI <buffer> call <SID>LocateFileCompletionAutocmd()
  augroup END

  return ''
endfunction " }}}

" s:LocateFileSelect() {{{
function s:LocateFileSelect ()
  if exists('b:completions') && !empty(b:completions)
    let winnr = winnr()
    let file = eclim#util#Simplify(b:completions[b:selection - 1].info)
    let command = b:command
    let bufnum = bufnr('%')
    let results_bufnum = b:results_bufnum
    call eclim#util#GoToBufferWindow(escape(b:file, '\'))
    call eclim#util#GoToBufferWindowOrOpen(escape(file, '\'), command)
    call feedkeys("\<esc>:bd " . bufnum . " | bd " . results_bufnum . "\<cr>", 'n')
  endif
  return ''
endfunction " }}}

" OpenRelative(command, arg [, open_existing]) {{{
" Open one or more relative files.
function eclim#common#util#OpenRelative (command, arg, ...)
  if a:arg =~ '\*' && a:command == 'edit'
    call eclim#util#EchoError(':EditRelative does not support wildcard characters.')
    return
  endif

  let dir = expand('%:p:h')
  let files = eclim#common#util#GetFiles(dir, a:arg)
  for file in files
    let file = escape(eclim#util#Simplify(file), ' ')
    if len(a:000) && a:000[0]
      call eclim#util#GoToBufferWindowOrOpen(file, a:command)
    else
      exec a:command . ' ' . file
    endif
  endfor
endfunction " }}}

" OpenFiles(arg) {{{
" Opens one or more files using the supplied command.
function eclim#common#util#OpenFiles (command, arg)
  let files = eclim#common#util#GetFiles('', a:arg)
  for file in files
    exec a:command . ' ' . escape(eclim#util#Simplify(file), ' ')
  endfor
endfunction " }}}

" OtherWorkingCopy(project, action) {{{
" Opens the same file from another project using the supplied action
function! eclim#common#util#OtherWorkingCopy (project, action)
  let path = eclim#project#util#GetProjectRelativeFilePath(expand('%:p'))
  let projects = eclim#project#util#GetProjects()
  let project_path = s:OtherWorkingCopyPath(a:project)
  if project_path == ''
    return
  endif
  call eclim#util#GoToBufferWindowOrOpen(project_path, a:action)
endfunction " }}}

" OtherWorkingCopyDiff(project) {{{
" Diffs the current file against the same file from another project.
function! eclim#common#util#OtherWorkingCopyDiff (project)
  let project_path = s:OtherWorkingCopyPath(a:project)
  if project_path == ''
    return
  endif

  let filename = expand('%:p')
  diffthis

  call eclim#util#GoToBufferWindowOrOpen(project_path, 'vertical split')
  diffthis

  let b:filename = filename
  augroup other_diff
    autocmd! BufUnload <buffer>
    call eclim#util#GoToBufferWindowRegister(b:filename)
    autocmd BufUnload <buffer> diffoff
  augroup END
endfunction " }}}

" s:OtherWorkingCopyPath(project) {{{
function s:OtherWorkingCopyPath (project)
  let path = eclim#project#util#GetProjectRelativeFilePath(expand('%:p'))
  let projects = eclim#project#util#GetProjects()

  let project_name = a:project
  if project_name =~ '[\\/]$'
    let project_name = project_name[:-2]
  endif

  if !has_key(projects, project_name)
    call eclim#util#EchoWarning("Project '" . project_name . "' not found.")
    return ''
  endif
  return projects[project_name] . '/' . path
endfunction " }}}

" SwapTypedArguments() {{{
" Swaps typed method declaration arguments.
function! eclim#common#util#SwapTypedArguments ()
  " FIXME: add validation to see if user is executing on a valid position.
  normal w
  SwapWords
  normal b
  SwapWords
  normal www
  SwapWords
  normal bb
  SwapWords
  normal b
endfunction " }}}

" SwapWords() {{{
" Initially based on http://www.vim.org/tips/tip.php?tip_id=329
function! eclim#common#util#SwapWords ()
  " save the last search pattern
  let save_search = @/

  normal "_yiw
  s/\(\%#\w\+\)\(\_W\+\)\(\w\+\)/\3\2\1/
  exec "normal \<C-O>"

  " restore the last search pattern
  let @/ = save_search
endfunction " }}}

" CommandCompleteRelative(argLead, cmdLine, cursorPos) {{{
" Custom command completion for relative files and directories.
function! eclim#common#util#CommandCompleteRelative (argLead, cmdLine, cursorPos)
  let dir = substitute(expand('%:p:h'), '\', '/', 'g')

  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseArgs(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]

  let results = split(eclim#util#Glob(dir . '/' . argLead . '*', 1), '\n')
  call map(results, "substitute(v:val, '\\', '/', 'g')")
  call map(results, 'isdirectory(v:val) ? v:val . "/" : v:val')
  call map(results, 'substitute(v:val, dir, "", "")')
  call map(results, 'substitute(v:val, "^\\(/\\|\\\\\\)", "", "g")')
  call map(results, "substitute(v:val, ' ', '\\\\ ', 'g')")

  return eclim#util#ParseCommandCompletionResults(argLead, results)
endfunction " }}}

" CommandCompleteRelativeDirs(argLead, cmdLine, cursorPos) {{{
" Custom command completion for relative directories.
function! eclim#common#util#CommandCompleteRelativeDirs (argLead, cmdLine, cursorPos)
  let dir = substitute(expand('%:p:h'), '\', '/', 'g')

  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseArgs(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]

  let results = split(eclim#util#Glob(dir . '/' . argLead . '*', 1), '\n')
  call filter(results, "isdirectory(v:val)")
  call map(results, "substitute(v:val, '\\', '/', 'g')")
  call map(results, 'substitute(v:val, dir . "\\(.*\\)", "\\1/", "")')
  call map(results, 'substitute(v:val, "^\\(/\\|\\\\\\)", "", "g")')
  call map(results, "substitute(v:val, ' ', '\\\\ ', 'g')")

  return eclim#util#ParseCommandCompletionResults(argLead, results)
endfunction " }}}

" vim:ft=vim:fdm=marker
