" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/classpath.html
"
" License:
"
" Copyright (C) 2005 - 2012  Eric Van Dewoestine
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
runtime! ftplugin/xml.vim
runtime! indent/xml.vim

augroup eclim_xml
  autocmd! BufWritePost <buffer>
  autocmd BufWritePost <buffer> call eclim#project#util#ProjectUpdate()
augroup END

" Command Declarations {{{
if !exists(":NewSrcEntry")
  command -nargs=+ -complete=customlist,eclim#project#util#CommandCompleteProjectRelative -buffer
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
    \     (substitute('<args>', '\', '/', 'g'), s:entry_var)
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
