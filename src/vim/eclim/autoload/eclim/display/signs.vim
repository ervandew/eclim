" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Functions for working with vim signs.
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
if !exists("g:EclimShowQuickfixSigns")
  let g:EclimShowQuickfixSigns = 1
endif

if !exists("g:EclimUserSignText")
  let g:EclimUserSignText = '#'
endif

if !exists("g:EclimUserSignHighlight")
  let g:EclimUserSignHighlight = g:EclimInfoHighlight
endif
" }}}

" Define(name, text, highlight) {{{
" Defines a new sign name or updates an existing one.
function! eclim#display#signs#Define (name, text, highlight)
  exec "sign define " . a:name . " text=" . a:text . " texthl=" . a:highlight
endfunction " }}}

" Place(name, line) {{{
" Places a sign in the current buffer.
function! eclim#display#signs#Place (name, line)
  if a:line > 0
    exec "sign place " . a:line . " line=" . a:line . " name=" . a:name .
      \ " buffer=" . bufnr('%')
  endif
endfunction " }}}

" PlaceAll(name, list) {{{
" Places a sign in the current buffer for each line in the list.
function! eclim#display#signs#PlaceAll (name, list)
  for line in a:list
    if line > 0
      exec "sign place " . line . " line=" . line . " name=" . a:name .
        \ " buffer=" . bufnr('%')
    endif
  endfor
endfunction " }}}

" Undefine(name) {{{
" Undefines a sign name.
function! eclim#display#signs#Undefine (name)
  exec "sign undefine " . a:name
endfunction " }}}

" Unplace(id) {{{
" Un-places a sign in the current buffer.
function! eclim#display#signs#Unplace (id)
  exec 'sign unplace ' . a:id . ' buffer=' . bufnr('%')
endfunction " }}}

" UnplaceAll(id) {{{
" Un-places all signs in the supplied list from the current buffer.
" The list may be a list of ids or a list of dictionaries as returned by
" GetExisting()
function! eclim#display#signs#UnplaceAll (list)
  for sign in a:list
    if type(sign) == 4
      call eclim#display#signs#Unplace(sign['id'])
    else
      call eclim#display#signs#Unplace(sign)
    endif
  endfor
endfunction " }}}

" Toggle(name, line) {{{
" Toggle a sign on the current line.
function! eclim#display#signs#Toggle (name, line)
  if a:line > 0
    let existing = eclim#display#signs#GetExisting(a:name)
    let exists = len(filter(existing, "v:val['line'] == a:line"))
    if exists
      call eclim#display#signs#Unplace(a:line)
    else
      call eclim#display#signs#Place(a:name, a:line)
    endif
  endif
endfunction " }}}

" CompareSigns(s1, s2) {{{
" Used by ViewSigns to sort list of sign dictionaries.
function! s:CompareSigns (s1, s2)
  if a:s1.line == a:s2.line
    return 0
  endif
  if a:s1.line > a:s2.line
    return 1
  endif
  return -1
endfunction " }}}

" ViewSigns(name) {{{
" Open a window to view all placed signs with the given name in the current
" buffer.
function! eclim#display#signs#ViewSigns (name)
  let filename = expand('%:p')
  let signs = eclim#display#signs#GetExisting(a:name)
  call sort(signs, 's:CompareSigns')
  let content = map(signs, "v:val.line . '|' . getline(v:val.line)")

  call eclim#util#TempWindow('[Sign List]', content)

  set ft=qf
  nnoremap <silent> <buffer> <cr> :call <SID>JumpToSign()<cr>

  " Store filename so that plugins can use it if necessary.
  let b:filename = filename
  augroup temp_window
    autocmd! BufUnload <buffer>
    call eclim#util#GoToBufferWindowRegister(filename)
  augroup END
endfunction " }}}

" JumpToSign () {{{
function! s:JumpToSign ()
  let winnr = bufwinnr(bufnr(b:filename))
  if winnr != -1
    let line = substitute(getline('.'), '^\(\d\+\)|.*', '\1', '')
    exec winnr . "winc w"
    echom 'line = ' . line
    call cursor(line, 1)
  endif
endfunction " }}}

