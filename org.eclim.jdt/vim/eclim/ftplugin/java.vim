" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/index.html
"
" License:
"
" Copyright (C) 2005 - 2013  Eric Van Dewoestine
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

" Global Variables {{{

if !exists("g:EclimJavaValidate")
  let g:EclimJavaValidate = 1
endif

if !exists("g:EclimJavaSetCommonOptions")
  let g:EclimJavaSetCommonOptions = 1
endif

if !exists("g:EclimJavaCompilerAutoDetect")
  let g:EclimJavaCompilerAutoDetect = 1
endif

if !exists("g:EclimJavaSyntasticEnabled")
  let g:EclimJavaSyntasticEnabled = 0
endif

if !exists('g:EclimJavaCallHierarchyDefaultAction')
  let g:EclimJavaCallHierarchyDefaultAction = g:EclimDefaultFileOpenAction
endif

" }}}

" Options {{{

exec 'setlocal ' . g:EclimCompletionMethod . '=eclim#java#complete#CodeComplete'

if g:EclimJavaSetCommonOptions
  " allow cpp keywords in java files (delete, friend, union, template, etc).
  let java_allow_cpp_keywords=1

  " tell vim how to search for included files.
  setlocal include=^\s*import
  setlocal includeexpr=substitute(v:fname,'\\.','/','g')
  setlocal suffixesadd=.java
endif

" set make program and error format accordingly.
if g:EclimJavaCompilerAutoDetect
  " use ant settings
  if eclim#util#Findfile('build.xml', '.;') != '' &&
   \ eclim#util#CompilerExists('eclim_ant')
    compiler eclim_ant

  " use mvn settings
  elseif eclim#util#Findfile('pom.xml', '.;') != '' &&
   \     eclim#util#CompilerExists('eclim_mvn')
    compiler eclim_mvn

    if !g:EclimMakeLCD && !exists('g:EclimMakeLCDWarning')
      call eclim#util#EchoWarning("WARNING: g:EclimMakeLCD disabled.\n" .
        \ "Unlike maven and ant, mvn does not provide a mechanism to " .
        \ "search for the target build file.\n" .
        \ "Disabling g:EclimMakeLCD may cause issues when executing :make or :Mvn")
      let g:EclimMakeLCDWarning = 1
    endif

  " use maven settings
  elseif eclim#util#Findfile('project.xml', '.;') != '' &&
   \     eclim#util#CompilerExists('eclim_maven')
    compiler eclim_maven

  " use standard jikes if available
  elseif executable('jikes')
    compiler jikes
    let g:EclimMakeLCD = 0

  " default to standard javac settings
  else
    compiler javac
    let g:EclimMakeLCD = 0
  endif
endif

" disable syntastic
if exists('g:loaded_syntastic_plugin') && !g:EclimJavaSyntasticEnabled
  let g:syntastic_java_checkers = []
endif

" }}}

" Abbreviations {{{

if !exists("g:EclimLoggingDisabled") || !g:EclimLoggingDisabled
  inoreabbrev <buffer> log log<c-r>=eclim#java#logging#LoggingInit("log")<cr>
  inoreabbrev <buffer> logger logger<c-r>=eclim#java#logging#LoggingInit("logger")<cr>
endif

" }}}

" Autocmds {{{

if &ft == 'java'
  augroup eclim_java
    autocmd! BufWritePost <buffer>
    autocmd BufWritePost <buffer> call eclim#lang#UpdateSrcFile('java')
  augroup END
endif

" }}}

" Command Declarations {{{

if !exists(":Validate")
  command -nargs=0 -buffer Validate :call eclim#lang#UpdateSrcFile('java', 1)
endif

if !exists(":JavaCorrect")
  command -buffer JavaCorrect :call eclim#java#correct#Correct()
endif

if !exists(":JavaFormat")
  command -buffer -range JavaFormat :call eclim#java#src#Format(<line1>, <line2>)
endif

if !exists(":JavaImport")
  command -buffer JavaImport :call eclim#java#import#Import()
endif
if !exists(":JavaImportOrganize")
  command -buffer JavaImportOrganize :call eclim#java#import#OrganizeImports()
endif

if !exists(":JavaDocComment")
  command -buffer JavaDocComment :call eclim#java#doc#Comment()
