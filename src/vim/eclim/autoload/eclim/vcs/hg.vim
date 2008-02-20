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

if !exists('g:eclim_vcs_hg_loaded')
  let g:eclim_vcs_hg_loaded = 1
else
  finish
endif

" GetAnnotations (file, revision) {{{
function! eclim#vcs#hg#GetAnnotations (file, revision)
  let cmd = 'annotate -udn'
  if a:revision != ''
    let cmd .= ' -r ' . a:revision
  endif
  let result = eclim#vcs#hg#Hg(cmd . ' "' . a:file . '"')
  if result == '0'
    return
  endif

  let annotations = split(result, '\n')
  call map(annotations,
      \ "substitute(v:val, '^\\(.\\{-}\\)\\s\\([0-9]\\+\\)\\s\\(.\\{-}\\):\\s.*', '\\2 (\\3) \\1', '')")

  return annotations
endfunction " }}}

" GetPath(dir, file) {{{
function eclim#vcs#hg#GetPath (dir, file)
  let root = eclim#vcs#hg#Hg('root')
  if root == '0'
    return
  endif
  let root = fnamemodify(substitute(root, '\n', '', ''), ':h')
  return substitute(a:dir, root, '', '')
endfunction " }}}

" GetPreviousRevision() {{{
function eclim#vcs#hg#GetPreviousRevision ()
  let log = eclim#vcs#hg#Hg('log -q --limit=2 "' . expand('%:t') . '"')
  if log == '0'
    return
  endif
  let revisions = split(log, '\n')
  return len(revisions) > 1 ? revisions[1] : 0
endfunction " }}}

" GetRevision(url) {{{
function eclim#vcs#hg#GetRevision (url)
  let log = eclim#vcs#hg#Hg('log -q --limit=1 "' . a:url . '"')
  if log == '0'
    return
  endif
  return substitute(log, '\n', '', '')
endfunction " }}}

" GetRevisions() {{{
function eclim#vcs#hg#GetRevisions ()
  let log = eclim#vcs#hg#Hg('log -q "' . expand('%:t') . '"')
  if log == '0'
    return
  endif
  return split(log, '\n')
endfunction " }}}

" Info() {{{
function eclim#vcs#hg#Info ()
  let result = eclim#vcs#hg#Hg('log --limit 1 "' . expand('%:t') . '"')
  if result == '0'
    return
  endif
  call eclim#util#Echo(result)
endfunction " }}}

" Hg(args) {{{
" Executes 'hg' with the supplied args.
function eclim#vcs#hg#Hg (args)
  return eclim#vcs#util#Vcs('hg', a:args)
endfunction " }}}

" vim:ft=vim:fdm=marker
