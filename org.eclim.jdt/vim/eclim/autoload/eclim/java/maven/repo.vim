" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/guides/java/maven/maven_eclipse.html
"   see http://eclim.org/guides/java/maven/mvn_eclipse.html
"
" License:
"
" Copyright (C) 2005 - 2010  Eric Van Dewoestine
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

" SetClasspathVariable(cmd variable) {{{
function eclim#java#maven#repo#SetClasspathVariable(cmd, variable)
  let workspace = eclim#eclipse#ChooseWorkspace()
  if workspace == '0'
    return
  endif

  let command = a:cmd .
    \ ' -Declipse.workspace=' . workspace .
    \ ' -Dmaven.eclipse.workspace=' . workspace .
    \ ' eclipse:add-maven-repo'
  call eclim#util#Exec(command)

  if !v:shell_error
    " the maven command edits the eclipse preference file directly, so in
    " order to get the value in memory without restarting eclim, we read the
    " value out and let the server set it again.
    let winrestore = winrestcmd()

    " maven 1.x
    if a:cmd == 'Maven'
      let prefs = workspace . '.metadata/.plugins/org.eclipse.jdt.core/pref_store.ini'

    " maven 2.x
    else
      let prefs = workspace . '.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.jdt.core.prefs'
    endif

    if filereadable(prefs)
      silent exec 'sview ' . prefs
      let line = search('org.eclipse.jdt.core.classpathVariable.' . a:variable, 'cnw')
      if line
        let value = substitute(getline(line), '.\{-}=\(.*\)', '\1', '')
        call eclim#java#classpath#VariableCreate(a:variable, value)
      endif

      if substitute(bufname('%'), '\', '/', 'g') =~ prefs
        close
        exec winrestore
      endif
    endif
  endif

endfunction " }}}

" vim:ft=vim:fdm=marker
