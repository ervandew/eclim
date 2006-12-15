" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Common functions for the various language regex testers.
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
      \ 'Some test content to used to test',
      \ 'language specific regex against.',
    \ ]

  let s:regexfile = g:EclimTempDir . '/eclim_<lang>_regex.txt'
" }}}

" OpenTestWindow(lang) {{{
" Opens a buffer where the user can test regex expressions.
function! eclim#regex#OpenTestWindow (lang)
  let file = substitute(s:regexfile, '<lang>', a:lang, '')
  if bufwinnr(file) == -1
    let filename = expand('%:p')

    silent! exec "botright 10split " . file
    setlocal winfixheight
    setlocal bufhidden=delete
    setlocal nobackup
    setlocal nowritebackup

    command -buffer NextMatch :call s:NextMatch()
    command -buffer PrevMatch :call s:PrevMatch()

    augroup eclim_regex
      autocmd!
      exec "autocmd BufWritePost <buffer> call s:Evaluate('" . a:lang . "')"
      call eclim#util#GoToBufferWindowRegister(filename)
    augroup END
  endif

  nohlsearch
  write
endfunction " }}}

" Evaluate(lang) {{{
" Evaluates the test regex file.
function! s:Evaluate (lang)
  let lines = getline('.', '$')
  if len(lines) == 1 && lines[0] == ''
    call s:AddTestContent()
  endif

  " reload the file to reset syntax highlighting.
  " downside is that we lose undo (will be resolved when edit is recognized as
  " an modification by vim and changes added to undo tree).
  update
  edit

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
    let match = groups[0]
    let patterns = s:BuildPatterns(match)

    for pattern in patterns
      exec 'syntax match ' . g:EclimRegexHi{matchIndex % 2} .
        \ ' /' . pattern . '/ ' .
        \ 'contains=' . g:EclimRegexGroupHi0 . ',' . g:EclimRegexGroupHi1
    endfor

    let matchIndex += 1

    let groupIndex = 0
    if len(groups) > 1
      let groups = groups[1:]
      for group in groups
        let patterns = s:BuildPatterns(group)
        for pattern in patterns
          exec 'syntax match ' . g:EclimRegexGroupHi{groupIndex % 2} .
            \ ' /' . pattern . '/ '
        endfor
        let groupIndex += 1
      endfor
    endif
  endfor
endfunction "}}}

" NextMatch() {{{
" Moves the cursor to the next match.
function! s:NextMatch ()
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
function! s:PrevMatch ()
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
function! s:AddTestContent ()
  call append(1, s:test_content)
  let saved = @"
  1,1delete
  let @" = saved

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
function! s:BuildPatterns (match)
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
