" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2012  Eric Van Dewoestine
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

" Options {{{

" add xml semantic completion triggers for YCM
if exists("g:ycm_semantic_triggers")
    " start with user's
    let triggers = g:ycm_semantic_triggers
else
    let triggers = {}
endif

let triggers['android-xml'] = [':', '="', '<', '/', '@']
let g:ycm_semantic_triggers = triggers

" }}}

" Command Declarations {{{
if !exists(":AndroidReload")
  command AndroidReload :call eclim#android#Reload()
endif
" }}}

" vim:ft=vim:fdm=marker