endif
if !exists(":JavaDocPreview")
  command -buffer JavaDocPreview :call eclim#java#doc#Preview()
endif

if !exists(":Javadoc")
  command -buffer -bang -nargs=*
    \ -complete=customlist,eclim#java#doc#CommandCompleteJavadoc
    \ Javadoc :call eclim#java#doc#Javadoc('<bang>', <q-args>)
endif
if exists(":Java") != 2
  command -buffer -nargs=* Java :call eclim#java#util#Java('', <q-args>)
endif
if exists(":JavaClasspath") != 2
  command -buffer -nargs=* JavaClasspath :call eclim#java#util#Classpath(<f-args>)
endif
if exists(":JavaListInstalls") != 2
  command -buffer -nargs=* JavaListInstalls :call eclim#java#util#ListInstalls()
endif

if !exists(":JavaConstructor")
  command -buffer -range=0 -bang JavaConstructor
    \ :call eclim#java#impl#Constructor(<line1>, <line2>, '<bang>')
endif

if !exists(":JavaGet")
  command -buffer -range -bang JavaGet
    \ :call eclim#java#impl#GetterSetter(<line1>, <line2>, '<bang>', 'getter')
endif
if !exists(":JavaSet")
  command -buffer -range -bang JavaSet
    \ :call eclim#java#impl#GetterSetter(<line1>, <line2>, '<bang>', 'setter')
endif
if !exists(":JavaGetSet")
  command -buffer -range -bang JavaGetSet
    \ :call eclim#java#impl#GetterSetter(<line1>, <line2>, '<bang>', 'getter_setter')
endif

if !exists(":JavaImpl")
  command -buffer JavaImpl :call eclim#java#impl#Impl()
endif
if !exists(":JavaDelegate")
  command -buffer JavaDelegate :call eclim#java#impl#Delegate()
endif

if !exists(":JavaSearch")
  command -buffer -nargs=*
    \ -complete=customlist,eclim#java#search#CommandCompleteJavaSearch
    \ JavaSearch :call eclim#java#search#SearchAndDisplay('java_search', '<args>')
endif
if !exists(":JavaSearchContext")
  command -buffer JavaSearchContext
    \ :call eclim#java#search#SearchAndDisplay('java_search', '')
endif
if !exists(":JavaDocSearch")
  command -buffer -nargs=*
    \ -complete=customlist,eclim#java#search#CommandCompleteJavaSearch
    \ JavaDocSearch :call eclim#java#search#SearchAndDisplay('java_docsearch', '<args>')
endif

if !exists(":JavaCallHierarchy")
  command -buffer -bang JavaCallHierarchy
    \ :call eclim#lang#hierarchy#CallHierarchy(
      \ 'java', g:EclimJavaCallHierarchyDefaultAction, '<bang>')
endif

if !exists(":JavaHierarchy")
  command -buffer -range JavaHierarchy :call eclim#java#hierarchy#Hierarchy()
endif

if !exists(":JavaRename")
  command -nargs=1 -buffer JavaRename :call eclim#java#refactor#Rename('<args>')
endif
if !exists(":JavaMove")
  command -nargs=1 -buffer -complete=customlist,eclim#java#util#CommandCompletePackage
    \ JavaMove :call eclim#java#refactor#Move('<args>')
endif

if !exists(":JavaLoggingInit")
  command -buffer JavaLoggingInit :call eclim#java#logging#LoggingInit()
endif

if !exists(":JUnit")
  command -buffer -nargs=? -bang -complete=customlist,eclim#java#junit#CommandCompleteTest
    \ JUnit :call eclim#java#junit#JUnit('<args>', '<bang>')
endif
if !exists(":JUnitFindTest")
  command -buffer JUnitFindTest :call eclim#java#junit#JUnitFindTest()
endif
if !exists(":JUnitResult")
  command -buffer -nargs=? -complete=customlist,eclim#java#junit#CommandCompleteResult
    \ JUnitResult :call eclim#java#junit#JUnitResult('<args>')
endif
if !exists(":JUnitImpl")
  command -buffer JUnitImpl :call eclim#java#junit#JUnitImpl()
endif

if !exists(":Checkstyle")
  command -nargs=0 -buffer Checkstyle :call eclim#java#src#Checkstyle()
endif

" }}}

" vim:ft=vim:fdm=marker
