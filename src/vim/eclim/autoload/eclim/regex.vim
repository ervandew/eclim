" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Common functions for the various language regex testers.
"
" License:
"
" Copyright (C) 2005 - 2010  Eric Van Dewoestine
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
  if !exists("g:EclimRegexHi{0}")
    let g:EclimRegexHi{0} = 'Constant'
  endif

  if !exists("g:EclimRegexHi{1}")
    let g:EclimRegexHi{1} = 'MoreMsg'
  endif

  if !exists("g:EclimRegexGroupHi{0}")
    let g:EclimRegexGroupHi{0} = 'Statement'
  endif

  if !exists("g:EclimRegexGroupHi{1}")
    let g:EclimRegexGroupHi{1} = 'Todo'
  endif
" }}}

" Script Variables {{{
  " \%2l\%6c\_.*\%3l\%19c
  let s:pattern = '\%<startline>l\%<startcolumn>c\_.*\%<endline>l\%<endcolumn>c'

  let s:test_content = [
      \ 'te(st)',
      \ 'Some test content used to test',
      \ 'language specific regex against.',
    \ ]

  let s:regexfile = g:EclimTempDir . '/eclim_<lang>_regex.txt'
" }}}

" OpenTestWindow(lang) {{{
" Opens a buffer where the user can test regex expressions.
function! eclim#regex#OpenTestWindow(lang)
  let file = substitute(s:regexfile, '<lang>', a:lang, '')
  if bufwinnr(file) == -1
    let filename = expand('%:p')

    exec "botright 10split " . file
    let b:eclim_temp_window = 1

    setlocal ft=regex
    setlocal winfixheight
    setlocal bufhidden=delete
    setlocal nobuflisted
    setlocal nobackup
    setlocal nowritebackup

    command -buffer NextMatch :call s:NextMatch()
    command -buffer PrevMatch :call s:PrevMatch()

    augroup eclim_regex
      autocmd!
      exec "autocmd BufWritePost <buffer> call s:Evaluate('" . a:lang . "')"
      call eclim#util#GoToBufferWindowRegister(filename)
    augroup END

    if !exists('b:eclim_regex_type')
      let b:eclim_regex_type = 'file'
    endif
  endif

  nohlsearch
  write
endfunction " }}}

" Evaluate(lang) {{{
" Evaluates the test regex file.
function! s:Evaluate(lang)
  if line('$') == 1 && getline('$') == ''
    call s:AddTestContent()
  endif

  " forces reset of syntax
  set ft=regex

  let file = substitute(s:regexfile, '<lang>', a:lang, '')
  let b:results = []
  exec 'let out = eclim#' . a:lang . '#regex#Evaluate("' . file . '")'
  let results = split(out, '\n')
  if len(results) == 1 && results[0] == '0'
    return
  endif
  let b:results = results

  let matchIndex = 0
  for result in results
    let groups = split(result, '|')
    let groupIndex = 0
    if len(groups) > 1
      for group in groups[1:]
        let patterns = s:BuildPatterns(group)
        for pattern in patterns
          exec 'syntax match ' . g:EclimRegexGroupHi{groupIndex % 2} .
            \ ' /' . pattern . '/ '
        endfor
        let groupIndex += 1
      endfor
    endif

    let match = groups[0]
    let patterns = s:BuildPatterns(match)

    for pattern in patterns
      exec 'syntax match ' . g:EclimRegexHi{matchIndex % 2} .
        \ ' /' . pattern . '/ ' .
        \ 'contains=' . g:EclimRegexGroupHi0 . ',' . g:EclimRegexGroupHi1
    endfor

    let matchIndex += 1
  endfor
endfunction "}}}

