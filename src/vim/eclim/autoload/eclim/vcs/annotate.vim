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
function! eclim#vcs#annotate#Annotate ()
  let dir = expand('%:p:h')
  if isdirectory(dir . '/CVS')
    let file = expand('%:p')
    let annotations = eclim#vcs#annotate#GetCvsAnnotations(file, '')
  elseif isdirectory(dir . '/.svn')
    let file = expand('%:p:t')
    let cwd = getcwd()
    exec 'lcd ' . dir
    try
      let annotations = eclim#vcs#annotate#GetSvnAnnotations(file, '')
    finally
      exec 'lcd ' . cwd
    endtry
  else
    call eclim#util#EchoError('Current file is not under cvs or svn version control.')
    return
  endif

  call eclim#vcs#annotate#ApplyAnnotations(annotations)
endfunction " }}}

" AnnotateOff() {{{
function! eclim#vcs#annotate#AnnotateOff ()
  if exists('b:vcs_annotations')
    let defined = eclim#display#signs#GetDefined()
    for annotation in b:vcs_annotations
      let user = substitute(annotation, '^.*)\s\+\(.*\)', '\1', '')
      if index(defined, user) != -1
        let signs = eclim#display#signs#GetExisting(user)
        for sign in signs
          call eclim#display#signs#Unplace(sign.id)
        endfor
        call eclim#display#signs#Undefine(user)
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
function! eclim#vcs#annotate#AnnotateInfo ()
  if exists('b:vcs_annotations') && len(b:vcs_annotations) >= line('.')
    call eclim#util#WideMessage('echo', b:vcs_annotations[line('.') - 1])
  endif
endfunction " }}}

" ApplyAnnotations(annotations) {{{
function! eclim#vcs#annotate#ApplyAnnotations (annotations)
  let defined = eclim#display#signs#GetDefined()
  let index = 1
  for annotation in a:annotations
    let user = substitute(annotation, '^.\{-})\s\+\(.*\)', '\1', '')
    let user_abbrv = user[:1]
    if index(defined, user) == -1
      call eclim#display#signs#Define(user, user_abbrv, g:EclimInfoHighlight)
      call add(defined, user_abbrv)
    endif
    call eclim#display#signs#Place(user, index)
    let index += 1
  endfor
  let b:vcs_annotations = a:annotations

  augroup vcs_annotate
    autocmd!
    autocmd CursorHold <buffer> call eclim#vcs#annotate#AnnotateInfo()
  augroup END
endfunction " }}}

" GetCvsAnnotations (file, revision) {{{
function! eclim#vcs#annotate#GetCvsAnnotations (file, revision)
  let cmd = 'annotate'
  if a:revision != ''
    let cmd .= ' -r ' . a:revision
  endif

  let dir = fnamemodify(a:file, ':h')
  let file = fnamemodify(a:file, ':t')

  let cwd = getcwd()
  exec 'lcd ' . dir
  try
    let result = eclim#vcs#util#Cvs(cmd . ' "' . file . '"')
    let annotations = split(result, '\n')
    call filter(annotations, 'v:val =~ "^[0-9]"')
    call map(annotations,
      \ "substitute(v:val, '^\\s*\\([0-9.]\\+\\)\\s*(\\(.\\{-}\\)\\s\\+\\(.\\{-}\\)).*', '\\1 (\\3) \\2', '')")
  finally
    exec 'lcd ' . cwd
  endtry

  if v:shell_error
    call eclim#util#EchoError(result)
    return
  endif

  return annotations
endfunction " }}}

" GetSvnAnnotations (file, revision) {{{
function! eclim#vcs#annotate#GetSvnAnnotations (file, revision)
  let cmd = 'annotate -v'
  if a:revision != ''
    let cmd .= ' -r ' . a:revision
  endif
  let result = eclim#vcs#util#Svn(cmd . ' "' . a:file . '"')
  if result == '0'
    return
  endif

  let annotations = split(result, '\n')
  call map(annotations,
      \ "substitute(v:val, '^\\s*\\([0-9]\\+\\)\\s*\\(.\\{-}\\)\\s\\+.\\{-}\\s\\+\\(.\\{-}\\)\\s\\+.\\{-}(\\(.\\{-}\\)).*', '\\1 (\\4 \\3) \\2', '')")

  return annotations
endfunction " }}}

" vim:ft=vim:fdm=marker
