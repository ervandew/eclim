" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Plugin providing junit like framework for unit testing vim scripts.
"   Based heavily on vim_unit.vim by Staale Flock:
"     http://www.vim.org/scripts/script.php?script_id=1125
"
" Todo:
"   - Add means to run all test functions from all .vim files in a given
"     directory.
"   - Add means to edit a specific file prior to calling a test file.
"   - Support xml output?
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

" Global Variables {{{
  if !exists('g:vimUnitOutputDir')
    " Sets the output directory where test results will be written to.
    " Default value '' forces all output to be written to the screen (echo).
    let g:vimUnitOutputDir = ''
  endif

  if !exists('g:vimUnitVerbosity')
    " At the moment there is just 0 (quiet) and 1(verbose)
    " Only used if output is written to screen instead of a result file.
    let g:vimUnitVerbosity = 1
  endif
" }}}

" Script Variables {{{
  let s:testRunCount = 0
  let s:testRunFailureCount = 0

  let s:suite_methods = []

  let s:function_regex = '^\s*fu\%[nction]\%[!]\s\+\(.\{-}\)\s*(\s*).*$'
  let s:non_failure_regex = '^\%(\%(^Fail.*$\)\@!.\)*$'
" }}}

" VUAssertEquals(arg1, arg2, ...) {{{
" Compares the two arguments to determine if they are equal.
function! VUAssertEquals(arg1, arg2, ...)
  if a:arg1 != a:arg2
    let message = '"' . a:arg1 . '" != "' . a:arg2 . '"'
    if a:0 > 0
      let message = a:1 . ' (' . message . ')'
    endif
    throw 'AssertEquals: ' . message
  endif
endfunction " }}}

" VUAssertTrue(arg1, ...) {{{
" Determines if the supplied argument is true.
function! VUAssertTrue(arg1, ...)
  if !a:arg1
    let message = '"' . a:arg1 . '" is not true.'
    if a:0 > 0
      let message = a:1 . ' (' . message . ')'
    endif
    throw 'AssertTrue: ' . message
  endif
endfunction " }}}

" VUAssertFalse(arg1, ...) {{{
" Determines if the supplied argument is false.
function! VUAssertFalse(arg1, ...)
  if a:arg1 || type(a:arg1) != 0
    let message = '"' . a:arg1 . '" is not false.'
    if a:0 > 0
      let message = a:1 . ' (' . message . ')'
    endif
    throw 'AssertFalse: ' . message
  endif
endfunction " }}}

" VUFail (...) {{{
" Fails the current test.
function! VUFail(...)
  let message = a:0 > 0 ? a:1 : ''
  throw 'Fail: ' . message
endfunction " }}}

" VUAddTest (test) {{{
" Used in Suite() functions to add a test function to the test suite.
function! VUAddTest(test)
  call add(s:suite_methods, a:test)
endfunction " }}}

" VURunnerRunTests(basedir, testfile, ...) {{{
" Runs the supplied test case.
" basedir - The base directory where the file is located.  Used to construct
"           the output file (testfile with basedir stripped off).
" testfile - The basedir relative test file to run.
if !exists('*VURunnerRunTests')
function! VURunnerRunTests(basedir, testfile)
  call s:Init(a:basedir, a:testfile)

  echo "Running: " . a:testfile

  let tests = s:GetTestFunctionNames()

  if exists('s:vimUnitOutputFile')
    exec 'redir >> ' . s:vimUnitOutputFile
  endif

  exec 'source ' . s:vimUnitTestFile

  if exists('*BeforeTestCase')
    call BeforeTestCase()
  endif

  for test in tests
    call s:RunTest(test)
  endfor

  if exists('*AfterTestCase')
    call AfterTestCase()
  endif

  if exists('s:vimUnitOutputFile')
    call s:PrintResults(a:testfile)
    redir END
    unlet s:vimUnitOutputFile
  endif

  call s:PrintResults(a:testfile)
endfunction
endif " }}}

" Init(basedir, testfile) {{{
function! s:Init(basedir, testfile)
  let s:testRunCount = 0
  let s:testRunFailureCount = 0
  let s:suite_methods = []

  unlet! s:vimUnitOutputFile

  silent! delfunction BeforeTestCase
  silent! delfunction SetUp
  silent! delfunction TearDown
  silent! delfunction AfterTestCase

  let s:vimUnitTestFile = fnamemodify(a:basedir . '/' . a:testfile, ':p')

  if !exists('s:vimUnitOutputDir')
    let g:vimUnitOutputDir = expand(g:vimUnitOutputDir)
    let s:vimUnitOutputDir = g:vimUnitOutputDir
  endif

  if s:vimUnitOutputDir != ''
    " check if directory exists, if not try to create it.
    if !isdirectory(g:vimUnitOutputDir)
      " FIXME: fix to create parent directories as necessary
      call mkdir(g:vimUnitOutputDir)
      if !isdirectory(g:vimUnitOutputDir)
        echoe "Directory '" . g:vimUnitOutputDir . "' does not exist and could not be created. " . "All output will be written to the screen."
        let s:vimUnitOutputDir = ''
        return
      endif
    endif

    " construct output file to use.
    if !exists('s:vimUnitOutputFile')
      let file = a:testfile

      " remove file extension
      let file = fnamemodify(file, ':r')
      " remove spaces, leading path separator, and drive letter
      let file = substitute(file, '\(\s\|^[a-zA-Z]:/\|^/\)', '', 'g')
      " substitute all path separators with '.'
      let file = substitute(file, '\(/\|\\\)', '.', 'g')

      let s:vimUnitOutputFile = s:vimUnitOutputDir . '/TEST-' . file . '.txt'

      " write output to the file
      call delete(s:vimUnitOutputFile)
    endif
  endif
