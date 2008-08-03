" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for find.vim
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

" TestFindByContextCommandRef() {{{
" Cursor on a command ref.
function! TestFindByContextCommandRef ()
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test'
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
  call cursor(28, 34)

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
  call cursor(127, 12)

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
  call cursor(137, 26)

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
  call cursor(228, 10)

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
  call cursor(32, 9)

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
  call PeekRedir()
endfunction " }}}

" vim:ft=vim:fdm=marker
