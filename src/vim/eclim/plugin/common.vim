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
  command -nargs=+ -complete=file Split :call eclim#common#OpenFiles('split', '<args>')
endif
if !exists(":Tabnew")
  command -nargs=+ -complete=file Tabnew :call eclim#common#OpenFiles('tabnew', '<args>')
endif

if !exists(":SplitRelative")
  command -nargs=+ -complete=customlist,eclim#common#CommandCompleteRelative
    \ SplitRelative :call eclim#common#OpenRelative('split', '<args>', 1)
endif
if !exists(":TabnewRelative")
  command -nargs=+ -complete=customlist,eclim#common#CommandCompleteRelative
    \ TabnewRelative :call eclim#common#OpenRelative('tabnew', '<args>', 1)
endif
if !exists(":EditRelative")
  command -nargs=1 -complete=customlist,eclim#common#CommandCompleteRelative
    \ EditRelative :call eclim#common#OpenRelative('edit', '<args>', 0)
endif
if !exists(":ReadRelative")
  command -nargs=1 -complete=customlist,eclim#common#CommandCompleteRelative
    \ ReadRelative :call eclim#common#OpenRelative('read', '<args>', 0)
endif
if !exists(":ArgsRelative")
  command -nargs=+ -complete=customlist,eclim#common#CommandCompleteRelative
    \ ArgsRelative :call eclim#common#OpenRelative('args', '<args>', 0)
endif
if !exists(":ArgAddRelative")
  command -nargs=+ -complete=customlist,eclim#common#CommandCompleteRelative
    \ ArgAddRelative :call eclim#common#OpenRelative('argadd', '<args>', 0)
endif

if !exists(":DiffLastSaved")
  command DiffLastSaved :call eclim#common#DiffLastSaved()
endif
if !exists(":SwapWords")
  command SwapWords :call eclim#common#SwapWords()
endif
if !exists(":SwapTypedArguments")
  command SwapTypedArguments :call eclim#common#SwapTypedArguments()
endif
if !exists(":LocateFileSplit")
  command -nargs=? LocateFileEdit :call eclim#common#LocateFile('edit', '<args>')
  command -nargs=? LocateFileSplit :call eclim#common#LocateFile('split', '<args>')
  command -nargs=? LocateFileTab :call eclim#common#LocateFile('tabnew', '<args>')
endif

if !exists(":Sign")
  command Sign :call eclim#signs#Toggle('user', line('.'))
endif
if !exists(":Signs")
  command Signs :call eclim#signs#ViewSigns('user')
endif
if !exists(":SignClearUser")
  command SignClearUser :call eclim#signs#UnplaceAll(eclim#signs#GetExisting('user'))
endif
if !exists(":SignClearAll")
  command SignClearAll :call eclim#signs#UnplaceAll(eclim#signs#GetExisting())
endif

if !exists(":QuickFixClear")
  command QuickFixClear :call setqflist([])
endif
if !exists(":LocationListClear")
  command LocationListClear :call setloclist(0, [])
endif
" }}}

" vim:ft=vim:fdm=marker
