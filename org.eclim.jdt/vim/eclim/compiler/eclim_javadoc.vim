" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2005 - 2017  Eric Van Dewoestine
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

if exists("current_compiler")
  finish
endif
let current_compiler = "eclim_javadoc"

let instance = eclim#client#nailgun#ChooseEclimdInstance()
let [retcode, result] = eclim#client#nailgun#GetEclimCommand(instance.home)
if retcode != 0
  call eclim#util#EchoError(result)
  finish
endif

let command = result
let command .= ' --nailgun-port ' . instance.port
let command .= ' -command javadoc $*'
let command = substitute(command, '"', '', 'g')
exec 'CompilerSet makeprg=' . escape(command, ' "')

exec 'CompilerSet errorformat=' .
  \ '\%A%.%#[javadoc]\ %f:%l:\ %m,' .
  \ '\%-Z%.%#[javadoc]\ %p^,' .
  \ '\%-G%.%#[javadoc]%.%#,' .
  \ '\%-G%.%#'

" vim:ft=vim:fdm=marker
