" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for find.vim
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

" SetUp() {{{
function! SetUp()
  let s:vimHome = (has('win32') || has('win64')) ? '~/vimfiles' : '~/.vim'
endfunction " }}}

" TestFindByContextCommandRef() {{{
" Cursor on a command ref.
function! TestFindByContextCommandRef()
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test'
  edit! vim/test.vim
  call vunit#PeekRedir()

  call cursor(9, 34)

  let g:EclimVimPaths = s:vimHome
  FindByContext!
  call vunit#PeekRedir()

  let results = getloclist(0)
  call vunit#AssertEquals(1, len(results), "Wrong number of results.")

  call vunit#AssertTrue(bufname(results[0].bufnr) =~ 'common.vim', "Wrong file.")
  call vunit#AssertTrue(results[0].text =~ 'command Buffers\>', "Wrong result.")
endfunction " }}}

" TestFindByContextCommandDef() {{{
" Cursor on a command def.
function! TestFindByContextCommandDef()
  exec 'edit! ' . s:vimHome . '/eclim/ftplugin/vim/eclim.vim'
  call vunit#PeekRedir()
  call cursor(32, 34)

  let g:EclimVimPaths = s:vimHome
  FindByContext!
  call vunit#PeekRedir()

  let results = getloclist(0)
  call vunit#AssertEquals(2, len(results), "Wrong number of results.")

  call vunit#AssertTrue(bufname(results[0].bufnr) =~ 'eclim.vim', "Wrong file (not eclim.vim).")
  call vunit#AssertTrue(results[0].text =~ '\<FindByContext\>', "Wrong result.")
  let name = substitute(bufname(results[1].bufnr), '\', '/', 'g')
  call vunit#AssertTrue(name =~ '/ftplugin/vim/vim\.vim', "Wrong file (not vim.vim).")
  call vunit#AssertTrue(results[1].text =~ '\<FindByContext\>', "Wrong result.")
endfunction " }}}

" TestFindByContextFunctionRef() {{{
" Cursor on function ref
function! TestFindByContextFunctionRef()
  exec 'edit! ' . s:vimHome . '/eclim/autoload/eclim/vim/find.vim'
  call vunit#PeekRedir()
  call cursor(126, 12)

  let g:EclimVimPaths = s:vimHome
  FindByContext!
  call vunit#PeekRedir()

  let results = getloclist(0)
  call vunit#AssertEquals(1, len(results), "Wrong number of results.")
  call vunit#AssertTrue(bufname(results[0].bufnr) =~ '\<find.vim', "Wrong file.")
  call vunit#AssertTrue(results[0].text =~ '\<s:Find\>', "Wrong result.")
endfunction " }}}

" TestFindByContextFunctionDef() {{{
" Cursor on function def
function! TestFindByContextFunctionDef()
  exec 'edit! ' . s:vimHome . '/eclim/autoload/eclim/vim/find.vim'
  call vunit#PeekRedir()
  call cursor(136, 26)

  let g:EclimVimPaths = s:vimHome
  FindByContext!
  call vunit#PeekRedir()

  let results = getloclist(0)
  call vunit#AssertEquals(2, len(results), "Wrong number of results.")

  call vunit#AssertTrue(bufname(results[0].bufnr) =~ '\<find.vim', "Wrong file.")
  call vunit#AssertTrue(results[0].text =~ 'eclim#vim#find#FindCommandDef', "Wrong result.")

  call vunit#AssertTrue(bufname(results[1].bufnr) =~ '\<eclim.vim', "Wrong file.")
  call vunit#AssertTrue(results[1].text =~ 'eclim#vim#find#FindCommandDef', "Wrong result.")
endfunction " }}}

" TestFindByContextVariableRef() {{{
" Cursor on variable ref
function! TestFindByContextVariableRef()
  exec 'edit! ' . s:vimHome . '/eclim/autoload/eclim/vim/find.vim'
  call vunit#PeekRedir()
  call cursor(231, 10)

  let g:EclimVimPaths = s:vimHome
  FindByContext!
  call vunit#PeekRedir()

  let results = getloclist(0)
  call vunit#AssertEquals(1, len(results), "Wrong number of results.")

  call vunit#AssertTrue(bufname(results[0].bufnr) =~ '\<find.vim', "Wrong file.")
  call vunit#AssertTrue(results[0].text =~ 'let g:EclimVimFindSingleResult', "Wrong result.")
endfunction " }}}

" TestFindByContextVariableDef() {{{
" Cursor on variable def
function! TestFindByContextVariableDef()
  exec 'edit! ' . s:vimHome . '/eclim/autoload/eclim/vim/find.vim'
  call vunit#PeekRedir()
  call cursor(31, 9)

  let g:EclimVimPaths = s:vimHome
  FindByContext!
  call vunit#PeekRedir()

  let results = getloclist(0)
  call vunit#AssertEquals(4, len(results), "Wrong number of results.")

  call vunit#AssertTrue(bufname(results[0].bufnr) =~ '\<find.vim', "Wrong file.")
  call vunit#AssertTrue(results[0].text =~ 'g:EclimVimFindSingleResult', "Wrong result.")

  call vunit#AssertTrue(bufname(results[1].bufnr) =~ '\<find.vim', "Wrong file.")
  call vunit#AssertTrue(results[1].text =~ 'g:EclimVimFindSingleResult', "Wrong result.")

  call vunit#AssertTrue(bufname(results[2].bufnr) =~ '\<find.vim', "Wrong file.")
  call vunit#AssertTrue(results[2].text =~ 'g:EclimVimFindSingleResult', "Wrong result.")

  call vunit#AssertTrue(bufname(results[3].bufnr) =~ '\<find.vim', "Wrong file.")
  call vunit#AssertTrue(results[3].text =~ 'g:EclimVimFindSingleResult', "Wrong result.")
endfunction " }}}

" vim:ft=vim:fdm=marker
