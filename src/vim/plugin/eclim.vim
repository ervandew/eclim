" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   Plugin which bootstraps the eclim environment.
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

" Init() {{{
" Initializes eclim.
function s:Init ()
  " on windows, this eclim plugin gets called first, so force taglist to be
  " called prior.
  runtime! plugin/taglist.vim

  " add eclim dir to runtime path.
  let file = findfile('plugin/eclim.vim', escape(&runtimepath, ' '))
  if file == ''
    echoe 'Unable to find path to plugin/eclim.vim.  ' .
      \ 'Please report this issue on the eclim forums.'
    finish
  endif

  let basedir = substitute(fnamemodify(file, ':p:h:h'), '\', '/', 'g')
  exec 'set runtimepath+=' . escape(basedir, ' ') . '/eclim'

  " need to be manually sourced
  runtime! eclim/plugin/*.vim
endfunction " }}}

" Validate() {{{
" Validates some settings and environment values required by eclim.
function s:Validate ()
  let errors = []

  " Check vim version.
  if v:version < 700
    let version = strpart(v:version, 0, 1) . '.' . strpart(v:version, 1)
    echom "Error: Your vim version is " . v:version . "."
    echom "       Eclim requires version 7.xx."
    return
  endif

  " Check 'compatible' option.
  if &compatible
    call add(errors, "Error: You have 'compatible' set:")
    call add(errors, "       Please add 'set nocompatible' to your vimrc.")
  endif

  " Check filetype support
  redir => ftsupport
  silent filetype
  redir END
  let ftsupport = substitute(ftsupport, '\n', '', 'g')
  if ftsupport !~ 'detection:ON' || ftsupport !~ 'plugin:ON'
    echo " "
    let chose = 0
    while string(chose) !~ '1\|2'
      redraw
      echo "Due to possible language differences, " .
        \ "please check the following line manually."
      echo "    " . ftsupport
      echo "Does it have detection and plugin 'ON'?"
      echo "1) Yes"
      echo "2) No"
      let chose = input("Please Choose (1 or 2): ")
    endwhile
    if chose != 1
      call add(errors, "Error: Filetype detection and plugins must be enabled.")
      call add(errors, "       Please add 'filetype plugin on' or " .
        \ "'filetype plugin indent on' to your vimrc.")
    endif
  endif

  " Print the results.
  redraw
  echohl Statement
  if len(errors) == 0
    echom "Result: OK, required settings are valid."
  else
    for error in errors
      echom error
    endfor
  endif
  echohl None
endfunction " }}}

" Command Declarations {{{
if !exists(":EclimValidate")
  command EclimValidate :call <SID>Validate()
endif
" }}}

if v:version < 700 || exists("g:EclimDisabled")
  finish
else
  call <SID>Init()
endif

" vim:ft=vim:fdm=marker
