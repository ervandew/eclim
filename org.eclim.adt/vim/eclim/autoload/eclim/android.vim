" Author:  Eric Van Dewoestine
"
" License: {{{
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

" Script Variables {{{
let s:command_reload = '-command android_reload'
" }}}

function! eclim#android#Reload() " {{{
  let workspace = eclim#eclipse#ChooseWorkspace()
  if workspace == '0'
    return
  endif

  let port = eclim#client#nailgun#GetNgPort(workspace)
  let result = eclim#ExecuteEclim(s:command_reload, port)
  if type(result) != g:DICT_TYPE
    return
  endif

  if has_key(result, 'error')
    call eclim#util#EchoError(result.error)
  else
    call eclim#util#Echo(result.message)
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
