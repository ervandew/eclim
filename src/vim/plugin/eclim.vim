" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Plugin which bootstraps the eclim environment.
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

if v:version < 700 || exists("g:EclimDisabled")
  finish
endif

" EclimBaseDir() {{{
" Gets the base directory where the eclim vim scripts are located.
function EclimBaseDir()
  if !exists("g:EclimBaseDir")
    let savewig = &wildignore
    set wildignore=""
    let file = findfile('plugin/eclim.vim', escape(&runtimepath, ' '))
    let &wildignore = savewig

    if file == ''
      echoe 'Unable to determine eclim basedir.  ' .
        \ 'Please report this issue on the eclim forums.'
      finish
    endif
    let basedir = substitute(fnamemodify(file, ':p:h:h'), '\', '/', 'g')

    let g:EclimBaseDir = escape(basedir, ' ')
  endif

  return g:EclimBaseDir
endfunction " }}}

" Init() {{{
" Initializes eclim.
function s:Init()
  " on windows, this eclim plugin gets called first, so force taglist to be
  " called prior.
  runtime! plugin/taglist.vim

  " add eclim dir to runtime path.
  exec 'set runtimepath+=' .
    \ EclimBaseDir() . '/eclim,' .
    \ EclimBaseDir() . '/eclim/after'

  " need to be manually sourced
  runtime! eclim/plugin/*.vim
  runtime! eclim/after/plugin/*.vim
endfunction " }}}

" Validate() {{{
" Validates some settings and environment values required by eclim.
function s:Validate()
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
    call add(errors, "       Type ':help 'compatible'' for more details.")
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
      echo "Filetype plugin support looks to be disabled, but due to possible"
      echo "language differences, please check the following line manually."
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
      call add(errors, "       Type ':help filetype-plugin-on' for more details.")
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

call <SID>Init()

" vim:ft=vim:fdm=marker
