" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for android.vim
"
" License:
"
" Copyright (C) 2012  Eric Van Dewoestine
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

function! TestAndroidReload() " {{{
  let g:result = ''
  call vunit#PushRedir('=> g:result')
  AndroidReload
  call vunit#PopRedir()
  let g:result = substitute(g:result, "^\n", '', '')
  call vunit#AssertEquals(g:result, 'Android SDK Reloaded')
endfunction " }}}

function! TestAndroidSdkSetting() " {{{
  EclimSettings
  call cursor(1, 1)

  call vunit#AssertTrue(
    \ search('^# Android {$', 'c') != 0,
    \ 'Missing Android settings section.')

  let lnum = search('^\s*com\.android\.ide\.eclipse\.adt\.sdk=')
  call vunit#AssertTrue(lnum != 0, 'Missing Android Sdk setting.')

  let line = getline(lnum)
  let sdk = substitute(line, '.*=\(.*\)', '\1', '')
  call vunit#AssertTrue(len(sdk) != 0, 'Sdk not initially set')

  " no directory
  let empty = substitute(line, '\(.*=\).*', '\1', '')
  call setline(lnum, empty)
  write
  let loclist = getloclist(0)
  call vunit#AssertEquals(len(loclist), 1, 'Empty sdk loclist length not 1')
  call vunit#AssertEquals(loclist[0].lnum, lnum, 'Empty sdk loclist lnum incorrect')
  call vunit#AssertEquals(
    \ loclist[0].text,
    \ "Could not find folder 'tools' inside SDK '/'.",
    \ 'Empty sdk loclist text incorrect')

  " non-existant directory
  call setline(lnum, empty . '/foo/bar/')
  write
  let loclist = getloclist(0)
  call vunit#AssertEquals(len(loclist), 1, 'Invalid sdk loclist length not 1')
  call vunit#AssertEquals(loclist[0].lnum, lnum, 'Invalid sdk loclist lnum incorrect')
  call vunit#AssertEquals(
    \ loclist[0].text,
    \ "Could not find SDK folder '/foo/bar/'.",
    \ 'Invalid sdk loclist text incorrect')

  " original
  call setline(lnum, empty . sdk)
  write
  let loclist = getloclist(0)
  call vunit#AssertEquals(len(loclist), 0, 'Error w/ correct sdk')
endfunction " }}}

" vim:ft=vim:fdm=marker
