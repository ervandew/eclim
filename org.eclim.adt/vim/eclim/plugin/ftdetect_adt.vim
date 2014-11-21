" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Vim file type detection script for eclim.
"
" License:
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

function! CheckAndroidXml()

  let s:project = eclim#project#util#GetCurrentProjectName()
  let s:aliases = eclim#project#util#GetProjectNatureAliases(s:project)
  if index(s:aliases, "android") == -1
    " only in android projects
    return
  endif

  if expand('%') != 'AndroidManifest.xml'
    " if not the manifest, we must be somewhere
    " in /res, but NOT in /res/raw
    let fullPath = expand('%:p')
    if fullPath !~ '/res/' || fullPath =~ '/res/raw/'
      " Nope
      return
    endif
  endif

  " yes!
  set ft=android-xml
endfunction

autocmd BufRead *.aidl set ft=java

autocmd BufRead *.xml call CheckAndroidXml()

" vim:ft=vim:fdm=marker