endfunction " }}}

" RunTest(test) {{{
function! s:RunTest(test)
  if exists('*SetUp')
    call SetUp()
  endif

  try
    let s:testRunCount += 1
    call {a:test}()
  catch
    let s:testRunFailureCount += 1
    call s:Output(v:throwpoint)
    call s:Output('  ' . v:exception)
  endtry

  if exists('*TearDown')
    call TearDown()
  endif
endfunction " }}}

" PrintResults(testfile) {{{
function! s:PrintResults(testfile)
  let statistics = 'Tests run: ' . s:testRunCount . ', '
  let statistics .= 'Failures: ' . s:testRunFailureCount

  if s:testRunFailureCount > 0
    let statistics .= "\nTest " . a:testfile . " FAILED"
  endif

  call s:Output('')
  call s:Output(statistics)

  return statistics
endfunction " }}}

" Output(message) {{{
function! s:Output(message)
  if exists('s:vimUnitOutputFile')
    silent echo a:message
  else
    if g:vimUnitVerbosity > 0
      echo a:message
    endif
  endif
endfunction " }}}

" GetTestFunctionNames() " {{{
function! s:GetTestFunctionNames ()
  let winreset = winrestcmd()
  let results = []
  try
    new
    silent exec 'view ' . s:vimUnitTestFile

    call cursor(1, 1)

    while search(s:function_regex, 'cW')
      let name = substitute(getline('.'), s:function_regex, '\1', '')
      if name == 'Suite'
        call Suite()
        return s:suite_methods
      endif
      call add(results, name)
      call cursor(line('.') + 1, 1)
    endwhile

  finally
    close
    exec winreset
  endtry
  return filter(results, 'v:val =~ "^Test"')
endfunction " }}}

" VimUnitTest() {{{
" Executes test suite for testing vimunit.
function! VimUnitTest ()
  let vimunit = findfile('plugin/vunit.vim', escape(&runtimepath, ' '))

  " not in runtime path, search :scriptnames.
  if vimunit == ''
    redir => scripts
    silent scriptnames
    redir END

    let names = split(scripts, '\n')
    call filter(names, 'v:val =~ "\\<vunit.vim$"')
    if len(names) == 1
      let vimunit = substitute(names[0], '\s*[0-9]\+:\s\(.*\)', '\1', '')
    endif
  endif

  if vimunit == ''
    echoe 'Unable to find vunit.vim location.'
    return
  endif

  call VURunnerRunTests(fnamemodify(vimunit, ':h'), fnamemodify(vimunit, ':p'))
endfunction " }}}

" Suite() {{{
function! Suite()
  call VUAddTest('TestVUAssertEquals')
  call VUAddTest('TestVUAssertTrue')
  call VUAddTest('TestVUAssertFalse')
  call VUAddTest('TestVUFail')
endfunction " }}}

" TestVUAssertEquals() {{{
function! TestVUAssertEquals()
    call VUAssertEquals(1,1,'Simple test comparing numbers')

    try
      call VUAssertEquals(1, 2, 'Simple test comparing numbers.')
      throw "Should have failed."
    catch /^Assert/
    endtry

    call VUAssertEquals('str1','str1','Simple test comparing two strings')

    try
      call VUAssertEquals('str1', 'str2',
        \ 'Simple test comparing two diffrent strings.')
      throw "Should have failed."
    catch /^Assert/
    endtry

    call VUAssertEquals(123, '123',
      \ 'Simple test comparing number and string containing number')

    try
      call VUAssertEquals(123, '321',
        \ 'Simple test comparing number and string containing diffrent number.')
      throw "Should have failed."
    catch /^Assert/
    endtry
endfunction " }}}

" TestVUAssertTrue() {{{
function! TestVUAssertTrue()
  call VUAssertTrue(1, 'Simple test.')

  try
    call VUAssertTrue(0)
    throw "Should have failed."
  catch /^Assert/
  endtry

  try
    call VUAssertTrue("test")
    throw "Should have failed."
  catch /^Assert/
  endtry

  try
    call VUAssertTrue("")
    throw "Should have failed."
  catch /^Assert/
  endtry
endfunction " }}}

" TestVUAssertFalse() {{{
function! TestVUAssertFalse()
  call VUAssertFalse(0, 'Simple test.')

  try
    call VUAssertFalse(1)
    throw "Should have failed."
  catch /^Assert/
  endtry

  try
    call VUAssertFalse("test")
    throw "Should have failed."
  catch /^Assert/
  endtry

  try
    call VUAssertFalse("")
    throw "Should have failed."
  catch /^Assert/
  endtry
endfunction " }}}

" TestVUFail() {{{
function! TestVUFail()
  try
    call VUFail('Test fail.')
    throw "Should have failed."
  catch /^Fail/
  endtry
endfunction " }}}

" vim:ft=vim:fdm=marker
