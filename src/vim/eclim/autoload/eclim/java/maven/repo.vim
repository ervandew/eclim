" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   see http://eclim.sourceforge.net/guides/java/maven/maven_eclipse.html
"   see http://eclim.sourceforge.net/guides/java/maven/mvn_eclipse.html
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

" SetClasspathVariable(cmd variable) {{{
function eclim#java#maven#repo#SetClasspathVariable (cmd, variable)
  let workspace = eclim#eclipse#GetWorkspaceDir()
  if workspace == ''
    return
  endif

  let command = a:cmd .
    \ ' -Declipse.workspace=' . workspace .
    \ ' -Dmaven.eclipse.workspace=' . workspace .
    \ ' eclipse:add-maven-repo'
  exec command

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
      silent exec 'split ' . prefs
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
