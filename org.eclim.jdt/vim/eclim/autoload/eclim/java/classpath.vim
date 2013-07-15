" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/classpath.html
"
" License:
"
" Copyright (C) 2005 - 2013  Eric Van Dewoestine
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
  let s:command_variables = '-command java_classpath_variables'
  let s:command_variable_create =
    \ '-command java_classpath_variable_create -n "<name>" -p "<path>"'
  let s:command_variable_delete =
    \ '-command java_classpath_variable_delete -n "<name>"'

  let s:entry_project =
    \ "\t<classpathentry exported=\"true\" kind=\"src\" path=\"/<path>\"/>"
  let s:entry ="\t<classpathentry kind=\"<kind>\" path=\"<path>\"/>"
  let s:entry_sourcepath =
    \ "\t<classpathentry kind=\"<kind>\" path=\"<path>\"\n\t\t\tsourcepath=\"<src>\"/>"
  let s:entry_javadoc =
    \ "\t<classpathentry kind=\"<kind>\" path=\"<path>\"\n" .
    \ "\t\t\tsourcepath=\"<src>\">\n" .
    \ "\t\t<attributes>\n" .
    \ "\t\t\t<attribute name=\"javadoc_location\" value=\"<javadoc>\"/>\n" .
    \ "\t\t</attributes>\n" .
    \ "\t</classpathentry>"
" }}}

function! eclim#java#classpath#NewClasspathEntry(kind, arg, ...) " {{{
  " Adds a new entry to the current .classpath file.
  let template_name = 's:entry'
  let args = {'kind': a:kind, 'path': substitute(a:arg, '\', '/', 'g')}
  if a:0
    if a:0 == 1
      let template_name = 's:entry_sourcepath'
      let args['src'] = substitute(a:1, '\', '/', 'g')
    elseif a:0 == 2
      let template_name = 's:entry_javadoc'
      let args['src'] = substitute(a:1, '\', '/', 'g')
      let javadoc = substitute(a:2, '\', '/', 'g')
      let absolute = javadoc =~? '^\([a-z]:\)\?/'

      " handle absolute vs project relative javadoc location
      if absolute
        " windows paths need a leading slash
        if javadoc =~? '^[a-z]:/'
          let javadoc = '/' . javadoc
        endif
        let javadoc = 'file:' . javadoc
      else
        if !eclim#project#util#IsCurrentFileInProject(1)
          return
        endif
        if javadoc =~? '\.jar$'
          let project = eclim#project#util#GetCurrentProjectName()
          let javadoc = 'platform:/resource/' . project . '/' . javadoc
        else
          " relative dirs must be made absolute
          let project = eclim#project#util#GetCurrentProjectRoot()
          let javadoc = project . '/' . javadoc
        endif
      endif

      if javadoc =~? '\.jar$'
        let javadoc = 'jar:' . javadoc . '!/'
      elseif javadoc !~ '^file:'
        let javadoc = 'file:' . javadoc
      endif
      let args['javadoc'] = javadoc
    else
      call eclim#util#EchoError('Too many arguments.')
      return
    endif
  endif

  if exists(template_name . '_' . a:kind)
    let template = {template_name}_{a:kind}
  else
    let template = {template_name}
  endif

  for [key, value] in items(args)
    let template = substitute(template, '<' . key . '>', value, 'g')
  endfor

  let cline = line('.')
  let ccol = col('.')
  call s:MoveToInsertPosition()
  let line = line('.')
  call append(line, split(template, '\n'))
  call cursor(cline + 1, ccol)
endfunction " }}}

function! s:MoveToInsertPosition() " {{{
  " If necessary moves the cursor to a valid insert position.
  let start = search('<classpath\s*>', 'wn')
  let end = search('</classpath\s*>', 'wn')
  if line('.') < start || line('.') >= end
    call cursor(end - 1, 1)
  endif
endfunction " }}}

function! eclim#java#classpath#GetVariableNames() " {{{
  let variables = eclim#Execute(s:command_variables)
  if type(variables) != g:LIST_TYPE
    return []
  endif
  return map(variables, "v:val.name")
endfunction " }}}

function! eclim#java#classpath#VariableList() " {{{
  let variables = eclim#Execute(s:command_variables)
  if type(variables) != g:LIST_TYPE
    return
  endif
  if len(variables) == 0
    call eclim#util#Echo("No variables.")
  endif

  let pad = 0
  for variable in variables
    let pad = len(variable.name) > pad ? len(variable.name) : pad
  endfor

  let output = []
  for variable in variables
    call add(output, eclim#util#Pad(variable.name, pad) . ' - ' . variable.path)
  endfor

  call eclim#util#Echo(join(output, "\n"))
endfunction " }}}

function! eclim#java#classpath#VariableCreate(name, path) " {{{
  let path = substitute(fnamemodify(a:path, ':p'), '\', '/', 'g')
  if has('win32unix')
    let path = eclim#cygwin#WindowsPath(path)
  endif
  let command = s:command_variable_create
  let command = substitute(command, '<name>', a:name, '')
  let command = substitute(command, '<path>', path, '')

  let result = eclim#Execute(command)
  if result != '0'
    call eclim#util#Echo(result)
  endif
endfunction " }}}

function! eclim#java#classpath#VariableDelete(name) " {{{
  let command = s:command_variable_delete
  let command = substitute(command, '<name>', a:name, '')

  let result = eclim#Execute(command)
  if result != '0'
    call eclim#util#Echo(result)
  endif
endfunction " }}}

function! eclim#java#classpath#CommandCompleteVar(argLead, cmdLine, cursorPos) " {{{
  " Custom command completion for classpath var relative files.
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')

  let vars = eclim#java#classpath#GetVariableNames()
  call filter(vars, 'v:val =~ "\\M^' . argLead . '"')

  return vars
endfunction " }}}

function! eclim#java#classpath#CommandCompleteVarPath(argLead, cmdLine, cursorPos) " {{{
  " Custom command completion for classpath var relative files.
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]

  let vars = eclim#Execute(s:command_variables)

  " just the variable name
  if argLead !~ '/'
    let var_names = deepcopy(vars)
    call filter(var_names, 'v:val.name =~ "^' . argLead . '"')
    if len(var_names) > 0
      call map(var_names,
        \ "isdirectory(v:val.path) ? v:val.name . '/' : v:val.name")
    endif
    return var_names
  endif

  " variable name + path
  let var = substitute(argLead, '\(.\{-}\)/.*', '\1', '')
  let var_dir = ""
  for cv in vars
    if cv.name =~ '^' . var
      let var_dir = cv.path
      break
    endif
  endfor
  if var_dir == ''
    return []
  endif

  let var_dir = escape(substitute(var_dir, '\', '/', 'g'), ' ')
  let argLead = substitute(argLead, var, var_dir, '')
  let files = eclim#util#CommandCompleteFile(argLead, a:cmdLine, a:cursorPos)
  let replace = escape(var_dir, '\')
  call map(files, "substitute(v:val, '" . replace . "', '" . var . "', '')")

  return files
endfunction " }}}

function! eclim#java#classpath#CommandCompleteVarAndDir(argLead, cmdLine, cursorPos) " {{{
  " Custom command completion for classpath var relative files.
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]

  " complete vars for first arg
  if cmdLine =~ '^' . args[0] . '\s*' . escape(argLead, '~.\') . '$'
    return eclim#java#classpath#CommandCompleteVar(argLead, a:cmdLine, a:cursorPos)
  endif

  return eclim#util#CommandCompleteDir(a:argLead, a:cmdLine, a:cursorPos)
endfunction " }}}

" vim:ft=vim:fdm=marker
