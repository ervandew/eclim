" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for eclim.vim
"
" License:
"
" Copyright (C) 2005 - 2008  Eric Van Dewoestine
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

" TestGetEclimCommand() {{{
function! TestGetEclimCommand ()
  let result = eclim#GetEclimCommand()
  call VUAssertTrue(result =~ '\<eclim\>', "Invalid eclim command.")
endfunction " }}}

" TestPing() {{{
function! TestPing ()
  let result = eclim#PingEclim(0)
  call VUAssertTrue(result, "Ping did not return true.")

  "let g:Ping = ''
  "call PushRedir('=> g:Ping')
  "try
  "  call eclim#PingEclim(1)
  "  call VUAssertTrue(g:Ping =~ 'eclim [0-9]\+\.[0-9]\+\.[0-9]\+', "Invalid ping output.")
  "finally
  "  unlet! g:Ping
  "  call PopRedir()
  "endtry
endfunction " }}}

" TestSettings() {{{
function! TestSettings ()
  :EclimSettings
  call VUAssertEquals('Eclim_Global_Settings', expand('%'),
    \ "Didn't open settings window.")
  close
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

"  let results = eclim#CommandCompleteScriptRevision(
"    \ 'eclim/autoload/eclim.vim 59', 'PatchEclim eclim/autoload/eclim.vim 59', 38)
"  call VUAssertTrue(len(results) > 0, "Insuficient number of completions.")
"  for result in results
"    call VUAssertTrue(result =~ '^59', "Invalid completion: '" . result . "'")
"  endfor
endfunction " }}}

" vim:ft=vim:fdm=marker
