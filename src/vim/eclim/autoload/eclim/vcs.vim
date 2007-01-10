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

  augroup vcs_annotate
    autocmd!
    autocmd CursorHold <buffer> call eclim#vcs#AnnotateInfo()
  augroup END
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
  augroup vcs_annotate
    autocmd!
  augroup END
endfunction " }}}

" AnnotateInfo() {{{
function! eclim#vcs#AnnotateInfo ()
  if exists('b:vcs_annotations')
    echo b:vcs_annotations[line('.') - 1]
  endif
endfunction " }}}

" Viewvc(file) {{{
" Convert file or directory to viewvc url and open in the browser.
function eclim#vcs#Viewvc (file)
  let root = eclim#project#GetProjectSetting("org.eclim.project.vcs.viewvc")
  if root == ''
    return
  elseif root !~ '/$'
    let root .= '/'
  endif

  let file = a:file
  let project_root = eclim#project#GetCurrentProjectRoot()
  if file == ''
    let file = substitute(expand('%:p'), project_root . '/', '', '')
  endif

  let url = root . file
  if !isdirectory(project_root . '/' . file)
    let url .= '?view=log'
  endif

  call eclim#web#OpenUrl(url)
endfunction " }}}

" vim:ft=vim:fdm=marker
