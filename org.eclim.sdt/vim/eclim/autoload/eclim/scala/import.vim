" Author:  Fangmin Lv
"
" Description: {{{
"   see http://eclim.org/vim/scala/import.html (TBD)
"
" License:
"
" Copyright (C) 2011  Eric Van Dewoestine
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
"
" Script Varables {{{
let s:command_import =
    \ '-command scala_import -p "<project>" -f "<file>" ' .
    \ '-o <offset> -e <encoding> -t <type>'
" }}}

" Import() {{{
" Handle package import
function! eclim#scala#import#Import()
    if !eclim#project#util#IsCurrentFileInProject(0)
        return
    endif

    let project = eclim#project#util#GetCurrentProjectName()
    let file = eclim#project#util#GetProjectRelativeFilePath()
    let offset = eclim#util#GetOffset()
    let encoding = eclim#util#GetEncoding()
    let type = expand('<cword>')

    let command = s:command_import
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')
    let command = substitute(command, '<offset>', offset, '')
    let command = substitute(command, '<encoding>', encoding, '')
    let command = substitute(command, '<type>', type, '')

    let result = eclim#Execute(command)

    if type(result) == g:STRING_TYPE
        call eclim#util#EchoError(result)
        return
    endif

    if type(result) != g:LIST_TYPE
        return
    endif

    let choice = eclim#scala#import#ImportPrompt(result)
    if choice != ''
        call eclim#scala#import#AddImport(choice)
    endif
endfunction " }}}

" ImportPrompt(choices) {{{
" Pop up a list of import suggestions
function! eclim#scala#import#ImportPrompt(choices)
  " prompt the user to choose the class to import.
  let response = eclim#util#PromptList("Choose the class to import", a:choices)
  if response == -1
    return ''
  endif

  return get(a:choices, response)
endfunction " }}}

" AddImport(choice) {{{
" Add the selected import to the scala file
function! eclim#scala#import#AddImport(choice)
    let save_cursor = getpos(".")
    let content = "import " . a:choice
    let addPos = eclim#scala#import#FindImportPos(content)

    if addPos == -1
        call eclim#util#Echo("Import " . a:choice . " already exist")
        call setpos('.', save_cursor)
        return
    endif

    call append(addPos, content)
    write

    call eclim#util#Reload({'pos': [save_cursor[1] + 1, save_cursor[2]]})
    call eclim#lang#UpdateSrcFile('scala', 1)
    call eclim#util#Echo('Imported ' . a:choice)
    let save_cursor[1] = save_cursor[1] + 1
    call setpos('.', save_cursor)
endfunction " }}}

" FindImportPos(content) {{{
" Find the insert position according to the alphabet order
function! eclim#scala#import#FindImportPos(content)
    " set the the first col row pos to start search
    call cursor(1, 1)
    let importPos = search("^import", "cW")
    if importPos == 0
        " import not exist
        let packagePos = search("^package", "cW")
        if packagePos == 0
            " package not exist
            return 0
        endif
        return packagePos + 1
    endif

    while importPos != 0
        let importContent = getline(importPos)
        if a:content < importContent
            return importPos - 1
        elseif a:content == importContent
            return -1
        else
            " search next import position
            let preImportPos = importPos
            call cursor(importPos + 1, 1)
            let importPos = search("^import", "cW")
            if importPos == 0
                return preImportPos + 1
            endif
        endif
    endwhile
endfunction " }}}
