" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Contains any global vim side code for embedding gvim in eclipse.
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

" BufferWritten() {{{
" Invoked when a buffer opened from eclipse is saved, to notifiy eclipse of
" the save.
function eclim#vimplugin#BufferWritten()
  if has('netbeans_enabled')
    nbkey unmodified
  endif
endfunction " }}}

" FeedKeys(keys) {{{
" Feeds eclipse compatible key string to eclipse if current gvim instance is
" attached via the netbeans protocol.
function eclim#vimplugin#FeedKeys(keys)
  if has('netbeans_enabled')
    silent exec 'nbkey feedkeys ' . a:keys
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
