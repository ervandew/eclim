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
    \ 'Lang/Python', 'g:EclimPythonValidate', 1,
    \ 'Sets whether or not to validate python files on save.',
    \ '\(0\|1\)')
  call eclim#AddVimSetting(
    \ 'Lang/Python', 'g:EclimPythonSyntasticEnabled', 0,
    \ "Only enable this if you want both eclim and syntastic to validate your python files.\n" .
    \ "If you want to use syntastic instead of eclim, simply disable PythonValidate.",
    \ '\(0\|1\)')

  call eclim#AddVimSetting(
    \ 'Lang/Python', 'g:EclimPythonSearchSingleResult', g:EclimDefaultFileOpenAction,
    \ 'Sets the command to use when opening a single result from a python search.')

  call eclim#AddVimSetting(
    \ 'Lang/Python/Django', 'g:EclimDjangoAdmin', 'django-admin.py',
    \ 'The file name, or full path of if not in your env path, of your django admin script.')
  call eclim#AddVimSetting(
    \ 'Lang/Python/Django', 'g:EclimDjangoFindAction', g:EclimDefaultFileOpenAction,
    \ 'Sets the command to use when opening a single result from a django search.')
  call eclim#AddVimSetting(
    \ 'Lang/Python/Django', 'g:EclimDjangoStaticPaths', [],
    \ 'List of paths to search when searching for static files.')
  call eclim#AddVimSetting(
    \ 'Lang/Python/Django', 'g:EclimDjangoStaticPattern', '',
    \ 'Vim regex used to match a static file reference.')
" }}}

" vim:ft=vim:fdm=marker
