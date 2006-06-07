" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/classpath.html
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
  let s:variables_command = '-command java_classpath_variables -filter vim'
  let s:update_command = '-command project_update -n "<name>" -filter vim'
" }}}

" NewClasspathEntry(template) {{{
" Adds a new entry to the current .classpath file.
function! eclim#eclipse#NewClasspathEntry (arg, template)
  let args = split(a:arg)
  for arg in args
    call s:MoveToInsertPosition()

    let line = line('.')

    let saved = @"
    let @" = substitute(a:template, '<arg>', arg, 'g')
    silent put
    let @" = saved

    call cursor(line, 1)
  endfor
endfunction " }}}

" MoveToInsertPosition() {{{
" If necessary moves the cursor to a valid insert position.
function! s:MoveToInsertPosition ()
  let start = search('<classpath\s*>', 'wn')
  let end = search('</classpath\s*>', 'wn')
  if line('.') < start || line('.') > end
    call cursor(end - 1, 1)
  endif
endfunction " }}}

" UpdateClasspath() {{{
" Updates the classpath on the server w/ the changes made to the current file.
function! eclim#eclipse#UpdateClasspath ()
  let name = eclim#project#GetCurrentProjectName()
  let command = substitute(s:update_command, '<name>', name, '')

  let result = eclim#ExecuteEclim(command)
  if result =~ '|'
    let errors = eclim#util#ParseLocationEntries(split(result, '\n'))
    call eclim#util#SetLocationList(errors)
  else
    call eclim#util#SetLocationList([], 'r')
  endif
endfunction " }}}

" CommandCompleteVar(argLead, cmdLine, cursorPos) {{{
" Custom command completion for classpath var relative files.
function! eclim#eclipse#CommandCompleteVar (argLead, cmdLine, cursorPos)
  let vars = split(eclim#ExecuteEclim(s:variables_command), '\n')

  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseArgs(cmdLine)
  let argLead = len(args) > 1 ? args[len(args) - 1] : ""

  let var_names = deepcopy(vars)
  call filter(var_names, 'v:val =~ "^' . argLead . '"')
  if len(var_names) > 0
    call map(var_names,
      \ "isdirectory(substitute(v:val, '.\\{-} - \\(.*\\)', '\\1', '')) ? " .
      \ "substitute(v:val, '\\(.\\{-}\\)\\s-.*', '\\1', '') . '/' : " .
      \ "substitute(v:val, '\\(.\\{-}\\)\\s-.*', '\\1', '')")
    return var_names
  endif

  let var = substitute(argLead, '\(.\{-}\)/.*', '\1', '')
  let var_dir = ""
  for cv in vars
    if cv =~ '^' . var
      let var_dir = substitute(cv, '.\{-} - \(.*\)', '\1', '')
      break
    endif
  endfor
  let argLead = substitute(argLead, var, var_dir, '')
  let dirs = eclim#util#CommandCompleteDir(argLead, a:cmdLine, a:cursorPos)
  call map(dirs, "substitute(v:val, '" . var_dir . "', '" . var . "', '')")

  return dirs
endfunction " }}}

" vim:ft=vim:fdm=marker
