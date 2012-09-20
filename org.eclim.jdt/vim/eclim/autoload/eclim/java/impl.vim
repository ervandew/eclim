" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/impl.html
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

" Script Varables {{{
  let s:command_constructor =
    \ '-command java_constructor -p "<project>" -f "<file>" -o <offset> -e <encoding>'
  let s:command_properties =
    \ '-command java_bean_properties -p "<project>" -f "<file>" ' .
    \ '-o <offset> -e <encoding> -t <type> -r <properties> <indexed>'
  let s:command_impl =
    \ '-command java_impl -p "<project>" -f "<file>" -o <offset> -e <encoding>'
  let s:command_impl_insert =
    \ '-command java_impl -p "<project>" -f "<file>" -t "<type>" ' .
    \ '-s "<superType>" <methods>'
  let s:command_delegate =
    \ '-command java_delegate -p "<project>" -f "<file>" -o <offset> -e <encoding>'
  let s:command_delegate_insert =
    \ '-command java_delegate -p "<project>" -f "<file>" -v "<type>" ' .
    \ '-s "<superType>" <methods>'


  let s:no_properties =
    \ 'Unable to find property at current cursor position: ' .
    \ 'Not on a field declaration or possible java syntax error.'
  let s:cross_type_selection = "Visual selection is currently limited to methods of one super type at a time."
" }}}

