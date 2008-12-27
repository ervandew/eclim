" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Plugin to perform common operations on java src files.
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
" }}}

" Options {{{
if g:EclimJavaSetCommonOptions
  " allow cpp keywords in java files (delete, friend, union, template, etc).
  let java_allow_cpp_keywords=1

  " tell vim how to search for included files.
  setlocal include=^\s*import
  setlocal includeexpr=substitute(v:fname,'\\.','/','g')
  setlocal suffixesadd=.java

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
" }}}

augroup eclim_java
  autocmd!
  autocmd BufWritePost *.java call eclim#java#util#UpdateSrcFile(0)
augroup END

" Command Declarations {{{
if !exists(":Validate")
  command -nargs=0 -buffer Validate :call eclim#java#util#UpdateSrcFile(1)
endif
" }}}

" vim:ft=vim:fdm=marker
