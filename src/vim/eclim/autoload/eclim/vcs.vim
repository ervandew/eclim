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
  let defined = eclim#display#signs#GetDefined()
  let index = 1
  for annotation in annotations
    let user = substitute(annotation, '^.\{-}\s\+\(.*\)', '\1', '')
    let user_abbrv = user[:1]
    if index(defined, user) == -1
      call eclim#display#signs#Define(user, user_abbrv, g:EclimInfoHighlight)
      call add(defined, user_abbrv)
    endif
    call eclim#display#signs#Place(user, index)
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
    let defined = eclim#display#signs#GetDefined()
    for annotation in b:vcs_annotations
      let user = substitute(annotation, '^.\{-}\s\+\(.*\)', '\1', '')
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
function! eclim#vcs#AnnotateInfo ()
  if exists('b:vcs_annotations')
    echo b:vcs_annotations[line('.') - 1]
  endif
endfunction " }}}

" Viewvc(file) {{{
" Convert file or directory to viewvc url and open in the browser.
function eclim#vcs#Viewvc (file)
  let root = eclim#project#util#GetProjectSetting('org.eclim.project.vcs.viewvc')
  if root == '0'
    return
  endif

  if root == ''
    call eclim#util#EchoWarning(
      \ ":Viewvc requires project setting 'org.eclim.project.vcs.viewvc'.")
    return
  elseif root =~ '/$'
    let root = root[:-2]
  elseif type(root) == 0 && root == 0
    return
  endif

  let project_root = eclim#project#util#GetCurrentProjectRoot()

  let file = a:file
  let dir = file
  if file == ''
    let file = expand('%:t')
    let dir = expand('%:p:h')
  elseif !isdirectory(project_root . '/' . file)
    let dir = fnamemodify(project_root . '/' . file, ':p:h')
    let file = fnamemodify(project_root . '/' . file, ':p:t')
  else
    let dir = fnamemodify(project_root . '/' . file, ':p')
    let file = ''
  endif

  let cmd = winrestcmd()
  if isdirectory(dir . '/CVS')
    silent exec 'sview ' . escape(dir . '/CVS/Repository', ' ')
    setlocal noswapfile
    setlocal bufhidden=delete

    let path = '/' . getline(1)

    silent close
  elseif isdirectory(dir . '/.svn')
    silent exec 'sview ' . escape(dir . '/.svn/entries', ' ')
    setlocal noswapfile
    setlocal bufhidden=delete

    " xml entries format < 1.4
    if getline(1) =~ '<?xml'
      call cursor(1, 1)
      let url = substitute(
        \ getline(search('^\s*url=')), '^\s*url="\(.*\)"', '\1', '')
      let repos = substitute(
        \ getline(search('^\s*repos=')), '^\s*repos="\(.*\)"', '\1', '')

    " entries format >= 1.4
    else
      " can't find official doc on the format, but lines 5 and 6 seem to
      " always have the necessary values.
      let url = getline(5)
      let repos = getline(6)
    endif

    let path = substitute(url, repos, '', '')

    silent close
  else
    call eclim#util#EchoError('Current file is not under cvs or svn version control.')
    return
  endif
  silent exec cmd

  let url = root . path . '/' . file
  if !isdirectory(project_root . '/' . file)
    let url .= '?view=log'
  endif

  call eclim#web#OpenUrl(url)
endfunction " }}}

" vim:ft=vim:fdm=marker
