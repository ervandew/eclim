" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2005 - 2017  Eric Van Dewoestine
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

" Script Variables {{{
let s:command_read = '-command archive_read -f "<file>"'
" }}}

function! eclim#common#util#DiffLastSaved() " {{{
  " Diff a modified file with the last saved version.
  if &modified
    let winnum = winnr()
    let filetype=&ft
    vertical belowright new | r #
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

function! eclim#common#util#SwapWords() " {{{
  " Initially based on http://www.vim.org/tips/tip.php?tip_id=329

  " save the last search pattern
  let save_search = @/

  normal! "_yiw
  let pos = getpos('.')
  keepjumps s/\(\%#\w\+\)\(\_W\+\)\(\w\+\)/\3\2\1/
  call setpos('.', pos)

  " restore the last search pattern
  let @/ = save_search

  silent! call repeat#set(":call eclim#common#util#SwapWords()\<cr>", v:count)
endfunction " }}}

function! eclim#common#util#SwapAround(char) " {{{
  if len(a:char) != 1
    call eclim#util#EchoError('Arg must be a single character.')
    return
  endif

  let pos = getpos('.')
  let save_search = @/
  try
    let lnum = line('.')
    let line = getline('.')
    let start_col = 0
    if line[col('.') - 1] =~ '[(\[{]'
      let start_col = col('.')
      normal! %
    endif
    let col = col('.')
    exec 'normal! f' . a:char
    if col('.') == col
      call eclim#util#EchoError('Char not found on this line.')
      return
    endif

    let delim_col = col('.')

    let [_, end_col] = searchpos('\S', 'b', lnum)
    if !start_col
      if line[col('.') - 1] =~ '[)\]}]'
        normal! %
        let start_col = col('.')
      else
        let [_, start_col] = searchpos('[(\[{' . a:char . ']', 'b', lnum)
        if start_col == end_col
          call eclim#util#EchoError('Unable to determine the start of the first block.')
          return
        endif
        let start_col += 1
      endif
    endif

    let first = [start_col, end_col]

    call cursor(0, delim_col)
    let [_, start_col] = searchpos('\S', '', lnum)
    if start_col == delim_col
      call eclim#util#EchoError('Could not find item to swap with.')
      return
    endif
    if line[col('.') - 1] =~ '[(\[{]'
      normal! %
      let end_col = col('.')
    else
      let [_, end_col] = searchpos('[)\]}' . a:char . ']', '', lnum)
      if start_col == end_col
        call eclim#util#EchoError('Unable to determine the end of the second block.')
        return
      endif
      let end_col -= 1
    endif

    let second = [start_col, end_col]

    let first_part = strpart(line, first[0] - 1, first[1] - first[0] + 1)
    let second_part = strpart(line, second[0] - 1, second[1] - second[0] + 1)

    " replace second with first
    let prefix = strpart(line, 0, second[0] - 1)
    let suffix = strpart(line, second[1])
    let line = prefix . first_part . suffix

    " replace first with second
    let prefix = strpart(line, 0, first[0] - 1)
    let suffix = strpart(line, first[1])
    let line = prefix . second_part . suffix

    call setline('.', line)
    silent! call repeat#set(
      \ ":call eclim#common#util#SwapAround(" . string(a:char) . ")\<cr>", v:count)
  finally
    call setpos('.', pos)
    let @/ = save_search
  endtry
endfunction " }}}

function! eclim#common#util#Tcd(dir) " {{{
  " Like vim's :lcd, but tab local instead of window local.
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

function! s:ApplyTcd(honor_lcd) " {{{
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

function! eclim#common#util#ReadFile() " {{{
  " Reads the contents of an archived file.
  let archive = substitute(expand('%'), '\', '/', 'g')
  let command = substitute(s:command_read, '<file>', archive, '')

  let file = eclim#Execute(command)

  if string(file) != '0'
    let project = exists('b:eclim_project') ? b:eclim_project : ''
    let bufnum = bufnr('%')
    silent exec "keepalt keepjumps edit! " . escape(file, ' ')
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
