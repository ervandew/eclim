" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/python/find.html
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

" Global Varables {{{
  if !exists("g:EclimPythonSearchSingleResult")
    " possible values ('split', 'edit', 'lopen')
    let g:EclimPythonSearchSingleResult = "split"
  endif
" }}}

" Find(context) {{{
function eclim#python#search#Find(context)
  if !eclim#project#util#IsCurrentFileInProject() || !filereadable(expand('%'))
    return
  endif

  " update the file before vim makes any changes.
  call eclim#util#ExecWithoutAutocmds('silent update')

  let offset = eclim#python#rope#GetOffset()
  let encoding = eclim#util#GetEncoding()
  let project = eclim#project#util#GetCurrentProjectRoot()
  let filename = eclim#project#util#GetProjectRelativeFilePath(expand('%:p'))

  let results =
    \ eclim#python#rope#Find(project, filename, offset, encoding, a:context)
  if type(results) == 0 && results == 0
    call eclim#util#SetLocationList([])
    return
  endif

  if !empty(results)
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
      call eclim#util#GoToBufferWindowOrOpen(
        \ bufname(entry.bufnr), g:EclimPythonSearchSingleResult)
      call eclim#util#SetLocationList(eclim#util#ParseLocationEntries(results))
      call eclim#display#signs#Update()

      call cursor(entry.lnum, entry.col)
    else
      lopen
    endif
  else
    call eclim#util#EchoInfo("Element not found.")
  endif
endfunction " }}}

" SearchContext() {{{
" Executes a contextual search.
function! eclim#python#search#SearchContext()
  if getline('.')[col('.') - 1] == '$'
    call cursor(line('.'), col('.') + 1)
    let cnum = eclim#util#GetCurrentElementColumn()
    call cursor(line('.'), col('.') - 1)
  else
    let cnum = eclim#util#GetCurrentElementColumn()
  endif

  if getline('.') =~ '\<\(class\|def\)\s\+\%' . cnum . 'c'
    call eclim#python#search#Find('occurrences')
    return
  endif

  call eclim#python#search#Find('definition')

endfunction " }}}

" vim:ft=vim:fdm=marker
