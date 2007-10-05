" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Various commands that are useful in and out of eclim.
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

" Command Declarations {{{
if !exists(":Split")
  command -nargs=+ -complete=file Split :call eclim#common#util#OpenFiles('split', '<args>')
endif
if !exists(":Tabnew")
  command -nargs=+ -complete=file Tabnew :call eclim#common#util#OpenFiles('tabnew', '<args>')
endif

if !exists(":SplitRelative")
  command -nargs=+ -complete=customlist,eclim#common#util#CommandCompleteRelative
    \ SplitRelative :call eclim#common#util#OpenRelative('split', '<args>', 1)
endif
if !exists(":TabnewRelative")
  command -nargs=+ -complete=customlist,eclim#common#util#CommandCompleteRelative
    \ TabnewRelative :call eclim#common#util#OpenRelative('tabnew', '<args>', 1)
endif
if !exists(":EditRelative")
  command -nargs=1 -complete=customlist,eclim#common#util#CommandCompleteRelative
    \ EditRelative :call eclim#common#util#OpenRelative('edit', '<args>', 0)
endif
if !exists(":ReadRelative")
  command -nargs=1 -complete=customlist,eclim#common#util#CommandCompleteRelative
    \ ReadRelative :call eclim#common#util#OpenRelative('read', '<args>', 0)
endif
if !exists(":ArgsRelative")
  command -nargs=+ -complete=customlist,eclim#common#util#CommandCompleteRelative
    \ ArgsRelative :call eclim#common#util#OpenRelative('args', '<args>', 0)
endif
if !exists(":ArgAddRelative")
  command -nargs=+ -complete=customlist,eclim#common#util#CommandCompleteRelative
    \ ArgAddRelative :call eclim#common#util#OpenRelative('argadd', '<args>', 0)
endif

if !exists(":Buffers")
  command Buffers :call eclim#common#buffers#Buffers()
endif

if !exists(":DiffLastSaved")
  command DiffLastSaved :call eclim#common#util#DiffLastSaved()
endif
if !exists(":SwapWords")
  command SwapWords :call eclim#common#util#SwapWords()
endif
if !exists(":SwapTypedArguments")
  command SwapTypedArguments :call eclim#common#util#SwapTypedArguments()
endif
if !exists(":LocateFileSplit")
  command -nargs=? LocateFileEdit :call eclim#common#util#LocateFile('edit', '<args>')
  command -nargs=? LocateFileSplit :call eclim#common#util#LocateFile('split', '<args>')
  command -nargs=? LocateFileTab :call eclim#common#util#LocateFile('tabnew', '<args>')
endif

if has('signs')
  if !exists(":Sign")
    command Sign :call eclim#display#signs#Toggle('user', line('.'))
  endif
  if !exists(":Signs")
    command Signs :call eclim#display#signs#ViewSigns('user')
  endif
  if !exists(":SignClearUser")
    command SignClearUser :call eclim#display#signs#UnplaceAll(
      \ eclim#display#signs#GetExisting('user'))
  endif
  if !exists(":SignClearAll")
    command SignClearAll :call eclim#display#signs#UnplaceAll(
      \ eclim#display#signs#GetExisting())
  endif
endif

if !exists(":QuickFixClear")
  command QuickFixClear :call setqflist([]) | call eclim#display#signs#Update()
endif
if !exists(":LocationListClear")
  command LocationListClear :call setloclist(0, []) | call eclim#display#signs#Update()
endif

if !exists(":VimgrepRelative")
  command -nargs=+ -complete=customlist,eclim#common#util#CommandCompleteRelative
    \ VimgrepRelative :call eclim#common#util#GrepRelative('vimgrep', <q-args>)
endif
if !exists(":VimgrepAddRelative")
  command -nargs=+ -complete=customlist,eclim#common#util#CommandCompleteRelative
    \ VimgrepAddRelative :call eclim#common#util#GrepRelative('vimgrepadd', <q-args>)
endif
if !exists(":LvimgrepRelative")
  command -nargs=+ -complete=customlist,eclim#common#util#CommandCompleteRelative
    \ LvimgrepRelative :call eclim#common#util#GrepRelative('lvimgrep', <q-args>)
endif
if !exists(":LvimgrepAddRelative")
  command -nargs=+ -complete=customlist,eclim#common#util#CommandCompleteRelative
    \ LvimgrepAddRelative :call eclim#common#util#GrepRelative('lvimgrepadd', <q-args>)
endif

if !exists(":CdRelative")
  command -nargs=1 -complete=customlist,eclim#common#util#CommandCompleteRelativeDirs
    \ CdRelative :exec 'cd ' . expand('%:p:h') . '/<args>'
endif

if !exists(":LcdRelative")
  command -nargs=1 -complete=customlist,eclim#common#util#CommandCompleteRelativeDirs
    \ LcdRelative :exec 'lcd ' . expand('%:p:h') . '/<args>'
endif
" }}}

" vim:ft=vim:fdm=marker
