" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Utility functions for java eclim ftplugins.
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

" Script Variables {{{
  let s:keywords = '\(abstract\|assert\|boolean\|case\|catch\|char\|class\|do\|double\|enum\|extends\|final\|finally\|float\|for\|if\|implements\|import\|int\|interface\|long\|new\|null\|package\|private\|protected\|public\|return\|short\|static\|switch\|throw\|throws\|try\|void\|while\)'

  let s:class_declaration = '^\s*\(public\|private\|protected\)\?\(\s\+abstract\)\?\s\+\(class\|interface\|enum\)\s\+[A-Z]'

  let s:update_command = '-command java_src_update -p "<project>" -f "<file>"'
  let s:command_src_exists = '-command java_src_exists -f "<file>"'
  let s:command_list_installs = '-command java_list_installs'

  let s:import_pattern = '^\s*import\_s\+<import>\_s*;'
" }}}

" FileExists(name) {{{
" Determines if the src dir relative file exists.
function! eclim#java#util#FileExists(name)
  let command = substitute(s:command_src_exists, '<file>', a:name, '')
  let result = eclim#ExecuteEclim(command)
  return result =~ '^true$'
endfunction " }}}

" GetClassname(...) {{{
" Gets the classname of the current file.
" Optional file argument may be supplied.
function! eclim#java#util#GetClassname(...)
  if a:0 > 0
    return fnamemodify(a:1, ":t:r")
  endif
  return expand("%:t:r")
endfunction " }}}

" GetClassDeclarationPosition(movecursor) {{{
" Gets the line number of the current file's class declaration.
function! eclim#java#util#GetClassDeclarationPosition(movecursor)
  let pos = getpos('.')
  call cursor(1,1)

  let position = search(s:class_declaration)

  if !a:movecursor || !position
    call setpos('.', pos)
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
function! eclim#java#util#GetFilename()
  "let filename = substitute(eclim#java#util#GetPackage(), '\.', '/', 'g')
  "return filename . '/' . expand('%:t')
  return eclim#project#util#GetProjectRelativeFilePath(expand('%:p'))
endfunction " }}}

" GetPackage(...) {{{
" Gets the package of the current src file, or of the optionally supplied file
" argument.
function! eclim#java#util#GetPackage(...)
  if a:0 > 0
    let winreset = winrestcmd()
    silent exec "sview " . a:1
  endif

  let pos = getpos('.')

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
    call setpos('.', pos)
  endif

  return package
endfunction " }}}

" GetPackageFromImport(class) {{{
" Attempt to determine a class' package from the current file's import
" statements.
function! eclim#java#util#GetPackageFromImport(class)
  let pattern = '^\s*import\s\+\([0-9A-Za-z._]*\)\.' . a:class . '\s*;'
  let found = search(pattern, 'wn')
  if found
    return substitute(getline(found), pattern, '\1', '')
  endif
  return ""
endfunction " }}}

" GetSelectedFields(first, last) {{{
" Gets list of selected fields.
function! eclim#java#util#GetSelectedFields(first, last) range
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
function! eclim#java#util#IsKeyword(word)
  return (a:word =~ '^' . s:keywords . '$\C')
endfunction " }}}

" IsImported(classname) {{{
" Determines if the supplied fully qualified classname is imported by the
" current java source file.
function! eclim#java#util#IsImported(classname)
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
function! eclim#java#util#IsValidIdentifier(word)
  if a:word == '' || a:word =~ '\W' || eclim#java#util#IsKeyword(a:word)
    return 0
  endif
  return 1
endfunction " }}}

" SilentUpdate() {{{
" Silently updates the current source file w/out validation.
function! eclim#java#util#SilentUpdate()
  " i couldn't reproduce the issue, but at least one person experienced the
  " cursor moving on update and breaking code completion:
  " http://sourceforge.net/tracker/index.php?func=detail&aid=1995319&group_id=145869&atid=763323
  let pos = getpos('.')

  let saved = g:EclimJavaSrcValidate
  try
    let g:EclimJavaSrcValidate = 0
    silent update
  finally
    call setpos('.', pos)
    let g:EclimJavaSrcValidate = saved
  endtry
endfunction " }}}

