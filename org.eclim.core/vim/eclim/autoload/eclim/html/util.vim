" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Various html relatd functions.
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

" HtmlToText() {{{
" Converts the supplied basic html to text.
function! eclim#html#util#HtmlToText(html)
  let text = a:html
  let text = substitute(text, '<br/\?>\c', '\n', 'g')
  let text = substitute(text, '</\?b>\c', '', 'g')
  let text = substitute(text, '</\?ul>\c', '', 'g')
  let text = substitute(text, '<li>\c', '- ', 'g')
  let text = substitute(text, '</li>\c', '', 'g')
  let text = substitute(text, '</\?p/\?>\c', '', 'g')
  let text = substitute(text, '</\?code>\c', '', 'g')
  let text = substitute(text, '</\?pre>\c', '', 'g')
  let text = substitute(text, '<a .\{-}>\c', '', 'g')
  let text = substitute(text, '</a>', '', 'g')
  let text = substitute(text, '&quot;\c', '"', 'g')
  let text = substitute(text, '&amp;\c', '&', 'g')
  let text = substitute(text, '&lt;\c', '<', 'g')
  let text = substitute(text, '&gt;\c', '>', 'g')

  return text
endfunction " }}}

" InCssBlock() {{{
" Determines if the cusor is inside of <style> tags.
function! eclim#html#util#InCssBlock()
  let line = line('.')

  let stylestart = search('<style\>', 'bcWn')
  if stylestart > 0
    let styleend = search('</style\s*>', 'bWn')
  endif
  if stylestart > 0 && stylestart < line &&
      \ (styleend == 0 || (styleend > stylestart && line < styleend))
    return stylestart
  endif

  return 0
endfunction " }}}

" InJavascriptBlock() {{{
" Determines if the cursor is inside of <script> tags.
function! eclim#html#util#InJavascriptBlock()
  let line = line('.')

  let scriptstart = search('<script\>', 'bcWn')
  if scriptstart > 0
    let scriptend = search('</script\s*>', 'bWn')
  endif
  if scriptstart > 0 && scriptstart < line &&
        \ (scriptend == 0 || (scriptend > scriptstart && line < scriptend))
    return scriptstart
  endif

  return 0
endfunction " }}}

" OpenInBrowser(file) {{{
function! eclim#html#util#OpenInBrowser(file)
  let file = a:file
  if file == ''
    let file = expand('%:p')
  else
    let file = getcwd() . '/' . file
  endif
  let url = 'file://' . file
  call eclim#web#OpenUrl(url)
endfunction " }}}

" UrlEncode(string) {{{
function! eclim#html#util#UrlEncode(string)
  let result = a:string

  " must be first
  let result = substitute(result, '%', '%25', 'g')

  let result = substitute(result, '\s', '%20', 'g')
  let result = substitute(result, '!', '%21', 'g')
  let result = substitute(result, '"', '%22', 'g')
  let result = substitute(result, '#', '%23', 'g')
  let result = substitute(result, '\$', '%24', 'g')
  let result = substitute(result, '&', '%26', 'g')
  let result = substitute(result, "'", '%27', 'g')
  let result = substitute(result, '(', '%28', 'g')
  let result = substitute(result, ')', '%29', 'g')
  let result = substitute(result, '*', '%2A', 'g')
  let result = substitute(result, '+', '%2B', 'g')
  let result = substitute(result, ',', '%2C', 'g')
  let result = substitute(result, '-', '%2D', 'g')
  let result = substitute(result, '\.', '%2E', 'g')
  let result = substitute(result, '\/', '%2F', 'g')
  let result = substitute(result, ':', '%3A', 'g')
  let result = substitute(result, ';', '%3B', 'g')
  let result = substitute(result, '<', '%3C', 'g')
  let result = substitute(result, '=', '%3D', 'g')
  let result = substitute(result, '>', '%3E', 'g')
  let result = substitute(result, '?', '%3F', 'g')
  let result = substitute(result, '@', '%40', 'g')
  let result = substitute(result, '[', '%5B', 'g')
  let result = substitute(result, '\\', '%5C', 'g')
  let result = substitute(result, ']', '%5D', 'g')
  let result = substitute(result, '\^', '%5E', 'g')
  let result = substitute(result, '`', '%60', 'g')
  let result = substitute(result, '{', '%7B', 'g')
  let result = substitute(result, '|', '%7C', 'g')
  let result = substitute(result, '}', '%7D', 'g')
  let result = substitute(result, '\~', '%7E', 'g')

  return result
endfunction " }}}

" vim:ft=vim:fdm=marker
