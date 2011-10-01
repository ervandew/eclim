" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Plugin which bootstraps the eclim environment.
"
" License:
"
" Copyright (C) 2005 - 2010  Eric Van Dewoestine
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

" Command Declarations {{{
if !exists(":EclimValidate")
  command EclimValidate :call <SID>Validate()
endif
" }}}

" Validate() {{{
" Validates some settings and environment values required by eclim.
" NOTE: don't add command-line continuation characters anywhere in the
" function, just in case the user has &compatible set.
function! s:Validate()
  " Check vim version.
  if v:version < 700
    let ver = strpart(v:version, 0, 1) . '.' . strpart(v:version, 2)
    echom "Error: Your vim version is " . ver . "."
    echom "       Eclim requires version 7.x.x"
    return
  endif

  let errors = []

  " Check 'compatible' option.
  if &compatible
    call add(errors, "Error: You have 'compatible' set:")
    call add(errors, "       Eclim requires 'set nocompatible' in your vimrc.")
    call add(errors, "       Type \":help 'compatible'\" for more details.")
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
      call add(errors, "Error: Eclim requires filetype plugins to be enabled.")
      call add(errors, "       Please add 'filetype plugin indent on' to your vimrc.")
      call add(errors, "       Type \":help filetype-plugin-on\" for more details.")
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

" exit early if unsupported vim version, compatible is set, or eclim is
" disabled.
if v:version < 700 || &compatible || exists("g:EclimDisabled")
  finish
endif

" EclimBaseDir() {{{
" Gets the base directory where the eclim vim scripts are located.
function! EclimBaseDir()
  if !exists("g:EclimBaseDir")
    let savewig = &wildignore
    set wildignore=""
    let file = findfile('plugin/eclim.vim', escape(&runtimepath, ' '))
    let &wildignore = savewig

    if file == ''
      echoe 'Unable to determine eclim basedir.  ' .
        \ 'Please report this issue on the eclim user mailing list.'
      let g:EclimBaseDir = ''
      return g:EclimBaseDir
    endif
    let basedir = substitute(fnamemodify(file, ':p:h:h'), '\', '/', 'g')

    let g:EclimBaseDir = escape(basedir, ' ')
  endif

  return g:EclimBaseDir
endfunction " }}}

" Init() {{{
" Initializes eclim.
function! s:Init()
  " add eclim dir to runtime path.
  let basedir = EclimBaseDir()
  if basedir == ''
    return
  endif

  exec 'set runtimepath+=' .
    \ basedir . '/eclim,' .
    \ basedir . '/eclim/after'

  " Alternate version which inserts the eclim path just after the currently
  " executing runtime path element and puts the eclim/after path at the very
  " end.
  "let paths = split(&rtp, ',')
  "let index = 0
  "for path in paths
  "  let index += 1
  "  if tolower(path) == tolower(basedir)
  "    break
  "  endif
  "endfor

  "let tail = paths[index :]

  "for path in tail
  "  exec 'set runtimepath-=' . escape(path, ' ')
  "endfor

  "exec 'set runtimepath+=' .  basedir . '/eclim'

  "for path in tail
  "  exec 'set runtimepath+=' . escape(path, ' ')
  "endfor

  "exec 'set runtimepath+=' .  basedir . '/eclim/after'

  " need to be manually sourced
  runtime! eclim/plugin/*.vim
  runtime! eclim/after/plugin/*.vim
endfunction " }}}

call <SID>Init()

" vim:ft=vim:fdm=marker
