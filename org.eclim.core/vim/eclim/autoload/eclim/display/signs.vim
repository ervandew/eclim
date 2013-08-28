" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Functions for working with vim signs.
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
if !exists("g:EclimShowQuickfixSigns")
  let g:EclimShowQuickfixSigns = 1
endif

if !exists("g:EclimShowLoclistSigns")
  let g:EclimShowLoclistSigns = 1
endif

if !exists("g:EclimQuickfixSignText")
  let g:EclimQuickfixSignText = '> '
endif

if !exists("g:EclimLoclistSignText")
  let g:EclimLoclistSignText = '>>'
endif

if !exists("g:EclimUserSignText")
  let g:EclimUserSignText = '#'
endif

if !exists("g:EclimUserSignHighlight")
  let g:EclimUserSignHighlight = g:EclimInfoHighlight
endif
" }}}

function! eclim#display#signs#Define(name, text, highlight) " {{{
  " Defines a new sign name or updates an existing one.
  exec "sign define " . a:name . " text=" . a:text . " texthl=" . a:highlight
endfunction " }}}

function! eclim#display#signs#Place(name, line) " {{{
  " Places a sign in the current buffer.
  if a:line > 0
    let lastline = line('$')
    let line = a:line <= lastline ? a:line : lastline
    let id = a:name == 'placeholder' ? 999999 : line
    exec "sign place " . id . " line=" . line . " name=" . a:name .
      \ " buffer=" . bufnr('%')
  endif
endfunction " }}}

function! eclim#display#signs#PlaceAll(name, list) " {{{
  " Places a sign in the current buffer for each line in the list.

  let lastline = line('$')
  for line in a:list
    if line > 0
      let line = line <= lastline ? line : lastline
      exec "sign place " . line . " line=" . line . " name=" . a:name .
        \ " buffer=" . bufnr('%')
    endif
  endfor
endfunction " }}}

function! eclim#display#signs#Undefine(name) " {{{
  " Undefines a sign name.
  exec "sign undefine " . a:name
endfunction " }}}

function! eclim#display#signs#Unplace(id) " {{{
  " Un-places a sign in the current buffer.
  exec 'sign unplace ' . a:id . ' buffer=' . bufnr('%')
endfunction " }}}

function! eclim#display#signs#UnplaceAll(list) " {{{
  " Un-places all signs in the supplied list from the current buffer.
  " The list may be a list of ids or a list of dictionaries as returned by
  " GetExisting()

  for sign in a:list
    if type(sign) == g:DICT_TYPE
      call eclim#display#signs#Unplace(sign['id'])
    else
      call eclim#display#signs#Unplace(sign)
    endif
  endfor
endfunction " }}}

function! eclim#display#signs#Toggle(name, line) " {{{
  " Toggle a sign on the current line.
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

function! s:CompareSigns(s1, s2) " {{{
  " Used by ViewSigns to sort list of sign dictionaries.

  if a:s1.line == a:s2.line
    return 0
  endif
  if a:s1.line > a:s2.line
    return 1
  endif
  return -1
endfunction " }}}

function! eclim#display#signs#ViewSigns(name) " {{{
  " Open a window to view all placed signs with the given name in the current
  " buffer.

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
    autocmd! BufWinLeave <buffer>
    call eclim#util#GoToBufferWindowRegister(filename)
  augroup END
endfunction " }}}

function! s:JumpToSign() " {{{
  let winnr = bufwinnr(bufnr('^' . b:filename))
  if winnr != -1
    let line = substitute(getline('.'), '^\(\d\+\)|.*', '\1', '')
    exec winnr . "winc w"
    call cursor(line, 1)
  endif
endfunction " }}}

function! eclim#display#signs#GetDefined() " {{{
  " Gets a list of defined sign names.

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

function! eclim#display#signs#GetExisting(...) " {{{
  " Gets a list of existing signs for the current buffer.
  " The list consists of dictionaries with the following keys:
  "   id:   The sign id.
  "   line: The line number.
  "   name: The sign name (erorr, warning, etc.)
  "
  " Optionally a sign name may be supplied to only retrieve signs of that name.

  let bufnr = bufnr('%')

  redir => signs
  silent exec 'sign place buffer=' . bufnr
  redir END

  let existing = []
  for line in split(signs, '\n')
    if line =~ '.\{-}=.\{-}=' " only two equals to account for swedish output
      call add(existing, s:ParseSign(line))
    endif
  endfor

  if len(a:000) > 0
    call filter(existing, "v:val['name'] == a:000[0]")
  endif

  return existing
endfunction " }}}

function! eclim#display#signs#HasExisting(...) " {{{
  " Determines if there are any existing signs.
  " Optionally a sign name may be supplied to only test for signs of that name.

  let bufnr = bufnr('%')

  redir => results
  silent exec 'sign place buffer=' . bufnr
  redir END

  for line in split(results, '\n')
    if line =~ '.\{-}=.\{-}=' " only two equals to account for swedish output
      if len(a:000) == 0
        return 1
      endif
      let sign = s:ParseSign(line)
      if sign.name == a:000[0]
        return 1
      endif
    endif
  endfor

  return 0
