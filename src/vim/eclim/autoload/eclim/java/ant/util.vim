" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Utility functions for working with ant.
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

" FindBuildFile() {{{
" Finds the build file relative to the current file (like ant -find).
function! eclim#java#ant#util#FindBuildFile()
  let buildFile = eclim#util#Findfile('build.xml', fnamemodify(expand('%:p'), ':h') . ';')
  if filereadable(buildFile)
    return substitute(fnamemodify(buildFile, ':p'), '\', '/', 'g')
  endif

  return ''
endfunction " }}}

" SilentUpdate() {{{
" Silently updates the current source file w/out validation.
function! eclim#java#ant#util#SilentUpdate()
  try
    let saved_ant = g:EclimAntValidate
    let saved_xml = g:EclimXmlValidate

    let g:EclimAntValidate = 0
    let g:EclimXmlValidate = 0

    silent update
  finally
    let g:EclimAntValidate = saved_ant
    let g:EclimXmlValidate = saved_xml
  endtry
endfunction " }}}

" vim:ft=vim:fdm=marker
