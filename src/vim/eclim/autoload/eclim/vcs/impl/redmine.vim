" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/common/vcs.html
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

if !exists('g:eclim_vcs_redmine_loaded')
  let g:eclim_vcs_redmine_loaded = 1
else
  finish
endif

" GetLogUrl(root, file, args) {{{
function eclim#vcs#impl#redmine#GetLogUrl(root, file, args)
  let root = substitute(a:root, '<cmd>', 'changes', '')
  return root . '/' . a:file . '?rev=' . split(a:args[0], ':')[1]
endfunction " }}}

" GetChangeSetUrl(root, file, args) {{{
function eclim#vcs#impl#redmine#GetChangeSetUrl(root, file, args)
" redmine uses a local revision number... how do we handle that?
  let root = substitute(a:root, '<cmd>', 'revision', '')
  return root . '?rev=' . split(a:args[0], ':')[1]
endfunction " }}}

" GetAnnotateUrl(root, file, args) {{{
function eclim#vcs#impl#redmine#GetAnnotateUrl(root, file, args)
" redmine uses a local revision number... how do we handle that?
  let root = substitute(a:root, '<cmd>', 'annotate', '')
  return root . '/' . a:file . '?rev=' . split(a:args[0], ':')[1]
endfunction " }}}

" GetDiffUrl(root, file, args) {{{
function eclim#vcs#impl#redmine#GetDiffUrl(root, file, args)
" redmine uses a local revision number... how do we handle that?
  let root = substitute(a:root, '<cmd>', 'diff', '')
  let r1 = split(a:args[0], ':')[1]
  let r2 = split(a:args[1], ':')[1]
  return root . '/' . a:file '?rev=' . r1 . '&rev_to' . r2
endfunction " }}}

" vim:ft=vim:fdm=marker
