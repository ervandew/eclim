" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/common/vcs.html
"
" License:
"
" Copyright (c) 2005 - 2008
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

" ViewDiff {{{
function eclim#vcs#editor#ViewDiff ()
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
    :VcsDiff
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
