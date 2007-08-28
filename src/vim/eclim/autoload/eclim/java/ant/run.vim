" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/ant/run.html
"
" License:
"
" Copyright (c) 2005 - 2006
"
" Licensed under the Apache License, Version 2.0 (the "License");
" you may not use this file except in compliance with the License.
" You may obtain a copy of the License at
"
"      http://www.apache.org/licenses/LICENSE-2.0
"
" Unless required by applicable law or agreed to in writing, software
" distributed under the License is distributed on an "AS IS" BASIS,
" WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
" See the License for the specific language governing permissions and
" limitations under the License.
"
" }}}

" Script Variables {{{
  let s:command_targets = '-command ant_targets -p "<project>" -f "<file>"'
" }}}

" Ant(bang, args) {{{
" Executes ant using the supplied arguments.
function! eclim#java#ant#run#Ant (bang, args)
  call eclim#util#MakeWithCompiler('eclim_ant', a:bang, a:args)
endfunction " }}}

" CommandCompleteTarget(argLead, cmdLine, cursorPos) {{{
" Custom command completion for ant targets.
function! eclim#java#ant#run#CommandCompleteTarget (argLead, cmdLine, cursorPos)
  let project = eclim#project#util#GetCurrentProjectName()
  if project == ''
    return []
  endif

  let file = eclim#java#ant#util#FindBuildFile()
  if project != "" && file != ""
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
