" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Commands view / search eclim help files.
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
  let g:EclimHelpDir = g:EclimBaseDir . '/eclim/doc'
" }}}

" Help(tag) {{{
function! eclim#help#Help(tag, link)
  if !filereadable(substitute(g:EclimHelpDir, '\\\s', ' ', 'g') . '/tags')
    call eclim#util#Echo('indexing eclim help files...')
    silent! exec 'helptags ' . g:EclimHelpDir
    let paths = split(glob(g:EclimHelpDir . '/**/*'), '\n')
    call filter(paths, 'isdirectory(v:val)')
    for path in paths
      silent! exec 'helptags ' . path
    endfor
    call eclim#util#Echo('eclim help files indexed')
  endif

  let savetags = &tags
  exec 'set tags=' . escape(escape(g:EclimHelpDir, ' '), ' ') . '/**/tags'
  try
    let tag = a:tag
    if tag == '' && !a:link
      let tag = 'vim-index'
    elseif tag ==''
      let line = getline('.')
      let tag = substitute(
        \ line, '.*|\(\S\{-}\%' . col('.') . 'c\S\{-}\)|.*', '\1', '')
      if tag == line
        return
      endif
    endif

    call s:HelpWindow()
    exec 'tag ' . tag
    let w:eclim_help = 1

    " needed to ensure taglist is updated if open
    doautocmd BufEnter
  catch /^Vim\%((\a\+)\)\=:E426/
    if !exists('w:eclim_help')
      close
    endif
    call eclim#util#EchoError('Sorry no eclim help for ' . tag)
  finally
    let &tags = savetags
  endtry
endfunction " }}}

" HelpGrep() {{{
function! eclim#help#HelpGrep(args)
  exec 'vimgrep ' a:args . ' ' . g:EclimHelpDir . '/**/*.txt'
endfunction " }}}

" s:HelpWindow() {{{
function s:HelpWindow()
  let max = winnr('$')
  let index = 1
  while index <= max
    if getwinvar(index, 'eclim_help')
      exec index . 'winc w'
      return
    endif
    let index += 1
  endwhile

  botright new
endfunction " }}}

" BufferHelp(lines, orientation, size) {{{
" Function to display a help window for the current buffer.
function! eclim#help#BufferHelp(lines, orientation, size)
  let orig_bufnr = bufnr('%')
  let name = expand('%')
  if name =~ '^\W.*\W$'
    let name = name[:-2] . ' Help' . name[len(name) - 1]
  else
    let name .= ' Help'
  endif

  let bname = eclim#util#EscapeBufferName(name)

  let orient = a:orientation == 'vertical' ? 'v' : ''
  if bufwinnr(bname) != -1
    exec 'bd ' . bufnr(bname)
    return
  endif

  silent! noautocmd exec a:size . orient . "new " . escape(name, ' ')
  if a:orientation == 'vertical'
    setlocal winfixwidth
  else
    setlocal winfixheight
  endif
  setlocal nowrap
  setlocal noswapfile nobuflisted nonumber
  setlocal buftype=nofile bufhidden=delete
  nnoremap <buffer> <silent> ? :bd<cr>
  nnoremap <buffer> <silent> q :bd<cr>

  setlocal modifiable noreadonly
  silent 1,$delete _
  call append(1, a:lines)
  retab
  silent 1,1delete _

  if len(a:000) == 0 || a:000[0]
    setlocal nomodified nomodifiable readonly
  endif

  let help_bufnr = bufnr('%')
  augroup eclim_help_buffer
    autocmd! BufWinLeave <buffer>
    autocmd BufWinLeave <buffer> nested autocmd! eclim_help_buffer * <buffer>
    exec 'autocmd BufWinLeave <buffer> nested ' .
      \ 'autocmd! eclim_help_buffer * <buffer=' . orig_bufnr . '>'
    exec 'autocmd! BufWinLeave <buffer=' . orig_bufnr . '>'
    exec 'autocmd BufWinLeave <buffer=' . orig_bufnr . '> nested bd ' . help_bufnr
  augroup END

  return help_bufnr
endfunction " }}}

" CommandComplete(argLead, cmdLine, cursorPos) {{{
function! eclim#help#CommandCompleteTag(argLead, cmdLine, cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')

  let savetags = &tags
  exec 'set tags=' . escape(escape(g:EclimHelpDir, ' '), ' ') . '/**/tags'
  try
    let results = taglist(argLead . '.*')
    call map(results, "v:val['name']")
    return results
  finally
    let &tags = savetags
  endtry
endfunction " }}}

" vim:ft=vim:fdm=marker
