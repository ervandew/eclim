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

if !exists('g:eclim_vcs_trac_loaded')
  let g:eclim_vcs_trac_loaded = 1
else
  finish
endif

" GetLogUrl(root, file, args) {{{
function eclim#vcs#impl#trac#GetLogUrl(root, file, args)
  return a:root . '/log/' . a:file . '?verbose=on'
endfunction " }}}

" GetChangeSetUrl(root, file, args) {{{
function eclim#vcs#impl#trac#GetChangeSetUrl(root, file, args)
  return a:root . '/changeset/' . a:args[0]
endfunction " }}}

" GetAnnotateUrl(root, file, args) {{{
function eclim#vcs#impl#trac#GetAnnotateUrl(root, file, args)
  let path = substitute(a:file, '/', '%2F', 'g')
  return a:root . '/' . a:file . '?annotate=1&rev=' . a:args[0]
endfunction " }}}

" GetDiffUrl(root, file, args) {{{
function eclim#vcs#impl#trac#GetDiffUrl(root, file, args)
  let path = substitute(a:file, '/', '%2F', 'g')
  let r1 = path . '%40' . a:args[0]
  let r2 = path . '%40' . a:args[1]
  return a:root . printf('/changeset?new=%s&old=%s', r1, r2)
endfunction " }}}

" vim:ft=vim:fdm=marker
