" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Shared functions for java unit testing.
"
" License:
"
" Copyright (C) 2005 - 2010  Eric Van Dewoestine
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

" Script Variables {{{
let s:command_src_find = '-command java_src_find -p "<project>" -c "<classname>"'

let s:entry_match{'junit'} = 'Tests run:'
let s:entry_match{'testng'} = 'eclim testng:'

let s:entry_text_replace{'junit'} = '.*[junit] '
let s:entry_text_with{'junit'} = ''

let s:entry_text_replace{'testng'} = '.*eclim testng: .\{-}:'
let s:entry_text_with{'testng'} = ''
" }}}

" ResolveQuickfixResults(framework) {{{
" Invoked after a :make to resolve any junit results in the quickfix entries.
function! eclim#java#test#ResolveQuickfixResults(framework)
  let entries = getqflist()
  let newentries = []
  for entry in entries
    let filename = bufname(entry.bufnr)
    let text = entry.text
    if entry.text =~ s:entry_match{a:framework}
      let filename = fnamemodify(filename, ':t')
      let text = substitute(text,
        \ s:entry_text_replace{a:framework}, s:entry_text_with{a:framework}, '')

      let project = eclim#project#util#GetCurrentProjectName()
      let command = s:command_src_find
      let command = substitute(command, '<project>', project, '')
      let command = substitute(command, '<classname>', filename, '')
      let filename = eclim#ExecuteEclim(command)
      if filename == ''
        " file not found.
        continue
      endif
    elseif !filereadable(filename)
      continue
    endif

    let newentry = {
        \ 'filename': filename,
        \ 'lnum': entry.lnum,
        \ 'col': entry.col,
        \ 'text': text
      \ }
    call add(newentries, newentry)
  endfor

  call setqflist(newentries, 'r')
endfunction " }}}

" GetTestSrcDir(type) {{{
" Where type is 'junit', etc.
function! eclim#java#test#GetTestSrcDir(type)
  let setting = "org.eclim.java." . a:type . ".src_dir"
  let path = eclim#project#util#GetProjectSetting(setting)
  if type(path) == 0
    return
  endif

  let root = eclim#project#util#GetCurrentProjectRoot()
  let path = substitute(path, '<project>', root, '')
  let path = path !~ '/$' ? path . '/' : path
  return path
endfunction " }}}

" CommandCompleteTest(type, argLead, cmdLine, cursorPos) {{{
" Custom command completion for test cases.
function! eclim#java#test#CommandCompleteTest(type, argLead, cmdLine, cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')

  let path = eclim#java#test#GetTestSrcDir(a:type)
  if path == '' || path == '/'
    call eclim#util#EchoWarning(
      \ "Source directory setting for '" . a:type . "' not set. " .
      \ "Use :EclimSettings or :ProjectSettings to set it.")
    return []
  endif

  let partial = fnamemodify(argLead, ':t')
  let argLead = fnamemodify(argLead, ':h')
  let argLead = argLead != '' ? argLead . '/' : argLead
  let results = split(eclim#util#Globpath(path . argLead, '*'), '\n')

  call filter(results, 'v:val =~ argLead . partial')
  call filter(results, '(isdirectory(v:val) && v:val !~ "CVS") || v:val =~ "\\.java$"')
  call map(results, 'isdirectory(v:val) ? v:val . "/" : v:val')
  call map(results, 'substitute(v:val, "\\(" . path . "\\|\\.java$\\)", "", "g")')
  call map(results, 'substitute(v:val, "\\\\", "/", "g")')

  " filter out invalid package / class names.
  call filter(results, 'v:val =~ "^[[:alnum:]_/]\\+$"')

  return results
endfunction " }}}

" vim:ft=vim:fdm=marker
