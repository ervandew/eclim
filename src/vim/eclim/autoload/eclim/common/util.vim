" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Various functions that are useful in and out of eclim.
"
" License:
"
" Copyright (C) 2005 - 2009  Eric Van Dewoestine
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

" DiffLastSaved() {{{
" Diff a modified file with the last saved version.
function! eclim#common#util#DiffLastSaved()
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
function! eclim#common#util#FindInPath(file, path)
  let results = split(eclim#util#Globpath(a:path . '/**', a:file, 1), '\n')
  "let results = split(eclim#util#Globpath(a:path, a:file, 1), '\n') + results
  call map(results, "fnamemodify(v:val, ':p')")
  return results
endfunction " }}}

" GetFiles(dir, arg) {{{
" Parses the supplied arg to obtain a list of files based in the supplied
" directory.
function eclim#common#util#GetFiles(dir, arg)
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
function! eclim#common#util#GrepRelative(command, args)
  let rel_dir = expand('%:p:h')
  let cwd = getcwd()
  try
    silent exec 'lcd ' . escape(rel_dir, ' ')
    silent! exec a:command . ' ' . a:args
  finally
    silent exec 'lcd ' . escape(cwd, ' ')
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

" OpenRelative(command, arg [, open_existing]) {{{
" Open one or more relative files.
function eclim#common#util#OpenRelative(command, arg, ...)
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
function eclim#common#util#OpenFiles(command, arg)
  let files = eclim#common#util#GetFiles('', a:arg)
  for file in files
    exec a:command . ' ' . escape(eclim#util#Simplify(file), ' ')
  endfor
endfunction " }}}

" OtherWorkingCopy(project, action) {{{
" Opens the same file from another project using the supplied action
function! eclim#common#util#OtherWorkingCopy(project, action)
  let path = eclim#project#util#GetProjectRelativeFilePath(expand('%:p'))
  let project_path = s:OtherWorkingCopyPath(a:project)
  if project_path == ''
    return
  endif
  call eclim#util#GoToBufferWindowOrOpen(project_path, a:action)
endfunction " }}}

" OtherWorkingCopyDiff(project) {{{
" Diffs the current file against the same file from another project.
function! eclim#common#util#OtherWorkingCopyDiff(project)
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
    autocmd! BufWinLeave <buffer>
    call eclim#util#GoToBufferWindowRegister(b:filename)
    autocmd BufWinLeave <buffer> diffoff
  augroup END
endfunction " }}}

" s:OtherWorkingCopyPath(project) {{{
function s:OtherWorkingCopyPath(project)
  let path = eclim#project#util#GetProjectRelativeFilePath(expand('%:p'))

  let project_name = a:project
  if project_name =~ '[\\/]$'
    let project_name = project_name[:-2]
  endif

  let project = {}
  for p in eclim#project#util#GetProjects()
    if p.name == project_name
      let project = p
      break
    endif
  endfor

  if len(project) == 0
    call eclim#util#EchoWarning("Project '" . project_name . "' not found.")
    return ''
  endif
  return eclim#project#util#GetProjectRoot(project_name) . '/' . path
endfunction " }}}

" SwapTypedArguments() {{{
" Swaps typed method declaration arguments.
function! eclim#common#util#SwapTypedArguments()
  " FIXME: add validation to see if user is executing on a valid position.
  normal! w
  SwapWords
  normal! b
  SwapWords
  normal! www
  SwapWords
  normal! bb
  SwapWords
  normal! b
endfunction " }}}

" SwapWords() {{{
" Initially based on http://www.vim.org/tips/tip.php?tip_id=329
function! eclim#common#util#SwapWords()
  " save the last search pattern
  let save_search = @/

  normal! "_yiw
  s/\(\%#\w\+\)\(\_W\+\)\(\w\+\)/\3\2\1/
  exec "normal! \<C-O>"

  " restore the last search pattern
  let @/ = save_search
endfunction " }}}

" CommandCompleteRelative(argLead, cmdLine, cursorPos) {{{
" Custom command completion for relative files and directories.
function! eclim#common#util#CommandCompleteRelative(argLead, cmdLine, cursorPos)
  let dir = substitute(expand('%:p:h'), '\', '/', 'g')

  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)
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
function! eclim#common#util#CommandCompleteRelativeDirs(argLead, cmdLine, cursorPos)
  let dir = substitute(expand('%:p:h'), '\', '/', 'g')

  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)
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
