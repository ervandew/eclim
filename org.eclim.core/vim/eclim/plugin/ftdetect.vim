" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Vim file type detection script for eclim.
"
" License:
"
" Copyright (C) 2005 - 2011  Eric Van Dewoestine
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

" EclimSetXmlFileType(map) {{{
" Sets the filetype of the current xml file to the if its root element is in the
" supplied map.
function! EclimSetXmlFileType(map)
  if !exists("b:eclim_xml_filetype")
    " cache the root element so that subsiquent calls don't need to re-examine
    " the file.
    if !exists("b:xmlroot")
      let b:xmlroot = s:GetRootElement()
    endif

    if has_key(a:map, b:xmlroot)
      let b:eclim_xml_filetype = a:map[b:xmlroot]
      let &filetype = b:eclim_xml_filetype
    endif

  " occurs when re-opening an existing buffer.
  elseif &ft != b:eclim_xml_filetype
    if has_key(a:map, b:xmlroot)
      let &filetype = a:map[b:xmlroot]
    endif
  endif
endfunction " }}}

" GetRootElement() {{{
" Get the root element name.
function! s:GetRootElement()
  " handle case where file doesn't have xml an extension or an xml declaration
  if expand('%:e') != 'xml' && getline(1) !~ '<?\s*xml.*?>'
    set filetype=xml
  endif

  let root = ''
  let element = '.\{-}<\([a-zA-Z].\{-}\)\(\s\|>\|$\).*'

  " search for usage of root element (first occurence of <[a-zA-Z]).
  let numlines = line("$")
  let line = 1
  let pos = getpos('.')
  try
    while line <= numlines
      call cursor(line, 1)
      let found = searchpos('<[a-zA-Z]', 'cn', line)
      if found[0]
        let syntaxName = synIDattr(synID(found[0], found[1], 1), "name")
        if syntaxName == 'xmlTag'
          let root = substitute(getline(line), element, '\1', '')
          break
        endif
      endif
      let line = line + 1
    endwhile
  finally
    call setpos('.', pos)
  endtry

  " no usage, so look for doctype definition of root element
  if root == ''
    let linenum = search('<!DOCTYPE\s\+\_.\{-}>', 'bcnw')
    if linenum > 0
      let line = ''
      while getline(linenum) !~ '>'
        let line = line . getline(linenum)
        let linenum += 1
      endwhile
      let line = line . getline(linenum)

      let root = substitute(line, '.*DOCTYPE\s\+\(.\{-}\)\s\+.*', '\1', '')

      return root != line ? root : ''
    endif
  endif

  return root
endfunction " }}}

" vim:ft=vim:fdm=marker
