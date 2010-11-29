" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Implements the :LocateFile functionality.
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

" Global Variables {{{
if !exists('g:EclimLocateFileDefaultAction')
  let g:EclimLocateFileDefaultAction = g:EclimDefaultFileOpenAction
endif

if !exists('g:EclimLocateFileScope')
  let g:EclimLocateFileScope = 'project'
endif

if !exists('g:EclimLocateFileFuzzy')
  let g:EclimLocateFileFuzzy = 1
endif

if !exists('g:EclimLocateUserScopes')
  let g:EclimLocateUserScopes = []
endif

let g:eclim_locate_default_updatetime = &updatetime

" }}}

" Script Variables {{{
let s:command_locate = '-command locate_file -s "<scope>"'
let s:scopes = [
    \ 'project',
    \ 'workspace',
    \ 'buffers',
    \ 'quickfix',
  \ ]

let s:help = [
    \ '<esc> - close the locate prompt + results',
    \ '<tab>, <down> - select the next file',
    \ '<s-tab>, <up> - select the previous file',
    \ '<cr> - open selected file w/ default action',
    \ '<c-e> - open with :edit',
    \ '<c-s> - open in a split window',
    \ '<c-t> - open in a new tab',
    \ '<c-l> - choose search scope',
    \ '<c-h> - toggle help buffer',
  \ ]
" }}}

" LocateFile(action, file, [scope]) {{{
" Locates a file using the specified action for opening the file when found.
"   action - '' (use user default), 'split', 'edit', etc.
"   file - 'somefile.txt',
"          '', (kick off completion mode),
"          '<cursor>' (locate the file under the cursor)
"   scope - optional scope to search in (project, workspace, buffers, etc.)
function! eclim#common#locate#LocateFile(action, file, ...)
  let project = eclim#project#util#GetCurrentProjectName()
  let scope = a:0 > 0 ? a:1 : g:EclimLocateFileScope

  if !eclim#util#ListContains(s:scopes, scope) &&
   \ !eclim#util#ListContains(g:EclimLocateUserScopes, scope)
    call eclim#util#EchoWarning('Unrecognized scope: ' . scope)
    return
  endif

  if scope == 'project' && project == ''
    let scope = 'workspace'
  endif

  let workspace = eclim#eclipse#ChooseWorkspace()
  if workspace == '0'
    return
  endif

  if !eclim#PingEclim(0, workspace)
    call eclim#util#EchoError('Unable to connect to eclimd.')
    return
  endif

  let results = []
  let action = a:action
  if action == ''
    let action = g:EclimLocateFileDefaultAction
  endif

  let file = a:file
  if file == ''
    call s:LocateFileCompletionInit(action, scope, project, workspace)
    return
  elseif file == '<cursor>'
    let file = eclim#util#GrabUri()

    " if grabbing a relative url, remove any anchor info or query parameters
    let file = substitute(file, '[#?].*', '', '')
  endif

  let name = fnamemodify(file, ':t')
  if name == ''
    call eclim#util#Echo('Please supply more than just a directory name.')
    return
  endif

  let pattern = file
  let pattern = s:LocateFileConvertPattern(pattern, 0)
  let pattern = '[^/]*' . pattern
  try
    let b:workspace = workspace
    let b:project = project
    let results = s:LocateFileFunction(scope)(pattern)
  finally
    unlet! b:workspce
    unlet! b:project
  endtry

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
    call eclim#util#Echo('Unable to locate file pattern "' . file . '".')
    return
  endif

  if has('win32unix')
    let result = eclim#cygwin#CygwinPath(result)
  endif

  call eclim#util#GoToBufferWindowOrOpen(eclim#util#Simplify(result), action)
  call eclim#util#Echo(' ')
endfunction " }}}

