" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/css/validate.html
"
" License:
"
" Copyright (C) 2012  Eric Van Dewoestine
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

function! eclim#css#validate#Filter(errors) " {{{
  let results = []
  let ignore_next_parse_error = 0
  for error in a:errors
    " ignore errors related to browser targeted properties
    if error.text =~ '\(^\|\s\)-\(moz\|webkit\|khtml\|o\)-\w\+\>'
      continue
    endif

    " ignore errors on IE filter property line
    if getline(error.lnum) =~ '^\s*filter:\s*progid'
      " next parse error will be because of this filter
      let ignore_next_parse_error = 1
      continue
    endif
    if error.text == 'Parse Error' && ignore_next_parse_error
      let ignore_next_parse_error = 0
      continue
    endif

    call add(results, error)
  endfor

  return results
endfunction " }}}

" vim:ft=vim:fdm=marker
