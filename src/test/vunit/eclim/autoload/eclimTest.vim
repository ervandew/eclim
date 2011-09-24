" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for eclim.vim
"
" License:
"
" Copyright (C) 2005 - 2011  Eric Van Dewoestine
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
function! TestGetEclimCommand()
  let result = eclim#client#nailgun#GetEclimCommand()
  call vunit#AssertTrue(result =~ '\<eclim\>', "Invalid eclim command.")
endfunction " }}}

" TestPing() {{{
function! TestPing()
  let result = eclim#PingEclim(0)
  call vunit#AssertTrue(result, "Ping did not return true.")

  "let g:Ping = ''
  "call PushRedir('=> g:Ping')
  "try
  "  call eclim#PingEclim(1)
  "  call vunit#AssertTrue(g:Ping =~ 'eclim [0-9]\+\.[0-9]\+\.[0-9]\+', "Invalid ping output.")
  "finally
  "  unlet! g:Ping
  "  call PopRedir()
  "endtry
endfunction " }}}

" TestSettings() {{{
function! TestSettings()
  exec 'EclimSettings ' . g:TestEclimWorkspace
  call vunit#AssertEquals('Eclim_Global_Settings', expand('%'),
    \ "Didn't open settings window.")
  close
endfunction " }}}

" vim:ft=vim:fdm=marker
