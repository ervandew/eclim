" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for eclim.vim
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

" TestGetEclimCommand() {{{
function! TestGetEclimCommand ()
  let result = eclim#GetEclimCommand()
  call VUAssertTrue(result =~ '\<eclim\>', "Invalid eclim command.")
endfunction " }}}

" TestPing() {{{
function! TestPing ()
  let result = eclim#PingEclim(0)
  call VUAssertTrue(result, "Ping did not return true.")

  let g:ping = ''
  call PushRedir('=> g:ping')
  try
    call eclim#PingEclim(1)
    call VUAssertTrue(g:ping =~ 'eclim [0-9]\+\.[0-9]\+\.[0-9]\+', "Invalid ping output.")
  finally
    unlet! g:ping
    call PopRedir()
  endtry
endfunction " }}}

" TestSettings() {{{
function! TestSettings ()
  :Settings
  call VUAssertEquals('Eclim_Global_Settings', expand('%'),
    \ "Didn't open settings window.")
endfunction " }}}

" TestCommandCompleteScriptRevision() {{{
function! TestCommandCompleteScriptRevision ()
  let results = eclim#CommandCompleteScriptRevision(
    \ 'eclim/autoload/ec', 'PatchEclim eclim/autoload/ec', 28)
  call VUAssertEquals(2, len(results), "Wrong number of completions.")
  call VUAssertEquals('eclim/autoload/eclim/', results[0],
    \ "Wrong first completion.")
  call VUAssertEquals('eclim/autoload/eclim.vim', results[1],
    \ "Wrong second completion.")

  let results = eclim#CommandCompleteScriptRevision(
    \ 'eclim/autoload/eclim.vim 1.', 'PatchEclim eclim/autoload/eclim.vim 1.', 38)
  call VUAssertTrue(len(results) > 0, "Insuficient number of completions.")
  for result in results
    call VUAssertTrue(result =~ '^1\.', "Invalid completion: '" . result . "'")
  endfor
endfunction " }}}

" vim:ft=vim:fdm=marker
