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

" TestFindByContextCommandRef() {{{
" Cursor on a command ref.
function! TestFindByContextCommandRef ()
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
  call s:EditFile('vim/test.vim')
  call cursor(9, 34)

  call s:FindByContext()

  let results = getloclist(0)
  call VUAssertEquals(1, len(results), "Wrong number of results.")

  call VUAssertTrue(bufname(results[0].bufnr) =~ 'eclim_find.vim', "Wrong file.")
  call VUAssertTrue(results[0].text =~ 'command -buffer .* FindByContext$',
    \ "Wrong result.")
endfunction " }}}

" TestFindByContextCommandDef() {{{
" Cursor on a command def.
function! TestFindByContextCommandDef ()
  call s:EditFile('~/.vim/eclim/ftplugin/vim/eclim_find.vim')
  call cursor(27, 34)

  call s:FindByContext()

  let results = getloclist(0)
  call VUAssertEquals(2, len(results), "Wrong number of results.")

  call VUAssertTrue(bufname(results[0].bufnr) =~ 'eclim_find.vim', "Wrong file.")
  call VUAssertTrue(results[0].text =~ '\<FindByContext\>', "Wrong result.")
  call VUAssertTrue(bufname(results[1].bufnr) =~ '\.vim/ftplugin/vim/vim\.vim',
    \ "Wrong file.")
  call VUAssertTrue(results[1].text =~ '\<FindByContext\>', "Wrong result.")
endfunction " }}}

" TestFindByContextFunctionRef() {{{
" Cursor on function ref
function! TestFindByContextFunctionRef ()
  call s:EditFile('~/.vim/eclim/autoload/eclim/vim/find.vim')
  call cursor(126, 12)

  call s:FindByContext()

  let results = getloclist(0)
  call VUAssertEquals(1, len(results), "Wrong number of results.")
  call VUAssertTrue(bufname(results[0].bufnr) =~ '\<find.vim', "Wrong file.")
  call VUAssertTrue(results[0].text =~ '\<s:Find\>', "Wrong result.")
endfunction " }}}

" TestFindByContextFunctionDef() {{{
" Cursor on function def
function! TestFindByContextFunctionDef ()
  call s:EditFile('~/.vim/eclim/autoload/eclim/vim/find.vim')
  call cursor(136, 26)

  call s:FindByContext()

  let results = getloclist(0)
  call VUAssertEquals(2, len(results), "Wrong number of results.")

  call VUAssertTrue(bufname(results[0].bufnr) =~ '\<find.vim', "Wrong file.")
  call VUAssertTrue(results[0].text =~ 'eclim#vim#find#FindCommandDef', "Wrong result.")

  call VUAssertTrue(bufname(results[1].bufnr) =~ '\<eclim_find.vim', "Wrong file.")
  call VUAssertTrue(results[1].text =~ 'eclim#vim#find#FindCommandDef', "Wrong result.")
endfunction " }}}

" TestFindByContextVariableRef() {{{
" Cursor on variable ref
function! TestFindByContextVariableRef ()
  call s:EditFile('~/.vim/eclim/autoload/eclim/vim/find.vim')
  call cursor(227, 10)

  call s:FindByContext()

  let results = getloclist(0)
  call VUAssertEquals(1, len(results), "Wrong number of results.")

  call VUAssertTrue(bufname(results[0].bufnr) =~ '\<find.vim', "Wrong file.")
  call VUAssertTrue(results[0].text =~ 'let g:EclimVimFindSingleResult', "Wrong result.")
endfunction " }}}

" TestFindByContextVariableDef() {{{
" Cursor on variable def
function! TestFindByContextVariableDef ()
  call s:EditFile('~/.vim/eclim/autoload/eclim/vim/find.vim')
  call cursor(31, 9)

  call s:FindByContext()

  let results = getloclist(0)
  call VUAssertEquals(4, len(results), "Wrong number of results.")

  call VUAssertTrue(bufname(results[0].bufnr) =~ '\<find.vim', "Wrong file.")
  call VUAssertTrue(results[0].text =~ 'g:EclimVimFindSingleResult', "Wrong result.")

  call VUAssertTrue(bufname(results[1].bufnr) =~ '\<find.vim', "Wrong file.")
  call VUAssertTrue(results[1].text =~ 'g:EclimVimFindSingleResult', "Wrong result.")

  call VUAssertTrue(bufname(results[2].bufnr) =~ '\<find.vim', "Wrong file.")
  call VUAssertTrue(results[2].text =~ 'g:EclimVimFindSingleResult', "Wrong result.")

  call VUAssertTrue(bufname(results[3].bufnr) =~ '\<find.vim', "Wrong file.")
  call VUAssertTrue(results[3].text =~ 'g:EclimVimFindSingleResult', "Wrong result.")
endfunction " }}}

" EditFile {{{
function s:EditFile (file)
  exec 'edit! ' . a:file
  call PeekRedir()
endfunction " }}}

" FindByContext() {{{
function s:FindByContext()
  let g:EclimVimPaths = '~/.vim'
  call eclim#vim#find#FindByContext('!')

  bdelete!
  call PeekRedir()
endfunction " }}}

" vim:ft=vim:fdm=marker
