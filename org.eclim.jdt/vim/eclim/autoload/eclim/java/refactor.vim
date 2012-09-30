" Author:  Eric Van Dewoestine
"
" License: {{{
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

" Script Varables {{{
  let s:command_rename = '-command java_refactor_rename ' .
    \ '-p "<project>" -f "<file>" -o <offset> -e <encoding> -l <length> -n <name>'
  let s:command_move = '-command java_refactor_move ' .
    \ '-p "<project>" -f "<file>" -n <package>'
" }}}

function! eclim#java#refactor#Rename(name) " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let element = expand('<cword>')
  if !eclim#java#util#IsValidIdentifier(element)
    call eclim#util#EchoError
      \ ("Element under the cursor is not a valid java identifier.")
    return
  endif

  let line = getline('.')
  let package_pattern = '^\s*package\s\+\(.*\%' . col('.') . 'c\w*\).*;'
  if line =~ package_pattern
    let element = substitute(line, package_pattern, '\1', '')
  endif

  let prompt = printf('Rename "%s" to "%s"', element, a:name)
  let result = exists('g:EclimRefactorPromptDefault') ?
    \ g:EclimRefactorPromptDefault : eclim#lang#RefactorPrompt(prompt)
  if result <= 0
    return
  endif

  " update the file before vim makes any changes.
  call eclim#lang#SilentUpdate()
  wall

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()
  let position = eclim#util#GetCurrentElementPosition()
  let offset = substitute(position, '\(.*\);\(.*\)', '\1', '')
  let length = substitute(position, '\(.*\);\(.*\)', '\2', '')

  let command = s:command_rename
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<offset>', offset, '')
  let command = substitute(command, '<length>', length, '')
  let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')
  let command = substitute(command, '<name>', a:name, '')
  " user chose preview at the prompt
  if result == 2
    let command .= ' -v'
    call eclim#lang#RefactorPreview(command)
    return
  endif

  call eclim#lang#Refactor(command)
endfunction " }}}

function! eclim#java#refactor#Move(package) " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let line = getline('.')
  let package_pattern = '^\s*package\s\+\(.*\%' . col('.') . 'c\w*\).*;'
  if line =~ package_pattern
    let element = substitute(line, package_pattern, '\1', '')
  endif

  let name = eclim#java#util#GetClassname()
  let package = eclim#java#util#GetPackage()
  let prompt = printf('Move %s from "%s" to "%s"', name, package, a:package)
  let result = exists('g:EclimRefactorPromptDefault') ?
    \ g:EclimRefactorPromptDefault : eclim#lang#RefactorPrompt(prompt)
  if result <= 0
    return
  endif

  " update the file before vim makes any changes.
  call eclim#lang#SilentUpdate()
  wall

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()
  let command = s:command_move
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<package>', a:package, '')
  " user chose preview at the prompt
  if result == 2
    let command .= ' -v'
    call eclim#lang#RefactorPreview(command)
    return
  endif
  call eclim#lang#Refactor(command)
endfunction " }}}

" vim:ft=vim:fdm=marker
