" Author:  Daniel Leong
"
" License: {{{
"
" Copyright (C) 2005 - 2014  Eric Van Dewoestine
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

" Script Varables {{{
  let s:command_new = '-command java_new -p "<project>" -t "<type>" -n "<name>"'

  let s:java_types = ['class', 'abstract', 'interface', 'enum', '@interface']

" }}}

function! eclim#java#new#Create(type, name) " {{{
  " Create a new Java Type

  if !eclim#project#util#IsCurrentFileInProject(0)
    return
  endif

  let project = eclim#project#util#GetCurrentProjectName()
  let myPackage = eclim#java#util#GetPackage()

  " prepend current package if necessary
  let newName = a:name
  if newName !~ '\.' && len(myPackage) > 0
    let newName = myPackage . '.' . newName
  endif

  let command = s:command_new
  let command = substitute(command, "<project>", project, '')
  let command = substitute(command, "<type>", a:type, '')
  let command = substitute(command, "<name>", newName, '')

  let result = eclim#Execute(command)
  if g:DICT_TYPE != type(result)
    return
  endif

  if has_key(result, "error")
    call eclim#util#EchoError(result.error)
    return
  endif

  let path = result.path
  call eclim#util#GoToBufferWindowOrOpen(path, g:EclimJavaNewOpenAction)
endfunction " }}}

function! eclim#java#new#CommandComplete(argLead, cmdLine, cursorPos) " {{{
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)
  if len(args) < 2 || (len(args) == 2 && cmdLine !~ '\s$')
    " Suggest java types
    let arg = substitute(a:argLead, '@', '\\@', '')
    return filter(copy(s:java_types), 'v:val =~ "^' . arg . '"')
  endif

  if len(a:argLead) <= 3 || a:argLead =~ '\.'
    " Propose packages
    return eclim#java#util#CommandCompletePackage(
      \ a:argLead, a:cmdLine, a:cursorPos)
  endif

  return []
endfunction " }}}


" vim:ft=vim:fdm=marker
