" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/make.html
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

" Command Declarations {{{
if !exists(":Make")
  command -bang -nargs=* Make :call s:Make('<bang>', '<args>')
endif
" }}}

" s:Make(bang, args) {{{
" Executes make using the supplied arguments.
function! s:Make(bang, args)
  let makefile = findfile('makefile', '.;')
  let makefile2 = findfile('Makefile', '.;')
  if len(makefile2) > len(makefile)
    let makefile = makefile2
  endif
  let cwd = getcwd()
  let save_mlcd = g:EclimMakeLCD
  exec 'lcd ' . fnamemodify(makefile, ':h')
  let g:EclimMakeLCD = 0
  try
    call eclim#util#MakeWithCompiler('eclim_make', a:bang, a:args)
  finally
    exec 'lcd ' . cwd
    let g:EclimMakeLCD = save_mlcd
  endtry
endfunction " }}}

" vim:ft=vim:fdm=marker