" LocateFileCompletion() {{{
function! eclim#common#locate#LocateFileCompletion()
  let line = getline('.')
  if line !~ '^> '
    call setline(1, substitute(line, '^>\?\s*', '> \1', ''))
    call cursor(1, 3)
    let line = getline('.')
  endif

  let completions = []
  let display = []
  let name = substitute(line, '^>\s*', '', '')
  if name !~ '^\s*$'
    let pattern = name
    let pattern = s:LocateFileConvertPattern(pattern, g:EclimLocateFileFuzzy)

    let results = s:LocateFileFunction(b:scope)(pattern)
    if !empty(results)
      for result in results
        let parts = split(result, '|')
        let rel = eclim#util#Simplify(parts[1])
        let dict = {'word': parts[0], 'menu': rel, 'info': parts[2]}
        call add(completions, dict)
        call add(display, parts[0] . '  ' . rel)
      endfor
    endif
  endif
  let b:completions = completions
  let winnr = winnr()
  noautocmd exec bufwinnr(b:results_bufnum) . 'winc w'
  setlocal modifiable
  1,$delete _
  call append(1, display)
  1,1delete _
  setlocal nomodifiable
  exec winnr . 'winc w'

  " part of bad hack for gvim on windows
  let b:start_selection = 1

  call s:LocateFileSelection(1)
endfunction " }}}

" LocateFileClose() {{{
function! eclim#common#locate#LocateFileClose()
  if bufname(bufnr('%')) !~ '^\[Locate.*\]$'
    let bufnr = bufnr('\[Locate in *\]')
    let winnr = bufwinnr(bufnr)
    if winnr != -1
      let curbuf = bufnr('%')
      exec winnr . 'winc w'
      try
        exec 'bw ' . b:results_bufnum
        bw
        autocmd! locate_file_init
        stopinsert
      finally
        exec bufwinnr(curbuf) . 'winc w'
      endtry
    endif
  endif
endfunction " }}}

" s:LocateFileCompletionInit(action, scope, project, workspace) {{{
function! s:LocateFileCompletionInit(action, scope, project, workspace)
  let file = expand('%')
  let bufnum = bufnr('%')
  let winrestcmd = winrestcmd()

  topleft 12split [Locate\ Results]
  set filetype=locate_results
  setlocal nonumber nowrap
  setlocal noswapfile nobuflisted
  setlocal buftype=nofile bufhidden=delete

  let results_bufnum = bufnr('%')

  let locate_in = (a:scope == 'project' ? a:project : a:scope)
  exec 'topleft 1split ' . escape('[Locate in ' . locate_in . ']', ' ')
  setlocal modifiable
  call setline(1, '> ')
  call cursor(1, col('$'))
  set filetype=locate_prompt
  syntax match Keyword /^>/
  setlocal winfixheight
  setlocal nonumber
  setlocal nolist
  setlocal noswapfile nobuflisted
  setlocal buftype=nofile bufhidden=delete

  let b:bufnum = bufnum
  let b:project = a:project
  let b:workspace = a:workspace
  let b:scope = a:scope
  let b:results_bufnum = results_bufnum
  let b:help_bufnum = 0
  let b:selection = 1

  set updatetime=300

  augroup locate_file_init
    autocmd!
    autocmd BufEnter <buffer> nested startinsert! | let &updatetime = 300
    autocmd BufLeave \[Locate*\]
      \ call eclim#util#DelayedCommand('call eclim#common#locate#LocateFileClose()')
    exec 'autocmd InsertLeave <buffer> ' .
      \ 'let &updatetime = g:eclim_locate_default_updatetime | ' .
      \ 'doautocmd BufWinLeave | bw | ' .
      \ 'doautocmd BufWinLeave | bw ' . b:results_bufnum . ' | ' .
      \ 'call eclim#util#GoToBufferWindow(' .  b:bufnum . ') | ' .
      \ 'doautocmd BufEnter | ' .
      \ 'doautocmd WinEnter | ' .
      \ winrestcmd
    exec 'autocmd WinEnter <buffer=' . b:results_bufnum .'> '
      \ 'exec bufwinnr(' . bufnr('%') . ') "winc w"'
  augroup END

  " enable completion after user starts typing
  call s:LocateFileCompletionAutocmdDeferred()

  imap <buffer> <silent> <tab> <c-r>=<SID>LocateFileSelection("n")<cr>
  imap <buffer> <silent> <down> <c-r>=<SID>LocateFileSelection("n")<cr>
  imap <buffer> <silent> <s-tab> <c-r>=<SID>LocateFileSelection("p")<cr>
  imap <buffer> <silent> <up> <c-r>=<SID>LocateFileSelection("p")<cr>
  exec 'imap <buffer> <silent> <cr> ' .
    \ '<c-r>=<SID>LocateFileSelect("' . a:action . '")<cr>'
  imap <buffer> <silent> <c-e> <c-r>=<SID>LocateFileSelect('edit')<cr>
  imap <buffer> <silent> <c-s> <c-r>=<SID>LocateFileSelect('split')<cr>
  imap <buffer> <silent> <c-t> <c-r>=<SID>LocateFileSelect("tablast \| tabnew")<cr>
  imap <buffer> <silent> <c-l> <c-r>=<SID>LocateFileChangeScope()<cr>
  imap <buffer> <silent> <c-h> <c-r>=<SID>LocateFileHelp()<cr>

  startinsert!
