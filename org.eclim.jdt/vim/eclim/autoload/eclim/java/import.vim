" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/import.html
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
let s:command_import_order = '-command java_import_order -p "<project>"'
let s:import_regex = '^\s*import\s\+\(static\s\+\)\?\(.\{-}\)\s*;\s*'
" }}}

" Import([classname]) {{{
" Import the element under the cursor.
function! eclim#java#import#Import(...)
  let classname = a:0 ? a:1 : expand('<cword>')

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
  let results = eclim#ExecuteEclim(command)
  if type(results) != g:LIST_TYPE
    return
  endif

  " filter the list if the user has any exclude patterns
  if exists("g:EclimJavaImportExclude")
    for exclude in g:EclimJavaImportExclude
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
    return 1
  endif
endfunction " }}}

" ImportMissing() {{{
" Add imports for any undefined types.
function! eclim#java#import#ImportMissing()
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  call eclim#lang#SilentUpdate()

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()
  let command = s:command_import_missing
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let results = eclim#ExecuteEclim(command)
  if type(results) != g:LIST_TYPE
    return
  endif

  for info in results
    let type = info.type
    let imports = info.imports

    " filter the list if the user has any exclude patterns
    if exists("g:EclimJavaImportExclude")
      for exclude in g:EclimJavaImportExclude
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
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  call s:InitImportOrder()
  if !exists('s:import_order')
    return
  endif

  let line = line('.')
  let col = col('.')

  let classes = a:classes[:]
  call sort(classes)

  let imports = s:CutImports()
  let index = 0
  let lastimport = -1
  let class = classes[0]
  let newimport = 'import ' . class . ';'
  let prevclass = ''
  for import in imports[:]
    if import =~ '^\s*import\s'
      let ic = s:GetImportClass(import)
      while s:CompareImports(newimport, import) < 0
        let line += 1
        " grouped with the previous import, insert just after it.
        if prevclass != '' && s:CompareClassGroups(prevclass, class)
          call insert(imports, newimport, lastimport + 1)

        " grouped with the current import, insert just before it
        elseif s:CompareClassGroups(ic, class)
          " edge case for 0 package level comparison, insert after the
          " previous import.
          if g:EclimJavaImportPackageSeparationLevel == 0
            call insert(imports, newimport, lastimport + 1)
          else
            call insert(imports, newimport, index)
          endif

        " not grouped with others.
        else
          call insert(imports, newimport, lastimport + 1)

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
    if prevclass != '' && !s:CompareClassGroups(prevclass, class)
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
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  call s:InitImportOrder()
  if !exists('s:import_order')
    return
  endif

  let line = line('.')
  let col = col('.')

  let imports = s:CutImports()
  let prevlen = len(imports)
  call filter(imports, 'v:val !~ "^\s*$"')
  let line -= prevlen - len(imports)

  if len(imports) > 0
    " sort the imports and put them back in the file
    call sort(imports, function('s:CompareImports'))

    " separate imports by package name
    let class = s:GetImportClass(imports[0])
    let package = substitute(class, '\(.*\)\..*', '\1', '')
    let index = 0
    for import in imports[:]
      let nextclass = s:GetImportClass(import)
      let nextpackage = substitute(nextclass, '\(.*\)\..*', '\1', '')
      if !s:ComparePackageGroups(package, nextpackage)
        let package = nextpackage
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
    call eclim#lang#SilentUpdate()

    let file = eclim#project#util#GetProjectRelativeFilePath()

    let command = s:command_unused_imports
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')

    let results = eclim#ExecuteEclim(command)
    if type(results) != g:LIST_TYPE
      return
    endif

    " save mark
    let markLine = eclim#util#MarkSave()

    for result in results
      let importPattern =
        \ '^\s*import\s\+\(static\s\+\)\?' . escape(result, '.*') . '\s*;\s*$'
      let importLine = search(importPattern, 'nw')
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

  call cursor(1, 1)
  let firstImport = search('^\s*import\s\+.*;', 'c')
  call cursor(line('$'), 1)
  let lastImport = search('^\s*import\s\+.*;', 'bW')

  if firstImport == 0
    call cursor(line, col)
    return
  endif

  " save mark
  let markLine = eclim#util#MarkSave()

  call cursor(firstImport, 1)
  while line('.') <= lastImport
    let curline = line('.')
    if getline(line('.')) !~ '^\s*$'
      let classname = substitute(getline(line('.')), s:import_regex, '\2', '')
      let classname = substitute(classname, '.*\.\(.*\)', '\1', '')
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

