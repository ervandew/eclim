" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/import.html
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
  if !exists('g:EclimJavaImportPackageSeparationLevel')
    " -1 = separate based on full package
    "  0 = never separate
    "  n = separate on comparing of n segments of the package.
    let g:EclimJavaImportPackageSeparationLevel = -1
  endif
" }}}

" Script Variables {{{
let s:command_import = '-command java_import -n "<project>" -p <classname>'
let s:command_import_missing =
  \ '-command java_import_missing -p "<project>" -f "<file>"'
let s:command_unused_imports =
  \ '-command java_imports_unused -p "<project>" -f "<file>"'
" }}}

" Import() {{{
" Import the element under the cursor.
function! eclim#java#import#Import()
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
  if class =~ '^java\.lang\.[A-Z]\C'
    call eclim#util#EchoInfo("No need to import '" . classname . "' from 'java.lang'.")
    return
  endif

  " check if the class is in the same package as the current file.
  if class =~ '^' . eclim#java#util#GetPackage() . '\.[a-zA-Z0-9_$]\+$'
    call eclim#util#Echo("No need to import '" . classname .
      \ "', since it is in the same package as the current file.")
    return
  endif

  if class != '0' && eclim#java#import#InsertImports([class])
    call eclim#util#EchoInfo("Imported '" . class . "'.")
  endif
endfunction " }}}

" ImportMissing() {{{
" Add imports for any undefined types.
function! eclim#java#import#ImportMissing()
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  call eclim#java#util#SilentUpdate()

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#java#util#GetFilename()
  let command = s:command_import_missing
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let result = eclim#ExecuteEclim(command)
  if result == "0"
    return
  endif

  let results = eval(result)
  for info in results
    let type = info.type
    let imports = info.imports

    " filter the list if the user has any exclude patterns
    if exists("g:JavaImportExclude")
      for exclude in g:JavaImportExclude
        call filter(imports, " v:val !~ '" . exclude . "'")
      endfor
    endif

    if len(imports) == 0
      continue
    endif

    " prompt the user to choose the class to import.
    let response = eclim#util#PromptList
      \ ("Choose the class to import", imports, g:EclimInfoHighlight)
    if response == -1
      return
    endif

    let class = get(imports, response)
    if class != '0'
      call eclim#java#import#InsertImports([class])
    endif
  endfor
endfunction " }}}

" InsertImports(classes) {{{
" Inserts list of fully qualified class names.
function! eclim#java#import#InsertImports(classes)
  let line = line('.')
  let col = col('.')

  let classes = a:classes[:]
  call sort(classes)

  let imports = s:CutImports()
  let index = 0
  let lastimport = -1
  let class = classes[0]
  let prevclass = ''
  for import in imports[:]
    if import =~ '^\s*import\s'
      let ic = substitute(import, '^\s*import\s\+\(.\{-}\)\s*;\s*', '\1', '')
      while class < ic
        let line += 1
        " grouped with the previous import, insert just after it.
        if prevclass != '' && s:CompareClasses(prevclass, class)
          call insert(imports, 'import ' . class . ';', lastimport + 1)

        " grouped with the current import, insert just before it
        elseif s:CompareClasses(ic, class)
          " edge case for 0 package level comparison, insert after the
          " previous import.
          if g:EclimJavaImportPackageSeparationLevel == 0
            call insert(imports, 'import ' . class . ';', lastimport + 1)
          else
            call insert(imports, 'import ' . class . ';', index)
          endif

        " not grouped with others.
        else
          call insert(imports, 'import ' . class . ';', lastimport + 1)

          " first import insert at the top, create the separator below
          if prevclass == ''
            call insert(imports, '', 1)

          " separator above
          else
            call insert(imports, '', lastimport + 1)
            let lastimport += 1
          endif
          let line += 1
          let index += 1
        endif

        call remove(classes, 0)
        if len(classes) == 0
          break
        endif

        let index += 1
        let lastimport += 1
        let prevclass = class
        let class = classes[0]
      endwhile

      if len(classes) == 0
        break
      endif

      let lastimport = index
      let prevclass = ic
    endif
    let index += 1
  endfor

  for class in classes
    let line += 1
    call add(imports, 'import ' . class . ';')
    if prevclass != '' && !s:CompareClasses(prevclass, class)
      let line += 1
      call insert(imports, '', -1)
    endif
    let prevclass = class
  endfor

  let line += s:PasteImports(imports)
  call cursor(line, col)

  return 1
endfunction " }}}

