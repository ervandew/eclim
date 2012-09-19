" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Various functions that are useful in and out of eclim.
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
let s:command_read = '-command archive_read -f "<file>"'
" }}}

" DiffLastSaved() {{{
" Diff a modified file with the last saved version.
function! eclim#common#util#DiffLastSaved()
  if &modified
    let winnum = winnr()
    let filetype=&ft
    vertical botright new | r #
    1,1delete _

    diffthis
    setlocal buftype=nofile
    setlocal bufhidden=wipe
    setlocal nobuflisted
    setlocal noswapfile
    setlocal readonly
    exec "setlocal ft=" . filetype
    let diffnum = winnr()

    augroup diff_saved
      autocmd! BufUnload <buffer>
      autocmd BufUnload <buffer> :diffoff!
    augroup END

    exec winnum . "winc w"
    diffthis

    " for some reason, these settings only take hold if set here.
    call setwinvar(diffnum, "&foldmethod", "diff")
    call setwinvar(diffnum, "&foldlevel", "0")
  else
    echo "No changes"
  endif
endfunction " }}}

" SwapTypedArguments() {{{
" Swaps typed method declaration arguments.
function! eclim#common#util#SwapTypedArguments()
  " FIXME: add validation to see if user is executing on a valid position.
  normal! w
  SwapWords
  normal! b
  SwapWords
  normal! www
  SwapWords
  normal! bb
  SwapWords
  normal! b
endfunction " }}}

" SwapWords() {{{
" Initially based on http://www.vim.org/tips/tip.php?tip_id=329
function! eclim#common#util#SwapWords()
  " save the last search pattern
  let save_search = @/

  normal! "_yiw
  s/\(\%#\w\+\)\(\_W\+\)\(\w\+\)/\3\2\1/
  exec "normal! \<C-O>"

  " restore the last search pattern
  let @/ = save_search

  silent! call repeat#set(":call eclim#common#util#SwapWords()\<cr>", v:count)
endfunction " }}}

" Tcd(dir) {{{
" Like vim's :lcd, but tab local instead of window local.
function! eclim#common#util#Tcd(dir)
  let t:cwd = fnamemodify(a:dir, ':p')

  " initialize the tab cwd for all other tabs if not already set
  let curtab = tabpagenr()
  try
    let index = 1
    while index <= tabpagenr('$')
      if index != curtab
        exec 'tabn ' . index
        if !exists('t:cwd')
          let t:cwd = getcwd()
          " try to find a window without a localdir if necessary
          if haslocaldir()
            let curwin = winnr()
            let windex = 1
            while windex <= winnr('$')
              if windex != curwin
                exec windex . 'winc w'
                if !haslocaldir()
                  let t:cwd = getcwd()
                  break
                endif
              endif
              let windex += 1
            endwhile
            exec curwin . 'winc w'
          endif
        endif
      endif
      let index += 1
    endwhile
  finally
    exec 'tabn ' . curtab
  endtry

  call s:ApplyTcd(0)

  augroup tcd
    autocmd!
    autocmd TabEnter * call <SID>ApplyTcd(1)
  augroup END
endfunction " }}}

" s:ApplyTcd(honor_lcd) {{{
function! s:ApplyTcd(honor_lcd)
  if !exists('t:cwd')
    return
  endif

  if a:honor_lcd && haslocaldir()
    let lcwd = getcwd()
    exec 'cd ' . escape(t:cwd, ' ')
    exec 'lcd ' . escape(lcwd, ' ')
  else
    exec 'cd ' . escape(t:cwd, ' ')
  endif
endfunction " }}}

" ReadFile() {{{
" Reads the contents of an archived file.
function! eclim#common#util#ReadFile()
  let archive = substitute(expand('%'), '\', '/', 'g')
  let command = substitute(s:command_read, '<file>', archive, '')

  let file = eclim#ExecuteEclim(command)

  if string(file) != '0'
    let project = exists('b:eclim_project') ? b:eclim_project : ''
    let bufnum = bufnr('%')
    if has('win32unix')
      let file = eclim#cygwin#CygwinPath(file)
    endif
    silent exec "keepjumps edit! " . escape(file, ' ')
    if project != ''
      let b:eclim_project = project
      let b:eclim_file = archive
    endif

    exec 'bdelete ' . bufnum

    " alternate solution, that keeps the archive url as the buffer's filename,
    " but prevents taglist from being able to parse tags.
    "setlocal noreadonly
    "setlocal modifiable
    "silent! exec "read " . file
    "1,1delete _

    silent exec "doautocmd BufReadPre " . file
    silent exec "doautocmd BufReadPost " . file

    setlocal readonly
    setlocal nomodifiable
    setlocal noswapfile
    " causes taglist.vim errors (fold then delete fails)
    "setlocal bufhidden=delete
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
