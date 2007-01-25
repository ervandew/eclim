" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Utility functions for java eclim ftplugins.
"
" Platform:
"   All platforms (tested on linux).
"
" Dependencies:
"   Requires plugin/eclim.vim
"
" Usage:
"   :call JavaIsKeyword(word)
"   :call JavaGetPackage()
"   etc...
"
" Configuration:
"   None.
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

" Script Variables {{{
  let s:keywords = '\(abstract\|assert\|boolean\|case\|catch\|char\|class\|do\|double\|enum\|extends\|final\|finally\|float\|for\|if\|implements\|import\|int\|interface\|long\|new\|null\|package\|private\|protected\|public\|return\|short\|static\|switch\|throw\|throws\|try\|void\|while\)'

  let s:class_declaration = '^\s*\(public\|private\|protected\)\?\(\s\+abstract\)\?\s\+\(class\|interface\|enum\)\s\+[A-Z]'

  let s:update_command =
    \ '-filter vim -command java_src_update -p "<project>" -f "<file>"'
  let s:command_src_exists = '-command java_src_exists -f "<file>" -filter vim'

  let s:import_pattern = '^\s*import\_s\+<import>\_s*;'
" }}}

" FileExists(name) {{{
" Determines if the src dir relative file exists.
function! eclim#java#util#FileExists (name)
  let command = substitute(s:command_src_exists, '<file>', a:name, '')
  let result = eclim#ExecuteEclim(command)
  return result =~ '^true$'
endfunction " }}}

" GetClassname(...) {{{
" Gets the classname of the current file.
" Optional file argument may be supplied.
function! eclim#java#util#GetClassname (...)
  if a:0 > 0
    return fnamemodify(a:1, ":t:r")
  endif
  return expand("%:t:r")
endfunction " }}}

" GetClassDeclarationPosition(movecursor) {{{
" Gets the line number of the current file's class declaration.
function! eclim#java#util#GetClassDeclarationPosition (movecursor)
  let line = line('.')
  let col = col('.')
  call cursor(1,1)

  let position = search(s:class_declaration)

  if !a:movecursor || !position
    call cursor(line, col)
  endif

  return position
endfunction " }}}

" GetFullyQualifiedClassname(...) {{{
" Gets the fully qualified classname of the current file.
" Optional file argument may be supplied.
function! eclim#java#util#GetFullyQualifiedClassname(...)
  if a:0 > 0
    return eclim#java#util#GetPackage(a:1) . '.' . eclim#java#util#GetClassname(a:1)
  endif
  return eclim#java#util#GetPackage() . '.' . eclim#java#util#GetClassname()
endfunction " }}}

" GetFilename() {{{
" Gets the src dir relative file name.
function! eclim#java#util#GetFilename ()
  "let filename = substitute(eclim#java#util#GetPackage(), '\.', '/', 'g')
  "return filename . '/' . expand('%:t')
  return eclim#project#GetProjectRelativeFilePath(expand('%:p'))
endfunction " }}}

" GetPackage(...) {{{
" Gets the package of the current src file, or of the optionally supplied file
" argument.
function! eclim#java#util#GetPackage (...)
  if a:0 > 0
    let winreset = winrestcmd()
    silent exec "split " . a:1
  endif

  let line = line('.')
  let col = col('.')

  call cursor(1,1)

  let package = ""
  let packageLine = search('^\s*\<package\>', 'w')
  if packageLine > 0
    let package =
      \ substitute(getline('.'), '.*\<package\>\s\+\(.\{-\}\)[ ;].*', '\1', '')
  endif

  if a:0 > 0
    close
    silent exec winreset

    " not necessary and may screw up display (see autoload/project.vim)
    "redraw
  else
    call cursor(line, col)
  endif

  return package
endfunction " }}}

" GetPackageFromImport(class) {{{
" Attempt to determine a class' package from the current file's import
" statements.
function! eclim#java#util#GetPackageFromImport (class)
  let pattern = '^\s*import\s\+\([0-9A-Za-z._]*\)\.' . a:class . '\s*;'
  let found = search(pattern, 'wn')
  if found
    return substitute(getline(found), pattern, '\1', '')
  endif
  return ""
endfunction " }}}

