" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/python/definition.html
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

" Global Varables {{{
  if !exists("g:EclimPythonSearchSingleResult")
    " possible values ('split', 'edit', 'lopen')
    let g:EclimPythonSearchSingleResult = "split"
  endif
" }}}

" Script Varables {{{
  let s:find_command =
    \ '-command python_find_definition -p "<project>" -f "<file>" ' .
    \ '-o <offset> -e <encoding>'
" }}}

" Find() {{{
function eclim#python#definition#Find ()
  " update the file before vim makes any changes.
  call eclim#util#ExecWithoutAutocmds('silent update')

  let offset = eclim#util#GetCharacterOffset()
  let project = eclim#project#util#GetCurrentProjectName()
  let filename = eclim#project#util#GetProjectRelativeFilePath(expand('%:p'))

  let command = s:find_command
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', filename, '')
  let command = substitute(command, '<offset>', offset, '')
  let command = substitute(command, '<encoding>', &fileencoding, '')

  let results = split(eclim#ExecuteEclim(command), '\n')
  if len(results) == 1 && results[0] == '0'
    return
  endif
  if !empty(results)
    " filter out pydev output which occurs on first invocation.
    call filter(results, 'v:val !~ "\\*sys-package-mgr\\*"')
    call eclim#util#SetLocationList(eclim#util#ParseLocationEntries(results))
    " if only one result and it's for the current file, just jump to it.
    " note: on windows the expand result must be escaped
    if len(results) == 1 && results[0] =~ escape(expand('%:p'), '\') . '|'
      if results[0] !~ '|1 col 1|'
        lfirst
      endif

    " single result in another file.
    elseif len(results) == 1 && g:EclimPythonSearchSingleResult != "lopen"
      let entry = getloclist(0)[0]
      exec g:EclimPythonSearchSingleResult . ' ' . bufname(entry.bufnr)

      call cursor(entry.lnum, entry.col)
    else
      lopen
    endif
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
