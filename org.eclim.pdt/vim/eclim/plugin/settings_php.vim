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
    \ 'Lang/Php', 'g:EclimPhpValidate', 1,
    \ 'Sets whether or not to validate php files on save.',
    \ '\(0\|1\)')

  call eclim#AddVimSetting(
    \ 'Lang/Php', 'g:EclimPhpHtmlValidate', 1,
    \ "When php validation is enabled, this sets whether or not to validate\n" .
    \ "html content in your php files on save.",
    \ '\(0\|1\)')

  call eclim#AddVimSetting(
    \ 'Lang/Php', 'g:EclimPhpSyntasticEnabled', 0,
    \ "Only enable this if you want both eclim and syntastic to validate your php files.\n" .
    \ "If you want to use syntastic instead of eclim, simply disable PhpValidate.",
    \ '\(0\|1\)')

  call eclim#AddVimSetting(
    \ 'Lang/Php', 'g:EclimPhpSearchSingleResult', g:EclimDefaultFileOpenAction,
    \ 'Sets the command to use when opening a single result from a php search.')
" }}}

" vim:ft=vim:fdm=marker