endfunction " }}}

" s:LocateFileCompletionAutocmd() {{{
function! s:LocateFileCompletionAutocmd()
  augroup locate_file
    autocmd!
    autocmd CursorHoldI <buffer> call eclim#common#locate#LocateFileCompletion()
  augroup END
endfunction " }}}

" s:LocateFileCompletionAutocmdDeferred() {{{
function! s:LocateFileCompletionAutocmdDeferred()
  augroup locate_file
    autocmd!
    autocmd CursorMovedI <buffer> call <SID>LocateFileCompletionAutocmd()
  augroup END
endfunction " }}}

" s:LocateFileSelection(sel) {{{
function! s:LocateFileSelection(sel)
  " pause completion while tabbing though results
  augroup locate_file
    autocmd!
  augroup END

  let sel = a:sel
  let prev_sel = b:selection

  " bad hack for gvim on windows
  let start_sel = b:start_selection
  let double_defer = 0
  if sel =~ '^[np]$' && (has('win32') || has('win64'))
    let double_defer = b:start_selection == 1
    let b:start_selection = 0
  endif

  let winnr = winnr()
  noautocmd exec bufwinnr(b:results_bufnum) . 'winc w'

  if sel == 'n'
    let sel = prev_sel < line('$') ? prev_sel + 1 : 1
  elseif sel == 'p'
    let sel = prev_sel > 1 ? prev_sel - 1 : line('$')
  endif

  syntax clear
  exec 'syntax match PmenuSel /\%' . sel . 'l.*/'
  exec 'call cursor(' . sel . ', 1)'
  let save_scrolloff = &scrolloff
  let &scrolloff = 5
  normal! zt
  let &scrolloff = save_scrolloff

  exec winnr . 'winc w'

  exec 'let b:selection = ' . sel

  if double_defer
    augroup locate_file
      autocmd!
      autocmd CursorMovedI <buffer> call <SID>LocateFileCompletionAutocmdDeferred()
    augroup END
  else
    call s:LocateFileCompletionAutocmdDeferred()
  endif

  return ''
endfunction " }}}

" s:LocateFileSelect(action) {{{
function! s:LocateFileSelect(action)
  if exists('b:completions') && !empty(b:completions)
    let winnr = winnr()
    let file = eclim#util#Simplify(b:completions[b:selection - 1].info)
    if has('win32unix')
      let file = eclim#cygwin#CygwinPath(file)
    endif
    let bufnum = bufnr('%')
    let results_bufnum = b:results_bufnum
    let &updatetime = g:eclim_locate_default_updatetime
    call eclim#util#GoToBufferWindow(b:bufnum)
    call eclim#util#GoToBufferWindowOrOpen(file, a:action)
    call feedkeys(
      \ "\<esc>:bd " . bufnum . " | " .
      \ "bd " . results_bufnum . " | " .
      \ "doautocmd WinEnter\<cr>", 'n')
  endif
  return ''
endfunction " }}}