endfunction " }}}

function! s:ParseSign(raw) " {{{
  let attrs = split(a:raw)

  exec 'let line = ' . split(attrs[0], '=')[1]

  let id = split(attrs[1], '=')[1]
  " hack for the italian localization
  if id =~ ',$'
    let id = id[:-2]
  endif

  " hack for the swedish localization
  if attrs[2] =~ '^namn'
    let name = substitute(attrs[2], 'namn', '', '')
  else
    let name = split(attrs[2], '=')[1]
  endif

  return {'id': id, 'line': line, 'name': name}
endfunction " }}}

function! eclim#display#signs#Update() " {{{
  " Updates the signs for the current buffer.  This function will read both the
  " location list and the quickfix list and place a sign for any entries for the
  " current file.
  " This function supports a severity level by examining the 'type' key of the
  " dictionaries in the location or quickfix list.  It supports 'i' (info), 'w'
  " (warning), and 'e' (error).

  if !has('signs') || !g:EclimSignLevel || &ft == 'qf'
    return
  endif

  let save_lazy = &lazyredraw
  set lazyredraw

  let placeholder = eclim#display#signs#SetPlaceholder()

  " remove all existing signs
  let existing = eclim#display#signs#GetExisting()
  for exists in existing
    if exists.name =~ '^\(qf_\)\?\(error\|info\|warning\)$'
      call eclim#display#signs#Unplace(exists.id)
    endif
  endfor

  let qflist = filter(g:EclimShowQuickfixSigns ? getqflist() : [],
    \ 'bufnr("%") == v:val.bufnr')
  let show_loclist = g:EclimShowLoclistSigns && exists('b:eclim_loclist')
  let loclist = filter(show_loclist ? getloclist(0) : [],
    \ 'bufnr("%") == v:val.bufnr')

  for [list, marker, prefix] in [
      \ [qflist, g:EclimQuickfixSignText, 'qf_'],
      \ [loclist, g:EclimLoclistSignText, '']]
    if g:EclimSignLevel >= 4
      let info = filter(copy(list), 'v:val.type == "" || tolower(v:val.type) == "i"')
      call eclim#display#signs#Define(prefix . 'info', marker, g:EclimInfoHighlight)
      call eclim#display#signs#PlaceAll(prefix . 'info', map(info, 'v:val.lnum'))
    endif

    if g:EclimSignLevel >= 3
      let warnings = filter(copy(list), 'tolower(v:val.type) == "w"')
      call eclim#display#signs#Define(prefix . 'warning', marker, g:EclimWarningHighlight)
      call eclim#display#signs#PlaceAll(prefix . 'warning', map(warnings, 'v:val.lnum'))
    endif

    if g:EclimSignLevel >= 2
      let errors = filter(copy(list), 'tolower(v:val.type) == "e"')
      call eclim#display#signs#Define(prefix . 'error', marker, g:EclimErrorHighlight)
      call eclim#display#signs#PlaceAll(prefix . 'error', map(errors, 'v:val.lnum'))
    endif
  endfor

  if placeholder
    call eclim#display#signs#RemovePlaceholder()
  endif

  let &lazyredraw = save_lazy
endfunction " }}}

function! eclim#display#signs#QuickFixCmdPost() " {{{
  " Force 'make' results to be of type error if no type set.
  if expand('<amatch>') == 'make'
    let newentries = []
    for entry in getqflist()
      if entry['type'] == ''
        let entry['type'] = 'e'
      endif
      call add(newentries, entry)
    endfor
    call setqflist(newentries, 'r')
  endif
  call eclim#display#signs#Update()
  redraw!
endfunction " }}}

function! eclim#display#signs#SetPlaceholder(...) " {{{
  " Set sign at line 1 to prevent sign column from collapsing, and subsiquent
  " screen redraw.
  " Optional args:
  "   only_if_necessary: if 1, only set a placeholder if there are no existing
  "   signs

  if !has('signs') || !g:EclimSignLevel
    return
  endif

  if len(a:000) > 0 && a:000[0]
    let existing = eclim#display#signs#GetExisting()
    if !len(existing)
      return
    endif
  endif

  call eclim#display#signs#Define('placeholder', '_ ', g:EclimInfoHighlight)
  let existing = eclim#display#signs#GetExisting('placeholder')
  if len(existing) == 0 && eclim#display#signs#HasExisting()
    call eclim#display#signs#Place('placeholder', 1)
    return 1
  endif
  return
endfunction " }}}

function! eclim#display#signs#RemovePlaceholder() " {{{
  if !has('signs') || !g:EclimSignLevel
    return
  endif

  let existing = eclim#display#signs#GetExisting('placeholder')
  for exists in existing
    call eclim#display#signs#Unplace(exists.id)
  endfor
endfunction " }}}

" define signs for manually added user marks.
if has('signs')
  call eclim#display#signs#Define(
    \ 'user', g:EclimUserSignText, g:EclimUserSignHighlight)
endif

" vim:ft=vim:fdm=marker