" SortImports() {{{
" Sorts the import statements for the current file.
function! eclim#java#import#SortImports()
  let line = line('.')
  let col = col('.')

  let imports = s:CutImports()
  let prevlen = len(imports)
  call filter(imports, 'v:val !~ "^\s*$"')
  let line -= prevlen - len(imports)

  if len(imports) > 0
    " sort the imports and put them back in the file
    call sort(imports)

    " find section of java imports
    let jf = -1
    let jl = -1
    let index = 0
    for import in imports
      if import =~ '^\s*import\s\+java[x]\?\..*;'
        if jf == -1
          let jf = index
          let jl = index
        else
          let jl = index
        endif
      elseif jf != -1
        break
      endif
      let index += 1
    endfor

    " move java imports to the top.
    let java_imports = remove(imports, jf, jl)
    let imports = java_imports + imports

    " separate imports by package name
    let package = substitute(imports[0], '.*import\s\+\(.*\)\..*\s*;.*', '\1', '')
    let index = 0
    for import in imports[:]
      let next = substitute(import, '.*import\s\+\(.*\)\..*\s*;.*', '\1', '')
      if !s:ComparePackages(package, next)
        let package = next
        call insert(imports, '', index)
        let index += 1
        let line += 1
      endif
      let index += 1
    endfor

    let line += s:PasteImports(imports)
  endif

  call cursor(line, col)
endfunction " }}}

" CleanImports() {{{
" Removes unused import statements for the current file.
function! eclim#java#import#CleanImports()
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

    " save mark
    let markLine = eclim#util#MarkSave()

    for result in results
      let importLine = search('^\s*import\s\+' . result . '\s*;\s*$', 'nw')
      if importLine > 0
        silent exec importLine . ',' . importLine . 'delete _'
        let markLine = markLine - 1
        let line = line - 1
        if getline(importLine) =~ '^\s*$' && getline(importLine - 1) =~ '^\s*$'
          silent exec importLine . ',' . importLine . 'delete _'
          let mark = markLine - 1
          let line = line - 1
        endif
      endif
    endfor

    " restore saved values
    call eclim#util#MarkRestore(markLine)
    call cursor(line, col)

    return
  endif

  " Vim only method.  Doesn't handle ignoring of comments when deciding if an
  " import is unused.

  call cursor(1,1)
  let firstImport = search('^\s*import\s\+.*;')
  call cursor(line('$'), 1)
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

" s:CutImports() {{{
" Cuts the imports from the current file and returns the lines as a list.
function! s:CutImports()
  call cursor(1,1)
  let firstImport = search('^\s*import\s\+.*;')
  call cursor(line('$'),1)
  let lastImport = search('^\s*import\s\+.*;', 'bW')

  if firstImport == 0
    return []
  endif

  " create list of the imports
  let save = @"
  silent exec firstImport . "," . lastImport . "delete"
  return split(@", '\n')
endfunction " }}}

" s:PasteImports(imports) {{{
" Pastes a list of imports into the current file.
" Returns a number indicating a change in lines added or removed, not counting
" the import lines, just any adjustment it performed for white space
" normalization.
function! s:PasteImports(imports)
  if len(a:imports)
    let lines = 0
    let position =  search('^\s*package\s\+', 'nw')
    if getline(position + 1) =~ '^\s*$'
      let position += 1
    elseif a:imports[0] !~ '^\s*$'
      call append(position, '')
      let lines += 1
      let position += 1
    endif

    call append(position, a:imports)

    call cursor(line('$'), 1)
    let lastImport = search('^\s*import\s\+.*;', 'bW')

    if getline(lastImport + 1) !~ '^\s*$'
      call append(lastImport, '')
      let lines += 1
    else
      while getline(lastImport + 2) =~ '^\s*$'
        exec (lastImport + 2) . ',' . (lastImport + 2) . 'delete _'
        let lines -= 1
      endwhile
    endif
    return lines
  endif
  return 0
endfunction " }}}

" s:CompareClasses() {{{
" Compares the two classes to determine if they should be separated from each
" other in the import block of the class.  Returns 1 if they should be grouped
" together, 0 otherwise.
function! s:CompareClasses(c1, c2)
  let p1 = substitute(a:c1, '\(.*\)\..*', '\1', '')
  let p2 = substitute(a:c2, '\(.*\)\..*', '\1', '')
  return s:ComparePackages(p1, p2)
endfunction " }}}

" s:ComparePackages() {{{
" Compares the two packages to determine if they should be separated from each
" other in the import block of the class.  Returns 1 if they should be grouped
" together, 0 otherwise.
function! s:ComparePackages(p1, p2)
  let level = g:EclimJavaImportPackageSeparationLevel

  if level == 0
    return 1
  endif

  if level == -1
    return a:p1 == a:p2
  endif

  let p1 = split(a:p1, '\.')[:level - 1]
  let p2 = split(a:p2, '\.')[:level - 1]

  return p1 == p2
endfunction " }}}

" vim:ft=vim:fdm=marker
