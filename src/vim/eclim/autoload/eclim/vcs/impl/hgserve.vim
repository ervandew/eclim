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

if !exists('g:eclim_vcs_hgserve_loaded')
  let g:eclim_vcs_hgserve_loaded = 1
else
  finish
endif

" GetLogUrl(root, file, args) {{{
function eclim#vcs#impl#hgserve#GetLogUrl(root, file, args)
  return a:root . '/log/' . split(a:args[0], ':')[1] . '/' . a:file
endfunction " }}}

" GetChangeSetUrl(root, file, args) {{{
function eclim#vcs#impl#hgserve#GetChangeSetUrl(root, file, args)
  return a:root . '/rev/' . split(a:args[0], ':')[1]
endfunction " }}}

" GetAnnotateUrl(root, file, args) {{{
function eclim#vcs#impl#hgserve#GetAnnotateUrl(root, file, args)
  return a:root . '/annotate/' . split(a:args[0], ':')[1] . '/' . a:file
endfunction " }}}

" GetDiffUrl(root, file, args) {{{
function eclim#vcs#impl#hgserve#GetDiffUrl(root, file, args)
  let r1 = split(a:args[0], ':')[1]
  " hgserve doesn't support diffing arbitrary revisions
  "let r2 = split(a:args[1], ':')[1]
  return a:root . '/diff/' . r1 . '/' . a:file
endfunction " }}}

" vim:ft=vim:fdm=marker
