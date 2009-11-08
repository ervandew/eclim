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
" Invoked when a buffer opened from eclipse is saved, to notify eclipse of the
" save.
function eclim#vimplugin#BufferWritten()
  if has('netbeans_enabled')
    if exists('b:eclim_file_modified')
      unlet b:eclim_file_modified
    endif
    nbkey unmodified
  endif
endfunction " }}}

" BufferUnmodified() {{{
" Invoked on cursor hold to check if a previously modified buffer is now
" unmodified, so that eclipse can be notified.
function eclim#vimplugin#BufferUnmodified()
  if has('netbeans_enabled')
    if !exists('b:eclim_file_modified')
      let b:eclim_file_modified = &modified
    endif

    if !&modified && b:eclim_file_modified
      unlet b:eclim_file_modified
      nbkey unmodified
    else
      let b:eclim_file_modified = &modified
    endif
  endif
endfunction " }}}

" BufferClosed() {{{
" Invoked when a buffer is removed from a window to signal that eclipse should
" close the associated editor tab.
function eclim#vimplugin#BufferClosed()
  if has('netbeans_enabled')
    exec 'nbkey fileClosed ' . expand('<afile>:p')
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
