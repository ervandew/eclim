" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Vim file type detection script for eclim.
"
" License:
"
" Copyright (C) 2005 - 2014  Eric Van Dewoestine
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
  \ call EclimSetXmlFileType({'classpath': 'eclipse_classpath'})
autocmd BufRead ivy.xml
  \ call EclimSetXmlFileType({'ivy-module': 'ivy'})
autocmd BufRead pom.xml
  \ call EclimSetXmlFileType({'project': 'mvn_pom'})
autocmd BufRead struts-config.xml
  \ call EclimSetXmlFileType({'struts-config': 'strutsconfig'})
autocmd BufRead *.tld
  \ call EclimSetXmlFileType({'taglib': 'tld'})
autocmd BufRead *web.xml
  \ call EclimSetXmlFileType({'web-app': 'webxml'})
autocmd BufRead *.wsdl
  \ call EclimSetXmlFileType({'definitions': 'wsdl', 'wsdl:definitions': 'wsdl'})
autocmd BufRead *.xml call EclimSetXmlFileType(xmltypes)

autocmd BufRead *.gant set ft=gant
autocmd BufRead *.gst set ft=groovy_simple_template

" vim:ft=vim:fdm=marker
