" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
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
      autocmd!
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

" OpenRelative(command,arg) {{{
function eclim#common#OpenRelative (command, arg)
  let dir = expand('%:p:h')
  let files = split(a:arg, '[^\\]\zs\s')
  for file in files
    exec a:command . ' ' . dir. '/' . file
  endfor
endfunction " }}}

" Split(arg) {{{
function eclim#common#Split (arg)
  let files = split(a:arg, '[^\\]\zs\s')
  for file in files
    exec 'split ' . file
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
  let dir = expand('%:p:h')

  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseArgs(cmdLine)
  let argLead = len(args) > 1 ? args[len(args) - 1] : ""

  let results = split(glob(expand(dir . '/' . argLead) . '*'), '\n')
  call map(results, 'isdirectory(v:val) ? v:val . "/" : v:val')
  call map(results, 'substitute(v:val, dir, "", "")')
  call map(results, 'substitute(v:val, "^\\(/\\|\\\\\\)", "", "g")')
  call map(results, "substitute(v:val, '\\', '/', 'g')")
  call map(results, "substitute(v:val, ' ', '\\\\ ', 'g')")

  return eclim#util#ParseCommandCompletionResults(argLead, results)
endfunction " }}}

" vim:ft=vim:fdm=marker
