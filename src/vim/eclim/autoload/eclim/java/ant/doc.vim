" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/java/ant/doc.html
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
let s:using = 'http://ant.apache.org/manual/using.html#<element>s'
let s:conditions = 'http://ant.apache.org/manual/CoreTasks/conditions.html'
let s:mappers = 'http://ant.apache.org/manual/CoreTypes/mapper.html'
let s:paths = 'http://ant.apache.org/manual/using.html#path'
let s:types =
  \ 'http://ant.apache.org/manual/CoreTypes/<element>.html'
let s:selectors =
  \ 'http://ant.apache.org/manual/CoreTypes/selectors.html#<element>select'
let s:contrib_1 =
  \ 'http://ant-contrib.sourceforge.net/tasks/tasks/<element>.html'
let s:contrib_2 =
  \ 'http://ant-contrib.sourceforge.net/tasks/tasks/<element>_task.html'
let s:element_docs = {
    \  'project'           : s:using,
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
function! eclim#java#ant#doc#FindDoc (element)
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
