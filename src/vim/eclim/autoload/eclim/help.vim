" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Commands view / search eclim help files.
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

" Global Variables {{{
  let g:EclimHelpDir = g:EclimBaseDir . '/eclim/doc'

  if !filereadable(g:EclimHelpDir . '/tags')
    call eclim#util#Echo('indexing eclim help files...')
    let paths = split(glob(g:EclimHelpDir . '/**/*'), '\n')
    call filter(paths, 'isdirectory(v:val)')
    for path in paths
      silent! exec 'helptags ' . path
    endfor
    call eclim#util#Echo('eclim help files indexed')
  endif
" }}}

" Help(tag) {{{
function! eclim#help#Help (tag)
  let savetags = &tags
  exec 'set tags=' . g:EclimHelpDir . '/**/tags'
  try
    let tag = a:tag
    if tag == ''
      let line = getline('.')
      let tag = substitute(
        \ line, '.*|\(\S\{-}\%' . col('.') . 'c\S\{-}\)|.*', '\1', '')
      if tag == line
        return
      endif
    endif
    exec 'tag ' . tag
  finally
    let &tags = savetags
  endtry
endfunction " }}}

" HelpGrep() {{{
function! eclim#help#HelpGrep (args)
  exec 'vimgrep ' a:args . ' ' . g:EclimHelpDir . '/**/*.txt'
endfunction " }}}

" CommandComplete(argLead, cmdLine, cursorPos) {{{
function! eclim#help#CommandCompleteTag (argLead, cmdLine, cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')

  let savetags = &tags
  exec 'set tags=' . g:EclimHelpDir . '/**/tags'
  try
    let results = taglist(argLead . '.*')
    call map(results, "v:val['name']")
    return results
  finally
    let &tags = savetags
  endtry
endfunction " }}}

" vim:ft=vim:fdm=marker
