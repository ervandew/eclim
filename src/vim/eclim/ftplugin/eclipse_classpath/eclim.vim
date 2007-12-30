" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/classpath.html
"
" License:
"
" Copyright (c) 2005 - 2008
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
    \ "\t<classpathentry kind=\"<kind>\" path=\"<arg>\"/>"
  "  \ "\t<classpathentry exported=\"true\" kind=\"<kind>\" path=\"<arg>\">\n" .
  "  \ "\t\t<!--\n" .
  "  \ "\t\t\tsourcepath=\"<path>\">\n" .
  "  \ "\t\t-->\n" .
  "  \ "\t\t<!--\n" .
  "  \ "\t\t<attributes>\n" .
  "  \ "\t\t\t<attribute value=\"file:<javadoc>\" name=\"javadoc_location\"/>\n" .
  "  \ "\t\t</attributes>\n" .
  "  \ "\t\t-->\n" .
  "  \ "\t</classpathentry>"
  let s:entry_jar = substitute(s:entry_var, '<kind>', 'lib', '')
  let s:entry_var = substitute(s:entry_var, '<kind>', 'var', '')
" }}}

" load any xml related functionality
runtime ftplugin/xml.vim
runtime indent/xml.vim

augroup eclim_xml
  autocmd! BufWritePost <buffer>
  autocmd BufWritePost <buffer> call eclim#java#classpath#UpdateClasspath()
augroup END

" Command Declarations {{{
if !exists(":NewSrcEntry")
  command -nargs=+ -complete=customlist,eclim#common#util#CommandCompleteRelative -buffer
    \ NewSrcEntry :call eclim#java#classpath#NewClasspathEntry
    \     (substitute('<args>', '\', '/', 'g') , s:entry_src)
endif
if !exists(":NewProjectEntry")
  command -nargs=+ -complete=customlist,eclim#java#util#CommandCompleteProject -buffer
    \ NewProjectEntry :call eclim#java#classpath#NewClasspathEntry('<args>', s:entry_project)
endif
if !exists(":NewJarEntry")
  command -nargs=+ -complete=file -buffer NewJarEntry
    \ :call eclim#java#classpath#NewClasspathEntry
    \     (substitute('<args>', '\', '/', 'g'), s:entry_jar)
endif
if !exists(":NewVarEntry")
  command -nargs=+ -complete=customlist,eclim#java#classpath#CommandCompleteVarPath -buffer
    \ NewVarEntry
    \ :call eclim#java#classpath#NewClasspathEntry
    \     (substitute(fnamemodify('<args>', ':p'), '\', '/', 'g'), s:entry_var)
endif
if !exists(":VariableList")
  command -buffer VariableList :call eclim#java#classpath#VariableList()
endif
if !exists(":VariableCreate")
  command -nargs=+ -buffer -complete=customlist,eclim#java#classpath#CommandCompleteVarAndDir
    \ VariableCreate :call eclim#java#classpath#VariableCreate(<f-args>)
endif
if !exists(":VariableDelete")
  command -nargs=1 -buffer -complete=customlist,eclim#java#classpath#CommandCompleteVar
    \ VariableDelete :call eclim#java#classpath#VariableDelete('<args>')
endif
" }}}

" vim:ft=vim:fdm=marker
