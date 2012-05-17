" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/ant/doc.html
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

" Global Varables {{{
if !exists("g:AntDocDefaultUrl")
  let g:AntDocDefaultUrl =
    \ 'http://www.google.com/search?btnI=1&q=allintitle%3A<element>+task+%7C+type+site%3Aant.apache.org'
endif

if !exists("g:AntUserDocs")
  let g:AntUserDocs = {}
endif
" }}}

" Script Varables {{{
let s:targets = 'http://ant.apache.org/manual/targets.html'
let s:using = 'http://ant.apache.org/manual/using.html#<element>s'
let s:conditions = 'http://ant.apache.org/manual/Tasks/conditions.html#<element>'
let s:mappers = 'http://ant.apache.org/manual/Types/mapper.html'
let s:paths = 'http://ant.apache.org/manual/using.html#path'
let s:types =
  \ 'http://ant.apache.org/manual/Types/<element>.html'
let s:selectors =
  \ 'http://ant.apache.org/manual/Types/selectors.html#<element>select'
let s:contrib_1 =
  \ 'http://ant-contrib.sourceforge.net/tasks/tasks/<element>.html'
let s:contrib_2 =
  \ 'http://ant-contrib.sourceforge.net/tasks/tasks/<element>_task.html'
let s:element_docs = {
    \  'project'           : s:using,
    \  'target'            : s:targets,
    \  'and'               : s:conditions,
    \  'checksum'          : s:conditions,
    \  'checs'             : s:conditions,
    \  'contains'          : s:conditions,
    \  'equals'            : s:conditions,
    \  'filesmatch'        : s:conditions,
    \  'http'              : s:conditions,
    \  'isfalse'           : s:conditions,
    \  'isfileselected'    : s:conditions,
    \  'isreference'       : s:conditions,
    \  'isset'             : s:conditions,
    \  'istrue'            : s:conditions,
    \  'length'            : s:conditions,
    \  'not'               : s:conditions,
    \  'or'                : s:conditions,
    \  'os'                : s:conditions,
    \  'socket'            : s:conditions,
    \  'compositemapper'   : s:mappers,
    \  'filtermapper'      : s:mappers,
    \  'flattenmapper'     : s:mappers,
    \  'globmapper'        : s:mappers,
    \  'identitymapper'    : s:mappers,
    \  'mergemapper'       : s:mappers,
    \  'packagemapper'     : s:mappers,
    \  'regexmapper'       : s:mappers,
    \  'antlib'            : s:types,
    \  'description'       : s:types,
    \  'dirset'            : s:types,
    \  'filelist'          : s:types,
    \  'fileset'           : s:types,
    \  'filterchain'       : s:types,
    \  'filterset'         : s:types,
    \  'mapper'            : s:types,
    \  'patternset'        : s:types,
    \  'permissions'       : s:types,
    \  'propertyset'       : s:types,
    \  'redirector'        : s:types,
    \  'regexp'            : s:types,
    \  'xmlcatalog'        : s:types,
    \  'zipfileset'        : s:types,
    \  'classpath'         : s:paths,
    \  'path'              : s:paths,
    \  'containsregexp'    : s:selectors,
    \  'date'              : s:selectors,
    \  'depend'            : s:selectors,
    \  'depth'             : s:selectors,
    \  'different'         : s:selectors,
    \  'filename'          : s:selectors,
    \  'majority'          : s:selectors,
    \  'modified'          : s:selectors,
    \  'none'              : s:selectors,
    \  'present'           : s:selectors,
    \  'selector'          : s:selectors,
    \  'size'              : s:selectors,
    \  'type'              : s:selectors,
    \  'for'               : s:contrib_1,
    \  'foreach'           : s:contrib_1,
    \  'if'                : s:contrib_1,
    \  'outofdate'         : s:contrib_1,
    \  'runtarget'         : s:contrib_1,
    \  'switch'            : s:contrib_1,
    \  'throw'             : s:contrib_1,
    \  'timestampselector' : s:contrib_1,
    \  'trycatch'          : s:contrib_1,
    \  'osfamily'          : s:contrib_1,
    \  'shellscript'       : s:contrib_1,
    \  'propertycopy'      : s:contrib_1,
    \  'propertyselector'  : s:contrib_1,
    \  'pathoffileset'     : s:contrib_1,
    \  'propertyregex'     : s:contrib_1,
    \  'sortlist'          : s:contrib_1,
    \  'urlencode'         : s:contrib_1,
    \  'forget'            : s:contrib_1,
    \  'compilewithwalls'  : s:contrib_1,
    \  'inifile'           : s:contrib_1,
    \  'verifydesign'      : s:contrib_1,
    \  'antcallback'       : s:contrib_2,
    \  'antfetch'          : s:contrib_2,
    \  'assert'            : s:contrib_2,
    \  'post'              : s:contrib_2,
    \  'stopwatch'         : s:contrib_2,
    \  'match'             : s:contrib_2,
    \  'variable'          : s:contrib_2,
    \  'limit'             : s:contrib_2,
    \  'antclipse'         : s:contrib_2
  \ }
" }}}

" FindDoc(element) {{{
" Open the url to the documentation for the supplied element name or if not
" provided, the element name under the cursor.
function! eclim#java#ant#doc#FindDoc(element)
  let element = a:element
  if element == ''
    let col = eclim#util#GetCurrentElementColumn()
    if getline('.')[col - 2] !~ '<\|\/'
      " not on an element
      return
    endif
    let element = expand('<cword>')
  endif
  let element = tolower(element)

  if has_key(s:element_docs, element)
    let url = s:element_docs[element]
  elseif has_key(g:AntUserDocs, element)
    let url = g:AntUserDocs[element]
  else
    let url = g:AntDocDefaultUrl
  endif

  "let url = escape(url, '&%#')
  "let url = escape(url, '%#')
  let url = substitute(url, '<element>', element, 'g')

  call eclim#web#OpenUrl(url)
endfunction " }}}

" vim:ft=vim:fdm=marker
