" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Test case for bean.vim
"
" License:
"
" Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
  exec 'cd ' . g:TestEclimWorkspace . 'eclim_unit_test_java'
endfunction " }}}

" TestJavaGet() {{{
function! TestJavaGet()
  edit! src/org/eclim/test/bean/TestBeanVUnit.java
  call PeekRedir()

  call cursor(1, 1)
  call search('private boolean enabled')

  JavaGet
  call PeekRedir()

  call cursor(1, 1)
  call search('private Date date')

  JavaGet
  call PeekRedir()

  " validate
  let dateLine = search('public Date getDate()', 'wc')
  let enabledLine = search('public boolean isEnabled()', 'wc')

  call VUAssertTrue(dateLine > 0, 'getDate() not found.')
  call VUAssertTrue(enabledLine > 0, 'isEnabled() not found.')

  call VUAssertTrue(dateLine < enabledLine, 'getDate() not before isEnabled().')
endfunction " }}}

" TestJavaSet() {{{
function! TestJavaSet()
  edit! src/org/eclim/test/bean/TestBeanVUnit.java
  call PeekRedir()

  call cursor(1, 1)
  call search('private boolean valid')

  JavaSet
  call PeekRedir()

  call cursor(1, 1)
  call search('private Date date')

  JavaSet
  call PeekRedir()

  " validate
  let getDateLine = search('public Date getDate()', 'wc')
  let setDateLine = search('public void setDate(Date date)', 'wc')
  let setValidLine = search('public void setValid(boolean valid)', 'wc')
  let isEnabledLine = search('public boolean isEnabled()', 'wc')

  call VUAssertTrue(getDateLine > 0, 'getDate() not found.')
  call VUAssertTrue(setDateLine > 0, 'setDate() not found.')
  call VUAssertTrue(setValidLine > 0, 'setValid() not found.')
  call VUAssertTrue(isEnabledLine > 0, 'isEnabled() not found.')

  call VUAssertTrue(getDateLine < setDateLine, 'getDate() not before setDate().')
  call VUAssertTrue(setDateLine < setValidLine, 'setDate() not before setValid().')
  call VUAssertTrue(setValidLine < isEnabledLine, 'setValid() not before isEnabled().')
endfunction " }}}

" TestJavaGetSet() {{{
function! TestJavaGetSet()
  edit! src/org/eclim/test/bean/TestBeanVUnit.java
  call PeekRedir()

  call cursor(1, 1)
  let start = search('private String name')
  let end = search('private boolean enabled')

  exec start . ',' . end . 'JavaGetSet'
  call PeekRedir()

  " validate
  let getNameLine = search('public String getName()', 'wc')
  let setNameLine = search('public void setName(String name)', 'wc')
  let getDescriptionLine = search('public String getDescription()', 'wc')
  let setDescriptionLine = search('public void setDescription(String description)', 'wc')
  let getDateLine = search('public Date getDate()', 'wc')
  let setDateLine = search('public void setDate(Date date)', 'wc')
  let isValidLine = search('public boolean isValid()', 'wc')
  let setValidLine = search('public void setValid(boolean valid)', 'wc')
  let isEnabledLine = search('public boolean isEnabled()', 'wc')
  let setEnabledLine = search('public void setEnabled(boolean enabled)', 'wc')

  call VUAssertTrue(getNameLine > 0, 'getName() not found.')
  call VUAssertTrue(setNameLine > 0, 'setName() not found.')
  call VUAssertTrue(getDescriptionLine > 0, 'getDescription() not found.')
  call VUAssertTrue(setDescriptionLine > 0, 'setDescription() not found.')
  call VUAssertTrue(getDateLine > 0, 'getDate() not found.')
  call VUAssertTrue(setDateLine > 0, 'setDate() not found.')
  call VUAssertTrue(isValidLine > 0, 'isValid() not found.')
  call VUAssertTrue(setValidLine > 0, 'setValid() not found.')
  call VUAssertTrue(isEnabledLine > 0, 'isEnabled() not found.')
  call VUAssertTrue(setEnabledLine > 0, 'setEnabled() not found.')

  call VUAssertTrue(getNameLine < setNameLine, 'getName() not before setName().')
  call VUAssertTrue(setNameLine < getDescriptionLine,
    \ 'setName() not before getDescription().')
  call VUAssertTrue(getDescriptionLine < setDescriptionLine,
    \ 'getDescription() not before setDescription().')
  call VUAssertTrue(setDescriptionLine < getDateLine,
    \ 'setDescription() not before getDate().')
  call VUAssertTrue(getDateLine < setDateLine, 'getDate() not before setDate().')
  call VUAssertTrue(setDateLine < isValidLine, 'setDate() not before isValid().')
  call VUAssertTrue(isValidLine < setValidLine, 'isValid() not before setValid().')
  call VUAssertTrue(setValidLine < isEnabledLine, 'setValid() not before isEnabled().')
  call VUAssertTrue(isEnabledLine < setEnabledLine,
    \ 'isEnabled() not before setEnabled().')

  let innerClassLine = search('private class SomeClass', 'wc')
  call VUAssertTrue(setEnabledLine < innerClassLine,
    \ 'setEnabled() not before inner class.')
endfunction " }}}

" vim:ft=vim:fdm=marker