function! eclim#java#impl#Constructor(first, last, bang) " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  call eclim#lang#SilentUpdate()

  let properties = a:last == 1 ? [] :
    \ eclim#java#util#GetSelectedFields(a:first, a:last)
  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()

  let command = s:command_constructor
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<offset>', eclim#util#GetOffset(), '')
  let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')
  if a:bang == ''
    let command .= ' -s'
  endif
  if len(properties) > 0
    let command .= ' -r ''' . substitute(string(properties), "'", '"', 'g') . ''''
  endif

  let result = eclim#ExecuteEclim(command)
  if type(result) == g:STRING_TYPE && result != ''
    call eclim#util#EchoError(result)
    return
  endif

  if result != "0"
    call eclim#util#Reload({'retab': 1})
    write
  endif
endfunction " }}}

function! eclim#java#impl#GetterSetter(first, last, bang, type) " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  call eclim#lang#SilentUpdate()

  let properties = eclim#java#util#GetSelectedFields(a:first, a:last)

  if len(properties) == 0
    call eclim#util#EchoError(s:no_properties)
    return
  endif

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()
  let indexed = a:bang != '' ? '-i' : ''

  let command = s:command_properties
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<offset>', eclim#util#GetOffset(), '')
  let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')
  let command = substitute(command, '<type>', a:type, '')
  let command = substitute(command, '<properties>', join(properties, ','), '')
  let command = substitute(command, '<indexed>', indexed, '')

  let result = eclim#ExecuteEclim(command)
  if result != "0"
    call eclim#util#Reload({'retab': 1})
    write
  endif
endfunction " }}}

function! eclim#java#impl#Impl() " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  call eclim#lang#SilentUpdate()

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()
  let offset = eclim#util#GetCurrentElementOffset()

  let command = s:command_impl
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<offset>', offset, '')
  let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')

  call eclim#java#impl#ImplWindow(command)
endfunction " }}}

function! eclim#java#impl#ImplWindow(command) " {{{
  if (eclim#java#impl#Window(a:command, "impl"))
    nnoremap <silent> <buffer> <cr> :call <SID>AddImpl(0)<cr>
    vnoremap <silent> <buffer> <cr> :<C-U>call <SID>AddImpl(1)<cr>
  endif
endfunction " }}}

function! eclim#java#impl#ImplWindowFolding() " {{{
  setlocal foldmethod=syntax
  setlocal foldlevel=99
endfunction " }}}

function! eclim#java#impl#Delegate() " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  call eclim#lang#SilentUpdate()

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()
  let offset = eclim#util#GetCurrentElementOffset()
  let encoding = eclim#util#GetEncoding()

  let command = s:command_delegate
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<offset>', offset, '')
  let command = substitute(command, '<encoding>', encoding, '')

  call eclim#java#impl#DelegateWindow(command)
endfunction " }}}

function! eclim#java#impl#DelegateWindow(command) " {{{
  if (eclim#java#impl#Window(a:command, "delegate"))
    nnoremap <silent> <buffer> <cr> :call <SID>AddDelegate(0)<cr>
    vnoremap <silent> <buffer> <cr> :<C-U>call <SID>AddDelegate(1)<cr>
  endif
endfunction " }}}

function! eclim#java#impl#Add(command, function, visual) " {{{
  let winnr = bufwinnr(bufnr('^' . b:filename))
  " src window is not longer open.
  if winnr == -1
    call eclim#util#EchoError(b:filename . ' no longer found in an open window.')
    return
  endif

  if a:visual
    let start = line("'<")
    let end = line("'>")
  endif

  let superType = ""
  let methods = []
  " non-visual mode or only one line selected
  if !a:visual || start == end
    " not a valid selection
    if line('.') == 1 || getline('.') =~ '^\(\s*//\|package\|$\|}\)'
      return
    endif

    let line = getline('.')
    if line =~ '^\s*throws'
      let line = getline(line('.') - 1)
    endif
    " on a method line
    if line =~ '^\s\+'
      call add(methods, s:MethodSig(line))
      let ln = search('^\w', 'bWn')
      if ln > 0
        let superType = substitute(getline(ln), '.*\s\(.*\) {', '\1', '')
      endif
    " on a type line
    else
      let superType = substitute(line, '.*\s\(.*\) {', '\1', '')
    endif

  " visual mode
  else
    let pos = getpos('.')
    let index = start
    while index <= end
      let line = getline(index)
      if line =~ '^\s*\($\|throws\|package\)'
        " do nothing
      " on a method line
      elseif line =~ '^\s\+'
        call add(methods, s:MethodSig(line))
        call cursor(index, 1)
        let ln = search('^\w', 'bWn')
        if ln > 0
          let super = substitute(getline(ln), '.*\s\(.*\) {', '\1', '')
          if superType != "" && super != superType
            call eclim#util#EchoError(s:cross_type_selection)
            call setpos('.', pos)
            return
          endif
          let superType = super
        endif
      " on a type line
      else
        let super = substitute(line, '.*\s\(.*\) {', '\1', '')
        if superType != "" && super != superType
          call eclim#util#EchoError(s:cross_type_selection)
          call setpos('.', pos)
          return
        endif
        let superType = super
      endif
      call setpos('.', pos)

      let index += 1
    endwhile

    if superType == ""
      return
    endif
  endif

  " search up for the nearest package
  let ln = search('^package', 'bWn')
  if ln > 0
    let package = substitute(getline(ln), '.*\s\(.*\);', '\1', '')
    let superType = package . '.' . substitute(superType, '<.\{-}>', '', 'g')
  endif

  let type = substitute(getline(1), '\$', '.', 'g')
  let impl_winnr = winnr()
  exec winnr . "winc w"
  call eclim#lang#SilentUpdate()

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()

  let command = a:command
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<type>', type, '')
  let command = substitute(command, '<superType>', superType, '')
  if len(methods)
    let json = substitute(string(methods), "'", '"', 'g')
    let command = substitute(command, '<methods>', '-m ''' . json . '''', '')
  else
    let command = substitute(command, '<methods>', '', '')
  endif

  call a:function(command)

  noautocmd exec winnr . "winc w"
  call eclim#util#Reload({'retab': 1})
  write
  noautocmd exec impl_winnr . "winc w"
endfunction " }}}

function! eclim#java#impl#Window(command, name) " {{{
  let name = eclim#project#util#GetProjectRelativeFilePath() . '_' . a:name
  let project = eclim#project#util#GetCurrentProjectName()
  let workspace = eclim#project#util#GetProjectWorkspace(project)
  let port = eclim#client#nailgun#GetNgPort(workspace)

  let result = eclim#ExecuteEclim(a:command, port)
  if type(result) == g:STRING_TYPE
    call eclim#util#EchoError(result)
    return
  endif
  if type(result) != g:DICT_TYPE
    return
  endif

  let content = [result.type]
  for super in result.superTypes
    call add(content, '')
    call add(content, 'package ' . super.packageName . ';')
    call add(content, super.signature . ' {')
    for method in super.methods
      let signature = split(method, '\n')
      let content += map(signature, '"\t" . v:val')
    endfor
    call add(content, '}')
  endfor

  call eclim#util#TempWindow(name, content, {'preserveCursor': 1})
  setlocal ft=java
  call eclim#java#impl#ImplWindowFolding()
  return 1
endfunction " }}}

function! s:AddImpl(visual) " {{{
  call eclim#java#impl#Add
    \ (s:command_impl_insert, function("eclim#java#impl#ImplWindow"), a:visual)
endfunction " }}}

function! s:AddDelegate(visual) " {{{
  call eclim#java#impl#Add
    \ (s:command_delegate_insert, function("eclim#java#impl#DelegateWindow"), a:visual)
endfunction " }}}

function! s:MethodSig(line) " {{{
  let sig = substitute(a:line, '.*\s\(\w\+(.*\)', '\1', '')
  let sig = substitute(sig, ',\s', ',', 'g')
  let sig = substitute(sig, '<.\{-}>', '', 'g')
  return sig
endfunction " }}}

" vim:ft=vim:fdm=marker
