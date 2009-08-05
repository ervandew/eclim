" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/refactor.html
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

" Script Varables {{{
  let s:command_rename = '-command java_refactor_rename ' .
    \ '-p "<project>" -f "<file>" -o <offset> -e <encoding> -l <length> -n <name>'
  let s:command_undoredo = '-command java_refactor_<operation>'
" }}}

" Rename(name) {{{
function eclim#java#refactor#Rename(name)
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let element = expand('<cword>')
  if !eclim#java#util#IsValidIdentifier(element)
    call eclim#util#EchoError
      \ ("Element under the cursor is not a valid java identifier.")
    return
  endif

  " update the file before vim makes any changes.
  call eclim#java#util#SilentUpdate()
  wall

  let project = eclim#project#util#GetCurrentProjectName()
  let filename = eclim#java#util#GetFilename()
  let position = eclim#util#GetCurrentElementPosition()
  let offset = substitute(position, '\(.*\);\(.*\)', '\1', '')
  let length = substitute(position, '\(.*\);\(.*\)', '\2', '')

  let command = s:command_rename
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', filename, '')
  let command = substitute(command, '<offset>', offset, '')
  let command = substitute(command, '<length>', length, '')
  let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')
  let command = substitute(command, '<name>', a:name, '')

  let result = eclim#ExecuteEclim(command)
  if result == "0"
    return
  endif

  if result !~ '^files:'
    call eclim#util#Echo(result)
    return
  endif

  " handle rename of the current file type
  if element == eclim#java#util#GetClassname() && !filereadable(expand('%'))
    let file = expand('%:h') . '/' . a:name . '.java'
    if file =~ '\./'
      let file = file[2:]
    endif
    let bufnr = bufnr('%')
    exec 'edit ' . escape(eclim#util#Simplify(file), ' ')
    exec 'bdelete ' . bufnr
  endif

  let files = split(result, "\n")[1:]
  let curwin = winnr()
  try
    for file in files
      let winnr = bufwinnr(file)
      if winnr > -1
        exec winnr . 'winc w'
        edit
      endif
    endfor
  finally
    exec curwin . 'winc w'
  endtry
endfunction " }}}

" UndoRedo(operation, peek) {{{
function eclim#java#refactor#UndoRedo(operation, peek)
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  " update the file before vim makes any changes.
  call eclim#java#util#SilentUpdate()
  wall

  let command = s:command_undoredo
  let command = substitute(command, '<operation>', a:operation, '')
  if a:peek
    let command .= ' -p'
  endif

  let result = eclim#ExecuteEclim(command)
  if result == "0"
    return
  endif

  call eclim#util#Echo(result)
endfunction " }}}

" vim:ft=vim:fdm=marker
