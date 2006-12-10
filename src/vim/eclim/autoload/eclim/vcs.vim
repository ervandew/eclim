" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Functions for working with version control systems.
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

" Annotate() {{{
function! eclim#vcs#Annotate ()
  let file = expand('%:p')
  let dir = expand('%:p:h')
  if isdirectory(dir . '/CVS')
    let result = system('cvs annotate "' . file . '"')
  elseif isdirectory(dir . '/.svn')
    let result = system('svn blame "' . file . '"')
  else
    call eclim#util#EchoError('Current file is not under cvs or svn version control.')
    return
  endif

  if v:shell_error
    call eclim#util#EchoError(result)
    return
  endif

  let annotations = split(result, '\n')
  call map(annotations,
      \ "substitute(v:val, '^\\s*\\(.\\{-}\\s\\+.\\{-}\\)\\s\\+.*', '\\1', '')")
  let defined = eclim#signs#GetDefined()
  let index = 1
  for annotation in annotations
    let user = substitute(annotation, '^.\{-}\s\+\(.*\)', '\1', '')
    let user_abbrv = user[:1]
    if index(defined, user) == -1
      call eclim#signs#Define(user, user_abbrv, g:EclimInfoHighlight)
      call add(defined, user_abbrv)
    endif
    call eclim#signs#Place(user, index)
    let index += 1
  endfor
  let b:vcs_annotations = annotations
  nmap <silent> j j:call eclim#vcs#AnnotateInfo()<cr>
  nmap <silent> k k:call eclim#vcs#AnnotateInfo()<cr>
endfunction " }}}

" AnnotateOff() {{{
function! eclim#vcs#AnnotateOff ()
  if exists('b:vcs_annotations')
    let defined = eclim#signs#GetDefined()
    for annotation in b:vcs_annotations
      let user = substitute(annotation, '^.\{-}\s\+\(.*\)', '\1', '')
      if index(defined, user) != -1
        let signs = eclim#signs#GetExisting(user)
        for sign in signs
          call eclim#signs#Unplace(sign.id)
        endfor
        call eclim#signs#Undefine(user)
        call remove(defined, index(defined, user))
      endif
    endfor
    unlet b:vcs_annotations
  endif
  nunmap j
  nunmap k
endfunction " }}}

" AnnotateInfo() {{{
function! eclim#vcs#AnnotateInfo ()
  if exists('b:vcs_annotations')
    echo b:vcs_annotations[line('.') - 1]
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