" s:LocateFileChangeScope() {{{
function! s:LocateFileChangeScope()
  if b:help_bufnum && bufexists(b:help_bufnum)
    exec 'bdelete ' . b:help_bufnum
  endif

  let bufnr = bufnr('%')
  let winnr = winnr()

  " trigger [Locate] buffer's BufLeave autocmd before we leave the buffer
  doautocmd BufLeave

  noautocmd exec bufwinnr(b:results_bufnum) . 'winc w'
  silent noautocmd exec '50vnew [Locate\ Scope]'

  let b:locate_bufnr = bufnr
  let b:locate_winnr = winnr
  stopinsert
  setlocal modifiable
  call append(1, s:scopes + g:EclimLocateUserScopes)
  1,1delete _
  call append(line('$'),
    \ ['', '" <cr> - select a scope', '" <c-c>, <c-l>, or q - cancel'])
  syntax match Comment /^".*/
  setlocal nomodifiable
  setlocal winfixheight
  setlocal nonumber
  setlocal nolist
  setlocal noswapfile nobuflisted
  setlocal buftype=nofile bufhidden=delete

  nmap <buffer> <silent> <cr> :call <SID>ChooseScope()<cr>
  nmap <buffer> <silent> q :call <SID>CloseScopeChooser()<cr>
  nmap <buffer> <silent> <c-c> :call <SID>CloseScopeChooser()<cr>
  nmap <buffer> <silent> <c-l> :call <SID>CloseScopeChooser()<cr>

  autocmd BufLeave <buffer> call <SID>CloseScopeChooser()

  return ''
endfunction " }}}

" s:ChooseScope() {{{
function! s:ChooseScope()
  let scope = getline('.')
  if scope =~ '^"\|^\s*$'
    return
  endif

  let workspace = ''
  let project = ''
  let locate_in = scope

  if scope == 'project'
    let project = ''
    let names = eclim#project#util#GetProjectNames()
    let prompt = 'Choose a project (ctrl-c to cancel): '
    while project == ''
      let project = input(
        \ prompt, '', 'customlist,eclim#project#util#CommandCompleteProject')
      if project == ''
        echo ''
        return
      endif

      if !eclim#util#ListContains(names, project)
        let prompt = "Project '" . project . "' not found (ctrl-c to cancel): "
        let project = ''
      endif
    endwhile
    let locate_in = project
    let workspace = eclim#project#util#GetProjectWorkspace(project)

  elseif scope == 'workspace'
    let project = ''
    let workspace = eclim#eclipse#ChooseWorkspace()
  endif

  call s:CloseScopeChooser()

  let b:scope = scope
  let b:project = project
  let b:workspace = workspace != '' ? workspace : b:workspace

  exec 'file ' . escape('[Locate in ' . locate_in . ']', ' ')

  call eclim#common#locate#LocateFileCompletion()
endfunction " }}}

" s:CloseScopeChooser() {{{
function! s:CloseScopeChooser()
  let winnum = b:locate_winnr
  bwipeout
  exec winnum . 'winc w'

  " hack to make :q work like the other close mappings
  doautocmd BufEnter
  " if we end up in a non-Locate window, make sure everything is as it should
  " be (a hack for the above hack).
  augroup locate_file_chooser_hack
    autocmd!
    autocmd BufEnter *
      \ if bufname('%') !~ '^\[Locate in .*\]$' |
      \   call eclim#common#locate#LocateFileClose() |
      \ endif |
      \ autocmd! locate_file_chooser_hack
  augroup END
endfunction " }}}

" s:LocateFileHelp() {{{
function! s:LocateFileHelp()
  let winnr = winnr()
  noautocmd exec bufwinnr(b:results_bufnum) . 'winc w'
  let help_bufnum = eclim#help#BufferHelp(s:help, 'vertical', 50)
  exec winnr . 'winc w'
  let b:help_bufnum = help_bufnum

  return ''
endfunction " }}}

