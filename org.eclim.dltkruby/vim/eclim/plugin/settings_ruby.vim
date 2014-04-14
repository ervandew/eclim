" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2005 - 2014  Eric Van Dewoestine
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

" Global Varables {{{
  call eclim#AddVimSetting(
    \ 'Lang/Ruby', 'g:EclimRubySearchSingleResult', g:EclimDefaultFileOpenAction,
    \ 'Sets the command to use when opening a single result from a ruby search.')

  call eclim#AddVimSetting(
    \ 'Lang/Ruby', 'g:EclimRubyValidate', 1,
    \ 'Sets whether or not to validate ruby files on save.',
    \ '\(0\|1\)')

  call eclim#AddVimSetting(
    \ 'Lang/Ruby', 'g:EclimRubySyntasticEnabled', 0,
    \ "Only enable this if you want both eclim and syntastic to validate your ruby files.\n" .
    \ "If you want to use syntastic instead of eclim, simply disable RubyValidate.",
    \ '\(0\|1\)')
" }}}

" vim:ft=vim:fdm=marker
