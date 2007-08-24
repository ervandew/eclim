" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/php/includepath.html
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
