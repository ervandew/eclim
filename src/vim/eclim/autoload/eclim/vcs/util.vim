" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/common/vcs.html
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

" GetPath(dir, file) {{{
" Gets the repository root relative path of the specified file in the supplied
" dir.
" Ex. /trunk/src/vim/eclim/autoload/eclim/eclipse.vim
function eclim#vcs#util#GetPath (dir, file)
  let path = ''
  let cmd = winrestcmd()

  try
    if isdirectory(a:dir . '/CVS')
      silent exec 'sview ' . escape(a:dir . '/CVS/Repository', ' ')
      setlocal noswapfile
      setlocal bufhidden=delete

      let path = '/' . getline(1) . '/' . a:file

      silent close
    elseif isdirectory(a:dir . '/.svn')
      let url = eclim#vcs#util#GetSvnUrl(a:dir, a:file)
      let repos = eclim#vcs#util#GetSvnReposUrl(a:dir)

      let path = substitute(url, repos, '', '')
    endif
  finally
    silent exec cmd
  endtry

  return path
endfunction " }}}

" GetPreviousRevision() {{{
" Gets the previous revision of the current file.
function eclim#vcs#util#GetPreviousRevision ()
  let revision = '0'

  let cwd = getcwd()
  let dir = expand('%:p:h')
  exec 'lcd ' . dir
  try
    if isdirectory(dir . '/CVS')
      let log = system('cvs log ' . expand('%:t'))
      let lines = split(log, '\n')
      call filter(lines, 'v:val =~ "^revision [0-9.]\\+\\s*$"')
      if len(lines) >= 2
        let revision = substitute(lines[1], '^revision \([0-9.]\+\)\s*.*', '\1', '')
      endif
    elseif isdirectory(dir . '/.svn')
      let log = eclim#vcs#util#Svn('log -q --limit 2 "' . expand('%:t') . '"')
      if log == '0'
        return
      endif
      let lines = split(log, '\n')
      if len(lines) == 5 && lines[1] =~ '^r[0-9]\+' && lines[3] =~ '^r[0-9]\+'
        let revision = substitute(lines[3], '^r\([0-9]\+\)\s.*', '\1', '')
      endif
    endif
  finally
    exec 'lcd ' . cwd
  endtry

  return revision
endfunction " }}}

" GetRevision() {{{
" Gets the current revision of the current file.
function eclim#vcs#util#GetRevision ()
  let revision = '0'

  let cwd = getcwd()
  let dir = expand('%:p:h')
  exec 'lcd ' . dir
  try
    if isdirectory(dir . '/CVS')
      let status = system('cvs status ' . expand('%:t'))
      let pattern = '.*Working revision:\s*\([0-9.]\+\)\s*.*'
      if status =~ pattern
        let revision = substitute(status, pattern, '\1', '')
      endif
    elseif isdirectory(dir . '/.svn')
      let info = eclim#vcs#util#Svn('info "' . expand('%:t') . '"')
      if info == '0'
        return
      endif
      let pattern = '.*Last Changed Rev:\s*\([0-9]\+\)\s*.*'
      if info =~ pattern
        let revision = substitute(info, pattern, '\1', '')
      endif
    endif
  finally
    exec 'lcd ' . cwd
  endtry

  return revision
endfunction " }}}

" GetRevisions() {{{
" Gets a list of revision numbers for the current file.
function eclim#vcs#util#GetRevisions ()
  let revisions = []

  let cwd = getcwd()
  let dir = expand('%:p:h')
  exec 'lcd ' . dir
  try
    if isdirectory(dir . '/CVS')
      let log = system('cvs log ' . expand('%:t'))
      let lines = split(log, '\n')
      call filter(lines, 'v:val =~ "^revision [0-9.]\\+\\s*$"')
      call map(lines, 'substitute(v:val, "^revision \\([0-9.]\\+\\)\\s*$", "\\1", "")')
      let revisions = lines
    elseif isdirectory(dir . '/.svn')
      let log = eclim#vcs#util#Svn('log -q "' . expand('%:t') . '"')
      if log == '0'
        return
      endif
      let lines = split(log, '\n')
      call filter(lines, 'v:val =~ "^r[0-9]\\+\\s.*"')
      call map(lines, 'substitute(v:val, "^r\\([0-9]\\+\\)\\s.*", "\\1", "")')
      let revisions = lines
    endif
  finally
    exec 'lcd ' . cwd
  endtry

  return revisions
