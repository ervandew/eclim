" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Commands view / search eclim help files.
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

" Global Variables {{{
  let g:EclimHelpDir = g:EclimBaseDir . '/eclim/doc'

  if !filereadable(substitute(g:EclimHelpDir, '\\\s', ' ', 'g') . '/tags')
    call eclim#util#Echo('indexing eclim help files...')
    silent! exec 'helptags ' . g:EclimHelpDir
    let paths = split(glob(g:EclimHelpDir . '/**/*'), '\n')
    call filter(paths, 'isdirectory(v:val)')
    for path in paths
      silent! exec 'helptags ' . path
    endfor
    call eclim#util#Echo('eclim help files indexed')
  endif
" }}}

" Help(tag) {{{
function! eclim#help#Help(tag, link)
  let savetags = &tags
  exec 'set tags=' . escape(escape(g:EclimHelpDir, ' '), ' ') . '/**/tags'
  try
    let tag = a:tag
    if tag == '' && !a:link
      let tag = 'vim-index'
    elseif tag ==''
      let line = getline('.')
      let tag = substitute(
        \ line, '.*|\(\S\{-}\%' . col('.') . 'c\S\{-}\)|.*', '\1', '')
      if tag == line
        return
      endif
    endif

    call s:HelpWindow()
    exec 'tag ' . tag
    let w:eclim_help = 1

    " needed to ensure taglist is updated if open
    doautocmd BufEnter
  catch /^Vim\%((\a\+)\)\=:E426/
    if !exists('w:eclim_help')
      close
    endif
    call eclim#util#EchoError('Sorry no eclim help for ' . tag)
  finally
    let &tags = savetags
  endtry
endfunction " }}}

" HelpGrep() {{{
function! eclim#help#HelpGrep(args)
  exec 'vimgrep ' a:args . ' ' . g:EclimHelpDir . '/**/*.txt'
endfunction " }}}

" s:HelpWindow() {{{
function s:HelpWindow()
  let max = winnr('$')
  let index = 1
  while index <= max
    if getwinvar(index, 'eclim_help')
      exec index . 'winc w'
      return
    endif
    let index += 1
  endwhile

  below new
endfunction " }}}

" CommandComplete(argLead, cmdLine, cursorPos) {{{
function! eclim#help#CommandCompleteTag(argLead, cmdLine, cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')

  let savetags = &tags
  exec 'set tags=' . escape(escape(g:EclimHelpDir, ' '), ' ') . '/**/tags'
  try
    let results = taglist(argLead . '.*')
    call map(results, "v:val['name']")
    return results
  finally
    let &tags = savetags
  endtry
endfunction " }}}

" vim:ft=vim:fdm=marker
