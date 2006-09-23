" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Utility functions for working with ant.
"
" License:
"
" Copyright (c) 2005 - 2006
"
" Licensed under the Apache License, Version 2.0 (the "License");
" you may not use this file except in compliance with the License.
" You may obtain a copy of the License at
"
"      http://www.apache.org/licenses/LICENSE-2.0
"
" Unless required by applicable law or agreed to in writing, software
" distributed under the License is distributed on an "AS IS" BASIS,
" WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
" See the License for the specific language governing permissions and
" limitations under the License.
"
" }}}

" FindBuildFile () {{{
" Finds the build file relative to the current file (like ant -find).
function! eclim#java#ant#util#FindBuildFile ()
  let buildFile = eclim#util#Findfile('build.xml', fnamemodify(expand('%:p'), ':h') . ';')
  if filereadable(buildFile)
    return substitute(fnamemodify(buildFile, ':p'), '\', '/', 'g')
  endif

  return ''
endfunction " }}}

" SilentUpdate() {{{
" Silently updates the current source file w/out validation.
function! eclim#java#ant#util#SilentUpdate ()
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
