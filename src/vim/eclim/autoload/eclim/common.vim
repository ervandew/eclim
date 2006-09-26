" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Various functions that are useful in and out of eclim.
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

" DiffLastSaved() {{{
" Diff a modified file with the last saved version.
function! eclim#common#DiffLastSaved ()
  if &modified
    let winnum = winnr()
    let filetype=&ft
    vertical botright new | r #
    let saved = @"
    1,1delete
    let @" = saved

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

" GetFiles(dir, arg) {{{
" Parses the supplied arg to obtain a list of files based in the supplied
" directory.
function eclim#common#GetFiles (dir, arg)
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

" FindInPath(file, path) {{{
" Find a file in the supplied path returning a list of results.
function! eclim#common#FindInPath (file, path)
  let results = eclim#util#Findfile(a:file, a:path . '/**', -1)
  call map(results, "fnamemodify(v:val, ':p')")
  return results
endfunction " }}}

" LocateFile(command, file) {{{
" Locates a file using the following steps:
" 1) First if current file is in a project, search that project.
" 2) No results from #1, then search relative to current file.
" 3) No results from #2, then search other projects.
function eclim#common#LocateFile (command, file)
  let results = []
  let file = a:file
  if file == ''
    let file = eclim#util#GrabUri()

    " if grabbing a relative url, remove any anchor info or query parameters
    let file = substitute(file, '[#?].*', '', '')
  endif

  " Step 1: Find in current project.
  if eclim#project#IsCurrentFileInProject(0)
    let projectDir = eclim#project#GetCurrentProjectRoot()
    call eclim#util#Echo('Searching current project: ' . projectDir . ' ...')
    let results = eclim#common#FindInPath(file, projectDir)
  endif

  " Step 2: Find relative to current file.
  if len(results) == 0
    let dir = expand('%:p:h')
    call eclim#util#Echo('Searching current file path: ' . dir . ' ...')
    let results = eclim#common#FindInPath(file, dir)
  endif

  " Step 3: Find in other projects.
  if len(results) == 0
    let currentProjectDir = eclim#project#GetCurrentProjectRoot()
    let projectDirs = eclim#project#GetProjectDirs()
    for dir in projectDirs
      if dir != currentProjectDir
        call eclim#util#Echo('Searching project: ' . dir . ' ...')
        let results += eclim#common#FindInPath(file, dir)
      endif
    endfor
  endif

  let result = ''

  " One result.
  if len(results) == 1
    let result = results[0]

  " More than one result.
  elseif len(results) > 1
    let response = eclim#util#PromptList
      \ ("Multiple results, choose the file to open", results, g:EclimInfoHighlight)
    if response == -1
      return
    endif

    let result = results[response]

  " No results
  else
    call eclim#util#Echo('Unable to locate file named "' . file . '".')
    return
  endif

  silent exec a:command . ' ' . escape(eclim#util#Simplify(result), ' ')
  call eclim#util#Echo(' ')
endfunction " }}}

" OpenRelative(command, arg) {{{
" Open one or more relative files.
function eclim#common#OpenRelative (command, arg)
  if a:arg =~ '\*' && a:command == 'edit'
    call eclim#util#EchoError(':EditRelative does not support wildcard characters.')
    return
  endif

  let dir = expand('%:p:h')
  let files = eclim#common#GetFiles(dir, a:arg)
  for file in files
    exec a:command . ' ' . escape(eclim#util#Simplify(file), ' ')
  endfor
endfunction " }}}

" OpenFiles(arg) {{{
" Opens one or more files using the supplied command.
function eclim#common#OpenFiles (command, arg)
  let files = eclim#common#GetFiles('', a:arg)
  for file in files
    exec a:command . ' ' . escape(eclim#util#Simplify(file), ' ')
  endfor
endfunction " }}}

" SwapTypedArguments() {{{
" Swaps typed method declaration arguments.
function! eclim#common#SwapTypedArguments ()
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
function! eclim#common#SwapWords ()
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
function! eclim#common#CommandCompleteRelative (argLead, cmdLine, cursorPos)
  let dir = substitute(expand('%:p:h'), '\', '/', 'g')

  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseArgs(cmdLine)
  let argLead = len(args) > 1 ? args[len(args) - 1] : ""

  let results = split(eclim#util#Glob(dir . '/' . argLead . '*'), '\n')
  call map(results, "substitute(v:val, '\\', '/', 'g')")
  call map(results, 'isdirectory(v:val) ? v:val . "/" : v:val')
  call map(results, 'substitute(v:val, dir, "", "")')
  call map(results, 'substitute(v:val, "^\\(/\\|\\\\\\)", "", "g")')
  call map(results, "substitute(v:val, ' ', '\\\\ ', 'g')")

  return eclim#util#ParseCommandCompletionResults(argLead, results)
endfunction " }}}

" vim:ft=vim:fdm=marker
