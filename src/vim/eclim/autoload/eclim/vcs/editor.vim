" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/common/vcs.html
"
" License:
"
" Copyright (C) 2005 - 2009  Eric Van Dewoestine
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

" ViewDiff {{{
function eclim#vcs#editor#ViewDiff()
  let GetEditorFile = eclim#vcs#util#GetVcsFunction('GetEditorFile')
  if type(GetEditorFile) != 2
    return
  endif

  let file = GetEditorFile()
  if file != ''
    let winend = winnr('$')
    let winnum = 1
    while winnum <= winend
      let bufnr = winbufnr(winnum)
      if getbufvar(bufnr, 'vcs_editor_diff') != '' ||
         \ getbufvar(bufnr, 'vcs_diff_temp') != ''
        exec bufnr . 'bd'
        continue
      endif
      let winnum += 1
    endwhile

    exec 'belowright split ' . escape(file, ' ')
    let b:vcs_editor_diff = 1

    " if file is versioned, execute VcsDiff
    let path = substitute(expand('%:p'), '\', '/', 'g')
    let revision = eclim#vcs#util#GetRevision(path)
    if revision != ''
      VcsDiff
    endif
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
