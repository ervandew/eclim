" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/php/includepath.html
"
" License:
"
" Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
  let s:entry_lib =
    \ "\t<includepathentry kind=\"lib\" path=\"<arg>\" createdReference=\"false\" contentKind=\"source\">" .
    \ "\n\t</includepathentry>"
  let s:entry_project =
    \ "\t<includepathentry kind=\"prj\" path=\"/<arg>\" resource=\"<arg>\" createdReference=\"true\" contentKind=\"source\">" .
    \ "\n\t</includepathentry>"
  let s:entry_var =
    \ "\t<includepathentry kind=\"var\" path=\"<arg>\" createdReference=\"false\" contentKind=\"source\">" .
    \ "\n\t</includepathentry>"
" }}}

" load any xml related functionality
runtime ftplugin/xml.vim
runtime indent/xml.vim

augroup eclim_xml
  autocmd! BufWritePost <buffer>
  autocmd BufWritePost <buffer> call eclim#php#projectOptions#UpdateIncludePath()
augroup END

" Command Declarations {{{
if !exists(":NewLibEntry")
  command -nargs=+ -complete=dir -buffer
    \ NewLibEntry :call eclim#php#projectOptions#NewIncludePathEntry
    \     (substitute('<args>', '\', '/', 'g') , s:entry_lib)
endif
if !exists(":NewProjectEntry")
  command -nargs=+ -complete=customlist,eclim#php#util#CommandCompleteProject -buffer
    \ NewProjectEntry :call eclim#php#projectOptions#NewIncludePathEntry('<args>', s:entry_project)
endif
if !exists(":NewVarEntry")
  command -nargs=+ -complete=customlist,eclim#php#projectOptions#CommandCompleteVarPath -buffer
    \ NewVarEntry
    \ :call eclim#php#projectOptions#NewIncludePathEntry('<args>', s:entry_var)
endif
if !exists(":VariableList")
  command -buffer VariableList :call eclim#php#projectOptions#VariableList()
endif
if !exists(":VariableCreate")
  command -nargs=+ -buffer -complete=customlist,eclim#php#projectOptions#CommandCompleteVarAndDir
    \ VariableCreate :call eclim#php#projectOptions#VariableCreate(<f-args>)
endif
if !exists(":VariableDelete")
  command -nargs=1 -buffer -complete=customlist,eclim#php#projectOptions#CommandCompleteVar
    \ VariableDelete :call eclim#php#projectOptions#VariableDelete('<args>')
endif
" }}}

" vim:ft=vim:fdm=marker
