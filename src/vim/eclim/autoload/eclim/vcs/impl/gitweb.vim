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

if !exists('g:eclim_vcs_gitweb_loaded')
  let g:eclim_vcs_gitweb_loaded = 1
else
  finish
endif

" Example url: http://localhost:1234?p=.git

" GetLogUrl(root, file, args) {{{
function eclim#vcs#impl#gitweb#GetLogUrl(root, file, args)
  "return a:root . ';a=history;f=' . a:file . ';h=' . a:args[0]
  return a:root . ';a=history;f=' . a:file . ';h=HEAD'
endfunction " }}}

" GetChangeSetUrl(root, file, args) {{{
function eclim#vcs#impl#gitweb#GetChangeSetUrl(root, file, args)
  return a:root . ';a=commitdiff;h=' . a:args[0]
endfunction " }}}

" GetAnnotateUrl(root, file, args) Not supported by gitweb {{{
function eclim#vcs#impl#gitweb#GetAnnotateUrl(root, file, args)
  echoe 'Sorry, this function is not yet supported by gitweb.'
  return
endfunction " }}}

" GetDiffUrl(root, file, args) {{{
function eclim#vcs#impl#gitweb#GetDiffUrl(root, file, args)
  let r1 = a:args[0]
  let r2 = a:args[1]
  return a:root . ';a=blobdiff;f=' . a:file . ';hb=' . r1 . ';hpb=' . r2
endfunction " }}}

" vim:ft=vim:fdm=marker
