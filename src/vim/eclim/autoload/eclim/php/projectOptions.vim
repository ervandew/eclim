" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/php/includepath.html
"
" License:
"
" Copyright (c) 2005 - 2008
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
  let s:command_variables = '-command php_includepath_variables'
  let s:command_update = '-command project_update -p "<project>"'
  let s:command_variable_create =
    \ '-command php_includepath_variable_create -n "<name>" -p "<path>"'
  let s:command_variable_delete =
    \ '-command php_includepath_variable_delete -n "<name>"'
" }}}

" NewIncludePathEntry(template) {{{
" Adds a new entry to the current .projectOptions file.
function! eclim#php#projectOptions#NewIncludePathEntry (arg, template)
  let args = split(a:arg)
  let cline = line('.')
  let ccol = col('.')
  for arg in args
    call s:MoveToInsertPosition()
    let line = line('.')
    call append(line, split(substitute(a:template, '<arg>', arg, 'g'), '\n'))
    call cursor(line + 1, 1)
  endfor
  call cursor(cline + 1, ccol)
endfunction " }}}

" MoveToInsertPosition() {{{
" If necessary moves the cursor to a valid insert position.
function! s:MoveToInsertPosition ()
  let start = search('<includepath\s*>', 'wn')
  let end = search('</includepath\s*>', 'wn')
  if line('.') < start || line('.') >= end
    call cursor(end - 1, 1)
  else
    let start = search('<includepathentry\s*>', 'n')
    let end = search('</includepathentry\s*>', 'cn')
    if end > start
      call cursor(end, 1)
    endif
  endif
endfunction " }}}

" UpdateIncludePath() {{{
" Updates the include path on the server w/ the changes made to the current file.
function! eclim#php#projectOptions#UpdateIncludePath ()
  let name = eclim#project#util#GetCurrentProjectName()
  let command = substitute(s:command_update, '<project>', name, '')

  let result = eclim#ExecuteEclim(command)
  if result =~ '|'
    let errors = eclim#util#ParseLocationEntries(split(result, '\n'))
    for error in errors
      let path = error.filename
      let error.filename = expand('%')
      let lnum = search("['\"]" . path . "['\"]", 'nw')
      if lnum > 0
        let error.lnum = lnum
      endif
    endfor
    call eclim#util#SetLocationList(errors)
  else
    call eclim#util#SetLocationList([], 'r')
    call eclim#util#Echo(result)
  endif
endfunction " }}}

" GetVariableNames() {{{
" Gets a list of all variable names.
function! eclim#php#projectOptions#GetVariableNames ()
  let variables = split(eclim#ExecuteEclim(s:command_variables), '\n')
  return map(variables, "substitute(v:val, '\\(.\\{-}\\)\\s.*', '\\1', '')")
endfunction " }}}

" VariableList() {{{
" Lists all the variables currently available.
function! eclim#php#projectOptions#VariableList ()
  let variables = split(eclim#ExecuteEclim(s:command_variables), '\n')
  if len(variables) == 0
    call eclim#util#Echo("No variables.")
  endif
  if len(variables) == 1 && variables[0] == '0'
    return
  endif
  exec "echohl " . g:EclimInfoHighlight
  redraw
  for variable in variables
    echom variable
  endfor
 echohl None
endfunction " }}}

" VariableCreate(name, path) {{{
" Create or update a variable.
function! eclim#php#projectOptions#VariableCreate (name, path)
  let command = s:command_variable_create
  let command = substitute(command, '<name>', a:name, '')
  let command = substitute(command, '<path>', fnamemodify(a:path, ':p'), '')

  let result = eclim#ExecuteEclim(command)
  if result != '0'
    call eclim#util#Echo(result)
  endif
endfunction " }}}

" VariableDelete(name) {{{
" Delete a variable.
function! eclim#php#projectOptions#VariableDelete (name)
  let command = s:command_variable_delete
  let command = substitute(command, '<name>', a:name, '')

  let result = eclim#ExecuteEclim(command)
  if result != '0'
    call eclim#util#Echo(result)
  endif
endfunction " }}}

" CommandCompleteVar(argLead, cmdLine, cursorPos) {{{
" Custom command completion for classpath var relative files.
function! eclim#php#projectOptions#CommandCompleteVar (argLead, cmdLine, cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')

  let vars = eclim#php#projectOptions#GetVariableNames()
  call filter(vars, 'v:val =~ "^' . argLead . '"')

  return vars
endfunction " }}}

" CommandCompleteVarPath(argLead, cmdLine, cursorPos) {{{
" Custom command completion for classpath var relative files.
function! eclim#php#projectOptions#CommandCompleteVarPath (argLead, cmdLine, cursorPos)
  let vars = split(eclim#ExecuteEclim(s:command_variables), '\n')

  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseArgs(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]

  let var_names = deepcopy(vars)
  call filter(var_names, 'v:val =~ "^' . argLead . '"')
  if len(var_names) > 0
    call map(var_names,
      \ "isdirectory(substitute(v:val, '.\\{-}\\s\\+- \\(.*\\)', '\\1', '')) ? " .
      \ "substitute(v:val, '\\(.\\{-}\\)\\s\\+-.*', '\\1', '') . '/' : " .
      \ "substitute(v:val, '\\(.\\{-}\\)\\s\\+-.*', '\\1', '')")
    return var_names
  endif

  let var = substitute(argLead, '\(.\{-}\)/.*', '\1', '')
  let var_dir = ""
  for cv in vars
    if cv =~ '^' . var
      let var_dir = substitute(cv, '.\{-}\s\+- \(.*\)', '\1', '')
      break
    endif
  endfor
  let argLead = substitute(argLead, var, var_dir, '')
  let files = eclim#util#CommandCompleteFile(argLead, a:cmdLine, a:cursorPos)
  call map(files, "substitute(v:val, '" . var_dir . "', '" . var . "', '')")

  return files
endfunction " }}}

" CommandCompleteVarAndDir(argLead, cmdLine, cursorPos) {{{
" Custom command completion for classpath var relative files.
function! eclim#php#projectOptions#CommandCompleteVarAndDir (argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseArgs(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]

  " complete dirs for first arg
  if cmdLine =~ '^' . args[0] . '\s*' . escape(argLead, '~.\') . '$'
    return eclim#php#projectOptions#CommandCompleteVar(argLead, a:cmdLine, a:cursorPos)
  endif

  return eclim#util#CommandCompleteDir(argLead, a:cmdLine, a:cursorPos)
endfunction " }}}

" vim:ft=vim:fdm=marker
