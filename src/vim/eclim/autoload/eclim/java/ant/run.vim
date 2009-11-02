" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/ant/run.html
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

" Script Variables {{{
  let s:command_targets = '-command ant_targets -p "<project>" -f "<file>"'
" }}}

" Ant(bang, args) {{{
" Executes ant using the supplied arguments.
function! eclim#java#ant#run#Ant(bang, args)
  call eclim#util#MakeWithCompiler('eclim_ant', a:bang, a:args)
endfunction " }}}

" CommandCompleteTarget(argLead, cmdLine, cursorPos) {{{
" Custom command completion for ant targets.
function! eclim#java#ant#run#CommandCompleteTarget(argLead, cmdLine, cursorPos)
  let project = eclim#project#util#GetCurrentProjectName()
  if project == ''
    return []
  endif

  let file = eclim#java#ant#util#FindBuildFile()
  if project != "" && file != ""
    let file = eclim#project#util#GetProjectRelativeFilePath(file)
    let command = s:command_targets
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')

    let targets = split(eclim#ExecuteEclim(command), '\n')
    if len(targets) == 1 && targets[0] == '0'
      return []
    endif

    let cmdTail = strpart(a:cmdLine, a:cursorPos)
    let argLead = substitute(a:argLead, cmdTail . '$', '', '')
    call filter(targets, 'v:val =~ "^' . argLead . '"')

    return targets
  endif

  return []
endfunction " }}}

" vim:ft=vim:fdm=marker
