" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for find.vim
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

" BeforeTestCase() {{{
function! BeforeTestCase ()
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
  edit! vim/test.vim
  call PeekRedir()

  let g:EclimVimPaths = '~/.vim'
endfunction " }}}

" TestFindCommandDef() {{{
function! TestFindCommandDef ()
  call eclim#vim#find#FindCommandDef('FindCommandDef', '!')
  bdelete!
  let g:EclimVimPaths = '~/.vim'
  call PeekRedir()

  let results = getloclist(0)
  call VUAssertEquals(1, len(results), "Wrong number of results.")

  call VUAssertTrue(bufname(results[0].bufnr) =~ 'eclim_find.vim', "Wrong file.")
  call VUAssertTrue(results[0].text =~ 'command -buffer .* FindCommandDef$', "Wrong result.")
endfunction " }}}

" TestFindCommandRef() {{{
function! TestFindCommandRef ()
  call eclim#vim#find#FindCommandRef('FindByContext', '!')
  bdelete!
  let g:EclimVimPaths = '~/.vim'
  call PeekRedir()

  let results = getloclist(0)
  for result in results
    echom 'TestFindCommandRef result = ' . bufname(result.bufnr)
  endfor
  call VUAssertEquals(2, len(results), "Wrong number of results.")

  call VUAssertTrue(bufname(results[0].bufnr) =~ 'eclim_find.vim', "Wrong file.")
  call VUAssertTrue(results[0].text =~ '\<FindByContext\>', "Wrong result.")

  call VUAssertTrue(bufname(results[1].bufnr) =~ '\.vim/ftplugin/vim/vim\.vim', "Wrong file.")
  call VUAssertTrue(results[1].text =~ '\<FindByContext\>', "Wrong result.")
endfunction " }}}

" TestFindByContext() {{{
function! TestFindByContext ()
  " Cursor on command ref
  call cursor(9, 34)
  call eclim#vim#find#FindByContext('!')
  bdelete!
  let g:EclimVimPaths = '~/.vim'
  call PeekRedir()

  let results = getloclist(0)
  call VUAssertEquals(1, len(results), "Wrong number of results.")

  call VUAssertTrue(bufname(results[0].bufnr) =~ 'eclim_find.vim', "Wrong file.")
  call VUAssertTrue(results[0].text =~ 'command -buffer .* FindByContext$', "Wrong result.")

  " Cursor on command def
  edit! ~/.vim/eclim/ftplugin/vim/eclim_find.vim
  let g:EclimVimPaths = '~/.vim'
  call PeekRedir()
  call cursor(27, 34)

  call eclim#vim#find#FindByContext('!')
  bdelete!
  let g:EclimVimPaths = '~/.vim'
  call PeekRedir()

  let results = getloclist(0)
  call VUAssertEquals(2, len(results), "Wrong number of results.")

  call VUAssertTrue(bufname(results[0].bufnr) =~ 'eclim_find.vim', "Wrong file.")
  call VUAssertTrue(results[0].text =~ '\<FindByContext\>', "Wrong result.")
  call VUAssertTrue(bufname(results[1].bufnr) =~ '\.vim/ftplugin/vim/vim\.vim', "Wrong file.")
  call VUAssertTrue(results[1].text =~ '\<FindByContext\>', "Wrong result.")

  " Cursor on function ref

  " Cursor on function def

  " Cursor on variable ref

  " Cursor on variable def
endfunction " }}}

" vim:ft=vim:fdm=marker
