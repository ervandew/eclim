" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Plugin to generate gvim eclim menus.
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

" Script Variables {{{

let s:eclim_menus_root = []
let s:eclim_menus = {}

" }}}

" Generate() {{{
" Generate gvim menu items for available eclim commands.
function! eclim#display#menu#Generate()
  if &guioptions !~ 'm'
    if exists('b:eclim_menus')
      unlet b:eclim_menus
    endif
    return
  endif

  redir => commands
  silent exec 'command'
  redir END

  if !exists('b:eclim_menus')
    let b:eclim_menus = {}

    let pattern = '\<eclim#'
    if len(s:eclim_menus_root) != 0
      let pattern = '^..b.*\<eclim#'
    endif

    for cmd in split(commands, '\n')
      if cmd =~ pattern
        let name = substitute(cmd, '....\(\w\+\)\s.*', '\1', '')
        if cmd =~ '\<eclim#[A-Z]'
          if index(s:eclim_menus_root, name) == -1
            call add(s:eclim_menus_root, name)
          endif
        else
          let group = substitute(cmd, '.\{-}\<eclim#\(\w\+\)#.*', '\1', '')
          let var = cmd =~ '^..b' ? 'b:eclim_menus' : 's:eclim_menus'
          if !has_key({var}, group)
            let {var}[group] = []
          endif
          if index({var}[group], name) == -1
            call add({var}[group], name)
          endif
        endif
      endif
    endfor

    call sort(s:eclim_menus_root)
  endif

  silent! unmenu &Plugin.eclim

  " merge non-buffer items with buffer items
  let menus = deepcopy(s:eclim_menus, 1)
  for group in keys(b:eclim_menus)
    if !has_key(menus, group)
      let menus[group] = []
    endif
    for name in b:eclim_menus[group]
      call add(menus[group], name)
    endfor
  endfor

  for name in s:eclim_menus_root
    exec 'menu &Plugin.eclim.' . name . ' :' . name . ' '
  endfor

  for group in sort(keys(menus))
    for name in sort(menus[group])
      exec 'menu &Plugin.eclim.' . group . '.' . name . ' :' . name . ' '
    endfor
  endfor
endfunction " }}}

" vim:ft=vim:fdm=marker
