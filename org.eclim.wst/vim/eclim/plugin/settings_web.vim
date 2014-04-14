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
    \ 'Lang/Css', 'g:EclimCssValidate', 1,
    \ 'Sets whether or not to validate css files on save.',
    \ '\(0\|1\)')
  call eclim#AddVimSetting(
    \ 'Lang/Dtd', 'g:EclimDtdValidate', 1,
    \ 'Sets whether or not to validate dtd files on save.',
    \ '\(0\|1\)')

  call eclim#AddVimSetting(
    \ 'Lang/Html', 'g:EclimHtmlValidate', 1,
    \ 'Sets whether or not to validate html files on save.',
    \ '\(0\|1\)')
  call eclim#AddVimSetting(
    \ 'Lang/Html', 'g:EclimHtmlSyntasticEnabled', 0,
    \ "Only enable this if you want both eclim and syntastic to validate your html files.\n" .
    \ "If you want to use syntastic instead of eclim, simply disable HtmlValidate.",
    \ '\(0\|1\)')

  call eclim#AddVimSetting(
    \ 'Lang/Javascript', 'g:EclimJavascriptValidate', 1,
    \ 'Sets whether or not to validate javascript files on save.',
    \ '\(0\|1\)')
  if !exists("g:EclimJavascriptLintEnabled")
    " enabling by default until jsdt validation is mature enough to use.
    "let g:EclimJavascriptLintEnabled = 0
    let g:EclimJavascriptLintEnabled = 1
  endif
  call eclim#AddVimSetting(
    \ 'Lang/Javascript', 'g:EclimJavascriptLintConf', eclim#UserHome() . '/.jslrc',
    \ 'The path to your JavaScript Lint configuration file.')

  call eclim#AddVimSetting(
    \ 'Lang/Xsd', 'g:EclimXsdValidate', 1,
    \ 'Sets whether or not to validate xsd files on save.',
    \ '\(0\|1\)')
" }}}

" vim:ft=vim:fdm=marker
