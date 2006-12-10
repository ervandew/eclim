" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/python/definition.html
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

" Global Varables {{{
  if !exists("g:EclimPythonSearchSingleResult")
    " possible values ('split', 'edit', 'lopen')
    let g:EclimPythonSearchSingleResult = "split"
  endif
" }}}

" Script Varables {{{
  let s:find_command =
    \ '-command python_find_definition -p "<project>" -f "<file>" -o <offset>'
" }}}

" Find() {{{
function eclim#python#definition#Find ()
  " update the file before vim makes any changes.
  call eclim#util#ExecWithoutAutocmds('silent update')

  let offset = eclim#util#GetCharacterOffset()
  let project = eclim#project#GetCurrentProjectName()
  let filename = expand('%:p')

  let command = s:find_command
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', filename, '')
  let command = substitute(command, '<offset>', offset, '')

  let results = split(eclim#ExecuteEclim(command), '\n')
  if len(results) == 1 && results[0] == '0'
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
      exec g:EclimPythonSearchSingleResult . ' ' . bufname(entry.bufnr)

      call cursor(entry.lnum, entry.col)
    else
      lopen
    endif
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
