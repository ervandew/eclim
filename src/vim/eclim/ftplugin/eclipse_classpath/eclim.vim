" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/classpath.html
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

" Script Variables {{{
  let s:entry_src = "\t<classpathentry kind=\"src\" path=\"<arg>\"/>"
  let s:entry_project =
    \ "\t<classpathentry exported=\"true\" kind=\"src\" path=\"/<arg>\"/>"
  let s:entry_var =
    \ "\t<classpathentry exported=\"true\" kind=\"<kind>\" path=\"<arg>\">\n" .
    \ "\t\t<!--\n" .
    \ "\t\t\tsourcepath=\"<path>\">\n" .
    \ "\t\t-->\n" .
    \ "\t\t<!--\n" .
    \ "\t\t<attributes>\n" .
    \ "\t\t\t<attribute value=\"file:<javadoc>\" name=\"javadoc_location\"/>\n" .
    \ "\t\t</attributes>\n" .
    \ "\t\t-->\n" .
    \ "\t</classpathentry>"
  let s:entry_jar = substitute(s:entry_var, '<kind>', 'lib', '')
  let s:entry_var = substitute(s:entry_var, '<kind>', 'var', '')
" }}}

" load any xml related functionality
runtime ftplugin/xml.vim

augroup eclipse_classpath
  autocmd!
  autocmd BufWritePost .classpath call eclim#eclipse#UpdateClasspath()
augroup END

" Command Declarations {{{
if !exists(":NewSrcEntry")
  command -nargs=+ -complete=customlist,eclim#common#CommandCompleteRelative -buffer
    \ NewSrcEntry :call eclim#eclipse#NewClasspathEntry
    \     (substitute('<args>', '\', '/', 'g') , s:entry_src)
endif
if !exists(":NewProjectEntry")
  command -nargs=+ -complete=customlist,eclim#project#CommandCompleteProject -buffer
    \ NewProjectEntry :call eclim#eclipse#NewClasspathEntry('<args>', s:entry_project)
endif
if !exists(":NewJarEntry")
  command -nargs=+ -complete=file -buffer NewJarEntry
    \ :call eclim#eclipse#NewClasspathEntry
    \     (substitute(fnamemodify('<args>', ':p'), '\', '/', 'g'), s:entry_jar)
endif
if !exists(":NewVarEntry")
  command -nargs=+ -complete=customlist,eclim#eclipse#CommandCompleteVar  -buffer
    \ NewVarEntry
    \ :call eclim#eclipse#NewClasspathEntry
    \     (substitute(fnamemodify('<args>', ':p'), '\', '/', 'g'), s:entry_var)
endif
if !exists(":VariableList")
  command -buffer VariableList :call eclim#eclipse#VariableList()
endif
" }}}

" vim:ft=vim:fdm=marker