" s:LocateFileConvertPattern(pattern, fuzzy) {{{
function! s:LocateFileConvertPattern(pattern, fuzzy)
  let pattern = a:pattern

  if a:fuzzy
    let pattern = '.*' . substitute(pattern, '\(.\)', '\1.*?', 'g')
    let pattern = substitute(pattern, '\.\([^*]\)', '\\.\1', 'g')
  else
    " if the user supplied a path, prepend a '.*/' to it so that they don't need
    " to type full paths to match.
    if pattern =~ '.\+/'
      let pattern = '.*/' . pattern
    endif
    let pattern = substitute(pattern, '\*\*', '.*', 'g')
    let pattern = substitute(pattern, '\(^\|\([^.]\)\)\*', '\1[^/]*?', 'g')
    let pattern = substitute(pattern, '\.\([^*]\)', '\\.\1', 'g')
    "let pattern = substitute(pattern, '\([^*]\)?', '\1.', 'g')
    let pattern .= '.*'
  endif

  return pattern
endfunction " }}}

" s:LocateFileFunction(scope) {{{
function! s:LocateFileFunction(scope)
  if eclim#util#ListContains(s:scopes, a:scope)
    return function('s:LocateFile_' . a:scope)
  endif
  return function('LocateFile_' . a:scope)
endfunction " }}}

" s:LocateFile_workspace(pattern) {{{
function! s:LocateFile_workspace(pattern)
  let command = s:command_locate
  let command = substitute(command, '<scope>', 'workspace', '')
  let command .= ' -p "' . a:pattern . '"'
  let port = eclim#client#nailgun#GetNgPort(b:workspace)
  let results = split(eclim#ExecuteEclim(command, port), '\n')
  if len(results) == 1 && results[0] == '0'
    return []
  endif
  return results
endfunction " }}}

" s:LocateFile_project(pattern) {{{
function! s:LocateFile_project(pattern)
  let command = s:command_locate
  let command = substitute(command, '<scope>', 'project', '')
  let command .= ' -p "' . a:pattern . '"'
  let command .= ' -n "' . b:project . '"'
  let port = eclim#client#nailgun#GetNgPort(b:workspace)
  let results = split(eclim#ExecuteEclim(command, port), '\n')
  if len(results) == 1 && results[0] == '0'
    return []
  endif
  return results
endfunction " }}}

" s:LocateFile_buffers(pattern) {{{
function! s:LocateFile_buffers(pattern)
  redir => list
  silent exec 'buffers'
  redir END

  let buffers = map(split(list, '\n'),
    \ "substitute(v:val, '.\\{-}\"\\(.\\{-}\\)\".*', '\\1', '')")
  if a:pattern =~ '/'
    let buffers = map(buffers, "fnamemodify(v:val, ':p')")
  endif

  if len(buffers) > 0
    let tempfile = substitute(tempname(), '\', '/', 'g')
    call writefile(buffers, tempfile)
    try
      return eclim#common#locate#LocateFileFromFileList(a:pattern, tempfile)
    finally
      call delete(tempfile)
    endtry
  endif
  return []
endfunction " }}}

" s:LocateFile_quickfix(pattern) {{{
function! s:LocateFile_quickfix(pattern)
  let buffers = []
  let prev = ''
  for entry in getqflist()
    let name = bufname(entry.bufnr)
    if a:pattern =~ '/'
      let name = fnamemodify(name, ':p')
    endif
    if name != prev
      call add(buffers, name)
      let prev = name
    endif
  endfor

  if len(buffers) > 0
    let tempfile = substitute(tempname(), '\', '/', 'g')
    call writefile(buffers, tempfile)
    try
      return eclim#common#locate#LocateFileFromFileList(a:pattern, tempfile)
    finally
      call delete(tempfile)
    endtry
  endif
  return []
endfunction " }}}

" LocateFileFromFileList(pattern, file) {{{
function! eclim#common#locate#LocateFileFromFileList(pattern, file)
  let file = a:file
  if has('win32unix')
    let file = eclim#cygwin#WindowsPath(file)
  endif
  let command = s:command_locate
  let command = substitute(command, '<scope>', 'list', '')
  let command .= ' -p "' . a:pattern . '"'
  let command .= ' -f "' . file . '"'
  let port = eclim#client#nailgun#GetNgPort(b:workspace)
  let results = split(eclim#ExecuteEclim(command, port), '\n')
  if len(results) == 1 && results[0] == '0'
    return []
  endif
  return results
endfunction " }}}

" vim:ft=vim:fdm=marker
