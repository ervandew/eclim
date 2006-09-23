" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/junit.html
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
let s:command_impl =
  \ '-filter vim -command java_junit_impl -p "<project>" -f "<file>" <base>'
let s:command_insert =
  \ '-filter vim -command java_junit_impl -p "<project>" -f "<file>" <base> ' .
  \ '-t "<type>" -s <superType> <methods>'
" }}}

" JUnitExecute(test) {{{
" Execute the supplied test, or if none supplied, the current test.
function! eclim#java#junit#JUnitExecute (test)
  let test = a:test
  if test == ''
    let test = substitute(eclim#java#util#GetFullyQualifiedClassname(), '\.', '/', 'g')
  endif

  let command = eclim#project#GetProjectSetting("org.eclim.java.junit.command")
  if command == ''
    call eclim#util#EchoWarning(
      \ "Command setting for 'junit' not set. " .
      \ "Use :Settings or :ProjectSettings to set it.")
    return
  endif

  let command = substitute(command, '<testcase>', test, 'g')
  echom 'command = ' . command
  exec command
endfunction " }}}

" JUnitResult(test) {{{
" Argument test can be one of the following:
"   Empty string: Use the current file to determine the test result file.
"   Class name of a test: Locate the results for class (ex. 'TestMe').
"   The results dir relative results file name: TEST-org.foo.TestMe.xml
function! eclim#java#junit#JUnitResult (test)
  let path = s:GetResultsDir()
  if path == '' || path == '/'
    call eclim#util#EchoWarning(
      \ "Output directory setting for 'junit' not set. " .
      \ "Use :Settings or :ProjectSettings to set it.")
    return
  endif

  if a:test != ''
    let file = a:test
    if file !~ '^TEST-'
      let file = '*' . file
    endif
  else
    let file = substitute(eclim#java#util#GetFullyQualifiedClassname(), '\.', '/', 'g')
  endif

  if file !~ '^TEST-'
    let file = substitute(file, '\/', '.', 'g')
    let file = 'TEST-' . file . '.xml'
  endif

  let found = eclim#util#Globpath(path, file)

  " try text version if xml not found.
  if found == ""
    let file = fnamemodify(file, ':r') . '.txt'
    let found = eclim#util#Globpath(path, file)
  endif

  if found != ""
    let filename = expand('%:p')
    exec "botright split " . found

    let b:filename = filename
    augroup temp_window
      autocmd! BufUnload <buffer>
      autocmd BufUnload <buffer> call eclim#util#GoToBufferWindow(b:filename)
    augroup END

    return
  endif
  call eclim#util#Echo("Test result file not found for: " . fnamemodify(file, ':r'))
endfunction " }}}

" JUnitImpl() {{{
" Opens a window that allows the user to choose methods to implement tests
" for.
function! eclim#java#junit#JUnitImpl ()
  if !eclim#project#IsCurrentFileInProject()
    return
  endif

  call eclim#java#util#SilentUpdate()

  let project = eclim#project#GetCurrentProjectName()

  let command = s:command_impl
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', eclim#java#util#GetFilename(), '')
  let base = substitute(expand('%:t'), 'Test', '', '')
  let base = substitute(eclim#java#util#GetPackage(), '\.', '/', 'g') . "/" . base
  if eclim#java#util#FileExists(base)
    let base = fnamemodify(base, ':r')
    let base = substitute(base, '/', '.', 'g')
    let command = substitute(command, '<base>', '-b ' . base, '')
  else
    let base = ""
    let command = substitute(command, '<base>', '', '')
  endif

  call eclim#java#junit#JUnitImplWindow(command)
  let b:base = base
endfunction " }}}

" JUnitImplWindow(command) {{{
function! eclim#java#junit#JUnitImplWindow (command)
  let name = eclim#java#util#GetFilename() . "_impl"
  if eclim#util#TempWindowCommand(a:command, name)
    setlocal ft=java
    call eclim#java#impl#ImplWindowFolding()

    nnoremap <silent> <buffer> <cr> :call <SID>AddTestImpl(0)<cr>
    vnoremap <silent> <buffer> <cr> :<C-U>call <SID>AddTestImpl(1)<cr>
  endif
endfunction " }}}

" AddTestImpl(visual) {{{
function! s:AddTestImpl (visual)
  let command = s:command_insert
  if b:base != ""
    let command = substitute(command, '<base>', '-b ' . b:base, '')
  else
    let command = substitute(command, '<base>', '', '')
  endif

  call eclim#java#impl#ImplAdd
    \ (command, function("eclim#java#junit#JUnitImplWindow"), a:visual)
endfunction " }}}

" GetResultsDir() {{{
function s:GetResultsDir ()
  let path = eclim#project#GetProjectSetting("org.eclim.java.junit.output_dir")
  let path = substitute(path, '<project>', eclim#project#GetCurrentProjectRoot(), '')
  let path = path !~ '/$' ? path . '/' : path
  return path
endfunction " }}}

" CommandCompleteTest(argLead, cmdLine, cursorPos) {{{
" Custom command completion for junit test cases.
function eclim#java#junit#CommandCompleteTest (argLead, cmdLine, cursorPos)
  return eclim#java#test#CommandCompleteTest('junit', a:argLead, a:cmdLine, a:cursorPos)
endfunction " }}}

" CommandCompleteResult(argLead, cmdLine, cursorPos) {{{
" Custom command completion for test case results.
function! eclim#java#junit#CommandCompleteResult (argLead, cmdLine, cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')

  let path = s:GetResultsDir()
  if path == '' || path == '/'
    call eclim#util#EchoWarning(
      \ "Output directory setting for 'junit' not set. " .
      \ "Use :Settings or :ProjectSettings to set it.")
    return []
  endif

  let results = split(eclim#util#Globpath(path, '*'), '\n')
  call map(results, 'fnamemodify(v:val, ":r:e")')
  call filter(results, 'v:val =~ "^' . argLead . '"')

  return results
endfunction " }}}

" vim:ft=vim:fdm=marker
