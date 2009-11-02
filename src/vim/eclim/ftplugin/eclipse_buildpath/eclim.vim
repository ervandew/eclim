" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/php/buildpath.html
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

" Script Variables {{{
  let s:entry_src =
    \ "\t<buildpathentry kind=\"src\" path=\"<arg>\"/>"
  let s:entry_lib =
    \ "\t<buildpathentry external=\"true\" kind=\"lib\" path=\"<arg>\"/>"
  let s:entry_project =
    \ "\t<buildpathentry combineaccessrules=\"false\" kind=\"prj\" path=\"<arg>\"/>"
  let s:entry_var =
    \ "\t<buildpathentry kind=\"var\" path=\"<arg>\"/>"
" }}}

" load any xml related functionality
runtime ftplugin/xml.vim
runtime indent/xml.vim

augroup eclim_xml
  autocmd! BufWritePost <buffer>
  autocmd BufWritePost <buffer> call eclim#project#util#ProjectUpdate()
augroup END

" Command Declarations {{{
if !exists(":NewSrcEntry")
  command -nargs=+ -complete=customlist,eclim#project#util#CommandCompleteProjectRelative -buffer
    \ NewSrcEntry :call eclim#dltk#buildpath#NewBuildPathEntry
    \     (substitute('<args>', '\', '/', 'g') , s:entry_src)
endif
if !exists(":NewLibEntry")
  command -nargs=+ -complete=dir -buffer
    \ NewLibEntry :call eclim#dltk#buildpath#NewBuildPathEntry
    \     (substitute('<args>', '\', '/', 'g') , s:entry_lib)
endif
if !exists(":NewProjectEntry")
  command -nargs=+ -complete=customlist,eclim#dltk#util#CommandCompleteProject -buffer
    \ NewProjectEntry :call eclim#dltk#buildpath#NewBuildPathEntry('<args>', s:entry_project)
endif

" Disabled until org.eclipse.dltk.internal.core.BuildpathEntry.elementDecode
" starts supporting kind="var"
"if !exists(":NewVarEntry")
"  command -nargs=+ -complete=customlist,eclim#dltk#buildpath#CommandCompleteVarPath -buffer
"    \ NewVarEntry
"    \ :call eclim#dltk#buildpath#NewBuildPathEntry('<args>', s:entry_var)
"endif
"if !exists(":VariableList")
"  command -buffer VariableList :call eclim#dltk#buildpath#VariableList()
"endif
"if !exists(":VariableCreate")
"  command -nargs=+ -buffer -complete=customlist,eclim#dltk#buildpath#CommandCompleteVarAndDir
"    \ VariableCreate :call eclim#dltk#buildpath#VariableCreate(<f-args>)
"endif
"if !exists(":VariableDelete")
"  command -nargs=1 -buffer -complete=customlist,eclim#dltk#buildpath#CommandCompleteVar
"    \ VariableDelete :call eclim#dltk#buildpath#VariableDelete('<args>')
"endif

" }}}

" vim:ft=vim:fdm=marker
