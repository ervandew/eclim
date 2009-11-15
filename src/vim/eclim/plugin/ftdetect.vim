" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Vim file type detection script for eclim.
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

let xmltypes = {
    \ 'project': 'ant',
    \ 'hibernate-mapping': 'hibernate',
    \ 'beans': 'spring',
    \ 'document': 'forrestdocument',
    \ 'form-validation': 'commonsvalidator',
    \ 'status': 'forreststatus',
    \ 'testsuite': 'junitresult',
    \ 'log4j:configuration': 'log4j'
  \ }

autocmd BufRead .classpath
  \ call <SID>SetXmlFileType({'classpath': 'eclipse_classpath'})
autocmd BufRead .buildpath
  \ call <SID>SetXmlFileType({'buildpath': 'eclipse_buildpath'})
autocmd BufRead .cproject
  \ call <SID>SetXmlFileType({'cproject': 'eclipse_cproject'})
autocmd BufRead ivy.xml
  \ call <SID>SetXmlFileType({'ivy-module': 'ivy'})
autocmd BufRead pom.xml
  \ call <SID>SetXmlFileType({'project': 'mvn_pom'})
autocmd BufRead project.xml
  \ call <SID>SetXmlFileType({'project': 'maven_project'})
autocmd BufRead struts-config.xml
  \ call <SID>SetXmlFileType({'struts-config': 'strutsconfig'})
autocmd BufRead *.tld
  \ call <SID>SetXmlFileType({'taglib': 'tld'})
autocmd BufRead web.xml
  \ call <SID>SetXmlFileType({'web-app': 'webxml'})
autocmd BufRead *.wsdl
  \ call <SID>SetXmlFileType({'definitions': 'wsdl', 'wsdl:definitions': 'wsdl'})
autocmd BufRead *.xml call <SID>SetXmlFileType(xmltypes)

autocmd BufRead *.gant set ft=gant
autocmd BufRead *.gst set ft=groovy_simple_template

autocmd BufRead hg-editor-* set ft=hg
autocmd BufRead COMMIT_EDITMSG set ft=gitcommit

" SetXmlFileType(map) {{{
" Sets the filetype of the current xml file to the if its root element is in the
" supplied map.
function! s:SetXmlFileType(map)
  if !exists("b:eclim_xml_filetype")
    " cache the root element so that subsiquent calls don't need to re-examine
    " the file.
    if !exists("b:xmlroot")
      let b:xmlroot = s:GetRootElement()
    endif

    if has_key(a:map, b:xmlroot)
      exec "set filetype=" . a:map[b:xmlroot]
      let b:eclim_xml_filetype = a:map[b:xmlroot]
    endif

  " occurs when re-opening an existing buffer.
  elseif &ft != b:eclim_xml_filetype
    if has_key(a:map, b:xmlroot)
      exec "set filetype=" . a:map[b:xmlroot]
    endif
  endif
endfunction " }}}

" GetRootElement() {{{
" Get the root element name.
function! s:GetRootElement()
  " handle case where file doesn't have the xml declaration
  set filetype=xml

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
      echom " root from doctype = " . root

      return root != line ? root : ''
    endif
  endif

  return root
endfunction " }}}

" vim:ft=vim:fdm=marker
