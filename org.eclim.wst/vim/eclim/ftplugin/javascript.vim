" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/javascript/index.html
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

" Options {{{

exec 'setlocal ' . g:EclimCompletionMethod . '=eclim#javascript#complete#CodeComplete'

" }}}

" Autocmds {{{

augroup eclim_javascript
  autocmd! BufWritePost <buffer>
  autocmd BufWritePost <buffer> call eclim#javascript#util#UpdateSrcFile(0)
augroup END

" }}}

" Command Declarations {{{

command! -nargs=0 -buffer Validate :call eclim#javascript#util#UpdateSrcFile(1)

" }}}

" vim:ft=vim:fdm=marker
