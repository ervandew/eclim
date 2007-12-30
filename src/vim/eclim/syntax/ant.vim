" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"  Enhancement to default ant syntax file to add support for ant-contrib and
"  allow user to define list of additional tasks to be recognized.
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

syn keyword antElement display if then else elseif for foreach switch
syn keyword antElement display throw trycatch try catch finally
syn keyword antElement display propertycopy propertyselector propertyregex var
syn keyword antElement display antcallback antfetch runtarget
syn keyword antElement display outofdate timestampselector osfamily shellscript
syn keyword antElement display pathtofileset sortlist urlencode compilewithwalls
syn keyword antElement display forget assert bool limit math post stopwatch
syn keyword antElement display inifile antclipse antserver remoteant

if exists("g:AntSyntaxElements")
  let elements = string(g:AntSyntaxElements)
  let elements = substitute(elements, '\[\(.*\)\]', '\1', '')
  let elements = substitute(elements, ',', '', 'g')
  let elements = substitute(elements, "'", '', 'g')
  exec 'syn keyword antElement display ' . elements
endif

" vim:ft=vim:fdm=marker
