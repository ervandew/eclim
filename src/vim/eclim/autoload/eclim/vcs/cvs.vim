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

if !exists('g:eclim_vcs_cvs_loaded')
  let g:eclim_vcs_svn_loaded = 1
else
  finish
endif

" GetAnnotations (file, revision) {{{
function! eclim#vcs#cvs#GetAnnotations (file, revision)
  let cmd = 'annotate'
  if a:revision != ''
    let cmd .= ' -r ' . a:revision
  endif

  let result = eclim#vcs#cvs#Cvs(cmd . ' "' . a:file . '"')
  let annotations = split(result, '\n')
  call filter(annotations, 'v:val =~ "^[0-9]"')
  call map(annotations,
    \ "substitute(v:val, '^\\s*\\([0-9.]\\+\\)\\s*(\\(.\\{-}\\)\\s\\+\\(.\\{-}\\)).*', '\\1 (\\3) \\2', '')")

  if v:shell_error
    call eclim#util#EchoError(result)
    return
  endif

  return annotations
endfunction " }}}

" GetPath(dir, file) {{{
function eclim#vcs#svn#GetPath (dir, file)
  silent exec 'sview ' . escape(a:dir . '/CVS/Repository', ' ')
  setlocal noswapfile
  setlocal bufhidden=delete
  let path = '/' . getline(1) . '/' . a:file
  silent close
  return path
endfunction " }}}

" GetPreviousRevision() {{{
function eclim#vcs#cvs#GetPreviousRevision ()
  let log = eclim#vcs#cvs#Cvs('log ' . expand('%:t'))
  let lines = split(log, '\n')
  call filter(lines, 'v:val =~ "^revision [0-9.]\\+\\s*$"')
  if len(lines) >= 2
    return substitute(lines[1], '^revision \([0-9.]\+\)\s*.*', '\1', '')
  endif

  return
endfunction " }}}

" GetRevision(file) {{{
function eclim#vcs#cvs#GetRevision (file)
  let status = eclim#vcs#cvs#Cvs('status ' . a:file)
  let pattern = '.*Working revision:\s*\([0-9.]\+\)\s*.*'
  if status =~ pattern
    return substitute(status, pattern, '\1', '')
  endif

  return
endfunction " }}}

" GetRevisions() {{{
function eclim#vcs#cvs#GetRevisions ()
  let log = eclim#vcs#cvs#Cvs('log ' . expand('%:t'))
  let lines = split(log, '\n')
  call filter(lines, 'v:val =~ "^revision [0-9.]\\+\\s*$"')
  call map(lines, 'substitute(v:val, "^revision \\([0-9.]\\+\\)\\s*$", "\\1", "")')
  return lines
endfunction " }}}

" Info() {{{
" Retrieves and echos info on the current file.
function eclim#vcs#util#Info ()
  let result = eclim#vcs#cvs#Cvs('status "' . expand('%:t') . '"')
  if result == '0'
    return
  endif
  let info = split(result, "\n")[1:]
  call map(info, "substitute(v:val, '^\\s\\+', '', '')")
  call map(info, "substitute(v:val, '\\t', ' ', 'g')")
  let info[0] = substitute(info[0], '.\{-}\(Status:.*\)', '\1', '')
  call eclim#util#Echo(join(info, "\n"))
endfunction " }}}

" Cvs(args) {{{
" Executes 'cvs' with the supplied args.
function eclim#vcs#cvs#Cvs (args)
  return eclim#vcs#util#Vcs('cvs', a:args)
endfunction " }}}

" vim:ft=vim:fdm=marker
