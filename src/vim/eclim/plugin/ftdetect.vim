" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Vim file type detection script for eclim.
"
" License:
"
" Copyright (c) 2005 - 2008
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
autocmd BufRead .projectOptions
  \ call <SID>SetXmlFileType({'phpProjectOptions': 'eclipse_projectOptions'})
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

autocmd BufRead *.gst set ft=groovy_simple_template

autocmd BufRead hg-editor-* set ft=hg

" SetXmlFileType(map) {{{
" Sets the filetype of the current xml file to the if its root element is in the
" supplied map.
function! s:SetXmlFileType (map)
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
function! s:GetRootElement ()
  let root = ''
  let element = '.\{-}<\([a-zA-Z].\{-}\)\(\s\|>\|$\).*'

  " search for usage of root element (first occurence of <[a-zA-Z]).
  let numlines = line("$")
  let line = 1
  while line <= numlines
    if getline(line) =~ '<[a-zA-Z]'
      let root = substitute(getline(line), element, '\1', '')
      break
    endif
    let line = line + 1
  endwhile

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
