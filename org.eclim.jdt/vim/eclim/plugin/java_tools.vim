" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/tools.html
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

" Autocmds {{{
augroup eclim_java_class_read
  autocmd!
  autocmd BufReadCmd *.class call eclim#java#util#ReadClassPrototype()
augroup END
" }}}

" Command Declarations {{{
if !exists(":Jps") && executable('jps')
  command Jps :call eclim#java#tools#Jps()
endif

if !exists(":Ant")
  command -bang -nargs=* -complete=customlist,eclim#java#ant#complete#CommandCompleteTarget
    \ Ant :call eclim#java#tools#MakeWithJavaBuildTool('eclim_ant', '<bang>', '<args>')
endif

if !exists(":Maven")
  command -bang -nargs=* Maven
    \ :call eclim#java#tools#MakeWithJavaBuildTool('eclim_maven', '<bang>', '<args>')
endif
if !exists(":MavenRepo")
  command -nargs=0 -buffer
    \ MavenRepo :call eclim#java#maven#SetClasspathVariable('Maven', 'MAVEN_REPO')
endif
if !exists(":Mvn")
  command -bang -nargs=* Mvn
    \ :call eclim#java#tools#MakeWithJavaBuildTool('eclim_mvn', '<bang>', '<args>')
endif
if !exists(":MvnRepo")
  command -nargs=0 -buffer
    \ MvnRepo :call eclim#java#maven#SetClasspathVariable('Mvn', 'M2_REPO')
endif
" }}}

" vim:ft=vim:fdm=marker
