" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Test case for bean.vim
"
" License:
"
" Copyright (C) 2005 - 2012  Eric Van Dewoestine
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

function! TestJavaGet() " {{{
  edit! src/org/eclim/test/bean/TestBeanVUnit.java
  call vunit#PeekRedir()

  call cursor(1, 1)
  call search('private boolean enabled')

  JavaGet
  call vunit#PeekRedir()

  call cursor(1, 1)
  call search('private Date date')

  JavaGet
  call vunit#PeekRedir()

  " validate
  let dateLine = search('public Date getDate()', 'wc')
  let enabledLine = search('public boolean isEnabled()', 'wc')

  call vunit#AssertTrue(dateLine > 0, 'getDate() not found.')
  call vunit#AssertTrue(enabledLine > 0, 'isEnabled() not found.')

  call vunit#AssertTrue(dateLine < enabledLine, 'getDate() not before isEnabled().')
endfunction " }}}

function! TestJavaSet() " {{{
  edit! src/org/eclim/test/bean/TestBeanVUnit.java
  call vunit#PeekRedir()

  call cursor(1, 1)
  call search('private boolean valid')

  JavaSet
  call vunit#PeekRedir()

  call cursor(1, 1)
  call search('private Date date')

  JavaSet
  call vunit#PeekRedir()

  " validate
  let getDateLine = search('public Date getDate()', 'wc')
  let setDateLine = search('public void setDate(Date date)', 'wc')
  let setValidLine = search('public void setValid(boolean valid)', 'wc')
  let isEnabledLine = search('public boolean isEnabled()', 'wc')

  call vunit#AssertTrue(getDateLine > 0, 'getDate() not found.')
  call vunit#AssertTrue(setDateLine > 0, 'setDate() not found.')
  call vunit#AssertTrue(setValidLine > 0, 'setValid() not found.')
  call vunit#AssertTrue(isEnabledLine > 0, 'isEnabled() not found.')

  call vunit#AssertTrue(getDateLine < setDateLine, 'getDate() not before setDate().')
  call vunit#AssertTrue(setDateLine < setValidLine, 'setDate() not before setValid().')
  call vunit#AssertTrue(setValidLine < isEnabledLine, 'setValid() not before isEnabled().')
endfunction " }}}

function! TestJavaGetSet() " {{{
  edit! src/org/eclim/test/bean/TestBeanVUnit.java
  call vunit#PeekRedir()

  call cursor(1, 1)
  let start = search('private String name')
  let end = search('private boolean enabled')

  exec start . ',' . end . 'JavaGetSet'
  call vunit#PeekRedir()

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

  call vunit#AssertTrue(getNameLine > 0, 'getName() not found.')
  call vunit#AssertTrue(setNameLine > 0, 'setName() not found.')
  call vunit#AssertTrue(getDescriptionLine > 0, 'getDescription() not found.')
  call vunit#AssertTrue(setDescriptionLine > 0, 'setDescription() not found.')
  call vunit#AssertTrue(getDateLine > 0, 'getDate() not found.')
  call vunit#AssertTrue(setDateLine > 0, 'setDate() not found.')
  call vunit#AssertTrue(isValidLine > 0, 'isValid() not found.')
  call vunit#AssertTrue(setValidLine > 0, 'setValid() not found.')
  call vunit#AssertTrue(isEnabledLine > 0, 'isEnabled() not found.')
  call vunit#AssertTrue(setEnabledLine > 0, 'setEnabled() not found.')

  call vunit#AssertTrue(getNameLine < setNameLine, 'getName() not before setName().')
  call vunit#AssertTrue(setNameLine < getDescriptionLine,
    \ 'setName() not before getDescription().')
  call vunit#AssertTrue(getDescriptionLine < setDescriptionLine,
    \ 'getDescription() not before setDescription().')
  call vunit#AssertTrue(setDescriptionLine < getDateLine,
    \ 'setDescription() not before getDate().')
  call vunit#AssertTrue(getDateLine < setDateLine, 'getDate() not before setDate().')
  call vunit#AssertTrue(setDateLine < isValidLine, 'setDate() not before isValid().')
  call vunit#AssertTrue(isValidLine < setValidLine, 'isValid() not before setValid().')
  call vunit#AssertTrue(setValidLine < isEnabledLine, 'setValid() not before isEnabled().')
  call vunit#AssertTrue(isEnabledLine < setEnabledLine,
    \ 'isEnabled() not before setEnabled().')

  let innerClassLine = search('private class SomeClass', 'wc')
  call vunit#AssertTrue(setEnabledLine < innerClassLine,
    \ 'setEnabled() not before inner class.')
endfunction " }}}

function! TestJavaGetSetIndex() " {{{
  edit! src/org/eclim/test/bean/TestBeanVUnit.java
  call vunit#PeekRedir()

  call cursor(1, 1)
  call search('private int\[\] ids')
  JavaGetSet!
  call vunit#PeekRedir()

  " validate
  let getIdsLine = search('public int\[\] getIds()', 'wc')
  let getIdsIndexLine = search('public int getIds(int index)', 'wc')
  let setIdsLine = search('public void setIds(int\[\] ids)', 'wc')
  let setIdsIndexLine = search('public void setIds(int index, int ids)', 'wc')

  call vunit#AssertTrue(getIdsLine > 0, 'getIds() not found.')
  call vunit#AssertTrue(getIdsIndexLine > 0, 'getIds(int) not found.')
  call vunit#AssertTrue(setIdsLine > 0, 'setIds(int[]) not found.')
  call vunit#AssertTrue(setIdsIndexLine > 0, 'setIds(int,int) not found.')

  call vunit#AssertTrue(getIdsLine < getIdsIndexLine, 'getIds() not before getIds(int).')
  call vunit#AssertTrue(getIdsIndexLine < setIdsLine, 'getIds(int) not before setIds(int[]).')
  call vunit#AssertTrue(setIdsLine < setIdsIndexLine, 'setIds(int[]) not before setIds(int,int).')
endfunction " }}}

" vim:ft=vim:fdm=marker
