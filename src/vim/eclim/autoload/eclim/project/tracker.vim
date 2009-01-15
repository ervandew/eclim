" Author:  Eric Van Dewoestine
"
" Description: {{{
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

" Ticket(ticket) {{{
function eclim#project#tracker#Ticket(ticket)
  let url = eclim#project#util#GetProjectSetting('org.eclim.project.tracker')
  if type(url) == 0 || url == ''
    call eclim#util#EchoWarning(
      \ "Viewing tickets requires project setting " .
      \ "'org.eclim.project.tracker'.")
    return
  endif

  let url = substitute(url, '<id>', a:ticket, 'g')
  call eclim#web#OpenUrl(url)
endfunction " }}}

" vim:ft=vim:fdm=marker
