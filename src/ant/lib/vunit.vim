" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Plugin providing junit like framework for unit testing vim scripts.
"   Based heavily on vim_unit.vim by Staale Flock:
"     http://www.vim.org/scripts/script.php?script_id=1125
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

" Global Variables {{{
  if !exists('g:vimUnitOutputDir')
    " Sets the output directory where test results will be written to.
    " Default value '' forces all output to be written to the screen (echo).
    let g:vimUnitOutputDir = '.'
  endif
" }}}

" Script Variables {{{
  let s:function_regex = '^\s*fu\%[nction]\%[!]\s\+\(.\{-}\)\s*(\s*).*$'
  let s:non_failure_regex = '^\%(\%(^Fail.*$\)\@!.\)*$'

  let s:testsuite = '<testsuite name="<suite>" tests="<tests>" failures="<failures>" time="<time>">'
  let s:testcase = '  <testcase classname="<testcase>" name="<test>" time="<time>"'
" }}}

" VUAssertEquals(arg1, arg2, ...) {{{
" Compares the two arguments to determine if they are equal.
function! VUAssertEquals(arg1, arg2, ...)
  if a:arg1 != a:arg2
    let message = '"' . string(a:arg1) . '" != "' . string(a:arg2) . '"'
    if a:0 > 0
      let message = a:1 . ' (' . message . ')'
    endif
    throw 'AssertEquals: ' . message
  endif
endfunction " }}}

" VUAssertNotEquals(arg1, arg2, ...) {{{
" Compares the two arguments to determine if they are equal.
function! VUAssertNotEquals(arg1, arg2, ...)
  if a:arg1 == a:arg2
    let message = '"' . string(a:arg1) . '" == "' . string(a:arg2) . '"'
    if a:0 > 0
      let message = a:1 . ' (' . message . ')'
    endif
    throw 'AssertNotEquals: ' . message
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

" VUFail(...) {{{
" Fails the current test.
function! VUFail(...)
  let message = a:0 > 0 ? a:1 : ''
  throw 'Fail: ' . message
endfunction " }}}

" VUAddTest(test) {{{
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

  "echom "Running: " . a:testfile

  let tests = s:GetTestFunctionNames()
  let testcase = fnamemodify(a:testfile, ':r')

  call PushRedir('=>> g:vu_sysout')
  exec 'source ' . s:vimUnitTestFile

  if exists('*BeforeTestCase')
    call BeforeTestCase()
  endif

  let now = localtime()
  for test in tests
    call s:RunTest(testcase, test)
  endfor

  if exists('*AfterTestCase')
    call AfterTestCase()
  endif

  call PopRedir()

  let duration = localtime() - now
  call s:WriteResults(a:testfile, duration)

  echom printf('Tests run: %s, Failures: %s, Time elapsed %s sec', s:tests_run, s:tests_failed, duration)

  if s:tests_failed > 0
    echom "Test " . a:testfile . " FAILED"
  endif
endfunction
endif " }}}

" Init(basedir, testfile) {{{
function! s:Init(basedir, testfile)
  let s:tests_run = 0
  let s:tests_failed = 0
  let s:suite_methods = []
  let s:test_results = []
  let s:redir_stack = []
  let g:vu_sysout = ''

  unlet! s:vimUnitOutputFile
  unlet! s:vimUnitOutputDir

  silent! delfunction BeforeTestCase
  silent! delfunction SetUp
  silent! delfunction TearDown
  silent! delfunction AfterTestCase

  let s:vimUnitTestFile = fnamemodify(a:basedir . '/' . a:testfile, ':p')

  if !exists('s:vimUnitOutputDir')
    let g:vimUnitOutputDir = expand(g:vimUnitOutputDir)
    let s:vimUnitOutputDir = g:vimUnitOutputDir

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

      let s:vimUnitOutputFile = s:vimUnitOutputDir . '/TEST-' . file . '.xml'

      " write output to the file
      call delete(s:vimUnitOutputFile)
    endif
  endif
endfunction " }}}

" RunTest(testcase, test) {{{
function! s:RunTest(testcase, test)
  let now = localtime()
  if exists('*SetUp')
    call SetUp()
  endif

  let fail = []
  try
    call {a:test}()
    let s:tests_run += 1
  catch /E117/
    " probably the result of the function being defined via some conditional
  catch
    let s:tests_run += 1
    let s:tests_failed += 1
    call add(fail, v:exception)
    call add(fail, v:throwpoint)
  endtry

  if exists('*TearDown')
    call TearDown()
  endif

  let time = localtime() - now
  let result = {'testcase': a:testcase, 'test': a:test, 'time': time, 'fail': fail}
  call add(s:test_results, result)

  call s:TearDown()
endfunction " }}}

" TearDown() {{{
function! s:TearDown()
  " dispose of all buffers
  let lastbuf = bufnr('$')
  let curbuf = 1

  " igore any error message from here by temporarily redirecting to black hole
  call PushRedir('@_>')
  while curbuf <= lastbuf
    exec 'silent! bdelete! ' . curbuf
    let curbuf += 1
  endwhile
  call PopRedir()
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
    bdelete!
    exec winreset
  endtry
  return filter(results, 'v:val =~ "^Test"')
endfunction " }}}

" WriteResults(testfile, running_time) {{{
function! s:WriteResults (testfile, running_time)
  let root = s:testsuite
  let root = substitute(root, '<suite>', a:testfile, '')
  let root = substitute(root, '<tests>', s:tests_run, '')
  let root = substitute(root, '<failures>', s:tests_failed, '')
  let root = substitute(root, '<time>', a:running_time, '')
  let results = ['<?xml version="1.0" encoding="UTF-8" ?>', root, '  <system-out>', '    <![CDATA[', '    ]]>', '  </system-out>', '</testsuite>']

  " insert test results
  let index = -5
  for result in s:test_results
    let testcase = s:testcase
    let testcase = substitute(testcase, '<testcase>', result.testcase, '')
    let testcase = substitute(testcase, '<test>', result.test, '')
    let testcase = substitute(testcase, '<time>', result.time, '')

    let testcase .= len(result.fail) == 0 ? '/>' : '>'

    call insert(results, testcase, index)

    if len(result.fail) > 0
      let message = substitute(result.fail[0], '"', '\&quot;', 'g')
      call insert(results, '    <failure message="' . message . '">', index)
      let lines = split(result.fail[1], '\n')
      call map(lines, '"      " . v:val')
      for line in lines
        call insert(results, line, index)
      endfor
      call insert(results, '    </failure>', index)
      call insert(results, '  </testcase>', index)
    endif
  endfor

  " insert system output
  let out = split(g:vu_sysout, '\n')
  call map(out, '"    " . v:val')
  let index = -3
  for line in out
    call insert(results, line, index)
  endfor

  call writefile(results, s:vimUnitOutputFile)
endfunction " }}}

" PushRedir(redir) {{{
function! PushRedir (redir)
  exec 'redir ' . a:redir
  call add(s:redir_stack, a:redir)
endfunction " }}}

" PopRedir() {{{
function! PopRedir ()
  let index = len(s:redir_stack) - 2
  if index >= 0
    let redir = s:redir_stack[index]
    exec 'redir ' . redir
    call remove(s:redir_stack, index + 1, len(s:redir_stack) - 1)
  else
    let s:redir_stack = []
    redir END
  endif
endfunction " }}}

" PeekRedir() {{{
function! PeekRedir ()
  call PushRedir(s:redir_stack[len(s:redir_stack) - 1])
  call PopRedir()
  return s:redir_stack[len(s:redir_stack) - 1]
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
