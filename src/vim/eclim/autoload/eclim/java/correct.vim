" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/correct.html
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

" define Correction group based on Normal.
hi link Correction Normal
hi Correction gui=underline,bold term=underline,bold cterm=underline,bold

" Script Varables {{{
  let s:command_correct = '-filter vim -command java_correct ' .
    \ '-p "<project>" -f "<file>" -l <line> -o <offset>'
  let s:command_correct_apply = s:command_correct . ' -a <apply>'
" }}}

" Correct() {{{
function! eclim#java#correct#Correct ()
  if !eclim#project#IsCurrentFileInProject()
    return
  endif

  call eclim#java#util#SilentUpdate()

  let filename = eclim#java#util#GetFilename()
  let project = eclim#project#GetCurrentProjectName()

  let command = s:command_correct
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', filename, '')
  let command = substitute(command, '<line>', line('.'), '')
  let command = substitute(command, '<offset>', eclim#util#GetCharacterOffset(), '')

  let window_name = filename . "_correct"
  let filename = expand('%:p')
  call eclim#util#TempWindowClear(window_name)

  let results = split(eclim#ExecuteEclim(command), '\n')

  " error executing the command.
  if len(results) == 1 && results[0] == '0'
    return

  " no error on the current line
  elseif len(results) == 1
    call eclim#util#Echo(results[0])
    return

  " no correction proposals found.
  elseif len(results) == 0
    call eclim#util#EchoInfo('No Suggestions')
    return
  endif

  call eclim#util#TempWindow(window_name, results)

  setlocal ft=java

  "exec "syntax match Normal /" . escape(getline(1), '^$/\') . "/"
  syntax match Correction /^[0-9]\+\.[0-9]\+:.*/

  nnoremap <silent> <buffer> <cr>
    \ :call eclim#java#correct#CorrectApply()<cr>

  redraw | echo ""
endfunction " }}}

" CorrectApply() {{{
function! eclim#java#correct#CorrectApply ()
  let line = getline('.')
  if line =~ '^[0-9]\+\.[0-9]\+:'
    let winnr = bufwinnr('%')
    let name = substitute(expand('%:p'), '_correct$', '', '')
    let file_winnr = bufwinnr(bufnr(b:filename))
    if file_winnr != -1
      let filename = b:filename
      exec file_winnr . "winc w"
      call eclim#java#util#SilentUpdate()

      let index = substitute(line, '^\([0-9]\+\)\..*', '\1', '')

      let project = eclim#project#GetCurrentProjectName()
      let command = s:command_correct_apply
      let command = substitute(command, '<project>', project, '')
      let command = substitute(command, '<file>', eclim#java#util#GetFilename(), '')
      let command = substitute(command, '<line>', line('.'), '')
      let command = substitute(command, '<offset>', eclim#util#GetCharacterOffset(), '')
      let command = substitute(command, '<apply>', index, '')

      let content = split(eclim#ExecuteEclim(command), '\n')

      let line = line('.')
      let col = col('.')

      let saved_reg = @"
      1,$delete
      call append(1, content)
      1delete
      let @" = saved_reg

      call cursor(line, col)
      update

      exec winnr . "winc w"
      close
    else
      call eclim#util#EchoError(name . ' no longer found in an open window.')
    endif
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
