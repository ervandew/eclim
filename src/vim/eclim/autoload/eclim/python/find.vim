" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/python/find.html
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

" FindDefinition() {{{
function eclim#python#find#FindDefinition ()
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  " update the file before vim makes any changes.
  call eclim#util#ExecWithoutAutocmds('silent update')

  let offset = eclim#util#GetOffset()
  let project = eclim#project#util#GetCurrentProjectRoot()
  let filename = eclim#project#util#GetProjectRelativeFilePath(expand('%:p'))

  let result = eclim#python#rope#FindDefinition(project, filename, offset)

  if result != ''
    call eclim#util#SetLocationList(eclim#util#ParseLocationEntries([result]))
    " if only one result and it's for the current file, just jump to it.
    " note: on windows the expand result must be escaped
    if result =~ escape(expand('%:p'), '\') . '|'
      if result !~ '|1 col 1|'
        lfirst
      endif

    " single result in another file.
    elseif g:EclimPythonSearchSingleResult != "lopen"
      let entry = getloclist(0)[0]
      exec g:EclimPythonSearchSingleResult . ' ' . bufname(entry.bufnr)

      call cursor(entry.lnum, entry.col)
    else
      lopen
    endif
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
