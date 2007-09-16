" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Functions for working with version control systems.
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

" Log() {{{
function! eclim#vcs#log#Log ()
  let dir = expand('%:p:h')
  let cwd = getcwd()
  exec 'lcd ' . dir

  try
    let file = expand('%:p:t')
    if isdirectory(dir . '/CVS')
      let type = 'cvs'
      let result = system('cvs log -l "' . file . '"')
      let log = s:ParseCvsLog(split(result, '\n'))
    elseif isdirectory(dir . '/.svn')
      let type = 'svn'
      let result = system('svn log "' . file . '"')
      let log = s:ParseSvnLog(split(result, '\n'))
    else
      call eclim#util#EchoError('Current file is not under cvs or svn version control.')
      return
    endif
  finally
    exec 'lcd ' . cwd
  endtry
  let lines = []
  let index = 0
  for entry in log
    let index += 1
    call add(lines, '------------------------------------------')
    if type == 'cvs'
      call add(lines, 'Revision: ' . entry.revision . ' |view| |annotate|')
    else
      call add(lines, 'Revision: |' . entry.revision . '| |view| |annotate|')
    endif
    call add(lines, 'Modified: ' . entry.date . ' by ' . entry.author)
    if index < len(log)
      call add(lines, 'Diff to |previous ' . log[index].revision . '|')
    endif
    call add(lines, '')
    let lines += entry.comment
  endfor

  call eclim#util#TempWindow('[vcs_log]', lines)
  let b:vcs_type = type
  " TODO: commands, syntax, formatting
  "   changeset: svn log -vr <revision>
endfunction " }}}

" s:ParseCvsLog() {{{
" Parse the cvs log.
function! s:ParseCvsLog (lines)
  let log = []
  let section = 'head'
  for line in a:lines
    if line =~ '^=\+$'
      continue
    elseif line =~ '^-\+$'
      let section = 'info'
      let entry = {'comment': []}
      call add(log, entry)
      continue
    elseif section == 'head'
      continue
    elseif section == 'info'
      if line =~ '^revision'
        let entry['revision'] = substitute(line, 'revision\s\(.*\)', '\1', '')
      elseif line =~ '^date:'
        let entry['author'] = substitute(line, '.*author:\s*\(.\{-}\);.*', '\1', '')
        let entry['date'] = substitute(line, '.*date:\s*\(.\{-}\);.*', '\1', '')
        let entry['date'] = substitute(entry.date, '/', '-', 'g')
        let section = 'comment'
      endif
    elseif section == 'comment'
      call add(entry.comment, line)
    endif
  endfor
  return log
endfunction " }}}

" s:ParseSvnLog() {{{
" Parse the svn log.
function! s:ParseSvnLog (lines)
  let log = []
  let section = 'head'
  let index = 0
  for line in a:lines
    let index += 1
    if line =~ '^-\+$' && index == len(a:lines)
      " get rid of empty line at the end of last entry's comment
      if exists('l:entry') && entry.comment[-1] =~ '^\s*$'
        let entry.comment = entry.comment[:-2]
      endif
      continue
    elseif line =~ '^-\+$'
      " get rid of empty line at the end of entry's comment
      if exists('l:entry') && entry.comment[-1] =~ '^\s*$'
        let entry.comment = entry.comment[:-2]
      endif
      let section = 'info'
      let entry = {'comment': []}
      call add(log, entry)
      continue
    elseif section == 'head'
      continue
    elseif section == 'info'
      let entry['revision'] = substitute(line, '^r\(\w\+\).*', '\1', '')
      let entry['author'] = substitute(line, '.\{-}|\s*\(\w\+\)\s*|.*', '\1', '')
      let entry['date'] = substitute(line, '.\{-}|.\{-}|\s*\(.\{-}\)\s\+[+-].\{-}|.*', '\1', '')
      let section = 'comment'
    elseif section == 'comment'
      " ignore leading blank line of comment section
      if len(entry.comment) == 0 && line =~ '^\s*$'
        continue
      endif
      call add(entry.comment, line)
    endif
  endfor
  return log
endfunction " }}}

" vim:ft=vim:fdm=marker