" GetDefined() {{{
" Gets a list of defined sign names.
function! eclim#display#signs#GetDefined ()
  redir => list
  silent exec 'sign list'
  redir END

  let names = []
  for name in split(list, '\n')
    let name = substitute(name, 'sign\s\(.\{-}\)\s.*', '\1', '')
    call add(names, name)
  endfor
  return names
endfunction " }}}

" GetExisting(...) {{{
" Gets a list of existing signs for the current buffer.
" The list consists of dictionaries with the following keys:
"   id:   The sign id.
"   line: The line number.
"   name: The sign name (erorr, warning, etc.)
"
" Optionally a sign name may be supplied to only retrieve signs of that name.
function! eclim#display#signs#GetExisting (...)
  let bufnr = bufnr('%')

  redir => signs
  silent exec 'sign place buffer=' . bufnr
  redir END

  let existing = []
  for sign in split(signs, '\n')
    if sign =~ 'id='
      let id = substitute(sign, '.*\sid=\(.\{-}\)\(\s.*\|$\)', '\1', '')
      exec 'let line = ' . substitute(sign, '.*\sline=\(.\{-}\)\(\s.*\|$\)', '\1', '')
      let name = substitute(sign, '.*\sname=\(.\{-}\)\(\s.*\|$\)', '\1', '')
      call add(existing, {'id': id, 'line': line, 'name': name})
    endif
  endfor

  if len(a:000) > 0
    call filter(existing, "v:val['name'] == a:000[0]")
  endif

  return existing
endfunction " }}}

" Update() {{{
" Updates the signs for the current buffer.  This function will read both the
" location list and the quickfix list and place a sign for any entries for the
" current file.
" This function supports a severity level by examining the 'type' key of the
" dictionaries in the location or quickfix list.  It supports 'i' (info), 'w'
" (warning), and 'e' (error).
function! eclim#display#signs#Update ()
  if !has("signs")
    return
  endif

  let save_lazy = &lazyredraw
  set lazyredraw

  call eclim#display#signs#Define("error", ">>", g:EclimErrorHighlight)
  call eclim#display#signs#Define("placeholder", ">>", g:EclimInfoHighlight)

  let existing = eclim#display#signs#GetExisting()

  " set sign at line 1 to prevent sign column from collapsing (prevent screen
  " flash).
  if len(existing) > 0
    call eclim#display#signs#Place("placeholder", 1)
  endif

  " remove all existing signs
  for exists in existing
    call eclim#display#signs#Unplace(exists.id)
  endfor

  if g:EclimShowQuickfixSigns
    let list = filter(getqflist(),
      \ 'bufnr("%") == v:val.bufnr && v:val.type == ""')
    call map(list, 'v:val.lnum')
    call eclim#display#signs#Define("qf", "> ", g:EclimErrorHighlight)
    call eclim#display#signs#PlaceAll("qf", list)
  endif

  let list = filter(getloclist(0), 'bufnr("%") == v:val.bufnr')

  if g:EclimSignLevel >= 4
    let info = filter(getqflist() + list,
      \ 'bufnr("%") == v:val.bufnr && v:val.type == "i"')
    call map(info, 'v:val.lnum')
    call eclim#display#signs#Define("info", ">>", g:EclimInfoHighlight)
    call eclim#display#signs#PlaceAll("info", info)
  endif

  if g:EclimSignLevel >= 3
    let warnings = filter(copy(list), 'v:val.type == "w"')
    call map(warnings, 'v:val.lnum')
    call eclim#display#signs#Define("warning", ">>", g:EclimWarningHighlight)
    call eclim#display#signs#PlaceAll("warning", warnings)
  endif

  if g:EclimSignLevel >= 2
    let errors = filter(list, 'v:val.type == "e"')
    call map(errors, 'v:val.lnum')
    call eclim#display#signs#PlaceAll("error", errors)
  endif

  " remove placeholder sign
  let existing = eclim#display#signs#GetExisting('placeholder')
  for exists in existing
    call eclim#display#signs#Unplace(exists.id)
  endfor

  let &lazyredraw = save_lazy
endfunction " }}}

" define signs for manually added user marks.
call eclim#display#signs#Define(
  \ 'user', g:EclimUserSignText, g:EclimUserSignHighlight)

" vim:ft=vim:fdm=marker
