" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/common/vcs.html
"
" License:
"
" Copyright (c) 2005 - 2008
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

if !exists('g:eclim_vcs_svn_loaded')
  let g:eclim_vcs_svn_loaded = 1
else
  finish
endif

" GetAnnotations (file, revision) {{{
function! eclim#vcs#svn#GetAnnotations (file, revision)
  let cmd = 'annotate -v'
  if a:revision != ''
    let cmd .= ' -r ' . a:revision
  endif
  let result = eclim#vcs#svn#Svn(cmd . ' "' . a:file . '"')
  if result == '0'
    return
  endif

  let annotations = split(result, '\n')
  call map(annotations,
      \ "substitute(v:val, '^\\s*\\([0-9]\\+\\)\\s*\\(.\\{-}\\)\\s\\+.\\{-}\\s\\+\\(.\\{-}\\)\\s\\+.\\{-}(\\(.\\{-}\\)).*', '\\1 (\\4 \\3) \\2', '')")

  return annotations
endfunction " }}}

" GetPath(dir, file) {{{
function eclim#vcs#svn#GetPath (dir, file)
  let url = eclim#vcs#svn#GetUrl(a:dir, a:file)
  let repos = eclim#vcs#svn#GetReposUrl(a:dir)
  return substitute(url, repos, '', '')
endfunction " }}}

" GetPreviousRevision() {{{
function eclim#vcs#svn#GetPreviousRevision ()
  let log = eclim#vcs#svn#Svn('log -q --limit 2 "' . expand('%:t') . '"')
  if log == '0'
    return
  endif
  let lines = split(log, '\n')
  if len(lines) == 5 && lines[1] =~ '^r[0-9]\+' && lines[3] =~ '^r[0-9]\+'
    return substitute(lines[3], '^r\([0-9]\+\)\s.*', '\1', '')
  endif

  return
endfunction " }}}

" GetRevision(url) {{{
function eclim#vcs#svn#GetRevision (url)
  let info = eclim#vcs#svn#Svn('info "' . a:url . '"')
  if info == '0'
    return
  endif
  let pattern = '.*Last Changed Rev:\s*\([0-9]\+\)\s*.*'
  if info =~ pattern
    return substitute(info, pattern, '\1', '')
  endif

  return
endfunction " }}}

" GetRevisions() {{{
function eclim#vcs#svn#GetRevisions ()
  let log = eclim#vcs#svn#Svn('log -q "' . expand('%:t') . '"')
  if log == '0'
    return
  endif
  let lines = split(log, '\n')
  call filter(lines, 'v:val =~ "^r[0-9]\\+\\s.*"')
  call map(lines, 'substitute(v:val, "^r\\([0-9]\\+\\)\\s.*", "\\1", "")')
  return lines
endfunction " }}}

" GetReposUrl(dir) {{{
" Gets the repository root url for the repository backing the supplied dir.
" Ex. http://svn.eclim.sf.net/
function eclim#vcs#svn#GetReposUrl (dir)
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

  return repos
endfunction " }}}

" GetUrl(dir, file) {{{
function eclim#vcs#svn#GetUrl (dir, file)
  let info = eclim#vcs#svn#Svn('info  "' . a:file . '"')
  if info == '0'
    return
  endif
  return substitute(info, '.*\nURL:\s*\(.\{-}\)\s*\n.*', '\1', '')
endfunction " }}}

" Info() {{{
function eclim#vcs#svn#Info ()
  let result = eclim#vcs#svn#Svn('info "' . expand('%:t') . '"')
  if result == '0'
    return
  endif
  let info = split(result, "\n")
  call filter(info, "v:val =~ '^\\(Last\\|URL\\)'")
  call eclim#util#Echo(join(info, "\n"))
endfunction " }}}

" Svn(args) {{{
" Executes 'svn' with the supplied args.
function eclim#vcs#svn#Svn (args)
  return eclim#vcs#util#Vcs('svn', a:args)
endfunction " }}}

" vim:ft=vim:fdm=marker
