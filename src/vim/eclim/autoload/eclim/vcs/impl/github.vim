" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/common/vcs.html
"
" License:
"
" Copyright (C) 2005 - 2010  Eric Van Dewoestine
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

if !exists('g:eclim_vcs_github_loaded')
  let g:eclim_vcs_github_loaded = 1
else
  finish
endif

" GetLogUrl(root, file, args) {{{
function eclim#vcs#impl#github#GetLogUrl(root, file, args)
  return a:root . '/commits/' . a:args[0] . '/' . a:file
endfunction " }}}

" GetChangeSetUrl(root, file, args) {{{
function eclim#vcs#impl#github#GetChangeSetUrl(root, file, args)
  return a:root . '/commit/' . a:args[0]
endfunction " }}}

" GetAnnotateUrl(root, file, args) {{{
function eclim#vcs#impl#github#GetAnnotateUrl(root, file, args)
  return a:root . '/blame/' . a:args[0] . '/' . a:file
endfunction " }}}

" GetDiffUrl(root, file, args) Not supported by github {{{
function eclim#vcs#impl#github#GetDiffUrl(root, file, args)
  echoe 'Sorry, this function is not yet supported by github. Try using VcsWebChangeSet instead.'
  return
endfunction " }}}

" vim:ft=vim:fdm=marker
