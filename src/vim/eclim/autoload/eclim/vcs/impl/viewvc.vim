" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/common/vcs.html
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

if !exists('g:eclim_vcs_viewvc_loaded')
  let g:eclim_vcs_viewvc_loaded = 1
else
  finish
endif

" GetLogUrl(root, file, args) {{{
function eclim#vcs#impl#viewvc#GetLogUrl(root, file, args)
  return a:root . '/' . a:file . '?view=log'
endfunction " }}}

" GetChangeSetUrl(root, file, args) {{{
function eclim#vcs#impl#viewvc#GetChangeSetUrl(root, file, args)
  return a:root . '/' . a:file . '?view=rev&revision=' . a:args[0]
endfunction " }}}

" GetAnnotateUrl(root, file, args) {{{
function eclim#vcs#impl#viewvc#GetAnnotateUrl(root, file, args)
  return a:root . '/' . a:file . '?annotate=' . a:args[0]
endfunction " }}}

" GetDiffUrl(root, file, args) {{{
function eclim#vcs#impl#viewvc#GetDiffUrl(root, file, args)
  return a:root . '/' . a:file . '?r1=' . a:args[0] . '&r2=' . a:args[1]
endfunction " }}}

" vim:ft=vim:fdm=marker
