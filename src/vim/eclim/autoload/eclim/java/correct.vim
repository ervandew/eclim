" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/correct.html
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

" define Correction group based on Normal.
hi link Correction Normal
hi Correction gui=underline,bold term=underline,bold cterm=underline,bold

" Script Varables {{{
  let s:command_correct =
    \ '-command java_correct -p "<project>" -f "<file>" ' .
    \ '-l <line> -o <offset> -e <encoding>'
  let s:command_correct_apply = s:command_correct . ' -a <apply>'
" }}}

" Correct() {{{
function! eclim#java#correct#Correct()
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  call eclim#java#util#SilentUpdate()

  let filename = eclim#java#util#GetFilename()
  let project = eclim#project#util#GetCurrentProjectName()

  let command = s:command_correct
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', filename, '')
  let command = substitute(command, '<line>', line('.'), '')
  let command = substitute(command, '<offset>', eclim#util#GetOffset(), '')
  let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')

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

  let b:filename = filename
  augroup temp_window
    autocmd! BufWinLeave <buffer>
    call eclim#util#GoToBufferWindowRegister(filename)
  augroup END

  setlocal ft=java

  "exec "syntax match Normal /" . escape(getline(1), '^$/\') . "/"
  syntax match Correction /^[0-9]\+\.[0-9]\+:.*/

  nnoremap <silent> <buffer> <cr>
    \ :call eclim#java#correct#CorrectApply()<cr>

  redraw | echo ""
endfunction " }}}

" CorrectApply() {{{
function! eclim#java#correct#CorrectApply()
  let line = getline('.')
  if line =~ '^[0-9]\+\.[0-9]\+:'
    let winnr = bufwinnr('%')
    let name = substitute(expand('%:p'), '_correct$', '', '')
    let file_winnr = bufwinnr(bufnr('^' . b:filename))
    if file_winnr != -1
      let filename = b:filename
      exec file_winnr . "winc w"
      call eclim#java#util#SilentUpdate()

      let index = substitute(line, '^\([0-9]\+\)\..*', '\1', '')

      let project = eclim#project#util#GetCurrentProjectName()
      let command = s:command_correct_apply
      let command = substitute(command, '<project>', project, '')
      let command = substitute(command, '<file>', eclim#java#util#GetFilename(), '')
      let command = substitute(command, '<line>', line('.'), '')
      let command = substitute(command, '<offset>', eclim#util#GetOffset(), '')
      let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')
      let command = substitute(command, '<apply>', index, '')

      let content = split(eclim#ExecuteEclim(command), '\n')

      if len(content) == 1 && content[0] == '0'
        return
      endif

      let pos = getpos('.')

      1,$delete _
      call append(1, content)
      1,1delete _

      call setpos('.', pos)
      update

      exec winnr . "winc w"
      close
    else
      call eclim#util#EchoError(name . ' no longer found in an open window.')
    endif
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