" s:InitImportOrder() {{{
" Should only be called once per import command (not in a loop).
function! s:InitImportOrder()
  let project = eclim#project#util#GetCurrentProjectName()
  let command = s:command_import_order
  let command = substitute(command, '<project>', project, '')
  let result = eclim#ExecuteEclim(command)
  if type(result) != g:LIST_TYPE
    return
  endif
  let s:import_order = result
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
  let save = @e
  try
    silent exec firstImport . "," . lastImport . "delete e"
    return split(@e, '\n')
  finally
    let @e = save
  endtry
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

" s:CompareImports(i1, i2) {{{
" Compares two import statements to determine which should come first.
function! s:CompareImports(i1, i2)
  let c1 = s:GetImportClass(a:i1)
  let c2 = s:GetImportClass(a:i2)
  let result = s:CompareClasses(c1, c2)
  " sort static after regular import if both from the same class. also honor
  " sorting by the static field name if more than one static import.
  if result == 0
    " normalize the text
    let n1 = substitute(a:i1, '\s', '', 'g')
    let n2 = substitute(a:i2, '\s', '', 'g')
    return n1 < n2 ? -1 : 1
  endif
  return result
endfunction " }}}

" s:CompareClasses(c1, c2) {{{
" Compares two classes to determine which should come first.
function! s:CompareClasses(c1, c2)
  let max = len(s:import_order)
  let c1index = max
  let c2index = max
  let index = 0
  for p in s:import_order
    if c1index == max && a:c1 =~ '^' . escape(p, '.') . '\>'
      let c1index = index
    endif
    if c2index == max && a:c2 =~ '^' . escape(p, '.') . '\>'
      let c2index = index
    endif

    if c1index != max && c2index != max
      break
    endif
    let index += 1
  endfor

  if c1index < c2index || (c1index == c2index && a:c1 < a:c2)
    return -1
  endif

  if c1index > c2index || (c1index == c2index && a:c1 > a:c2)
    return 1
  endif

  return 0
endfunction " }}}

" s:CompareClassGroups(c1, c2) {{{
" Compares the two classes to determine if they should be separated from each
" other in the import block of the class.  Returns 1 if they should be grouped
" together, 0 otherwise.
function! s:CompareClassGroups(c1, c2)
  let p1 = substitute(a:c1, '\(.*\)\..*', '\1', '')
  let p2 = substitute(a:c2, '\(.*\)\..*', '\1', '')
  return s:ComparePackageGroups(p1, p2)
endfunction " }}}

" s:ComparePackageGroups(p1, p2) {{{
" Compares the two packages to determine if they should be separated from each
" other in the import block of the class.  Returns 1 if they should be grouped
" together, 0 otherwise.
function! s:ComparePackageGroups(p1, p2)
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

" s:GetImportClass(import) {{{
function! s:GetImportClass(import)
  let class = substitute(a:import, s:import_regex, '\2', '')
  if a:import =~ '^\s*import\s\+static\>'
    let class = substitute(class, '\(.*\)\..*', '\1', '')
  endif

  if class == a:import
    return ''
  endif
  return class
endfunction " }}}

" s:GetImportPackage(import) {{{
function! s:GetImportPackage(import)
  let class = s:GetImportClass(a:import)
  return s:GetClassPackage(class)
endfunction " }}}

" vim:ft=vim:fdm=marker
