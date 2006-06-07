" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
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
" }}}

" Define(name, text, highlight) {{{
" Defines a new sign name or updates an existing one.
function! eclim#signs#Define (name, text, highlight)
  exec "sign define " . a:name . " text=" . a:text . " texthl=" . a:highlight
endfunction " }}}

" Place(name, line) {{{
" Places a sign in the current buffer.
function! eclim#signs#Place (name, line)
  if a:line > 0
    exec "sign place " . a:line . " line=" . a:line . " name=" . a:name .
      \ " buffer=" . bufnr('%')
  endif
endfunction " }}}

" PlaceAll(name, list) {{{
" Places a sign in the current buffer for each line in the list.
function! eclim#signs#PlaceAll (name, list)
  for line in a:list
    if line > 0
      exec "sign place " . line . " line=" . line . " name=" . a:name .
        \ " buffer=" . bufnr('%')
    endif
  endfor
endfunction " }}}

" Unplace(id) {{{
" Un-places a sign in the current buffer.
function! eclim#signs#Unplace (id)
  exec 'sign unplace ' . a:id . ' buffer=' . bufnr('%')
endfunction " }}}

" GetDefined() {{{
" Gets a list of defined sign names.
function! eclim#signs#GetDefined ()
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

" GetExisting() {{{
" Gets a list of existing signs for the current buffer.
" The list consists of dictionaries with the following keys:
"   id:   The sign id.
"   line: The line number.
"   name: The sign name (erorr, warning, etc.)
function! eclim#signs#GetExisting ()
  let bufnr = bufnr('%')

  redir => signs
  silent exec 'sign place buffer=' . bufnr
  redir END

  let existing = []
  for sign in split(signs, '\n')
    if sign =~ 'id='
      let id = substitute(sign, '.*\sid=\(.\{-}\)\(\s.*\|$\)', '\1', '')
      let line = substitute(sign, '.*\sline=\(.\{-}\)\(\s.*\|$\)', '\1', '')
      let name = substitute(sign, '.*\sname=\(.\{-}\)\(\s.*\|$\)', '\1', '')
      call add(existing, {'id': id, 'line': line, 'name': name})
    endif
  endfor
  return existing
endfunction " }}}

" Update() {{{
" Updates the signs for the current buffer.  This function will read both the
" location list and the quickfix list and place a sign for any entries for the
" current file.
" This function supports a severity level by examining the 'type' key of the
" dictionaries in the location or quickfix list.  It supports 'i' (info), 'w'
" (warning), and 'e' (error).
function! eclim#signs#Update ()
  if !has("signs")
    return
  endif

  let save_lazy = &lazyredraw
  set lazyredraw

  call eclim#signs#Define("error", ">>", g:EclimErrorHighlight)

  let existing = eclim#signs#GetExisting()

  " set sign at line 1 to prevent sign column from collapsing (prevent screen
  " flash).
  if len(existing) > 0
    call eclim#signs#Place("error", 1)
  endif

  " remove all existing signs
  for exists in existing
    if !(exists.name == "error" && exists.line == 1)
      call eclim#signs#Unplace(exists.id)
    endif
  endfor

  if g:EclimShowQuickfixSigns
    let list = filter(getqflist(),
      \ 'bufnr("%") == v:val.bufnr && v:val.type == ""')
    call map(list, 'v:val.lnum')
    call eclim#signs#Define("qf", "> ", g:EclimErrorHighlight)
    call eclim#signs#PlaceAll("qf", list)
  endif

  let list = filter(getloclist(0), 'bufnr("%") == v:val.bufnr')

  if g:EclimSignLevel >= 4
    let info = filter(getqflist() + list,
      \ 'bufnr("%") == v:val.bufnr && v:val.type == "i"')
    call map(info, 'v:val.lnum')
    call eclim#signs#Define("info", ">>", g:EclimInfoHighlight)
    call eclim#signs#PlaceAll("info", info)
  endif

  if g:EclimSignLevel >= 3
    let warnings = filter(copy(list), 'v:val.type == "w"')
    call map(warnings, 'v:val.lnum')
    call eclim#signs#Define("warning", ">>", g:EclimWarningHighlight)
    call eclim#signs#PlaceAll("warning", warnings)
  endif

  if g:EclimSignLevel >= 2
    let errors = filter(list, 'v:val.type == "e"')
    call map(errors, 'v:val.lnum')
    call eclim#signs#PlaceAll("error", errors)
  endif

  " remove placeholder sign if no real sign exists there
  let existing = eclim#signs#GetExisting()
  if len(existing) > 0 &&
    \ (!exists("errors") || len(errors) == 0 || errors[0] != 1) &&
    \ existing[0].name == 'error' &&
    \ existing[0].line == '1'
    call eclim#signs#Unplace(existing[0].id)
  endif

  let &lazyredraw = save_lazy
endfunction " }}}

" vim:ft=vim:fdm=marker
