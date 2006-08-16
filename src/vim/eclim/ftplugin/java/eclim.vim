" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   Plugin to perform common operations on java src files.
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

" Global Variables {{{
  if !exists("g:EclimJavaSrcValidate")
    let g:EclimJavaSrcValidate = 1
  endif

  if !exists("g:EclimJavaSetCommonOptions")
    let g:EclimJavaSetCommonOptions = 1
  endif

  let g:java_fori = "for (int ii = 0; ii < ${array}.length; ii++){\<cr>}" .
    \ "\<esc>\<up>\<tab>\<right>"
  let g:java_forI = "for (Iterator ii = ${col}.iterator(); ii.hasNext();){\<cr>}" .
    \ "\<esc>\<up>\<tab>\<right>"
  let g:java_fore =
    \ "for (${object} ${var} : ${col}){\<cr>}\<esc>\<up>\<tab>\<right>"

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
    if findfile('build.xml', '.;') != ''
      compiler eclim_ant

    " use mvn settings
    elseif findfile('pom.xml', '.;') != ''
      compiler eclim_mvn

      if !g:EclimMakeLCD && !exists('g:EclimMakeLCDWarning')
        call eclim#util#EchoWarning("WARNING: g:EclimMakeLCD disabled.\n" .
          \ "Unlike maven and ant, mvn does not provide a mechanism to " .
          \ "search for the target build file.\n" .
          \ "Disabling g:EclimMakeLCD may cause issues when executing :make or :Mvn")
        let g:EclimMakeLCDWarning = 1
      endif

    " use maven settings
    elseif findfile('project.xml', '.;') != ''
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
  inoreabbrev <buffer> fori <c-r>=<SID>Abbreviate(g:java_fori, ' ')<cr>
  inoreabbrev <buffer> forI <c-r>=<SID>Abbreviate(g:java_forI, ' ')<cr>
  inoreabbrev <buffer> fore <c-r>=<SID>Abbreviate(g:java_fore, '')<cr>
" }}}

" StartAutocommands() {{{
" Starts the global java autocommands.
function! StartAutocommands ()
  augroup eclim_java
    autocmd!
    autocmd BufWritePost *.java call eclim#java#util#UpdateSrcFile()
  augroup END
endfunction " }}}

" StopAutocommands() {{{
" Stops the global java autocommands.
function! StopAutocommands ()
  augroup eclim_java
    autocmd!
  augroup END
endfunction " }}}

" Abbreviate(abbreviation) {{{
function! s:Abbreviate (abbreviation, char)
  let char = nr2char(getchar())
  exec "normal i" . a:abbreviation
  return ''
endfunction " }}}

call StartAutocommands()

" vim:ft=vim:fdm=marker
