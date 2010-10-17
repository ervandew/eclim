" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/ant/run.html
"
" License:
"
" Copyright (C) 2005 - 2010  Eric Van Dewoestine
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

" Auto Commands {{{
runtime autoload/eclim/java/test.vim
augroup eclim_ant_make
  autocmd!
  if exists('*eclim#java#test#ResolveQuickfixResults')
    autocmd QuickFixCmdPost make call eclim#java#test#ResolveQuickfixResults('junit')
    autocmd QuickFixCmdPost make call eclim#java#test#ResolveQuickfixResults('testng')
  endif
augroup END
" }}}

" Command Declarations {{{
if !exists(":Ant")
  command -bang -nargs=* -complete=customlist,eclim#java#ant#run#CommandCompleteTarget
    \ Ant :call eclim#java#ant#run#Ant('<bang>', '<args>')
endif
" }}}

" vim:ft=vim:fdm=marker