endfunction " }}}

" GetSvnReposUrl(dir) {{{
" Gets the repository root url for the repository backing the supplied dir.
" Ex. http://svn.eclim.sf.net/
function eclim#vcs#util#GetSvnReposUrl (dir)
  let repos = ''
  let cmd = winrestcmd()

  try
    if isdirectory(a:dir . '/.svn')
      silent exec 'sview ' . escape(a:dir . '/.svn/entries', ' ')
      setlocal noswapfile
      setlocal bufhidden=delete

      " xml entries format < 1.4
      if getline(1) =~ '<?xml'
        call cursor(1, 1)
        let repos = substitute(
          \ getline(search('^\s*repos=')), '^\s*repos="\(.*\)"', '\1', '')

      " entries format >= 1.4
      else
        " can't find official doc on the format, but line 6 seems to
        " always have the necessary value.
        let repos = getline(6)
      endif

      silent close

      if repos !~ '/$'
        let repos .= '/'
      endif
    endif
  finally
    silent exec cmd
  endtry

  return repos
endfunction " }}}

" GetSvnUrl(dir, file) {{{
" Gets the repository url for the specified file in the supplied dir.
" Ex. http://svn.eclim.sf.net/trunk/src/vim/eclim/autoload/eclim/eclipse.vim
function eclim#vcs#util#GetSvnUrl (dir, file)
  let url = ''
  let cmd = winrestcmd()

  try
    if isdirectory(a:dir . '/.svn')
      silent exec 'sview ' . escape(a:dir . '/.svn/entries', ' ')
      setlocal noswapfile
      setlocal bufhidden=delete

      " xml entries format < 1.4
      if getline(1) =~ '<?xml'
        call cursor(1, 1)
        let url = substitute(
          \ getline(search('^\s*url=')), '^\s*url="\(.*\)"', '\1', '')

      " entries format >= 1.4
      else
        " can't find official doc on the format, but line 5 seems to
        " always have the necessary value.
        let url = getline(5)
      endif

      let url .= '/' . a:file

      silent close
    endif
  finally
    silent exec cmd
  endtry

  return url
endfunction " }}}

" GetSvnRevision(url) {{{
" Gets the current revision for the supplied svn url.
function eclim#vcs#util#GetSvnRevision (url)
  let info = eclim#vcs#util#Svn('info "' . a:url . '"')
  if info == '0'
    return
  endif
  let pattern = '.*Last Changed Rev:\s*\([0-9]\+\)\s*.*'
  if info =~ pattern
    return substitute(info, pattern, '\1', '')
  endif
  return ''
endfunction " }}}

" GetType(dir, file) {{{
" Gets the vcs type ('cvs' or 'svn') of the supplied file in the specified
" direcctory.
function eclim#vcs#util#GetType (dir, file)
  let type = ''
  let file = a:dir != '' ? a:file : expand('%:p:t')
  let dir = a:dir != '' ? a:dir : expand('%:p:h')

  let cwd = getcwd()
  exec 'lcd ' . dir
  try
    if isdirectory(dir . '/CVS')
      let type = 'cvs'
    elseif isdirectory(dir . '/.svn')
      let type = 'svn'
    endif
  finally
    exec 'lcd ' . cwd
  endtry

  return type
endfunction " }}}

" Svn(args) {{{
" Executes 'svn' with the supplied args.
function eclim#vcs#util#Svn (args)
  if !executable('svn')
    call eclim#util#EchoError('svn executable not found in your path.')
    return
  endif

  let result = system('svn ' . a:args)
  if v:shell_error
    call eclim#util#EchoError(
      \ "Error executing svn command: svn " . a:args . "\n" . result)
    return
  endif

  return result
endfunction " }}}

" CommandCompleteRevision(argLead, cmdLine, cursorPos) {{{
" Custom command completion for revision numbers out of viewvc.
function! eclim#vcs#util#CommandCompleteRevision (argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseArgs(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]

  let revisions = eclim#vcs#util#GetRevisions()
  call filter(revisions, 'v:val =~ "^' . argLead . '"')
  return revisions
endfunction " }}}

" vim:ft=vim:fdm=marker
