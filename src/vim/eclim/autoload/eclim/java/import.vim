" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/complete.html
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
let s:command_import = '-command java_import -n "<project>" -p <classname>'
let s:command_unused_imports =
  \ '-command java_imports_unused -p "<project>" -f "<file>"'
" }}}

" Import() {{{
" Import the element under the cursor.
function! eclim#java#import#Import ()
  let classname = expand('<cword>')

  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  if !eclim#java#util#IsValidIdentifier(classname) ||
     \ eclim#java#util#IsKeyword(classname)
    call eclim#util#EchoError("'" . classname . "' not a classname.")
    return
  endif

  " see if import already exists.
  if search('^\s*import\s\+.*\<' . classname . '\>\s*;', 'nw')
    call eclim#util#EchoInfo("'" . classname . "' already imported.")
    return
  endif

  let project = eclim#project#util#GetCurrentProjectName()
  let command = s:command_import
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<classname>', classname, '')
  let results = split(eclim#ExecuteEclim(command), '\n')

  " filter the list if the user has any exclude patterns
  if exists("g:JavaImportExclude")
    for exclude in g:JavaImportExclude
      call filter(results, " v:val !~ '" . exclude . "'")
    endfor
  endif

  if len(results) == 0
    call eclim#util#EchoError("No classes found for '" . classname . "'.")
    return
  endif

  " prompt the user to choose the class to import.
  let response = eclim#util#PromptList
    \ ("Choose the class to import", results, g:EclimInfoHighlight)
  if response == -1
    return
  endif

  let class = get(results, response)

  " check if the class is in java.lang
  if class =~ '^java\.lang\.[A-Z]'
    call eclim#util#EchoInfo("No need to import '" . classname . "' from 'java.lang'.")
    return
  endif

  " check if the class is in the same package as the current file.
  if class =~ '^' . eclim#java#util#GetPackage() . '\.[a-zA-Z0-9_$]\+$'
    call eclim#util#Echo("No need to import '" . classname .
      \ "', since it is in the same package as the current file.")
    return
  endif

  if class != '0' && eclim#java#import#InsertImport(class)
    call eclim#util#EchoInfo("Imported '" . class . "'.")
  endif
endfunction " }}}

" InsertImport(class) {{{
" Inserts the import for the fullyqualified classname supplied.
function! eclim#java#import#InsertImport (class)
  " insert the import statement.
  let position =  search('^\s*package\s\+', 'nw')

  " save mark
  let markLine = eclim#util#MarkSave()

  let class = substitute(a:class, '\$', '\.', 'g')
  call append(position, 'import ' . class . ';')
  call append(position, '')

  " restore mark
  call eclim#util#MarkRestore(markLine + 2)

  call eclim#java#import#CleanImports()
  call eclim#java#import#SortImports()

  return 1
endfunction " }}}

" SortImports() {{{
" Sorts the import statements for the current file.
function! eclim#java#import#SortImports ()
  let line = line('.')
  let col = col('.')

  let markLine = eclim#util#MarkSave()

  call cursor(1,1)
  let firstImport = search('^\s*import\s\+.*;')
  call cursor(line('$'),1)
  let lastImport = search('^\s*import\s\+.*;', 'bW')

  if firstImport == 0 || firstImport == lastImport
    call cursor(line, col)
    return
  endif

  " create list of the imports
  let saved = @"
  silent exec firstImport . "," . lastImport . "delete"
  let imports = split(@", '\n')
  let prevLength = len(imports)
  call filter(imports, 'v:val !~ "^\s*$"')
  let line = line - (prevLength - len(imports))
  let markLine = markLine - (prevLength - len(imports))
  let @" = saved

  " sort the imports and put them back in the file
  call sort(imports)
  call append(line('.') - 1, imports)

  " move java imports to the top
  call cursor(firstImport - 1, 1)
  let firstJava = search('^\s*import\s\+java[x]\?\..*;')
  call cursor(line('$'),1)
  let lastJava = search('^\s*import\s\+java[x]\?\..*;', 'bW')
  if firstJava != 0
    let save = @"
    silent exec firstJava . ',' . lastJava . 'delete'
    call cursor(firstImport, 1)
    silent put!
    let @" = save
  endif

  " separate imports by package name
  call cursor(firstImport, 1)
  let pattern = substitute(getline('.'), '.*import\s\+\(.*\)\..*\s*;.*', '\1', '')
  while line('.') <= lastImport && getline('.') =~ '.*import\s\+'
    let nextpattern = substitute(getline('.'), '.*import\s\+\(.*\)\..*\s*;.*', '\1', '')
    if nextpattern != pattern
      call append(line('.') - 1, '')
      let line = line + 1
      let markLine = markLine + 1
      let lastImport = lastImport + 1
      let pattern = nextpattern
    endif
    call cursor(line('.') + 1, 1)
  endwhile

  call eclim#util#MarkRestore(markLine)
  call cursor(line, col)
endfunction " }}}

" CleanImports() {{{
" Removes unused import statements for the current file.
function! eclim#java#import#CleanImports ()
  let line = line('.')
  let col = col('.')

  " eclim method
  let project = eclim#project#util#GetCurrentProjectName()
  if project != ''
    call eclim#java#util#SilentUpdate()

    let file = eclim#java#util#GetFilename()

    let command = s:command_unused_imports
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')

    let results = split(eclim#ExecuteEclim(command), '\n')
    if len(results) == 1 && results[0] == '0'
      return
    endif

    let saved = @"
    " save mark
    let markLine = eclim#util#MarkSave()

    for result in results
      let importLine = search('^\s*import\s\+' . result . '\s*;\s*$', 'nw')
      if importLine > 0
        silent exec importLine . ',' . importLine . 'delete'
        let markLine = markLine - 1
        let line = line - 1
        if getline(importLine) =~ '^\s*$' && getline(importLine - 1) =~ '^\s*$'
          silent exec importLine . ',' . importLine . 'delete'
          let mark = markLine - 1
          let line = line - 1
        endif
      endif
    endfor

    " restore saved values
    let @" = saved
    call eclim#util#MarkRestore(markLine)
    call cursor(line, col)

    return
  endif

  " Vim only method.  Doesn't handle ignoring of comments when deciding if an
  " import is unused.

  call cursor(1,1)
  let firstImport = search('^\s*import\s\+.*;')
  call cursor(line('$'),1)
  let lastImport = search('^\s*import\s\+.*;', 'bW')

  if firstImport == 0 || firstImport == lastImport
    call cursor(line, col)
    return
  endif

  " save mark
  let markLine = eclim#util#MarkSave()

  call cursor(firstImport, 1)
  while line('.') <= lastImport
    let curline = line('.')
    if getline(line('.')) !~ '^\s*$'
      let classname =
        \ substitute(getline(line('.')), '.*import\s\+.*\.\(.*\)\s*;.*', '\1', '')
      call cursor(curline + 1, 1)
      if classname != '*' && search('\<' . classname . '\>', 'W') == 0
        silent exec curline . 'delete'
        let markLine = markLine - 1
        let line = line - 1
        let lastImport = lastImport - 1
        if getline(curline) =~ '^\s*$' && getline(curline - 1) =~ '^\s*$'
          silent exec curline . 'delete'
          let mark = markLine - 1
          let line = line - 1
          let lastImport = lastImport - 1
        endif
      else
        cal cursor(curline + 1, 1)
      endif
    else
      call cursor(curline + 1, 1)
    endif
  endwhile

  " restore saved values
  call eclim#util#MarkRestore(markLine)
  call cursor(line, col)
endfunction " }}}

" vim:ft=vim:fdm=marker
