" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/maven/dependency.html
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
  let s:command_search =
    \ '-filter vim -command maven_dependency_search ' .
    \ '-p "<project>" -f "<file>" -s <query>'

  let s:dependency_scope = "\t\t<scope>compile</scope>"
  let s:dependency_template = [
    \ "\t<dependency>",
    \ "\t\t<groupId>${groupId}</groupId>",
    \ "\t\t<artifactId>${artifactId}</artifactId>",
    \ "\t\t<version>${version}</version>",
    \ "\t</dependency>" ]
" }}}

" Search(query) {{{
" Searches online maven repository.
function! eclim#java#maven#dependency#Search (query)
  update

  let filename = expand('%:p')
  let project = eclim#project#GetCurrentProjectName()

  let command = s:command_search
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', filename, '')
  let command = substitute(command, '<query>', a:query, '')

  call eclim#util#TempWindowCommand(command, "Maven_Dependency_Results")
  let b:filename = filename

  setlocal ft=maven_search_results
  syntax match Statement /^\w\+.*$/
  syntax match Identifier /(.\{-})/
  syntax match Comment /^\s*\/\/.*$/

  nnoremap <silent> <buffer> <cr> :call <SID>AddDependency()<cr>
endfunction " }}}

" AddDependency() {{{
function! s:AddDependency ()
  let line = getline('.')
  if line =~ '^\s\+.*(.*)$' && line !~ '^\s*//'
    let artifact = substitute(line, '\s\+\(.*\)\.\w\+ (.*)$', '\1', '')
    let vrsn = substitute(line, '.*(\(.*\))$', '\1', '')
    let group = getline(search('^\w\+', 'bnW'))

    let results_winnr = winnr()
    exec bufwinnr(b:filename) . "winc w"

    call s:InsertDependency(group, artifact, vrsn)
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

" InsertDependency(group, artifact, vrsn) {{{
function! s:InsertDependency (group, artifact, vrsn)
  let dependency = deepcopy(s:dependency_template)
  let dependency[1] = substitute(dependency[1], '\${groupId}', a:group, '')
  let dependency[2] = substitute(dependency[2], '\${artifactId}', a:artifact, '')
  let dependency[3] = substitute(dependency[3], '\${version}', a:vrsn, '')

  if expand('%') =~ 'pom.xml$'
    call insert(dependency, s:dependency_scope, 4)
  endif

  let lnum = search('</dependencies>', 'cnw')
  if !lnum
    call eclim#util#EchoError('No <dependencies> node found.')
  endif

  let indent = substitute(getline(lnum), '^\(\s*\).*', '\1', '')
  call map(dependency, 'indent . v:val')

  call append(lnum - 1, dependency)

  retab
endfunction " }}}

" vim:ft=vim:fdm=marker
