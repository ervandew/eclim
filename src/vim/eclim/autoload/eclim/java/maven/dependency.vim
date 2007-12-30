" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/maven/dependency.html
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

" Search(query, type) {{{
" Searches online maven repository.
function! eclim#java#maven#dependency#Search (query, type)
  update

  let filename = substitute(expand('%:p'), '\', '/', 'g')
  let project = eclim#project#util#GetCurrentProjectName()

  let command = s:command_search
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', filename, '')
  let command = substitute(command, '<type>', a:type, '')
  let command = substitute(command, '<query>', a:query, '')

  if eclim#util#TempWindowCommand(command, "Dependency_Search_Results")
    let b:filename = filename

    setlocal ft=dependency_search_results
    syntax match Statement /^\w\+.*$/
    syntax match Identifier /(.\{-})/
    syntax match Comment /^\s*\/\/.*$/

    let b:type = a:type
    nnoremap <silent> <buffer> <cr> :call <SID>AddDependency(b:type)<cr>
  endif
endfunction " }}}

" AddDependency(type) {{{
function! s:AddDependency (type)
  let line = getline('.')
  if line =~ '^\s\+.*(.*)$' && line !~ '^\s*//'
    let artifact = substitute(line, '\s\+\(.*\)\.\w\+ (.*)$', '\1', '')
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

" InsertDependency(group, artifact, vrsn) {{{
function! s:InsertDependency (type, group, artifact, vrsn)
  let depend = deepcopy(s:dependency_template{a:type})
  let depend = substitute(depend, '\${groupId}', a:group, '')
  let depend = substitute(depend, '\${artifactId}', a:artifact, '')
  let depend = substitute(depend, '\${version}', a:vrsn, '')
  let dependency = split(depend, '\n')

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