" GetSelectedFields(first, last) {{{
" Gets list of selected fields.
function! eclim#java#util#GetSelectedFields (first, last) range
  " normalize each field statement into a single line.
  let selection = ''
  let index = a:first
  let blockcomment = 0
  while index <= a:last
    let line = getline(index)

    " ignore comment lines
    if line =~ '^\s*/\*'
      let blockcomment = 1
    elseif blockcomment && line =~ '\*/\s*$'
      let blockcomment = 0
    elseif line !~ '^\s*//' && !blockcomment
      " remove quoted values.
      let line = substitute(line, '".\{-}"', '', 'g')
      " strip off trailing comments
      let line = substitute(line, '//.*', '', '')
      let line = substitute(line, '/\*.*\*/', '', '')

      let selection = selection . line
    endif

    let index += 1
  endwhile

  " compact comma separated multi field declarations
  let selection = substitute(selection, ',\s*', ',', 'g')
  " break fields back up into their own line.
  let selection = substitute(selection, ';', ';\n', 'g')
  " remove the assignment portion of the field.
  let selection = substitute(selection, '\(.\{-}\)\s*=.\{-};', '\1;', 'g')

  " extract field names
  let properties = []
  let lines = split(selection, '\n')
  for line in lines
    if line !~ '^\s*\/\/'
      let fields = substitute(line, '.*\s\(.*\);', '\1', '')
      if fields =~ '^[a-zA-Z0-9_,]'
        for field in split(fields, ',')
          call add(properties, field)
        endfor
      endif
    endif
  endfor

  return properties
endfunction " }}}

" IsKeyword(word) {{{
" Determines if the supplied word is a java keyword.
function! eclim#java#util#IsKeyword (word)
  return (a:word =~ '^' . s:keywords . '$')
endfunction " }}}

" IsImported(classname) {{{
" Determines if the supplied fully qualified classname is imported by the
" current java source file.
function! eclim#java#util#IsImported (classname)
  " search for fully qualified import
  let import_search = s:import_pattern
  let import_search = substitute(import_search, '<import>', a:classname, '')
  let found = search(import_search, 'wn')
  if found
    return 1
  endif

  " search for package.* import
  let package = substitute(a:classname, '\(.*\)\..*', '\1', '')
  let import_search = s:import_pattern
  let import_search = substitute(import_search, '<import>', package . '\\.\\*', '')
  let found = search(import_search, 'wn')
  if found
    return 1
  endif

  " check if current file and supplied classname are in the same package
  if eclim#java#util#GetPackage() == package
    return 1
  endif

  " not imported
  return 0
endfunction " }}}

" IsValidIdentifier(word) {{{
" Determines if the supplied word is a valid java identifier.
function! eclim#java#util#IsValidIdentifier (word)
  if a:word == '' || a:word =~ '\W' || eclim#java#util#IsKeyword(a:word)
    return 0
  endif
  return 1
endfunction " }}}

" SilentUpdate() {{{
" Silently updates the current source file w/out validation.
function! eclim#java#util#SilentUpdate ()
  let saved = g:EclimJavaSrcValidate
  try
    "stopping the autocommands breaks code completion.
    "call StopAutocommands()
    let g:EclimJavaSrcValidate = 0
    silent update
  finally
    let g:EclimJavaSrcValidate = saved
    "call StartAutocommands()
  endtry
endfunction " }}}

" UpdateSrcFile() {{{
" Updates the src file on the server w/ the changes made to the current file.
function! eclim#java#util#UpdateSrcFile ()
  let project = eclim#project#GetCurrentProjectName()
  if project != ""
    let file = eclim#java#util#GetFilename()
    let command = s:update_command
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')
    if g:EclimJavaSrcValidate && !eclim#util#WillWrittenBufferClose()
      let command = command . " -v"
    endif

    let result = eclim#ExecuteEclim(command)
    if g:EclimJavaSrcValidate && !eclim#util#WillWrittenBufferClose()
      if result =~ '|'
        let errors = eclim#util#ParseLocationEntries(split(result, '\n'))
        call eclim#util#SetLocationList(errors)
      else
        call eclim#util#SetLocationList([], 'r')
      endif
    endif
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
