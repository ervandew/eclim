" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/junit.html
"
" License:
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

" Script Variables {{{
let s:command_junit = '-command java_junit -p "<project>"'
let s:command_tests = '-command java_junit_tests -p "<project>"'
let s:command_find_test =
  \ '-command java_junit_find_test -p "<project>" -f "<file>" ' .
  \ '-o <offset> -e <encoding>'
let s:command_impl = '-command java_junit_impl -p "<project>" -f "<file>"'
let s:command_insert =
  \ '-command java_junit_impl -p "<project>" -f "<file>" ' .
  \ '-t "<type>" -s "<superType>" <methods>'
" }}}

function! eclim#java#junit#JUnit(test, bang) " {{{
  let project = eclim#project#util#GetCurrentProjectName()
  if project == '' && exists('b:project')
    let project = b:project
  endif

  if project == ''
    call eclim#project#util#IsCurrentFileInProject()
    return
  endif

  Validate
  if len(getloclist(0)) > 0
    call eclim#util#EchoError('Test case contains validation errors.')
    return
  endif

  let command = s:command_junit
  let command = substitute(command, '<project>', project, '')
  if a:test != ''
    if a:test == '%'
      let command .= ' -f "' . eclim#project#util#GetProjectRelativeFilePath() . '"'
    elseif a:test != '*'
      let command .= ' -t "' . a:test . '"'
    endif
  else
    let command .= ' -f "' . eclim#project#util#GetProjectRelativeFilePath() . '"'
    let command .= ' -o ' . eclim#util#GetOffset()
    let command .= ' -e ' . eclim#util#GetEncoding()
  endif

  let curbuf = bufnr('%')
  let result = eclim#Execute(command, {'project': project, 'exec': 1, 'raw': 1})
  let results = split(substitute(result, "^\n*", '', 'g'), "\n")
  let statusLine = matchlist(result,
    \ 'Tests run:.*Failures: \([0-9]*\), Errors: \([0-9]*\), [^\n]*sec')
  if len(statusLine) >= 3 && statusLine[1] == '0' && statusLine[2] == '0'
    let name = eclim#util#EscapeBufferName('[JUnit Output]')
    if bufwinnr(name) != -1
      " close existing output window; we've fixed the issue
      let curwinnr = winnr()
      exec bufwinnr(name) . "winc w"
      quit
      exec curwinnr . "winc w"
    endif
    call eclim#util#EchoSuccess(statusLine[0])
  elseif result != '0'
    " if result == '0', then there was some error;
    "  results won't have anything interesting anyway
    call eclim#util#TempWindow('[JUnit Output]', results)
  endif
  let b:project = project

  if exists(":JUnit") != 2
    command -buffer -nargs=? -complete=customlist,eclim#java#junit#CommandCompleteTest
      \ JUnit :call eclim#java#junit#JUnit('<args>', '<bang>')
  endif

  exec bufwinnr(curbuf) . 'winc w'
endfunction " }}}

function! eclim#java#junit#JUnitFindTest() " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  runtime eclim/autoload/eclim/java/search.vim

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#lang#SilentUpdate()
  let command = s:command_find_test
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<offset>', eclim#util#GetOffset(), '')
  let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')
  let result = eclim#Execute(command)
  if type(result) == g:STRING_TYPE
    call eclim#util#EchoError(result)
    return
  endif

  if type(result) != g:DICT_TYPE
    return
  endif

  let name = substitute(result.filename, '\', '/', 'g')
  call eclim#util#GoToBufferWindowOrOpen(name, g:EclimJavaSearchSingleResult)
  call cursor(result.line, result.column)
endfunction " }}}

function! eclim#java#junit#JUnitResult(test) " {{{
  " Argument test can be one of the following:
  "   Empty string: Use the current file to determine the test result file.
  "   Class name of a test: Locate the results for class (ex. 'TestMe').
  "   The results dir relative results file name: TEST-org.foo.TestMe.xml

  let path = s:GetResultsDir()
  if path == ''
    call eclim#util#EchoWarning(
      \ "Output directory setting for 'junit' not set. " .
      \ "Use :WorkspaceSettings or :ProjectSettings to set it.")
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
    exec "below split " . escape(found, ' ')

    augroup temp_window
      autocmd! BufWinLeave <buffer>
      call eclim#util#GoToBufferWindowRegister(filename)
    augroup END

    return
  endif
  call eclim#util#Echo("Test result file not found for: " . fnamemodify(file, ':r'))
endfunction " }}}

function! eclim#java#junit#JUnitImpl() " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  call eclim#lang#SilentUpdate()

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()
  let command = s:command_impl
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  call eclim#java#junit#JUnitImplWindow(command)
endfunction " }}}

function! eclim#java#junit#JUnitImplWindow(command) " {{{
  if (eclim#java#impl#Window(a:command, "impl"))
    nnoremap <silent> <buffer> <cr> :call <SID>AddTestImpl(0)<cr>
    vnoremap <silent> <buffer> <cr> :<C-U>call <SID>AddTestImpl(1)<cr>
  endif
endfunction " }}}

function! s:AddTestImpl(visual) " {{{
  call eclim#java#impl#Add
    \ (s:command_insert, function("eclim#java#junit#JUnitImplWindow"), a:visual)
endfunction " }}}

function! s:GetResultsDir() " {{{
  let path = eclim#project#util#GetProjectSetting("org.eclim.java.junit.output_dir")
  if type(path) == g:NUMBER_TYPE
    return
  endif

  let root = eclim#project#util#GetCurrentProjectRoot()
  let path = substitute(path, '<project>', root, '')
  let path = path != '' && path !~ '/$' ? path . '/' : path
  if path != '' && has('win32unix')
    let path = eclim#cygwin#CygwinPath(path)
  endif
  return path
endfunction " }}}

function! eclim#java#junit#CommandCompleteTest(argLead, cmdLine, cursorPos) " {{{
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')

  let project = eclim#project#util#GetCurrentProjectName()
  if project == '' && exists('b:project')
    let project = b:project
  endif
  if project == ''
    return []
  endif

  let command = s:command_tests
  let command = substitute(command, '<project>', project, '')
  let results = eclim#Execute(command)
  if type(results) != g:LIST_TYPE
    return []
  endif

  call filter(results, 'v:val =~ "' . argLead . '"')
  return results
endfunction " }}}

function! eclim#java#junit#CommandCompleteResult(argLead, cmdLine, cursorPos) " {{{
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')

  let path = s:GetResultsDir()
  if path == ''
    call eclim#util#EchoWarning(
      \ "Output directory setting for 'junit' not set. " .
      \ "Use :WorkspaceSettings or :ProjectSettings to set it.")
    return []
  endif

  let results = split(eclim#util#Globpath(path, '*'), '\n')
  call map(results, 'fnamemodify(v:val, ":r:e")')
  call filter(results, 'v:val =~ "^' . argLead . '"')

  return results
endfunction " }}}

" vim:ft=vim:fdm=marker