" NextMatch() {{{
" Moves the cursor to the next match.
function! s:NextMatch()
  if exists("b:results")
    let curline = line('.')
    let curcolumn = col('.')
    for result in b:results
      let line = substitute(result, '\([0-9]\+\):.*', '\1', '')
      let column = substitute(result, '[0-9]\+:\([0-9]\+\).*', '\1', '')
      if column > len(getline(line))
        let column -= 1
      endif
      if (line > curline) || (line == curline && column > curcolumn)
        call cursor(line, column)
        return
      endif
    endfor
    if len(b:results) > 0
      let result = b:results[0]
      call eclim#util#EchoWarning("Search hit BOTTOM, continuing at TOP")
      let line = substitute(result, '\([0-9]\+\):.*', '\1', '')
      let column = substitute(result, '[0-9]\+:\([0-9]\+\).*', '\1', '')
      call cursor(line, column)
    endif
  endif
endfunction " }}}

" PrevMatch() {{{
" Moves the cursor to the previous match.
function! s:PrevMatch()
  if exists("b:results")
    let curline = line('.')
    let curcolumn = col('.')
    let index = len(b:results) - 1
    while index >= 0
      let result = b:results[index]
      let line = substitute(result, '\([0-9]\+\):.*', '\1', '')
      let column = substitute(result, '[0-9]\+:\([0-9]\+\).*', '\1', '')
      if column > len(getline(line))
        let column -= 1
      endif
      if (line < curline) || (line == curline && column < curcolumn)
        call cursor(line, column)
        return
      endif
      let index -= 1
    endwhile
    if len(b:results) > 0
      let result = b:results[len(b:results) - 1]
      call eclim#util#EchoWarning("Search hit TOP, continuing at BOTTOM")
      let line = substitute(result, '\([0-9]\+\):.*', '\1', '')
      let column = substitute(result, '[0-9]\+:\([0-9]\+\).*', '\1', '')
      call cursor(line, column)
    endif
  endif
endfunction " }}}

" AddTestContent() {{{
" Add the test content to the current regex test file.
function! s:AddTestContent()
  call append(1, s:test_content)
  1,1delete _

  "augroup eclim_regex
  "  autocmd!
  "augroup END

  "write

  "augroup eclim_regex
  "  autocmd BufWritePost <buffer> call s:Evaluate()
  "augroup END
endfunction " }}}

" BuildPatterns(match) {{{
" Builds the regex patterns for the supplied match.
function! s:BuildPatterns(match)
  " vim (as of 7 beta 2) doesn't seem to be handling multiline matches very
  " well (highlighting can get lost while scrolling), so here we break them up.
  let startLine = substitute(a:match, '\([0-9]\+\):.*', '\1', '') + 0
  let startColumn = substitute(a:match, '[0-9]\+:\([0-9]\+\).*', '\1', '') + 0
  let endLine = substitute(a:match, '.*-\([0-9]\+\):.*', '\1', '') + 0
  let endColumn = substitute(a:match, '.*-[0-9]\+:\([0-9]\+\)', '\1', '') + 0

  let patterns = []

  if startLine < endLine
    while startLine < endLine
      " ignore virtual sections.
      if startColumn <= len(getline(startLine))
        let pattern = s:pattern
        let pattern = substitute(pattern, '<startline>', startLine, '')
        let pattern = substitute(pattern, '<startcolumn>', startColumn, '')
        let pattern = substitute(pattern, '<endline>', startLine, '')
        let pattern = substitute
          \ (pattern, '<endcolumn>', len(getline(startLine)) + 1, '')
        call add(patterns, pattern)
      endif
      let startLine += 1
      let startColumn = 1
    endwhile

    let pattern = s:pattern
    let pattern = substitute(pattern, '<startline>', endLine, '')
    let pattern = substitute(pattern, '<startcolumn>', 1, '')
    let pattern = substitute(pattern, '<endline>', endLine, '')
    let pattern = substitute(pattern, '<endcolumn>', endColumn + 1, '')
    call add(patterns, pattern)
  else
    let pattern = s:pattern
    let pattern = substitute(pattern, '<startline>', startLine, '')
    let pattern = substitute(pattern, '<startcolumn>', startColumn, '')
    let pattern = substitute(pattern, '<endline>', endLine, '')
    let pattern = substitute(pattern, '<endcolumn>', endColumn + 1, '')
    call add(patterns, pattern)
  endif

  return patterns
endfunction" }}}

" vim:ft=vim:fdm=marker
