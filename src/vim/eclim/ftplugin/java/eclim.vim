" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/index.html
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

" Global Variables {{{

if !exists("g:EclimJavaSrcValidate")
  let g:EclimJavaSrcValidate = 1
endif

if !exists("g:EclimJavaSetCommonOptions")
  let g:EclimJavaSetCommonOptions = 1
endif

let g:java_fori = "for (int ii = 0; ii < ${array}.length; ii++){\<cr>}" .
  \ "\<esc>\<up>"
let g:java_forI = "for (Iterator ii = ${col}.iterator(); ii.hasNext();){\<cr>}" .
  \ "\<esc>\<up>"
let g:java_fore = "for (${object} ${var} : ${col}){\<cr>}\<esc>\<up>"

if !exists("g:EclimJavaCompilerAutoDetect")
  let g:EclimJavaCompilerAutoDetect = 1
endif

if !exists("g:EclimJavaSearchMapping")
  let g:EclimJavaSearchMapping = 1
endif

if !exists("g:EclimJavaCheckstyleOnSave")
  let g:EclimJavaCheckstyleOnSave = 0
endif

" }}}

" Options {{{

setlocal completefunc=eclim#java#complete#CodeComplete

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
  if eclim#util#Findfile('build.xml', '.;') != ''
    compiler eclim_ant

  " use mvn settings
  elseif eclim#util#Findfile('pom.xml', '.;') != ''
    compiler eclim_mvn

    if !g:EclimMakeLCD && !exists('g:EclimMakeLCDWarning')
      call eclim#util#EchoWarning("WARNING: g:EclimMakeLCD disabled.\n" .
        \ "Unlike maven and ant, mvn does not provide a mechanism to " .
        \ "search for the target build file.\n" .
        \ "Disabling g:EclimMakeLCD may cause issues when executing :make or :Mvn")
      let g:EclimMakeLCDWarning = 1
    endif

  " use maven settings
  elseif eclim#util#Findfile('project.xml', '.;') != ''
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

" }}}

" Abbreviations {{{

" the vim latex plugin (http://vim-latex.sourceforge.net) prevents the
" normal abbreviations from working properly if the user has any IMAP calls
" with a rhs value that ends in a space:
"   au VimEnter * call IMAP(' . ', ' <++> ', 'tex')
" So, if we can't beat 'em, join 'em
if exists('*IMAP')
  call IMAP('fori', "\<c-r>=eclim#util#Abbreviate('fori', g:java_fori)\<cr>", 'java')
  call IMAP('forI', "\<c-r>=eclim#util#Abbreviate('forI', g:java_forI)\<cr>", 'java')
  call IMAP('fore', "\<c-r>=eclim#util#Abbreviate('fore', g:java_fore)\<cr>", 'java')
else
  inoreabbrev <buffer> fori <c-r>=eclim#util#Abbreviate('fori', g:java_fori)<cr>
  inoreabbrev <buffer> forI <c-r>=eclim#util#Abbreviate('forI', g:java_forI)<cr>
  inoreabbrev <buffer> fore <c-r>=eclim#util#Abbreviate('fore', g:java_fore)<cr>
endif

if !exists("g:EclimLoggingDisabled") || !g:EclimLoggingDisabled
  inoreabbrev <buffer> log log<c-r>=eclim#java#logging#LoggingInit("log")<cr>
  inoreabbrev <buffer> logger logger<c-r>=eclim#java#logging#LoggingInit("logger")<cr>
endif

" }}}

" Autocmds {{{

augroup eclim_java
  autocmd!
  autocmd BufWritePost *.java call eclim#java#util#UpdateSrcFile(0)
augroup END

" }}}

" Command Declarations {{{

if !exists(":Validate")
  command -nargs=0 -buffer Validate :call eclim#java#util#UpdateSrcFile(1)
endif

if !exists(":JavaCorrect")
  command -buffer JavaCorrect :call eclim#java#correct#Correct()
endif

if !exists(":JavaFormat")
  command -buffer -range JavaFormat
    \ :call eclim#java#format#Format(<line1>, <line2>, "dummy")
endif

if !exists(":JavaImport")
  command -buffer JavaImport :call eclim#java#import#Import()
endif
if !exists(":JavaImportSort")
  command -buffer JavaImportSort :call eclim#java#import#SortImports()
endif
if !exists(":JavaImportClean")
  command -buffer JavaImportClean :call eclim#java#import#CleanImports()
endif
if !exists(":JavaImportMissing")
  command -buffer JavaImportMissing :call eclim#java#import#ImportMissing()
endif

if !exists(":JavaDocComment")
  command -buffer JavaDocComment :call eclim#java#doc#Comment()
endif

if !exists(":JavaRegex")
  command -buffer JavaRegex :call eclim#regex#OpenTestWindow('java')
endif

if !exists(":JavaConstructor")
  command -buffer -range=0 JavaConstructor
    \ :call eclim#java#constructor#Constructor(<line1>, <line2>)
endif

if !exists(":JavaGet")
  command -buffer -range JavaGet
    \ :call eclim#java#bean#GetterSetter(<line1>, <line2>, "getter")
endif
if !exists(":JavaSet")
  command -buffer -range JavaSet
    \ :call eclim#java#bean#GetterSetter(<line1>, <line2>, "setter")
endif
if !exists(":JavaGetSet")
  command -buffer -range JavaGetSet
    \ :call eclim#java#bean#GetterSetter(<line1>, <line2>, "getter_setter")
endif

if !exists(":JavaImpl")
  command -buffer JavaImpl :call eclim#java#impl#Impl()
endif
if !exists(":JavaDelegate")
  command -buffer JavaDelegate :call eclim#java#delegate#Delegate()
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

if !exists(":JavaHierarchy")
  command -buffer -range JavaHierarchy :call eclim#java#hierarchy#Hierarchy()
endif

if !exists(":JavaLoggingInit")
  command -buffer JavaLoggingInit :call eclim#java#logging#LoggingInit()
endif

if !exists(":JUnitExecute")
  command -buffer -nargs=? -complete=customlist,eclim#java#junit#CommandCompleteTest
    \ JUnitExecute :call eclim#java#junit#JUnitExecute('<args>')
endif
if !exists(":JUnitResult")
  command -buffer -nargs=? -complete=customlist,eclim#java#junit#CommandCompleteResult
    \ JUnitResult :call eclim#java#junit#JUnitResult('<args>')
endif
if !exists(":JUnitImpl")
  command -buffer JUnitImpl :call eclim#java#junit#JUnitImpl()
endif

if !exists(":Checkstyle")
  command -nargs=0 -buffer Checkstyle :call eclim#java#checkstyle#Checkstyle()
endif

" }}}

" vim:ft=vim:fdm=marker
