" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/classpath.html
"
" License:
"
" Copyright (C) 2005 - 2013  Eric Van Dewoestine
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

" load any xml related functionality
runtime! ftplugin/xml.vim
runtime! indent/xml.vim

augroup eclim_xml
  autocmd! BufWritePost <buffer>
  autocmd BufWritePost <buffer> call eclim#project#util#ProjectUpdate()
augroup END

" Command Declarations {{{
if !exists(":NewSrcEntry")
  command -nargs=1 -complete=customlist,eclim#project#util#CommandCompleteProjectRelative -buffer
    \ NewSrcEntry :call eclim#java#classpath#NewClasspathEntry('src', '<args>')
endif
if !exists(":NewProjectEntry")
  command -nargs=1 -complete=customlist,eclim#java#util#CommandCompleteProject -buffer
    \ NewProjectEntry :call eclim#java#classpath#NewClasspathEntry('project', '<args>')
endif
if !exists(":NewJarEntry")
  command -nargs=+ -complete=customlist,eclim#project#util#CommandCompleteAbsoluteOrProjectRelative -buffer
    \ NewJarEntry
    \ :call eclim#java#classpath#NewClasspathEntry('lib', <f-args>)
endif
if !exists(":NewVarEntry")
  command -nargs=+ -complete=customlist,eclim#java#classpath#CommandCompleteVarPath -buffer
    \ NewVarEntry
    \ :call eclim#java#classpath#NewClasspathEntry('var', <f-args>)
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
