" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for search.vim
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_ruby'
endfunction " }}}

" TestSearchContext() {{{
function! TestSearchContext()
  edit! src/search/testSearch.rb
  call PeekRedir()

  " find class
  call cursor(3, 11)
  RubySearchContext
  let name = substitute(expand('%'), '\', '/', 'g')
  call VUAssertEquals(name, 'src/test.rb', 'Wrong file (class).')
  call VUAssertEquals(11, line('.'), 'Wrong line (class).')
  call VUAssertEquals(7, col('.'), 'Wrong col (class).')
  bdelete

  " find method
  call cursor(4, 8)
  RubySearchContext
  let name = substitute(expand('%'), '\', '/', 'g')
  call VUAssertEquals(name, 'src/test.rb', 'Wrong file (method).')
  call VUAssertEquals(13, line('.'), 'Wrong line (method).')
  call VUAssertEquals(7, col('.'), 'Wrong col (method).')
  bdelete

  " find class constant
  call cursor(5, 15)
  RubySearchContext
  let name = substitute(expand('%'), '\', '/', 'g')
  call VUAssertEquals(name, 'src/test.rb', 'Wrong file (class constant).')
  call VUAssertEquals(12, line('.'), 'Wrong line (class constant).')
  call VUAssertEquals(3, col('.'), 'Wrong col (class constant).')
  bdelete

  " find module
  call cursor(7, 6)
  RubySearchContext
  let name = substitute(expand('%'), '\', '/', 'g')
  call VUAssertEquals(name, 'src/test.rb', 'Wrong file (module).')
  call VUAssertEquals(1, line('.'), 'Wrong line (module).')
  call VUAssertEquals(8, col('.'), 'Wrong col (module).')
  bdelete

  " find module constant
  call cursor(7, 14)
  RubySearchContext
  let name = substitute(expand('%'), '\', '/', 'g')
  call VUAssertEquals(name, 'src/test.rb', 'Wrong file (module constant).')
  call VUAssertEquals(2, line('.'), 'Wrong line (module constant).')
  call VUAssertEquals(3, col('.'), 'Wrong col (module constant).')
  bdelete

  " find module method
  " dltkruby isn't finding module methods currently
  "call cursor(8, 12)
  "RubySearchContext
  "let name = substitute(expand('%'), '\', '/', 'g')
  "call VUAssertEquals(name, 'src/test.rb', 'Wrong file (module method).')
  "call VUAssertEquals(6, line('.'), 'Wrong line (module method).')
  "call VUAssertEquals(7, col('.'), 'Wrong col (module method).')
  "bdelete

  " find function
  call cursor(10, 12)
  RubySearchContext
  let name = substitute(expand('%'), '\', '/', 'g')
  call VUAssertEquals(name, 'src/test.rb', 'Wrong file (funciton).')
  call VUAssertEquals(21, line('.'), 'Wrong line (funciton).')
  call VUAssertEquals(5, col('.'), 'Wrong col (funciton).')
  bdelete
endfunction " }}}

" TestSearchExact() {{{
function! TestSearchExact()
  edit! src/search/testSearch.rb
  call PeekRedir()

  " find module
  RubySearch TestModule -t class
  let name = substitute(expand('%'), '\', '/', 'g')
  call VUAssertEquals(name, 'src/test.rb', 'Wrong file (module).')
  call VUAssertEquals(1, line('.'), 'Wrong line (module).')
  call VUAssertEquals(8, col('.'), 'Wrong col (module).')
  bdelete

  " find class
  RubySearch TestClass -t class
  let name = substitute(expand('%'), '\', '/', 'g')
  call VUAssertEquals(name, 'src/test.rb', 'Wrong file (class).')
  call VUAssertEquals(11, line('.'), 'Wrong line (class).')
  call VUAssertEquals(7, col('.'), 'Wrong col (class).')
  bdelete

  " find method
  " dltkruby isn't finding module methods currently
  "RubySearch methodA -t method
  "let name = substitute(expand('%'), '\', '/', 'g')
  "call VUAssertEquals(name, 'src/test.rb', 'Wrong file (method).')
  "call VUAssertEquals(6, line('.'), 'Wrong line (method).')
  "call VUAssertEquals(7, col('.'), 'Wrong col (method).')
  "bdelete

  " find function
  RubySearch testFunction -t function
  let name = substitute(expand('%'), '\', '/', 'g')
  call VUAssertEquals(name, 'src/test.rb', 'Wrong file (function).')
  call VUAssertEquals(21, line('.'), 'Wrong line (function).')
  call VUAssertEquals(5, col('.'), 'Wrong col (function).')
  bdelete

  " find constant
  RubySearch CONSTANT -t field
  let name = substitute(expand('%'), '\', '/', 'g')
  call VUAssertEquals(name, 'src/test.rb', 'Wrong file (constant).')
  call VUAssertEquals(12, line('.'), 'Wrong line (constant).')
  call VUAssertEquals(3, col('.'), 'Wrong col (constant).')
  bdelete
endfunction " }}}

" TestSearchPattern() {{{
function! TestSearchPattern()
  edit! src/search/testSearch.rb
  call PeekRedir()

  " find method
  RubySearch -p test* -t method
  lclose
  call PeekRedir()

  let results = getloclist(0)
  echo string(results)
  call VUAssertEquals(3, len(results), 'Wrong number of results.')
endfunction " }}}

" vim:ft=vim:fdm=marker
