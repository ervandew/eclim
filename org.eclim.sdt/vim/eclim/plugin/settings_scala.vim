" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2011 - 2014  Eric Van Dewoestine
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
    \ 'Lang/Scala', 'g:EclimScalaCompleteLayout',
    \ &completeopt !~ 'preview' && &completeopt =~ 'menu' ? 'standard' : 'compact',
    \ "Determines how overloaded methods are displayed in the completion popup.\n" .
    \ "The default is based on your current completion settings. If set to 'compact',\n" .
    \ "you can set it to 'standard' to force eclim to display all overridden members\n" .
    \ "in the popup rather than relying on the preview menu for that info.",
    \ '\(standard\|compact\)')

  call eclim#AddVimSetting(
    \ 'Lang/Scala', 'g:EclimScalaValidate', 1,
    \ 'Sets whether or not to validate scala files on save.',
    \ '\(0\|1\)')

  call eclim#AddVimSetting(
    \ 'Lang/Scala', 'g:EclimScalaSearchSingleResult', g:EclimDefaultFileOpenAction,
    \ 'Sets the command to use when opening a single result from a scala search.')
" }}}

" vim:ft=vim:fdm=marker
