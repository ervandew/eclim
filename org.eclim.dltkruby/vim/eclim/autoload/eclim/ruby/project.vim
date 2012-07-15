" Author:  Eric Van Dewoestine
"
" Description: {{{
"
" License:
"
" Copyright (C) 2005 - 2012  Eric Van Dewoestine
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

if !exists('g:eclim_ruby_project_loaded')
  let g:eclim_ruby_project_loaded = 1
else
  finish
endif

function eclim#ruby#project#ProjectCreatePre(folder) " {{{
  return s:InitInterpreters()
endfunction " }}}

function eclim#ruby#project#ProjectImportPre(folder) " {{{
  return s:InitInterpreters()
endfunction " }}}

function eclim#ruby#project#ProjectNatureAddPre(project) " {{{
  return s:InitInterpreters()
endfunction " }}}

function s:InitInterpreters() " {{{
  let interpreters = eclim#dltk#interpreter#GetInterpreters('ruby')
  if len(interpreters) == 0
    let path = ''
    if !has('win32') && !has('win64')
      silent! let path =
        \ substitute(eclim#util#System('which ruby 2> /dev/null'), '\n$', '', '')
    else
      let paths = escape(substitute(expand('$PATH'), ';', ',', 'g'), ' ')
      let path = substitute(findfile('ruby.exe', paths, ';'), '\', '/', 'g')
    endif
    let answer = 0
    if path != ''
      let answer = eclim#util#PromptConfirm(
        \ "No ruby interpreter configured.\n" .
        \ "Would you like to use the following interpreter?\n" .
        \ "  " . path, g:EclimInfoHighlight)
      if answer == -1
        return 0
      endif

      if answer == 1
        return eclim#ruby#interpreter#AddInterpreter(path)
      endif
    endif

    " prompt user for interpreter path
    if answer == 0
      exec "echohl " . g:EclimInfoHighlight
      try
        " clear any previous messages
        redraw
        echo "Ruby interpreter required.\n" .
          \  "Please enter the path to your ruby excutable (eg. /usr/bin/ruby)\n"
        let path = input("ruby interpreter path: ", '', 'file')
        let valid = filereadable(path)
        while 1
          while path != '' && !valid
            let path = input(
              \ "You must choose a valid interpreter path. (Ctrl-C to cancel): ",
              \ '', 'file')
            let valid = filereadable(path)
          endwhile

          " user canceled.
          if path == ''
            return 0
          endif

          let valid = eclim#ruby#interpreter#AddInterpreter(path)
          if valid
            break
          else
            " an error will reset the highlighting
            exec "echohl " . g:EclimInfoHighlight
          endif
        endwhile
      finally
        echohl None
      endtry
    endif
  endif

  return 1
endfunction " }}}

" vim:ft=vim:fdm=marker