" UpdateSrcFile(validate) {{{
" Updates the src file on the server w/ the changes made to the current file.
function! eclim#java#util#UpdateSrcFile(validate)
  let project = eclim#project#util#GetCurrentProjectName()
  if project != ""
    let file = eclim#java#util#GetFilename()
    let command = s:update_command
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')
    if (g:EclimJavaSrcValidate || a:validate) && !eclim#util#WillWrittenBufferClose()
      let command = command . ' -v'
      if eclim#project#problems#IsProblemsList()
        let command = command . ' -b'
      endif
    endif

    let result = eclim#ExecuteEclim(command)
    if (g:EclimJavaSrcValidate || a:validate) && !eclim#util#WillWrittenBufferClose()
      if result =~ '|'
        let errors = eclim#util#ParseLocationEntries(
          \ split(result, '\n'), g:EclimValidateSortResults)
        call eclim#display#signs#SetPlaceholder()
        call eclim#util#ClearLocationList('global')
        call eclim#util#SetLocationList(errors, 'a')
        call eclim#display#signs#RemovePlaceholder()
      else
        " prevent closing of sign column between validation methods
        call eclim#display#signs#SetPlaceholder()

        call eclim#util#ClearLocationList('global')

        " prevent closing of sign column between validation methods
        call eclim#display#signs#SetPlaceholder()

        " FIXME: if we start adding anything more here, may want to consider
        " some sort of register process for plugins to listen for events
        " during various stages of the save process.
        if g:EclimJavaCheckstyleOnSave
          call eclim#java#checkstyle#Checkstyle()
        endif

        call eclim#display#signs#RemovePlaceholder()
      endif

      call eclim#project#problems#ProblemsUpdate()
    endif
  endif
endfunction " }}}

" Javac(bang) {{{
" Run javac.
function! eclim#java#util#Javac(bang)
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let project_path = eclim#project#util#GetCurrentProjectRoot()
  let project = eclim#project#util#GetCurrentProjectName()
  let args = '-p "' . project . '"'

  let cwd = getcwd()
  try
    exec 'lcd ' . escape(project_path, ' ')
    let exec = has('win32') || has('win64')
    call eclim#util#MakeWithCompiler('eclim_javac', a:bang, args, exec)
  finally
    exec 'lcd ' . escape(cwd, ' ')
  endtry
endfunction " }}}

" Java([args]) {{{
" Run a projects main class.
function! eclim#java#util#Java(args)
  let project = eclim#project#util#GetCurrentProjectName()
  if project == '' && exists('b:project')
    let project = b:project
  endif

  if project == ''
    call eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let workspace = eclim#project#util#GetProjectWorkspace(project)
  let port = eclim#client#nailgun#GetNgPort(workspace)

  let command = '!'
  let command .= eclim#client#nailgun#GetEclimCommand()
  let command .= ' -Dnailgun.server.port=' . port
  let command .= ' -command java -p "' . project . '"'

  let args = eclim#util#ParseArgs(a:args)
  if len(args)
    let command .= ' -a'
    for arg in args
      let command .= ' "' . arg . '"'
    endfor
  endif

  call eclim#util#TempWindow('[Java Output]', [])

  let outfile = g:EclimTempDir . '/eclim_java_output.txt'

  if has("win32") || has("win64")
    if executable("tee")
      let command .= ' | tee "' . outfile . '" 2>&1"'
    else
      let command .= ' >"' . outfile . '" 2>&1"'
    endif
  else
    let command .= ' 2>&1| tee "' . outfile . '"'
  endif

  " ensure the temp window was opened (test for empty window vs dealing with
  " all the escaping necessary to test against buffer name).
  if len(getline(1)) == 0 && line('$') == 1
    call eclim#util#Exec(command)
    set modifiable noreadonly
    exec 'silent read ' . escape(outfile, ' ')
    1,1delete _
    $,$delete _
    set nomodifiable readonly
    let b:project = project

    if exists(":Java") != 2
      command -buffer -nargs=* Java :call eclim#java#util#Java(<q-args>)
    endif
  endif
endfunction " }}}

" ListInstalls() {{{
" Lists all installed jdks/jres.
function! eclim#java#util#ListInstalls()
  let installs = split(eclim#ExecuteEclim(s:command_list_installs), '\n')
  if len(installs) == 0
    call eclim#util#Echo("No jdk/jre installs found.")
  endif
  if len(installs) == 1 && installs[0] == '0'
    return
  endif
  call eclim#util#Echo(join(installs, "\n"))
endfunction " }}}

" CommandCompleteProject(argLead, cmdLine, cursorPos) {{{
" Custom command completion for project names.
function! eclim#java#util#CommandCompleteProject(argLead, cmdLine, cursorPos)
  return eclim#project#util#CommandCompleteProjectByNature(
    \ a:argLead, a:cmdLine, a:cursorPos, 'java')
endfunction " }}}

" vim:ft=vim:fdm=marker
