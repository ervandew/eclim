" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/maven/dependency.html
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
  let s:update_command = '-command project_update -p "<project>" -b "<build>"'

  let s:command_search =
    \ '-command maven_dependency_search ' .
    \ '-p "<project>" -f "<file>" -t "<type>" -s <query>'

  let s:dependency_template{'maven'} =
    \ "\t<dependency>\n" .
    \ "\t\t<groupId>${groupId}</groupId>\n" .
    \ "\t\t<artifactId>${artifactId}</artifactId>\n" .
    \ "\t\t<version>${version}</version>\n" .
    \ "\t</dependency>"

  let s:dependency_template{'mvn'} =
    \ "\t<dependency>\n" .
    \ "\t\t<groupId>${groupId}</groupId>\n" .
    \ "\t\t<artifactId>${artifactId}</artifactId>\n" .
    \ "\t\t<version>${version}</version>\n" .
    \ "\t\t<scope>compile</scope>\n" .
    \ "\t</dependency>"

  let s:dependency_template{'ivy'} =
    \ "\t<dependency org=\"${groupId}\" name=\"${artifactId}\" rev=\"${version}\"/>"
" }}}

function! eclim#java#maven#Search(query, type) " {{{
  " Searches online maven repository.

  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  update

  let filename = substitute(expand('%:p'), '\', '/', 'g')
  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()

  let command = s:command_search
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<type>', a:type, '')
  let command = substitute(command, '<query>', a:query, '')

  let results = eclim#Execute(command)
  if type(results) != g:LIST_TYPE
    return
  endif

  let content = []
  let groupId = ''
  for result in results
    if result.groupId != groupId
      let groupId = result.groupId
      call add(content, result.groupId)
    endif
    if result.existing
      let result.artifactId = '//' . result.artifactId
    endif
    call add(content, "\t" . result.artifactId . ' (' . result.version . ')')
  endfor

  let window_name = "Dependency_Search_Results"
  call eclim#util#TempWindowClear(window_name)

  if len(content)
    call eclim#util#TempWindow(window_name, content)

    let b:filename = filename
    let b:type = a:type
    setlocal ft=dependency_search_results
    syntax match Statement /^\w\+.*$/
    syntax match Identifier /(.\{-})/
    syntax match Comment /^\s*\/\/.*$/

    nnoremap <silent> <buffer> <cr> :call <SID>AddDependency(b:type)<cr>
  else
    call eclim#util#Echo('No results found.')
  endif
endfunction " }}}

function! s:AddDependency(type) " {{{
  let line = getline('.')
  if line =~ '^\s\+.*(.*)$' && line !~ '^\s*//'
    let artifact = substitute(line, '\s\+\(.*\) (.*)$', '\1', '')
    let vrsn = substitute(line, '.*(\(.*\))$', '\1', '')
    let group = getline(search('^\w\+', 'bnW'))

    let results_winnr = winnr()
    exec bufwinnr(b:filename) . "winc w"

    call s:InsertDependency(a:type, group, artifact, vrsn)
    exec results_winnr . "winc w"

    " mark dependency as added
    let line = substitute(line, '^\(\s*\)', '\1//', '')

    setlocal modifiable
    setlocal noreadonly
    call setline(line('.'), line)
    setlocal nomodifiable
    setlocal readonly
  endif
endfunction " }}}

function! s:InsertDependency(type, group, artifact, vrsn) " {{{
  let depend = deepcopy(s:dependency_template{a:type})
  let depend = substitute(depend, '\${groupId}', a:group, '')
  let depend = substitute(depend, '\${artifactId}', a:artifact, '')
  let depend = substitute(depend, '\${version}', a:vrsn, '')
  let dependency = split(depend, '\n')

  let lnum = search('</dependencies>', 'cnw')
  let insertDependenciesNode = 0
  if !lnum
    let lnum = search('<build>', 'cnw')
    if !lnum
      call eclim#util#EchoError('No <dependencies> node found.')
      return
    endif
    let insertDependenciesNode = 1
  endif

  let indent = substitute(getline(lnum), '^\(\s*\).*', '\1', '')
  call map(dependency, 'indent . v:val')

  if insertDependenciesNode
    call append(lnum - 1, indent . '</dependencies>')
    call append(lnum - 1, indent . '<dependencies>')
    let lnum += 1
  endif

  call append(lnum - 1, dependency)

  retab
endfunction " }}}

function! eclim#java#maven#SetClasspathVariable(cmd, variable, args) " {{{
  let instance = eclim#client#nailgun#ChooseEclimdInstance()
  if type(instance) != g:DICT_TYPE
    return
  endif

  let workspace = instance.workspace

  " maven 1.x
  if a:cmd == 'Maven'
    let prefs = workspace .
      \ '/.metadata/.plugins/org.eclipse.jdt.core/pref_store.ini'
    let command = a:cmd .
      \ ' "-Dmaven.eclipse.workspace=' . workspace . '"' .
      \ ' eclipse:add-maven-repo'

  " maven 2.x
  else
    let prefs = workspace .
      \ '/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.jdt.core.prefs'
    let command = a:cmd . ' ' . a:args .
      \ ' "-Declipse.workspace=' . workspace . '"' .
      \ ' eclipse:configure-workspace'
  endif

  call eclim#util#Exec(command)

  if !v:shell_error
    " the maven command edits the eclipse preference file directly, so in
    " order to get the value in memory without restarting eclim, we read the
    " value out and let the server set it again.
    let winrestore = winrestcmd()

    if filereadable(prefs)
      silent exec 'sview ' . prefs
      let line = search('org.eclipse.jdt.core.classpathVariable.' . a:variable, 'cnw')
      let value = line ? substitute(getline(line), '.\{-}=\(.*\)', '\1', '') : ''
      if line
        call eclim#java#classpath#VariableCreate(a:variable, value)
      endif

      if substitute(bufname('%'), '\', '/', 'g') =~ prefs
        close
        exec winrestore
      endif

      if line
        call eclim#util#Echo(a:variable . " classpath variable set to:\n" . value)
      else
        call eclim#util#EchoWarning(
          \ "Unable to locate " . a:variable . " classpath variable.\n" .
          \ "If it was successful set by maven, you may need to\n" .
          \ "restart eclipse for the change to take affect.")
      endif
    else
      call eclim#util#EchoWarning(
        \ "Unable to read:\n" . prefs . "\n" .
        \ "If the " . a:variable . " classpath variable was successfully set by maven\n" .
        \ "you may need to restart eclipse for the change to take affect.")
    endif
  endif
endfunction " }}}

function! eclim#java#maven#UpdateClasspath() " {{{
  " Updates the classpath on the server w/ the changes made to the current pom file.

  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  " validate the xml first
  if eclim#xml#validate#Validate(expand('%:p'), 0)
    return
  endif

  let name = eclim#project#util#GetCurrentProjectName()
  let command = s:update_command
  let command = substitute(command, '<project>', name, '')
  let command = substitute(command, '<build>', escape(expand('%:p'), '\'), '')
  let result = eclim#Execute(command)

  if type(result) == g:LIST_TYPE && len(result) > 0
    let errors = eclim#util#ParseLocationEntries(
      \ result, g:EclimValidateSortResults)
    call eclim#util#SetLocationList(errors, 'r')
    call eclim#util#EchoError(
      \ "Operation contained errors.  See location list for details (:lopen).")
  else
    call eclim#util#ClearLocationList()
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
